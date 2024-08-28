package com.ericsson.cifwk.taf.configuration.processor;

import com.ericsson.cifwk.taf.configuration.parser.ConfigurationParser;
import org.apache.commons.configuration.Configuration;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.ZipFile;

class ArchiveResourceConfigurationProcessor extends FileConfigurationProcessor {

    public ArchiveResourceConfigurationProcessor(ConfigurationParser parser) {
        super(parser);
    }

    @Override
    public Configuration apply(Vfs.File file) {
        if (!(file instanceof ZipFile)) return null;
        return super.apply(file);
    }

}
