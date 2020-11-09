package tga.hib_json_aws_kms.encryption.kms.internal

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import javax.crypto.SecretKey
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import javax.crypto.spec.SecretKeySpec

class SecretKeyDeserializer: StdDeserializer<SecretKey>(SecretKey::class.java) {

	override fun deserialize(jsonParser: JsonParser, context: DeserializationContext): SecretKey {
		return jsonParser.readValueAs(DeserializedSecretKeySpec::class.java)
	}
}