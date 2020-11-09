package tga.hib_json_aws_kms.encryption.kms.internal

import javax.crypto.spec.SecretKeySpec
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class DeserializedSecretKeySpec: SecretKeySpec {

	@JsonCreator
	constructor(@JsonProperty("encoded") key: ByteArray,
				@JsonProperty("algorithm") algorithm: String) : super(key, algorithm) {}
}