package com.ericsson.cifwk.taf.api.scanner;

import java.io.File;
import java.io.IOException;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 12.06.2016
 */
public class TafApiScannerLauncher {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException("Expects ${project.build.folder} as parameter");
        }

        String projectBuildFolder = args[0];

        AnnotatedApi annotatedApi = new TafApiScanner().scan();
        File targetFolder = new File(projectBuildFolder + "/generated-resources/");
        new AnnotatedApiSerializer().serialize(annotatedApi, targetFolder);
    }

}
