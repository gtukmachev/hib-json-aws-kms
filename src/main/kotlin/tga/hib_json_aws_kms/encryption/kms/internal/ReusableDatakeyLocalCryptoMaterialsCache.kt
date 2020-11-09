package tga.hib_json_aws_kms.encryption.kms.internal

import com.amazonaws.encryptionsdk.caching.LocalCryptoMaterialsCache
import com.amazonaws.encryptionsdk.model.DecryptionMaterials
import com.amazonaws.encryptionsdk.caching.CryptoMaterialsCache.UsageStats
import com.amazonaws.encryptionsdk.caching.CryptoMaterialsCache.EncryptCacheEntry
import java.util.Objects
import com.amazonaws.encryptionsdk.caching.CryptoMaterialsCache.DecryptCacheEntry
import com.amazonaws.encryptionsdk.model.EncryptionMaterials
import java.util.Arrays
import com.amazonaws.encryptionsdk.CryptoAlgorithm
import com.amazonaws.encryptionsdk.CommitmentPolicy
import com.amazonaws.encryptionsdk.model.KeyBlob
import java.util.HashMap
import com.amazonaws.encryptionsdk.internal.Constants
import com.amazonaws.encryptionsdk.internal.TrailingSignatureAlgorithm
import java.security.PublicKey
import com.amazonaws.encryptionsdk.caching.CryptoMaterialsCache
import org.slf4j.LoggerFactory
import com.amazonaws.encryptionsdk.caching.CryptoMaterialsCache.CacheHint
import java.security.MessageDigest
import com.amazonaws.encryptionsdk.internal.EncryptionContextSerializer
import java.security.GeneralSecurityException
import com.amazonaws.encryptionsdk.exception.AwsCryptoException
import java.nio.charset.StandardCharsets
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import java.security.KeyPair
import java.security.PrivateKey

