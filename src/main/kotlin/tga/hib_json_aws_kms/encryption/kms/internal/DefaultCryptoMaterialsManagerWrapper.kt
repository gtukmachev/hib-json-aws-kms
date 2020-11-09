package tga.hib_json_aws_kms.encryption.kms.internal

import com.amazonaws.encryptionsdk.DefaultCryptoMaterialsManager
import com.amazonaws.encryptionsdk.MasterKeyProvider
import com.amazonaws.encryptionsdk.model.EncryptionMaterials
import com.amazonaws.encryptionsdk.model.EncryptionMaterialsRequest
import com.amazonaws.encryptionsdk.model.DecryptionMaterials
import com.amazonaws.encryptionsdk.model.DecryptionMaterialsRequest
import java.util.Objects
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.lang.Exception
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import javax.crypto.SecretKey
import java.security.PrivateKey
import java.security.spec.ECParameterSpec

class DefaultCryptoMaterialsManagerWrapper(val masterKeyProvider: MasterKeyProvider<*>
): DefaultCryptoMaterialsManager(masterKeyProvider) {

	companion object {
	    private val logger: Logger = LoggerFactory.getLogger(DefaultCryptoMaterialsManager::class.java)

	    private const val LAST_MATERIALS_FOR_ENCRYPT_JSON = "last-materials-for-encrypt.json"
		private const val LAST_DECRYPT_MATERIALS_JSON = "last-decrypt-materials.json"

		private val awsMappingModule = SimpleModule()
				.addDeserializer(SecretKey::class.java, SecretKeyDeserializer())
				.addDeserializer(PrivateKey::class.java, PrivateKeyDeserializer())
				.addDeserializer(ECParameterSpec::class.java, ECParameterSpecDeserializer())

		private val objectMapper = ObjectMapper()
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.registerModule(awsMappingModule)
	}

	var lastMaterialsForEncrypt: EncryptionMaterials? = readMaterialsForEncrypt()
	var lastDecryptMaterials: DecryptionMaterials? = readDecryptMaterials()

	override fun getMaterialsForEncrypt(request: EncryptionMaterialsRequest): EncryptionMaterials {
		var storedMaterialsForEncrypt: EncryptionMaterials?

		if (Objects.isNull(this.lastMaterialsForEncrypt)) {
			storedMaterialsForEncrypt = super.getMaterialsForEncrypt(request)
			this.storeMaterialsForEncrypt(storedMaterialsForEncrypt!!)
		} else {
			storedMaterialsForEncrypt = this.lastMaterialsForEncrypt
			this.lastMaterialsForEncrypt = null
		}

        return storedMaterialsForEncrypt!!
	}

	override fun decryptMaterials(request: DecryptionMaterialsRequest): DecryptionMaterials {
		var storedDecryptMaterials: DecryptionMaterials?

		if (Objects.isNull(this.lastDecryptMaterials)) {
			storedDecryptMaterials = super.decryptMaterials(request)
			this.storeDecryptMaterials(storedDecryptMaterials!!)
		} else {
			storedDecryptMaterials = this.lastDecryptMaterials
			this.lastDecryptMaterials = null
		}

        return storedDecryptMaterials!!
	}

	private fun<T> readMaterials(materialsFilePath: String, materialsClass: Class<T>): T? {
		var materialsFile: File = File(materialsFilePath)
		if (!materialsFile.exists()) materialsFile.createNewFile()
		try {
			return objectMapper.readValue(materialsFile, materialsClass)
		} catch (exception: Exception) {
			logger.info("Exception reading materials from file")
		}
		return null
	}

	fun readMaterialsForEncrypt(): EncryptionMaterials? {
		var builder = this.readMaterials(LAST_MATERIALS_FOR_ENCRYPT_JSON, EncryptionMaterials.Builder::class.java)
		return when (builder) {
			null -> null
			else -> builder.build()
		}
	}

	fun readDecryptMaterials(): DecryptionMaterials? {
		var builder = this.readMaterials(LAST_DECRYPT_MATERIALS_JSON, DecryptionMaterials.Builder::class.java)
		return when (builder) {
			null -> null
			else -> builder.build()
		}
	}

	private fun storeMaterials(materialsFilePath: String, materials: Any) {
		try {
			var materialsFile: File = File(materialsFilePath)
    		if (materialsFile.exists()) {
    			materialsFile.delete()
    			materialsFile.createNewFile()
    		}
    		objectMapper.writeValue(materialsFile, materials)
		} catch (exception: Exception) {
			logger.info("Exception writing materials to file", exception)
		}
	}

	fun storeMaterialsForEncrypt(storedMaterialsForEncrypt: EncryptionMaterials) {
		this.storeMaterials(LAST_MATERIALS_FOR_ENCRYPT_JSON, storedMaterialsForEncrypt)
	}

	fun storeDecryptMaterials(storedDecryptMaterials: DecryptionMaterials) {
		this.storeMaterials(LAST_DECRYPT_MATERIALS_JSON, storedDecryptMaterials)
	}
}