package management.jolokia;

import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;

import javax.management.MalformedObjectNameException;

public class JolokiaDemo {


    public static void main(String[] args) throws MalformedObjectNameException, J4pException {
        J4pClient j4pClient = new J4pClient("http://localhost:8081/jolokia");
//        J4pReadRequest req = new J4pReadRequest("java.lang:type=Memory","HeapMemoryUsage");
        J4pReadRequest req = new J4pReadRequest("com.base.jmx.management:type=Memory","HeapMemoryUsage");
        req.setPath("used");
        J4pReadResponse resp = j4pClient.execute(req);
        System.out.println((char[]) resp.getValue());

    }

}
