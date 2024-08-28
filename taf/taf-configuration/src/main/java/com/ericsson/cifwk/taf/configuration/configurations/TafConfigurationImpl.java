package com.ericsson.cifwk.taf.configuration.configurations;

import com.ericsson.cifwk.taf.configuration.ConvertUtilsConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationBuilder;
import com.ericsson.cifwk.taf.configuration.TafConfigurationUtils;
import com.google.common.base.Optional;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.text.StrSubstitutor;

import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TafConfigurationImpl implements TafConfiguration {

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    protected CompositeConfiguration configuration;
    protected TafRuntimeConfiguration runtimeConfiguration;

    public TafConfigurationImpl() {
        this(new CompositeConfiguration());
    }

    public TafConfigurationImpl(CompositeConfiguration configuration) {
        this.configuration = configuration;
    }

    public void addConfiguration(Configuration configuration) {
        this.configuration.addConfiguration(configuration);
    }

    public void addConfiguration(TafRuntimeConfiguration runtimeConfiguration) {
        this.runtimeConfiguration = runtimeConfiguration;
        this.configuration.addConfiguration(runtimeConfiguration);
    }

    public CompositeConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public TafConfiguration getRuntimeConfiguration() {
        return runtimeConfiguration;
    }

    public StrSubstitutor getSubstitutor() {
        return configuration.getSubstitutor();
    }

    public Object getProperty(String key) {
        readLock.lock();
        try {
            return configuration.getProperty(key);
        } finally {
            readLock.unlock();
        }
    }

    public <T> T getProperty(String key, T defaultValue) {
        if (configuration.containsKey(key))
            return (T) configuration.getProperty(key);
        else
            return defaultValue;
    }

    public <T> T getProperty(String key, T defaultValue, Class<T> clazz) {
        if (configuration.containsKey(key)) {
            ConvertUtilsConfiguration.configure(Optional.<char[]>absent());
            return (T) ConvertUtils.convert(configuration.getProperty(key), clazz);
        }
        else
            return defaultValue;

    }

    public <T> T getProperty(String key, Class<T> clazz) {
        ConvertUtilsConfiguration.configure(Optional.<char[]>absent());
        return (T) ConvertUtils.convert(configuration.getProperty(key), clazz);
    }

    public void setProperty(String key, Object value) {
        writeLock.lock();
        runtimeConfiguration.setProperty(key, value);
        writeLock.unlock();
    }

    public void clearProperty(String key) {
        writeLock.lock();
        runtimeConfiguration.clearProperty(key);
        writeLock.unlock();
    }

    @Override
    public String getString(String key) {
        readLock.lock();
        try {
            return configuration.getString(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getString(String key, String defaultValue) {
        readLock.lock();
        try {
            return configuration.getString(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public double getDouble(String key) {
        readLock.lock();
        try {
            return configuration.getDouble(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        readLock.lock();
        try {
            return configuration.getDouble(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int getInt(String key) {
        readLock.lock();
        try {
            return configuration.getInt(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int getInt(String key, int defaultValue) {
        readLock.lock();
        try {
            return configuration.getInt(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean getBoolean(String key) {
        readLock.lock();
        try {
            return configuration.getBoolean(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        readLock.lock();
        try {
            return configuration.getBoolean(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        runtimeConfiguration.clear();
        writeLock.unlock();
    }

    @Override
    public boolean containsKey(String key) {
        readLock.lock();
        try {
            return configuration.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String[] getStringArray(String key) {
        readLock.lock();
        try {
            return configuration.getStringArray(key);
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public String getSource(String key) {
        readLock.lock();
        try {
            return TafConfigurationUtils.getSource(configuration, key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Properties getProperties() {
        readLock.lock();
        try {
            return TafConfigurationUtils.getProperties(configuration);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void reload() {
        writeLock.lock();
        configuration.clear();
        new TafConfigurationBuilder().build(this);
        writeLock.unlock();
    }

    //-------------------------------------

}
