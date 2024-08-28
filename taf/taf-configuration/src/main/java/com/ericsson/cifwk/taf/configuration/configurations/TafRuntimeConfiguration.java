package com.ericsson.cifwk.taf.configuration.configurations;

import com.ericsson.cifwk.taf.configuration.ConvertUtilsConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationUtils;
import com.google.common.base.Optional;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.configuration.BaseConfiguration;

import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TafRuntimeConfiguration extends BaseConfiguration implements TafConfiguration {

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public void setProperty(String key, Object value){
        try {
            writeLock.lock();
            super.setProperty(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public void clearProperty(String key){
        try {
            writeLock.lock();
            super.clearProperty(key);
        } finally {
            writeLock.unlock();
        }
    }

    public void clear(){
        try {
            writeLock.lock();
            super.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public TafConfiguration getRuntimeConfiguration() {
        return this;
    }

    @Override
    public <T> T getProperty(String key, T defaultValue) {
        if (this.containsKey(key))
            return (T) this.getProperty(key);
        else
            return defaultValue;
    }

    @Override
    public <T> T getProperty(String key, T defaultValue, Class<T> clazz) {
        if (this.containsKey(key)) {
            ConvertUtilsConfiguration.configure(Optional.<char[]>absent());
            return (T) ConvertUtils.convert(this.getProperty(key), clazz);
        }
        else
            return defaultValue;
    }

    @Override
    public <T> T getProperty(String key, Class<T> clazz) {
        ConvertUtilsConfiguration.configure(Optional.<char[]>absent());
        return (T) ConvertUtils.convert(this.getProperty(key), clazz);
    }

    @Override
    public String getSource(String key) {
        return TafConfigurationUtils.getSource(this, key);
    }

    @Override
    public Properties getProperties() {
        return TafConfigurationUtils.getProperties(this);
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException("RuntimeConfiguration can't support reload operation");
    }

}
