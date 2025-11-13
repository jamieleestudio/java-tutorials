package org.third.li.serialize;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerializeTest {

    @Test
    public void hessianSerializerTest(){
        User user=new User();
        user.setName("Mic");
        user.setAge(18);
        Serializer serializer=new HessianSerializer();
        byte[] bytes=serializer.serialize(user);

        User deserializeUser = serializer.deserialize(bytes);
        Assertions.assertTrue(bytes.length > 0);
        Assertions.assertEquals(user.toString(),deserializeUser.toString());
    }

    @Test
    public void javaSerializerTest(){
        User user=new User();
        user.setName("Mic");
        user.setAge(18);
        Serializer serializer=new JavaSerializer();
        byte[] bytes=serializer.serialize(user);

        User deserializeUser = serializer.deserialize(bytes);
        Assertions.assertTrue(bytes.length > 0);
        Assertions.assertEquals(user.toString(),deserializeUser.toString());
    }


    @Test
    public void xmlSerializerTest(){
        User user=new User();
        user.setName("Mic");
        user.setAge(18);
        Serializer serializer=new XMLSerializer();
        byte[] bytes=serializer.serialize(user);

        User deserializeUser = serializer.deserialize(bytes);
        Assertions.assertTrue(bytes.length > 0);
        Assertions.assertEquals(user.toString(),deserializeUser.toString());
    }

}
