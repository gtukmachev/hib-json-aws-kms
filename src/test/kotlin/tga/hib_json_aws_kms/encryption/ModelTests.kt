package tga.hib_json_aws_kms.encryption

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataBuilder
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistry
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tga.hib_json_aws_kms.model.Box
import tga.hib_json_aws_kms.model.OldBoxInfo
import java.util.*

class ModelTests {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ModelTests::class.java)
    }

    private lateinit var sessionFactory: SessionFactory
    private lateinit var session: Session

    @Before
    fun setUp() {
        // A SessionFactory is set up once for an application!
        val registry: StandardServiceRegistry = StandardServiceRegistryBuilder()
                .build()
        val sources = MetadataSources(registry).apply {
            addAnnotatedClass(Box::class.java)
        }

        val metadataBuilder: MetadataBuilder = sources.getMetadataBuilder()
        //metadataBuilder.applyAttributeConverter()

        sessionFactory = metadataBuilder.build().buildSessionFactory()!!
        session = sessionFactory.openSession()
        session.beginTransaction()
    }

    @After
    fun close() {
        session.getTransaction().commit()
        session.close()
    }

    private fun fill(): Map<Int, Box> {
        session.createNativeQuery("truncate Box").executeUpdate()
        val boxes = TreeMap<Int, Box>()
        for (i in 1..8) {
            val oldInfo = OldBoxInfo(inn = "$i-$i", address = "On $i");
            val box = Box(
                    login = "" + i,
                    oldInfo = oldInfo,
                    info = oldInfo.toBoxInfo()
            )

            when(i) {
                2 -> { box.info = null                                       }
                3 -> {                                    box.oldInfo = null }
                4 -> { box.info = null;                   box.oldInfo = null }
                5 -> { box.info = null;                   box.oldInfo!!.address = "Some where $i"}
                6 -> { box.info!!.address = "New at $i";  box.oldInfo = null }
                7 -> {                                    box.oldInfo!!.address = "Some where $i"}
                8 -> { box.info!!.address = "New at $i"                      }
            }

            session.save(box)

//            boxes.put(box.id!!, box.copy())
        }

        return boxes
    }

    @Test
    fun fillAndAutoUpdate() {
        val initialBoxes = fill()
        close()
        session = sessionFactory.openSession()
        session.beginTransaction()
        val changedBoxes = TreeMap<Int, Box>()
        val ids: List<Integer> = session.createQuery("select id from Box", Integer::class.java).list()
        logger.info("All boxes: ------------------------ ")
        for (id in ids) {
            val box: Box = session.get(Box::class.java, id)
            logger.info("{}", box)
            changedBoxes.put(id.toInt(), box)
        }
        logger.info("----------------------------------- ")
        close()
        session = sessionFactory.openSession()
        session.beginTransaction()
        logger.info("Changed boxes: ------------------------ ")
        for (id in ids) {
            val box: Box = session.get(Box::class.java, id)
            logger.info("{}", box)
            assertThat(box, `is`(changedBoxes[id]))
        }
        logger.info("----------------------------------- ")
    }

}
