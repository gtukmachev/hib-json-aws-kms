package tga.hib_json_aws_kms.encryption.kms.internal

import java.security.spec.ECParameterSpec
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.security.spec.EllipticCurve
import java.security.spec.ECPoint
import java.math.BigInteger

class DeserializedECParameterSpec: ECParameterSpec {

	@JsonCreator
	constructor(@JsonProperty("curve") curve: EllipticCurve,
				@JsonProperty("g") g: ECPoint,
				@JsonProperty("n") n: BigInteger,
				@JsonProperty("h") h: Int) : super(curve, g, n, h) {}
}