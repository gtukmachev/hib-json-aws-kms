package tga.hib_json_aws_kms.encryption.kms

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KmsEncryptorTests {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(KmsEncryptorTests::class.java)
    }

    private lateinit var encryptor: KmsEncryptor

    @Before
    fun setUp() {
        encryptor = KmsEncryptor()
    }


    @Test
    fun test1() {
        val content = "Hi!"
        val encrypted = encrypt(content, 1)
        val decrypted = decrypt(encrypted, 1)

        assertThat(content, `is`(decrypted))
    }

    @Test
    fun dataKeyCachingTest() {
        val content = "{ Hello: enc }"

        logger.info("content =\"{}\"", content)
        val enc = arrayOfNulls<String>(5)
        for (i in enc.indices) {
            enc[i] = encrypt(content, i)
        }
        val dec = arrayOfNulls<String>(enc.size)
        for (i in dec.indices) {
            dec[i] = decrypt(enc[i], i)
        }
        for (s in dec) {
            assertThat(s, `is`(content))
        }
    }


    private fun encrypt(content: String, n: Int): String {
        val encrypted = encryptor.encrypt(content)
        logger.info("$n: encrypt([len:${content.length}]$content) = [len:${encrypted.length}]\"$encrypted\"")
        return encrypted
    }

    private fun decrypt(encrypted: String?, n: Int): String {
        val content = encryptor.decrypt(encrypted!!)
        logger.info("$n: decrypt( [len:${encrypted.length}]$encrypted ) = [len:${content.length}]\"$content\"")
        return content
    }

}
