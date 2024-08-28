package com.ericsson.cifwk.taf.configuration.builder;

import com.ericsson.cifwk.taf.configuration.processor.ConfigurationProcessor;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.AbstractScanner;
import org.reflections.scanners.Scanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class BuildFromResource {

    static final Logger logger = LoggerFactory.getLogger(BuildFromResource.class);
    public static final String MSG_ERROR_CANT_CREATE_URL = "Can't create URL for path:%s,  throws %s ";

    private List<URL> urls = new ArrayList<>();
    private Pattern pattern;
    ConfigurationProcessor<Vfs.File> function;

    public BuildFromResource applyEach(final ConfigurationProcessor<Vfs.File> function) {
        this.function = function;
        return this;
    }

    public BuildFromResource forPackage(String packageName) {
        urls = new ArrayList<>(ClasspathHelper.forPackage(packageName));
        return this;
    }

    public BuildFromResource forPath(Path path) {
        try {
            URL url = path.toUri().toURL();
            urls = Arrays.asList(url);
        } catch (Exception e) {
            String msg = String.format(MSG_ERROR_CANT_CREATE_URL, path, e.getMessage());
            logger.warn(msg);
            logger.trace(e.getMessage(),e);
        }
        return this;
    }

    public BuildFromResource forURI(URI uri) {
        try {
            URL url = uri.toURL();
            urls = Arrays.asList(url);
        } catch (Exception e) {
            String msg = String.format(MSG_ERROR_CANT_CREATE_URL, uri, e.getMessage());
            logger.warn(msg);
            logger.trace(e.getMessage(),e);
        }
        return this;
    }

    public BuildFromResource forMatching(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }


    public Configuration build(final CompositeConfiguration configuration) {
        Scanner scanner = new AbstractScanner() {
            @Override
            public boolean acceptsInput(String file) {
                return pattern == null || pattern.matcher(file).matches();
            }

            @Override
            public void scan(Vfs.File file) {
                Configuration c = function.apply(file);
                if (c != null) configuration.addConfiguration(c);
            }

            @Override
            public void scan(Object cls) {
            }
        };
        this.scan(scanner);
        return configuration;
    }

    private void scan(Scanner scanner) {
        new Reflections(
                new org.reflections.util.ConfigurationBuilder()
                        .setUrls(urls)
                        .setScanners(scanner)
        );
    }

}
