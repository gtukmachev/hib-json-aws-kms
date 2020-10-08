package tga.hib_json_aws_kms.encryption

interface Encryptor {

    fun encrypt(content: String): String

    fun decrypt(content: String): String

}
