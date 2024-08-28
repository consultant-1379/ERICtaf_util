package com.ericsson.cifwk.taf.configuration.processor;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.configuration.parser.ConfigurationParser;

abstract class FileConfigurationProcessor extends ConfigurationProcessor<Vfs.File> {

    static private final Logger logger = LoggerFactory.getLogger(FileConfigurationProcessor.class);

    public FileConfigurationProcessor(ConfigurationParser parser) {
        super(parser);
    }

    public Configuration apply(Vfs.File file) {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.setFileName(file.toString());
        InputStream is = null;
        try {
            is = file.openInputStream();
            if (parser != null) {
                Configuration c = parser.parse(is);
                if (c != null) config.append(c);
            }
        } catch (Exception e) {
            logger.warn("Can't load configuration from:" + file.toString() + ", throws " + e.getMessage());
            logger.trace(e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.debug("Can't close stream from: {}, throws {}", file.toString(), e.getMessage());
                    logger.trace(e.getMessage(), e);
                }
            }
        }
        return config;
    }
}
