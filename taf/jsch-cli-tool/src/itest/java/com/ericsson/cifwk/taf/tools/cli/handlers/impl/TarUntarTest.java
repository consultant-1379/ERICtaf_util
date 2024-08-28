package com.ericsson.cifwk.taf.tools.cli.handlers.impl;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by ekonsla on 05/02/2016.
 */
public class TarUntarTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(TarUntarTest.class);
    private File dir;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        dir = new File(tempFolder.getRoot().getAbsolutePath(), "tartest");
        dir.mkdirs();
    }

    @Test
    public void testTarUntar() {
        File tarTo = new File(dir.getAbsolutePath() + "/test.tar");
        File untarTo = new File(dir.getAbsolutePath(), "unTarred");
        final String levelFolderToCopy = "topLevelFolderToCopy/";
        final String subfolder1Name = "subfolder1/";
        final String subfolder2Name = "subfolder2/";
        final String file1Name = "localFileForCopy1.txt";
        final String file2Name = "localFileForCopy2.txt";
        final String file3Name = "localFileForCopy3.txt";
        final String file1Content = "content file 1";
        final String file2Content = "content file 2";
        final String file3Content = "content file 3";
        final String fileSep = System.getProperty("file.separator");

        try {
            File topLevelFolderToCopy = new File(dir.getAbsolutePath(), levelFolderToCopy);
            topLevelFolderToCopy.mkdirs();
            File subfolder1 = new File(topLevelFolderToCopy.getAbsolutePath(), subfolder1Name);
            subfolder1.mkdirs();
            File subfolder2 = new File(subfolder1.getAbsolutePath(), subfolder2Name);
            subfolder2.mkdirs();

            File file1 = new File(topLevelFolderToCopy, file1Name);
            Files.write(file1.toPath(), file1Content.getBytes(), StandardOpenOption.CREATE);
            File file2 = new File(subfolder1, file2Name);
            Files.write(file2.toPath(), file2Content.getBytes(), StandardOpenOption.CREATE);
            File file3 = new File(subfolder2, file3Name);
            Files.write(file3.toPath(), file3Content.getBytes(), StandardOpenOption.CREATE);

            Tar.tar(topLevelFolderToCopy.getAbsolutePath(), tarTo.getAbsolutePath());

            assertThat(new File(tarTo.getAbsolutePath()).exists()).isTrue();

            Tar.untar(tarTo.getAbsolutePath(), untarTo.getAbsolutePath());

            File unTarFile1 = new File(untarTo.getAbsolutePath() + fileSep + levelFolderToCopy + file1Name);
            File unTarFile2 = new File(untarTo.getAbsolutePath() + fileSep + levelFolderToCopy + subfolder1Name + file2Name);
            File unTarFile3 = new File(untarTo.getAbsolutePath() + fileSep + levelFolderToCopy + subfolder1Name + subfolder2Name + file3Name);

            assertThat(unTarFile1.exists()).isTrue();
            assertThat(Files.readAllBytes(unTarFile1.toPath()).toString().matches(file1Content));
            assertThat(unTarFile2.exists()).isTrue();
            assertThat(Files.readAllBytes(unTarFile2.toPath()).toString().matches(file2Content));
            assertThat(unTarFile3.exists()).isTrue();
            assertThat(Files.readAllBytes(unTarFile3.toPath()).toString().matches(file3Content));
        } catch (IOException e) {
            LOGGER.error("Unable to create temp folder", e);
            fail("Test case failed, please check the stack trace for more details");
        }
    }

}
