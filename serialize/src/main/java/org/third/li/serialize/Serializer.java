package org.third.li.serialize;

public interface Serializer {


    <T> byte[] serialize(T obj);

    <T> T deserialize(byte[] data);

}
