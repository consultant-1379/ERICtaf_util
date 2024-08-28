package sun.net.www.protocol.test;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TestConnection extends URLConnection {

    private static Map<String, String> value = new HashMap();

    public TestConnection(URL u) {
        super(u);
    }

    @Override
    public void connect() throws IOException {
        System.out.println("connected");
    }

    @Override
    public Object getContent() throws IOException {
        return value.get(url.getHost()).getBytes("UTF-8");
    }

    @Override
    public InputStream getInputStream() throws IOException {

        return new ByteArrayInputStream(value.get(url.getHost()).getBytes("UTF-8"));
    }

    public static void setValue(String path, String content) {
        value.put(path, content);
    }
}