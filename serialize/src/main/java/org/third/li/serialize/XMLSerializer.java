package org.third.li.serialize;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

/**
 *
 * com.thoughtworks.xstream.security.ForbiddenClassException: xxx
 *
 */
public class XMLSerializer implements Serializer{

    static XStream stream=new XStream(new DomDriver());
    static {
        stream.addPermission(AnyTypePermission.ANY);
    }

    @Override
    public <T> byte[] serialize(T obj) {
        return stream.toXML(obj).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data) {
        return (T)stream.fromXML(new String(data));
    }


}
