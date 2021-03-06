package tga.hib_json_aws_kms.encryption.kms

import com.amazonaws.encryptionsdk.AwsCrypto
import com.amazonaws.encryptionsdk.CommitmentPolicy
import com.amazonaws.encryptionsdk.caching.CachingCryptoMaterialsManager
import com.amazonaws.encryptionsdk.caching.LocalCryptoMaterialsCache
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider
import org.slf4j.LoggerFactory
import tga.hib_json_aws_kms.encryption.Encryptor
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit

class KmsEncryptor : Encryptor {

    companion object {
        private val logger = LoggerFactory.getLogger(KmsEncryptor::class.java)

        private const val MAX_ENTRY_AGE_DAYS: Long = 90
        private const val MAX_CACHE_ENTRIES = 10000
        private const val KEY_ARN = "arn:aws:kms:us-east-1:575157453892:key/99934d01-73f6-4ced-9976-33c22216bc1e"
        private       val ENCRYPTION_CONTEXT = Collections.singletonMap("owner", "savvy")

        private val base64encoder = Base64.getEncoder()
        private val base64decoder = Base64.getDecoder()

        private val crypto = AwsCrypto.builder()
                .withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt)
                .build()

        private val keyProvider = KmsMasterKeyProvider.builder()
                .buildStrict(KEY_ARN)

        private val cache = LocalCryptoMaterialsCache(MAX_CACHE_ENTRIES)

        private val CACHING_MATERIALS_MANAGER = CachingCryptoMaterialsManager.newBuilder()
                .withMasterKeyProvider(keyProvider)
                .withCache(cache)
                .withMaxAge(MAX_ENTRY_AGE_DAYS, TimeUnit.DAYS)
                .build()
    }

    override fun encrypt(content: String): String {
        val incomeBytes = content.toByteArray(StandardCharsets.UTF_8)
        logger.trace("incomeBytes={}", incomeBytes.toLimitedStr())

        val encryptResult = crypto.encryptData(
                CACHING_MATERIALS_MANAGER, incomeBytes, ENCRYPTION_CONTEXT
        )

        val ciphertext = encryptResult.result
        logger.trace("ciphertext[{}]={}",ciphertext.size, ciphertext.toLimitedStr())

        val encryptedContent = base64encoder.encodeToString(ciphertext)
        logger.trace("encryptedContent[{}]={}", encryptedContent.toByteArray().size, encryptedContent)
        return encryptedContent
    }

    override fun decrypt(encryptedBase64encoded: String): String {
        logger.trace("encryptedBase64encoded[{}]={}", encryptedBase64encoded.toByteArray().size, encryptedBase64encoded)

        val ciphertext = base64decoder.decode(encryptedBase64encoded)
        logger.trace("ciphertext[{}]={}",ciphertext.size, ciphertext.toLimitedStr())

        val decryptResult = crypto.decryptData(CACHING_MATERIALS_MANAGER, ciphertext)

        // check(ENCRYPTION_CONTEXT.entries.stream()
        //        .allMatch { e: Map.Entry<String, String> -> e.value == decryptResult.encryptionContext[e.key] }) { "Wrong Encryption Context!" }

        val decryptedArray = decryptResult.result
        logger.trace("decryptedArray[{}]={}",decryptedArray.size, decryptedArray.toLimitedStr())

        val decryptedContent = decryptedArray.toString(StandardCharsets.UTF_8)
        logger.trace("decryptedContent[{}]={}", decryptedContent.toByteArray().size, decryptedContent)

        return decryptedContent
    }

    private fun ByteArray?.toLimitedStr(): String {
        return this?.joinToString(prefix = "[", postfix = "]", limit = 30) ?: "null"
    }

}
