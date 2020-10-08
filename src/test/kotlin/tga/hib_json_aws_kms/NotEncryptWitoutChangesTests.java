package tga.hib_json_aws_kms;



public class NotEncryptWitoutChangesTests {
/*

    private static final Logger logger = LoggerFactory.getLogger(NotEncryptWitoutChangesTests.class);
    private static SessionFactory sessionFactory = null;
    private static Session session;

    @Before
    public void setUp() {
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .build();

        MetadataSources sources = new MetadataSources( registry );
        sources.addAnnotatedClass( Box.class );

        MetadataBuilder metadataBuilder = sources.getMetadataBuilder();
        //metadataBuilder.applyAttributeConverter()

        sessionFactory = metadataBuilder.build().buildSessionFactory();

        session = sessionFactory.openSession();
        session.beginTransaction();
    }

    @After
    public void close() {
        session.getTransaction().commit();
        session.close();
    }



    public Map<Integer, Box> fill() {
        session.createNativeQuery("truncate Box").executeUpdate();

        Map<Integer, Box> boxes = new TreeMap<>();

        for (int i = 1; i <= 8; i++ ) {
            Box box =  new Box();
            box.setLogin(""+i);
            box.setOldInfo(new OldBoxInfo());
                box.getOldInfo().setInn("" + i + "-" + i);
                box.getOldInfo().setAddress("On "+ i);
            box.setInfo(box.getOldInfo().toBoxInfo());
                box.getInfo().setSize(i*10);

            boxes.put(box.getId(), box.clone());

            if (i == 2) { box.setInfo   (null); }
            if (i == 3) { box.setOldInfo(null); }
            if (i == 4) { box.setOldInfo(null); box.setInfo(null);}
            if (i == 5) { box.setInfo   (null); box.getOldInfo().setAddress("Some where " + i); }
            if (i == 6) { box.setOldInfo(null); box.getInfo().setAddress("Some where " + i);}
            if (i == 7) { box.getOldInfo().setAddress("Some where " + i); }
            if (i == 8) { box.getInfo().setAddress("Some where " + i);}

            session.save(box);
        }

        return boxes;
    }

    @Test
    public void FillAndAutoUpdate() {
        Map<Integer, Box> initialBoxes = fill();
        close();
        session = sessionFactory.openSession();
        session.beginTransaction();

        Map<Integer, Box> changedBoxes = new TreeMap<>();

        List<Integer> ids = session.createQuery("select id from Box", Integer.class).list();

        logger.info("All boxes: ------------------------ ");
        for(Integer id : ids) {
            Box box = session.get(Box.class, id);
            logger.info("{}", box);
            changedBoxes.put(id, box);
        }
        logger.info("----------------------------------- ");
        close();
        session = sessionFactory.openSession();
        session.beginTransaction();

        logger.info("Changed boxes: ------------------------ ");
        for(Integer id : ids) {
            Box box = session.get(Box.class, id);
            logger.info("{}", box);

            assertThat( box, is( changedBoxes.get(id) ) );

        }
        logger.info("----------------------------------- ");

    }

*/
}
