package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ITokenizer;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Created by ekongla on 17/01/2017.
 */
@API(Internal)
public class CsvMapReaderWrapper extends CsvMapReader {

    private static final AtomicInteger opened = new AtomicInteger(0);

    private static final AtomicInteger closed = new AtomicInteger(0);

    public CsvMapReaderWrapper(Reader reader, CsvPreference preferences) {
        super(reader, preferences);
        synchronized (CsvMapReaderWrapper.class) {
            opened.incrementAndGet();
        }
    }

    public CsvMapReaderWrapper(ITokenizer tokenizer, CsvPreference preferences) {
        super(tokenizer, preferences);
        synchronized (CsvMapReaderWrapper.class) {
            opened.incrementAndGet();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        synchronized (CsvMapReaderWrapper.class) {
            closed.incrementAndGet();
        }
    }

    public static synchronized int getOpened() {
        return opened.get();
    }

    public static synchronized int getClosed() {
        return closed.get();
    }

}