class ReusableDatakeyLocalCryptoMaterialsCache(
		capacity: Int,
		maxAgeMs: Long?,
		partitionId: String,
		val context: Map<String, String>,
		val commitmentPolicy: CommitmentPolicy?,
		val requestedAlgorithm: CryptoAlgorithm?
): LocalCryptoMaterialsCache(capacity) {

    companion object {
		private const val CACHE_ID_HASH_ALGORITHM = "SHA-512";
        private 	  val logger =
				LoggerFactory.getLogger(ReusableDatakeyLocalCryptoMaterialsCache::class.java)
		// use it for testing purposes (eg objectMapper.writeValueAsString(entryForEncrypt))
    	private		  val objectMapper = ObjectMapper()
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val hint = object: CacheHint {
    	override fun getMaxAgeMillis(): Long {
    		return maxAgeMs?:0
    	}
    }

	private val encryptCacheIdMap = HashMap<Int, ByteArray>()

	private var partitionIdHash = MessageDigest.getInstance(CACHE_ID_HASH_ALGORITHM).digest(
                    partitionId.toByteArray(StandardCharsets.UTF_8))

	override fun getEntryForEncrypt(encryptCacheId: ByteArray,
									usageIncrement: UsageStats): EncryptCacheEntry? {
	    var entryForEncrypt = super.getEntryForEncrypt(encryptCacheId, usageIncrement)

	    // for testing: in debug mode:
		// 1. add breakpoint below
		// 2. run KmsEncryptoTests
		// 3. skip first entrance
		// 4. during second entrance assign entryForEncrypt = null manually
		//    to create new instance of EncryptionMaterials
		// 5. verify decryption isn't working because of wrong datakey
		//    because trailing signate used for its generation doesn't match anymore
		if (Objects.isNull(entryForEncrypt)) {
			var decryptCachedId = this.encryptCacheIdMap.get(Arrays.hashCode(encryptCacheId))

			if (Objects.nonNull(decryptCachedId)) {
			    logger.trace("Decrypt key not found for cacheId={}", encryptCacheId)

				var entryForDecrypt = super.getEntryForDecrypt(decryptCachedId)
				var decryptionMaterials = entryForDecrypt!!.getResult()
				var dataKey = decryptionMaterials.getDataKey();
				// FIX: signing encrypted message with new trailing signature won't verify
				// previously encrypted messages so decryption for them will fail
				// because previous trailing signature public key is used in data key generation
				// (see DefaultCryptoMaterialsManager#getMaterialsForEncrypt line:95)
				// (pay attention to context.put(Constants.EC_PUBLIC_KEY_FIELD, serializeTrailingKeyForEc(algo, trailingKeys))
				var trailingKeys = this.getTrailingKeys()
				var publicSignatureKey = trailingKeys?.getPublic()
				var trailingSignatureKey = trailingKeys?.getPrivate()
				var encryptionContext = this.getEncryptionContext(publicSignatureKey)

				logger.trace("Create encrypt key for dataKey={}, encryptionContext={}",
						dataKey.getKey().getEncoded(),
						encryptionContext)

				var encryptionMaterials = EncryptionMaterials.newBuilder()
						.setAlgorithm(this.getAlgorithm())
						.setCleartextDataKey(dataKey.getKey())
						.setEncryptedDataKeys(Arrays.asList(KeyBlob(dataKey)))
						.setEncryptionContext(encryptionContext)
						.setTrailingSignatureKey(trailingSignatureKey)
						.setMasterKeys(Arrays.asList(dataKey.getMasterKey()))
						.build()

				entryForEncrypt = super.putEntryForEncrypt(encryptCacheId,
						                                   encryptionMaterials,
                                    					   this.hint,
                                    					   usageIncrement)
			}
		}

		return entryForEncrypt
	}

	override fun putEntryForDecrypt(decryptCacheId: ByteArray,
									decryptionMaterials: DecryptionMaterials,
									hint: CacheHint) {
		var encryptCachedId = this.getEncryptCacheId(this.context)
		this.encryptCacheIdMap.put(Arrays.hashCode(encryptCachedId), decryptCacheId)
		super.putEntryForDecrypt(decryptCacheId, decryptionMaterials, hint)
	}

	private fun getAlgorithm(): CryptoAlgorithm {
		return when (this.commitmentPolicy) {
			CommitmentPolicy.ForbidEncryptAllowDecrypt
			    -> CryptoAlgorithm.ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384
			else -> CryptoAlgorithm.ALG_AES_256_GCM_HKDF_SHA512_COMMIT_KEY_ECDSA_P384
		}
	}

	private fun getEncryptionContext(publicSignatureKey: PublicKey?): Map<String, String> {
		var encryptionContext = HashMap(this.context)
		if (Objects.nonNull(publicSignatureKey)) {
			var ecPublicKey = TrailingSignatureAlgorithm
				.forCryptoAlgorithm(getAlgorithm())
				.serializePublicKey(publicSignatureKey)
			encryptionContext.put(Constants.EC_PUBLIC_KEY_FIELD, ecPublicKey)
		}
		return encryptionContext
	}

	// copied and adjusted from com.amazonaws.encryptionsdk.caching.CachingCryptoMaterialsManager
	private fun getEncryptCacheId(context: Map<String, String>): ByteArray {
        try {
            var digest = MessageDigest.getInstance(CACHE_ID_HASH_ALGORITHM)

            digest.update(this.partitionIdHash)

			if (Objects.nonNull(this.requestedAlgorithm)) {
				digest.update(1)
				updateDigestWithAlgorithm(digest, this.requestedAlgorithm!!);
			} else {
				digest.update(0)
			}

            digest.update(MessageDigest.getInstance(CACHE_ID_HASH_ALGORITHM).digest(
                    EncryptionContextSerializer.serialize(context)
            ))

            return digest.digest()
        } catch (e: GeneralSecurityException) {
            throw AwsCryptoException(e)
        }
    }

	// copied and adjusted from com.amazonaws.encryptionsdk.caching.CachingCryptoMaterialsManager
	private fun updateDigestWithAlgorithm(digest: MessageDigest, algorithm: CryptoAlgorithm) {
        var algorithmValue = algorithm.getValue();
        digest.update(byteArrayOf((algorithmValue.toInt() shr 8).toByte(), algorithmValue.toByte()))
    }

	private fun getTrailingKeys(): KeyPair? {
		var algorithm = this.getAlgorithm()
		if (algorithm.getTrailingSignatureLength() > 0) {
			return TrailingSignatureAlgorithm.forCryptoAlgorithm(algorithm).generateKey()
		}
		return null
	}
}