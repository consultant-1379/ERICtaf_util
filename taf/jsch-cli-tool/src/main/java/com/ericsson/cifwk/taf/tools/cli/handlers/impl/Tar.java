package com.ericsson.cifwk.taf.tools.cli.handlers.impl;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.kamranzafar.jtar.TarOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ekonsla on 03/02/2016.
 */
public class Tar {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tar.class);

    private static final int BUFFER = 2048;
    private static final String FILE_SEPARATOR = "/";

    public static void tar(String srcFolder, String destTarFile) {
        try {
            FileOutputStream dest = new FileOutputStream(destTarFile);
            TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));
            tarFolder(null, srcFolder, out, destTarFile);
            out.close();
        } catch (IOException e) {
            LOGGER.error("Error adding to tar file", e);
        }
    }

    public static void untar(String tarFilePath, String destinationDir) {
        try {
            File destFolder = new File(destinationDir);
            destFolder.mkdirs();
            File zf = new File(tarFilePath);
            TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(zf)));
            untar(tis, destFolder.getAbsolutePath());
            tis.close();
        } catch (IOException e) {
            LOGGER.error("Error extracting from tar file", e);
        }
    }

    private static void tarFolder(String parent, String path, TarOutputStream out, String tarFileName) throws IOException {
        BufferedInputStream origin = null;
        File f = new File(path);
        String files[] = f.list();

        if (files == null) {
            files = new String[1];
            files[0] = f.getName();
        }

        parent = ((parent == null) ? (f.isFile()) ? "" : f.getName() + FILE_SEPARATOR : parent + f.getName() + FILE_SEPARATOR);

        for (String file : files) {
            LOGGER.debug(String.format("Adding: %s to %s", file.toString(), tarFileName));
            File fe = f;
            byte data[] = new byte[BUFFER];

            if (f.isDirectory()) {
                fe = new File(f, file);
            }

            if (fe.isDirectory()) {
                String[] fl = fe.list();
                if (fl != null && fl.length != 0) {
                    tarFolder(parent, fe.getPath(), out, tarFileName);
                } else {
                    TarEntry entry = new TarEntry(fe, parent + file + FILE_SEPARATOR);
                    out.putNextEntry(entry);
                }
                continue;
            }
            FileInputStream fi = new FileInputStream(fe);
            origin = new BufferedInputStream(fi);
            TarEntry entry = new TarEntry(fe, parent + file);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data)) != -1) {
                out.write(data, 0, count);
            }
            out.flush();
            origin.close();
        }
    }

    private static void untar(TarInputStream tis, String destFolder) throws IOException {
        BufferedOutputStream dest = null;
        TarEntry entry;
        while ((entry = tis.getNextEntry()) != null) {
            LOGGER.debug(String.format("Extracting: %s to %s", entry.getName(), destFolder));
            int count;
            byte data[] = new byte[BUFFER];
            if (entry.isDirectory()) {
                new File(destFolder + FILE_SEPARATOR + entry.getName()).mkdirs();
                continue;
            } else {
                int di = entry.getName().lastIndexOf(FILE_SEPARATOR);
                if (di != -1) {
                    new File(destFolder + FILE_SEPARATOR + entry.getName().substring(0, di)).mkdirs();
                }
            }
            FileOutputStream fos = new FileOutputStream(destFolder + FILE_SEPARATOR + entry.getName());
            dest = new BufferedOutputStream(fos);
            while ((count = tis.read(data)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
        }
    }
}
