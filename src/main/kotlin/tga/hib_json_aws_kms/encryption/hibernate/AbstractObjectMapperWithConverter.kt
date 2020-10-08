package tga.hib_json_aws_kms.encryption.hibernate

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider

abstract class AbstractObjectMapperWithConverter : ObjectMapper {
    protected abstract fun encodeContent(writeValueAsString: String?): String?
    protected abstract fun decodeString(content: String?): String?

    constructor() : super() {}
    constructor(jf: JsonFactory?) : super(jf) {}
    constructor(src: ObjectMapper?) : super(src) {}
    constructor(jf: JsonFactory?, sp: DefaultSerializerProvider?, dc: DefaultDeserializationContext?) : super(jf, sp, dc) {}

    @Throws(JsonProcessingException::class)
    override fun writeValueAsString(value: Any?): String? {
        return encodeContent(super.writeValueAsString(value))
    }

    @Throws(JsonProcessingException::class, JsonMappingException::class)
    override fun <T> readValue(content: String, valueType: Class<T>): T {
        val decodedContent = decodeString(content)
        return super.readValue(decodedContent, valueType)
    }

    @Throws(JsonProcessingException::class, JsonMappingException::class)
    override fun <T> readValue(content: String, valueTypeRef: TypeReference<T>): T {
        val decodedContent = decodeString(content)
        return super.readValue(decodedContent, valueTypeRef)
    }

    @Throws(JsonProcessingException::class, JsonMappingException::class)
    override fun <T> readValue(content: String, valueType: JavaType): T {
        val decodedContent = decodeString(content)
        return super.readValue(decodedContent, valueType)
    }

    @Throws(JsonProcessingException::class, JsonMappingException::class)
    override fun readTree(content: String): JsonNode {
        val decodedContent = decodeString(content)
        return super.readTree(decodedContent)
    }
}
