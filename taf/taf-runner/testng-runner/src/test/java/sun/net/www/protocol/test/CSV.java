package sun.net.www.protocol.test;

import java.util.ArrayList;
import java.util.List;

public class CSV {

    String[] header;
    List<String[]> rows = new ArrayList();

    public CSV(String... headers) {
        this.header = headers;
    }

    public CSV add(String... row) {
        rows.add(row);
        return this;
    }

    public CSV add(Object... row) {
        String[] r = new String[row.length];
        for (int i=0;i<row.length;i++) {
          r[i]=""+row[i];
        }
        rows.add(r);
        return this;
    }

    public static  CSV csv(String... headers) {
        return new CSV(headers);
    }

    @Override
    public String toString() {
        StringBuilder value = new StringBuilder();
        boolean first = true;
        for (String h : header) {
            value.append(first ? "" : ",").append(h);
            first = false;
        }
        value.append("\n");
        for (String[] row : rows) {
            first = true;
            for (String col : row) {
                value.append(first ? "" : ",").append(col);
                first = false;
            }
            value.append("\n");
        }
        return value.toString();
    }

}
