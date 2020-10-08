package tga.hib_json_aws_kms.encryption.hibernate

import tga.hib_json_aws_kms.encryption.Encryptor

class FakeEncryptor : Encryptor {
    override fun encrypt(content: String): String = "<$content>"

    override fun decrypt(content: String): String {
        return if (content.length > 1 && content[0] == '<') {
            content.substring(1, content.length - 1)
        } else content
    }
}
