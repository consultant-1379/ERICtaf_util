package com.ericsson.cifwk.taf.maven.plugin;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringBufferInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TafArchetypeMojoTest {

    private static Logger log = LoggerFactory.getLogger(TafArchetypeMojoTest.class);
    private static TafArchetypeMojo tafArchetypeMojo = new TafArchetypeMojo();

    @Test
    public void testGetInfoFromNexus(){
        assertThat(tafArchetypeMojo.getInfoFromNexus(), CoreMatchers.containsString("<artifactId>ERICtaf_util</artifactId>"));
    }

    @Test
    public void testTafReleaseFromNexus(){
        assertEquals(getLatestVersion(tafArchetypeMojo.getInfoFromNexus()), tafArchetypeMojo.getLatestVersionFromNexus());
    }

    /**
     * Get the latest version of TAF from the list of versions in the xml from nexus
     * @param nexusInfo2
     * @return
     */
    private String getLatestVersion(String nexusInfo2) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;
        String latestRelease = null;
        try {
            doc = factory.newDocumentBuilder().parse(
                    new StringBufferInputStream(nexusInfo2));
            NodeList list = doc.getElementsByTagName("version");
            latestRelease = list.item(list.getLength()-1).getTextContent();
            String buildNumber = latestRelease.split("\\.")[2];
            int index = latestRelease.lastIndexOf(buildNumber);
            latestRelease = new StringBuilder(latestRelease).replace(index, index+buildNumber.length(), "1").toString();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return latestRelease;
    }
}
