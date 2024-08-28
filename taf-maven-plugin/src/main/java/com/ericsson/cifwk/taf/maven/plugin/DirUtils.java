package com.ericsson.cifwk.taf.maven.plugin;

import com.ericsson.cifwk.meta.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class DirUtils {

    private static Logger logger = LoggerFactory.getLogger(DirUtils.class);

    /**
     * Utility method to walk a directory and return a collection of files,
     * will not include directories in the returned collection
     *
     * @param baseDirectory The directory from where to start searching from.
     * @return Collection of paths of files found.
     */
    public Collection<Path> scanForFiles(Path baseDirectory) throws IOException {
        final Collection<Path> filePaths = new ArrayList<>();
        Files.walkFileTree(baseDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                filePaths.add(file);
                return FileVisitResult.CONTINUE;
            }
        });
        logger.debug("Method scanForFiles found {} files", Integer.toString(filePaths.size()));
        return filePaths;
    }

    /**
     * Utility method to scan for empty directories
     *
     * @param   baseDirectory The directory from where to start searching from.
     * @return  Collection of empty directories found.
     */
    public Collection<Path> scanForEmptyDirectories(Path baseDirectory) throws IOException {
        final Collection<Path> dirPaths = new ArrayList<>();
        Files.walkFileTree(baseDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (((dir.toFile().listFiles().length) == 0)) {
                    dirPaths.add(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        logger.debug("Method scanForEmptyDirectories found {} empty directories", Integer.toString(dirPaths.size()));
        return dirPaths;
    }

    /**
     * Utility method to copy the contents (including sub directories and files) of a directory
     *
     * <p> The {@code options} parameter may include any of the following:
     *
     * <table border=1 cellpadding=5 summary="">
     * <tr> <th>Option</th> <th>Description</th> </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#REPLACE_EXISTING REPLACE_EXISTING} </td>
     *   <td> If the target file exists, then the target file is replaced if it
     *     is not a non-empty directory. If the target file exists and is a
     *     symbolic link, then the symbolic link itself, not the target of
     *     the link, is replaced. </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#COPY_ATTRIBUTES COPY_ATTRIBUTES} </td>
     *   <td> Attempts to copy the file attributes associated with this file to
     *     the target file. The exact file attributes that are copied is platform
     *     and file system dependent and therefore unspecified. Minimally, the
     *     {@link BasicFileAttributes#lastModifiedTime last-modified-time} is
     *     copied to the target file if supported by both the source and target
     *     file store. Copying of file timestamps may result in precision
     *     loss. </td>
     * </tr>
     * <tr>
     *   <td> {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} </td>
     *   <td> Symbolic links are not followed. If the file is a symbolic link,
     *     then the symbolic link itself, not the target of the link, is copied.
     *     It is implementation specific if file attributes can be copied to the
     *     new link. In other words, the {@code COPY_ATTRIBUTES} option may be
     *     ignored when copying a symbolic link. </td>
     * </tr>
     * </table>
     *
     * @param   source Where to copy from
     * @param   destination Where to copy to
     * @param   options Options specifying how the copy should be done
     */
    public void copyFiles(final Path source, final Path destination, final CopyOption... options) throws IOException {

        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                Path targetPath = destination.resolve(source.relativize(dir));
                try {
                    if (!Files.exists(targetPath)) {
                        Files.createDirectory(targetPath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, destination.resolve(source.relativize(file)), options);
                return FileVisitResult.CONTINUE;
            }
        });
        logger.debug("Copy from {}  to  {} was successful", source.toString(),destination.toString());
    }

    /**
     * Delete a directory structure (files and directories)
     *
     * @param baseDirectory The directory to delete
     */
    public void deleteDirectoryStructure(final Path baseDirectory) throws IOException {

        Files.walkFileTree(baseDirectory, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                logger.debug("Deleting file: {}", file.toString());
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                if (exc == null) {
                    try {
                        logger.debug("Deleting directory: {}", dir.toString());
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } catch (IOException e) {
                        logger.error("Could not delete directory", e.toString());
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        logger.debug("{} was successfully deleted", baseDirectory.toString());
    }

    /**
     * Utility method to move the contents (including sub directories and files) of a directory
     *
     * <p> The {@code options} parameter may include any of the following:
     *
     * <table border=1 cellpadding=5 summary="">
     * <tr> <th>Option</th> <th>Description</th> </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#REPLACE_EXISTING REPLACE_EXISTING} </td>
     *   <td> If the target file exists, then the target file is replaced if it
     *     is not a non-empty directory. If the target file exists and is a
     *     symbolic link, then the symbolic link itself, not the target of
     *     the link, is replaced. </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#COPY_ATTRIBUTES COPY_ATTRIBUTES} </td>
     *   <td> Attempts to copy the file attributes associated with this file to
     *     the target file. The exact file attributes that are copied is platform
     *     and file system dependent and therefore unspecified. Minimally, the
     *     {@link BasicFileAttributes#lastModifiedTime last-modified-time} is
     *     copied to the target file if supported by both the source and target
     *     file store. Copying of file timestamps may result in precision
     *     loss. </td>
     * </tr>
     * <tr>
     *   <td> {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} </td>
     *   <td> Symbolic links are not followed. If the file is a symbolic link,
     *     then the symbolic link itself, not the target of the link, is copied.
     *     It is implementation specific if file attributes can be copied to the
     *     new link. In other words, the {@code COPY_ATTRIBUTES} option may be
     *     ignored when copying a symbolic link. </td>
     * </tr>
     * </table>
     *
     * @param   source Where to copy from
     * @param   destination Where to copy to
     * @param   options Options specifying how the move should be done
     */
    public void moveFiles(final Path source, final Path destination, final CopyOption... options) throws IOException {
        copyFiles(source, destination, options);
        deleteDirectoryStructure(destination);
        logger.debug("Move from {} to {} was successful", source.toString(), destination.toString());
    }

    /**
     * Get a list of sub directories (one level deep) of a base directory
     *
     * @param   baseDirectory Directory from which to get the list of sub directories
     * @return  String array containing the directories that were found
     */
    public String[] getSubDirectories(Path baseDirectory) {
        File dir = baseDirectory.toFile();
        String[] directories = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        logger.debug("Method getSubDirectories found {} sub directories", Integer.toString(directories.length));
        return directories;
    }
}
