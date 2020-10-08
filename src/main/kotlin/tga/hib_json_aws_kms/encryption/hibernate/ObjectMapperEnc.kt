package tga.hib_json_aws_kms.encryption.hibernate

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider
import tga.hib_json_aws_kms.encryption.Encryptor
import tga.hib_json_aws_kms.encryption.kms.KmsEncryptor

class ObjectMapperEnc : AbstractObjectMapperWithConverter {
    private val encryptor: Encryptor = KmsEncryptor()
    //  private val encryptor: Encryptor = FakeEncryptor()

    override fun encodeContent(writeValueAsString: String?): String? {
        return when (writeValueAsString) {
            null -> null
            else -> encryptor.encrypt(writeValueAsString)
        }
    }

    override fun decodeString(content: String?): String? {
        return when (content) {
            null -> null
            else -> encryptor.decrypt(content)
        }
    }
    //------------------------------------------------------------
    constructor() : super() {}
    constructor(jf: JsonFactory?) : super(jf) {}
    constructor(src: ObjectMapper?) : super(src) {}
    constructor(jf: JsonFactory?, sp: DefaultSerializerProvider?, dc: DefaultDeserializationContext?) : super(jf, sp, dc) {}
}
