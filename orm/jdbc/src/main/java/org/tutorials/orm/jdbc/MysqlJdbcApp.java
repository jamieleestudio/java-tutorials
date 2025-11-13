package org.tutorials.orm.jdbc;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.sql.*;

public class MysqlJdbcApp {



    public static final String DROP_BOOK_TABLE_DDL_SQL = "DROP TABLE Book";
    public static final String CREATE_BOOK_TABLE_DDL_SQL = "create table Book (" +
            "    isbn varchar(255) not null," +
            "    title varchar(255)," +
            "    primary key (isbn)" +
            ")";
    public static final String INSERT_BOOK_DML_SQL = "insert into Book values ('1234','Harry Potter')";



    public static boolean accepts() throws ClassNotFoundException, SQLException {
        //Class.forName("org.h2.Driver");
        Driver driver = DriverManager.getDriver("jdbc:mysql://localhost:3306/test_demo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
//        var bool = driver.acceptsURL("jdbc:mysql://localhost:3306/test_demo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC","root","password");
        var bool = driver.acceptsURL("jdbc:mysql://localhost:3306/test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");

        return bool;
    }

    public static void simpleQuery() throws SQLException, IntrospectionException {

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC","root","123456");
//        con.setAutoCommit(false);
        Statement stmt = con.createStatement();
        stmt.execute(CREATE_BOOK_TABLE_DDL_SQL);
        stmt.execute(INSERT_BOOK_DML_SQL);
        ResultSet rs = stmt.executeQuery("SELECT isbn,title FROM Book");
        rs.getMetaData();//元数据
        BeanInfo userBeanInfo = Introspector.getBeanInfo(Book.class, Object.class);
        for (PropertyDescriptor propertyDescriptor : userBeanInfo.getPropertyDescriptors()) {
            System.out.println(propertyDescriptor.getName() + " , " + propertyDescriptor.getPropertyType());
            propertyDescriptor.getReadMethod();
            propertyDescriptor.getWriteMethod();
        }
        while (rs.next()) {
            String x = rs.getString("title");
            String s = rs.getString("isbn");
//            System.out.println("title:{},isbn:{}",x,s);
            System.out.println("title:"+x+",isbn:"+s);
        }
    }

    public static void main( String[] args ) throws ClassNotFoundException, SQLException, IntrospectionException {

//        System.out.println(accepts());
        simpleQuery();

    }

}
