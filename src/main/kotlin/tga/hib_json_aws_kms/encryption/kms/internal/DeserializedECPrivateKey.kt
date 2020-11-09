package tga.hib_json_aws_kms.encryption.kms.internal

import sun.security.ec.ECPrivateKeyImpl
import java.security.spec.ECParameterSpec
import java.math.BigInteger
import sun.security.pkcs.PKCS8Key
import java.security.interfaces.ECPrivateKey
import sun.security.x509.AlgorithmId
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class DeserializedECPrivateKey: PKCS8Key, ECPrivateKey {

	@JsonIgnore
	private var privateKey: ECPrivateKeyImpl

	@JsonCreator
	constructor(@JsonProperty("s") s: BigInteger, @JsonProperty("params") params: ECParameterSpec) {
		privateKey = ECPrivateKeyImpl(s, params)
	}

	override fun getParams(): ECParameterSpec {
		return privateKey.getParams()
	}

	override fun getS(): BigInteger {
		return privateKey.getS()
	}

	override fun getAlgorithm(): String {
		return privateKey.getAlgorithm()
	}

	override fun getFormat(): String {
		return privateKey.getFormat()
	}

	override fun getEncoded(): ByteArray {
		return privateKey.getEncoded()
	}
}