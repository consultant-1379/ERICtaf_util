package com.ericsson.cifwk.taf.utils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.ericsson.cifwk.taf.utils.FileUtils.getSeparator;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         05/09/2016
 */
public class FileUtilsTest {

    private static final String SUB_DIR = "SUB";

    private static final String FILE_NAME_A = "a.txt";
    private static final String FILE_NAME_B = "b.txt";

    private static final String CONTENT = "hello";

    File sourceDir;
    File destDir;
    File srcFile;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
    * Creates the following file tree inside a temporary folder:
     *  SOURCE
     *      a.txt
     *      SUB
     *          b.txt
     *
     *  DESTINATION
     */
    @Before
    public void setUp() throws Exception {
        sourceDir = folder.newFolder();
        destDir = folder.newFolder();

        srcFile = Files.createFile(Paths.get(sourceDir.getAbsolutePath(), FILE_NAME_A)).toFile();
        Files.write(srcFile.toPath(), CONTENT.getBytes(), StandardOpenOption.CREATE);

        Path sourceSubDir = Files.createDirectory(Paths.get(sourceDir.getAbsolutePath(), SUB_DIR));
        Files.createFile(Paths.get(sourceSubDir.toString(), FILE_NAME_B));
    }

    @Test
    public void getRelativePathTo() throws Exception {
        File file = sourceDir;

        String relativePathString = FileUtils.getRelativePathTo(file);

        Path relativePath = Paths.get(relativePathString);
        assertThat(relativePath.isAbsolute()).isFalse();

        Path realPath = relativePath.toRealPath();
        assertThat(file.toPath()).isEqualTo(realPath);
    }

    @Test
    public void getTopDirFromCurrent() throws Exception {
        String topDir = FileUtils.getTopDirFromCurrent();
        Path root = Paths.get(FileUtils.getCurrentDir()).getRoot();
        Path topDirPath = Paths.get(topDir).toRealPath();

        assertThat(topDirPath).isEqualTo(root);
        assertThat(topDir).endsWith(getSeparator());
    }

    @Test
    public void copyDirRecursive() throws Exception {

        FileUtils.copy(sourceDir, destDir);

        File[] files = destDir.listFiles();
        assertThat(files).hasSize(1);

        File target = files[0];
        assertThat(target.isDirectory()).isTrue();
        assertThat(target.getName()).isEqualTo(sourceDir.getName());

        String[] fileNames = target.list();
        assertThat(fileNames).containsExactlyInAnyOrder(SUB_DIR, FILE_NAME_A);

        File subDir = new File(target.getPath() + getSeparator() + SUB_DIR);

        assertThat(subDir.exists());
        assertThat(subDir.isDirectory());
        assertThat(subDir.list()).containsExactly(FILE_NAME_B);
    }

    @Test
    public void copyDirNonRecursive() throws Exception {

        FileUtils.copy(sourceDir, destDir, false);

        File[] files = destDir.listFiles();
        assertThat(files).hasSize(1);

        File target = files[0];
        assertThat(target.isDirectory()).isTrue();
        assertThat(target.getName()).isEqualTo(sourceDir.getName());

        files = target.listFiles();
        assertThat(files).isEmpty();
    }

    @Test
    public void copyFileToDir() throws Exception  {
        FileUtils.copy(srcFile, destDir);
        assertThat(destDir.list()).containsExactly(FILE_NAME_A);
    }

    @Test
    public void copyFileToFileOverwriting() throws Exception {
        File dstFile = Files.createFile(Paths.get(destDir.getAbsolutePath(), FILE_NAME_A)).toFile();
        dstFile.setLastModified(System.currentTimeMillis() - 60_000);

        assertThat(srcFile.lastModified()).isNotEqualTo(dstFile.lastModified());

        FileUtils.copy(srcFile, dstFile);

        assertThat(destDir.list()).containsExactly(FILE_NAME_A);
        assertThat(destDir.listFiles()[0].lastModified()).isEqualTo(srcFile.lastModified());
    }

    @Test
    public void copyFileToFileNonOverwriting() throws Exception {
        File dstFile = Files.createFile(Paths.get(destDir.getAbsolutePath(), FILE_NAME_A)).toFile();
        dstFile.setLastModified(System.currentTimeMillis() - 60_000);

        long lastModifiedDst = dstFile.lastModified();
        assertThat(srcFile.lastModified()).isNotEqualTo(lastModifiedDst);

        FileUtils.copy(srcFile, dstFile, false, false);

        assertThat(destDir.list()).containsExactly(FILE_NAME_A);
        assertThat(destDir.listFiles()[0].lastModified()).isEqualTo(lastModifiedDst);
    }


    @Test
    public void copyFileToNewFile() throws Exception {
        File dstFile = new File(destDir.getPath(), FILE_NAME_B);
        assertThat(dstFile.exists()).isFalse();

        FileUtils.copy(srcFile, dstFile);

        assertThat(dstFile.exists()).isTrue();

        String content = new String(Files.readAllBytes(dstFile.toPath()));
        assertThat(content).isEqualTo(CONTENT);
    }
}