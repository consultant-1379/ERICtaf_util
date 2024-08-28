package sun.net.www.protocol.test;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 */
public class TestConnection extends URLConnection {

    private static String value;

    public static void setValue(String value) {
        TestConnection.value = value;
    }

    public TestConnection(URL u) {
        super(u);
    }

    @Override
    public void connect() throws IOException {
        System.out.println("connected");
    }

    @Override
    public Object getContent() throws IOException {
        return value;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(value.getBytes("UTF-8"));
    }

}
