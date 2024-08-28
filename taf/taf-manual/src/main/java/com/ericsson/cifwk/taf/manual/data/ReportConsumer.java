package com.ericsson.cifwk.taf.manual.data;

import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.data.DataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import com.ericsson.cifwk.meta.API;

/**
 * Class preparing test report data from CSV report
 * @deprecated Usage is deprecated
 */

@Deprecated
@API(API.Quality.Deprecated)
@API.Since(2.37)
public class ReportConsumer {
    /**
     * System property to specify location of directory with CSV files
     */
    public static final String LOCATION_ARG = "manual_reports";
    /**
     * System property to specify how often location should be checked
     */
    public static final String CHECK__ARG = "sleep_before_check";
    /**
     * Default location of manual report of CSV files
     */
    public static final String DEFAULT_LOCATION = "/tmp/manual_reports";
    /**
     * Default sleep time between checks - 1sec
     */
    public static final Long DEFAULT_SLEEP_BETWEEN_CHECKS = 1000L;

    private String location = null;
    private AtomicBoolean waitForReport = new AtomicBoolean(true);
    private File[] filesToRead;
    private final Timer scanTimer = new Timer("Manual test scanner", true);
    private static final Logger log = LoggerFactory.getLogger(ReportConsumer.class);

    /**
     * Method to fetch location lazily
     *
     * @return
     */
    public String getLocation() {
        Object tmpLocation;
        if ((tmpLocation = DataHandler.getAttribute(LOCATION_ARG)) != null) {
            location = (String) tmpLocation;
        } else {
            location = DEFAULT_LOCATION;
        }
        return location;
    }

    /**
     * ReportScanner is a task that will look into directory and check existence of CSV files
     */
    private class ReportScanner extends TimerTask {
        File reportDir = new File(getLocation());

        @Override
        public void run() {
            File[] csvFiles;
            if (waitForReport.get()) {
                csvFiles = reportDir.listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".csv") || pathname.getName().endsWith(".CSV");
                    }
                });
                if (csvFiles != null && csvFiles.length > 0) {
                    waitForReport.set(false);
                    filesToRead = csvFiles;
                }
            }
        }
    }

    /**
     * Blocking method to start scanning in the predefined location for CSV file
     *
     * @throws InterruptedException
     */
    private void scan() throws InterruptedException {
        Long period = DEFAULT_SLEEP_BETWEEN_CHECKS;
        final Object periodTxt = DataHandler.getAttribute(CHECK__ARG);
        if (periodTxt != null) {
            try {
                period = Long.valueOf((String) periodTxt);
            } catch (Exception e) {
                log.info("Exception has been thrown:", e);
            }
        }
        String hostName = "uknown machine";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.info("Hostname is not known. Exception has been thrown:", e);
        }
        scanTimer.purge();
        log.info("From now on the report can be consumed in {} on machine {}", getLocation(), hostName);
        scanTimer.schedule(new ReportScanner(), 0, period);
        while (waitForReport.get()) {
            Thread.sleep(period);
        }
        scanTimer.cancel();
    }

    /**
     * Method providing data from all CSV files from scanned location
     *
     * @return
     */
    @DataSource
    public List<Map<String, Object>> getTestReportData() {
        final List<Map<String, Object>> allTheData = new ArrayList<>();
        try {
            scan();
            for (File csv : filesToRead) {
                allTheData.addAll(readCsvFiles(csv));
            }
        } catch (InterruptedException e1) {
            log.error("Cannot process manual data report due to {}", e1.getMessage());
            log.info("Interrupted Exception has been thrown:", e1);
        }
        return allTheData;
    }

    private List<Map<String, Object>> readCsvFiles(File csv) {
        try {
            FileReader csvFile;
            CsvMapReader reader;
            csvFile = new FileReader(csv);
            reader = new CsvMapReader(csvFile, CsvPreference.EXCEL_PREFERENCE);
            return readCsvRows(csv, reader, csvFile);
        } catch (IOException e) {
            log.warn("Problem reading file {}. Cause: {}. File will be ignored", csv.getName(), e.getMessage());
            log.info("IO Exception has been thrown:", e);
        }
        return null;
    }

    private List<Map<String, Object>> readCsvRows(File csv, CsvMapReader reader, FileReader csvFile) throws IOException {
        List<Map<String, Object>> singleFileData = new ArrayList<>();
        try {
            String[] headers = reader.getHeader(true);
            Map<String, String> row;
            Map<String, Object> rowData;
            while ((row = reader.read(headers)) != null) {
                rowData = new HashMap<>();
                for (Map.Entry<String, String> entry : row.entrySet()) {
                    rowData.put(entry.getKey(), entry.getValue());
                }
                singleFileData.add(rowData);
            }
        } catch (Exception anyException) {
            log.warn("File {} does not have proper format or content", csv);
            log.info("Exception has been thrown:", anyException);
        } finally {
            reader.close();
            csvFile.close();
        }
        return singleFileData;
    }
}
