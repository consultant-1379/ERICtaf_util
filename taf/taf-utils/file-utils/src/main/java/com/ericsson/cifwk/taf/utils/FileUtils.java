package com.ericsson.cifwk.taf.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileUtils {

    private static Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static String getCurrentDir() {
        return new File("").getAbsolutePath();
    }

    public static String getRelativePathTo(File file) {
        return Paths.get(getCurrentDir()).relativize(file.toPath()).toString();
    }

    /**
     * Get Relative Path of root dir from the current directory
     */
    public static String getTopDirFromCurrent() {
        Path currentPath = Paths.get(getCurrentDir());
        return currentPath.relativize(currentPath.getRoot()).toString() + getSeparator();
    }

    static String getSeparator() {
        return File.separator;
    }

    public static boolean copy(File src, File dst) {
        return copy(src, dst, true, true);
    }

    public static boolean copy(File src, File dst, boolean recursive) {
        return copy(src, dst, recursive, true);
    }


    /**
     * Method for copying files or folders
     */
    public static boolean copy(File src, File dst, boolean recursive, boolean overwrite) {

        final CopyOption[] options = (overwrite) ? new CopyOption[]{REPLACE_EXISTING} : new CopyOption[]{};

        final Path sourcePath = src.toPath();

        final Path targetPath;

        if (dst.isDirectory()) {
            targetPath = dst.toPath().resolve(sourcePath.getFileName());
        } else {
            targetPath = dst.toPath();
        }

        try {
            if (recursive && src.isDirectory()) {
                walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir,
                                                             final BasicFileAttributes attrs) throws IOException {
                        Files.createDirectories(targetPath
                                .resolve(sourcePath.relativize(dir)));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file,
                                                     final BasicFileAttributes attrs) throws IOException {
                        try {
                            Files.copy(file, targetPath
                                    .resolve(sourcePath.relativize(file)), options);
                        } catch (FileAlreadyExistsException e) {
                            // skip
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                try {
                    Files.copy(sourcePath,
                            targetPath,
                            options);
                } catch (FileAlreadyExistsException e) {
                    // skip
                }
            }
        } catch (IOException e) {
            log.error("Error copying files from {} to {}", src, dst, e);
            return false;
        }
        return true;
    }
}
