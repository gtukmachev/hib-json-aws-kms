package tga.hib_json_aws_kms.encryption.kms.internal

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import java.security.PrivateKey

class PrivateKeyDeserializer: StdDeserializer<PrivateKey>(PrivateKey::class.java) {

	override fun deserialize(jsonParser: JsonParser, context: DeserializationContext): PrivateKey {
		return jsonParser.readValueAs(DeserializedECPrivateKey::class.java)
	}
}