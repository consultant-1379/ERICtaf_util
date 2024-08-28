package com.ericsson.cifwk.taf.datasource;

import static org.junit.Assert.assertEquals;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.supercsv.io.CsvMapReader;

import com.ericsson.cifwk.taf.utils.FileFinder;

public class CsvReaderFactoryTest {

    @Test
    public void shouldReturnSpecificFile() throws IOException {
        CsvReaderFactory csvReaderFactory = new CsvReaderFactory("specific.csv", "", ",");
        CsvMapReader reader = csvReaderFactory.createCsvReader();
        List<String> headers = Arrays.asList(reader.getHeader(false));
        assertThat(headers).containsAllOf("my", "name");
    }

    @Test
    public void shouldReturnSpecificFileFromSubFolder() throws IOException {
        CsvReaderFactory csvReaderFactory = new CsvReaderFactory("sub.csv", "", ",");
        CsvMapReader reader = csvReaderFactory.createCsvReader();
        List<String> headers = Arrays.asList(reader.getHeader(false));
        assertThat(headers).containsAllOf("sub1", "sub2");
    }

    @Test
    public void shouldReturnSpecificFileWithAlreadyIncludedFileSeparator() throws IOException {
        CsvReaderFactory csvReaderFactory = new CsvReaderFactory("/specific.csv", "", ",");
        CsvMapReader reader = csvReaderFactory.createCsvReader();
        List<String> headers = Arrays.asList(reader.getHeader(false));
        assertThat(headers).containsAllOf("my", "name");
    }

    @Test
    public void shouldReturnSpecificFileWithAbsolutePath() throws IOException {
        String fileName = FileFinder.findFile("/specific.csv").get(0);
        CsvReaderFactory csvReaderFactory = new CsvReaderFactory(fileName, "", ",");
        CsvMapReader reader = csvReaderFactory.createCsvReader();
        List<String> headers = Arrays.asList(reader.getHeader(false));
        assertThat(headers).containsAllOf("my", "name");
    }

    @Test
    public void shouldReturnSpecificFileWithAlreadyIncludedFileSeparatorandInitialLocation() throws IOException {
        CsvReaderFactory csvReaderFactory = new CsvReaderFactory("/specific.csv", "src/main/resources/data/", ",");
        CsvMapReader reader = csvReaderFactory.createCsvReader();
        List<String> headers = Arrays.asList(reader.getHeader(false));
        assertThat(headers).containsAllOf("my", "name");
    }

    @Test
    public void shouldIgnoreCommentsDefaultDelimiter() throws IOException {
        shouldIgnoreComments("specific.csv", ",");
    }

    private void shouldIgnoreComments(final String location, final String dataProviderDelimiter) throws IOException {
        CsvReaderFactory csvReaderFactory = new CsvReaderFactory(location, "", dataProviderDelimiter);
        CsvMapReader reader = csvReaderFactory.createCsvReader();
        String[] headers = reader.getHeader(true);
        Map<String, String> row = reader.read(headers);
        assertThat(row).containsEntry("my", "be");
        assertThat(row).containsEntry("name", "for");
        assertThat(reader.read(headers)).isNull();
    }

    @Test
    public void shouldIgnoreCommentSpecificDelimiter() throws IOException {
        System.setProperty(CsvReaderFactory.COMMENT_DELIMITER_PROPERTY, "||");
        shouldIgnoreComments("specificDelimiter.csv", ",");
        System.clearProperty(CsvReaderFactory.COMMENT_DELIMITER_PROPERTY);
    }

    @Test
    public void shouldIgnoreCommentDefaultDelimiterSpecificSeparator() throws IOException {
        System.setProperty(CsvReaderFactory.COMMENT_DELIMITER_PROPERTY, "||");
        shouldIgnoreComments("default.csv", "|");
        System.clearProperty(CsvReaderFactory.COMMENT_DELIMITER_PROPERTY);
    }

    @Test
    public void shouldIgnoreCommentsfDelimiterAndSeparatorAreTheSame() throws IOException {
        System.setProperty(CsvReaderFactory.COMMENT_DELIMITER_PROPERTY, "|");
        shouldIgnoreComments("sameDelimiter.csv", "|");
        System.clearProperty(CsvReaderFactory.COMMENT_DELIMITER_PROPERTY);
    }

    @Test
    public void searchTerm() throws Exception {
        assertEquals(File.separator + "my.csv", CsvReaderFactory.getSearchTerm("my.csv"));
        assertEquals(File.separator + "my.csv", CsvReaderFactory.getSearchTerm(File.separator + "my.csv"));
        assertEquals("/my.csv", CsvReaderFactory.getSearchTerm("/my.csv"));
        assertEquals("data/my.csv", CsvReaderFactory.getSearchTerm("data/my.csv"));
    }
}
