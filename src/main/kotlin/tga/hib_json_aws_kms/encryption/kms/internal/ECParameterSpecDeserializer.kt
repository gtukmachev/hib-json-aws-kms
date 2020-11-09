package tga.hib_json_aws_kms.encryption.kms.internal

import java.security.spec.ECParameterSpec
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext

class ECParameterSpecDeserializer: StdDeserializer<ECParameterSpec>(ECParameterSpec::class.java) {

	override fun deserialize(jsonParser: JsonParser, context: DeserializationContext): ECParameterSpec {
		return jsonParser.readValueAs(DeserializedECParameterSpec::class.java)
	}
}