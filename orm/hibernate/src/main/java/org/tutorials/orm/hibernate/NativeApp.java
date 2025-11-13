package org.tutorials.orm.hibernate;

import org.hibernate.cfg.Configuration;

import static java.lang.System.out;
import static org.hibernate.cfg.JdbcSettings.*;

/**
 * Hello world!
 *
 */
public class NativeApp {
    public static void main(String[] args) {
        var sessionFactory = new Configuration()
                .addAnnotatedClass(Book.class)
                // use H2 in-memory database
                .setProperty(JAKARTA_JDBC_URL, "jdbc:h2:mem:db1")
                .setProperty(JAKARTA_JDBC_USER, "sa")
                .setProperty(JAKARTA_JDBC_PASSWORD, "")
                // use Agroal connection pool
                .setProperty("hibernate.agroal.maxSize", 20)
                // log slow queries
                .setProperty("hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS",1)
                // display SQL in console
                .setProperty(SHOW_SQL, true)
                .setProperty(FORMAT_SQL, true)
                .setProperty(HIGHLIGHT_SQL, true)
                .buildSessionFactory();
        // export the inferred database schema
        sessionFactory.getSchemaManager().exportMappedObjects(true);
        // persist an entity
        sessionFactory.inTransaction(session -> {
            session.persist(new Book("9781932394153", "Hibernate in Action"));
        });

        // query data using HQL
        sessionFactory.inSession(session -> {
            out.println(session.createSelectionQuery("select isbn||': '||title from Book").getSingleResult());
        });

        // query data using criteria API
        sessionFactory.inSession(session -> {
            var builder = sessionFactory.getCriteriaBuilder();
            var query = builder.createQuery(String.class);
            var book = query.from(Book.class);

//            query.select(builder.concat(builder.concat(book.get(Book_.isbn), builder.literal(": ")),
//                    book.get(Book_.title)));

            query.select(builder.concat(builder.concat(book.get("isbn"), builder.literal(": ")),
                    book.get("title")));
            out.println(session.createSelectionQuery(query).getSingleResult());
        });
    }
}
