package com.yineng.bpe;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Unit test for simple App.
 */
@SpringBootTest
public class AppTest{


    @Test
    void contextLoads() {
        try {

            final InitialContext context = new InitialContext();
            context.bind("java:comp/env/jdbc/datasource", new DriverManagerDataSource("jdbc:h2:mem:mydb"));

            final DataSource ds = (DataSource) context.lookup("java:comp/env/jdbc/datasource");

            assert ds.getConnection() != null;
            System.out.println(ds.getConnection());

        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        }
    }

}
