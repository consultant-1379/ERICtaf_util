package com.ericsson.cifwk.taf.configuration;

import java.util.Properties;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 11/03/2016
 */
public interface Configuration {

    /**
     * Get a list of properties
     *
     * @return The associated properties.
     */
    Properties getProperties();

    /**
     * Finding the requested configuration property.
     *
     * @param key requested property
     * @return a object representation of the configuration property requested
     */
    Object getProperty(String key);

    /**
     * Gets a property from the configuration. This is the most basic get
     * method for retrieving values of properties.
     *
     * @param key property to retrieve
     * @return the value to which this configuration maps the specified key, or
     * null if the configuration contains no mapping for this key.
     */
    <T> T getProperty(String key, T defaultValue);

    /**
     * Finding the requested configuration property.
     *
     * @param key   property to retrieve
     * @param clazz Class of the value to be converted to (must not be null)
     * @return a object representation of the configuration property requested
     */
    <T> T getProperty(String key, Class<T> clazz);

    /**
     * Finding the requested configuration property.
     *
     * @param key          property to retrieve
     * @param defaultValue default value
     * @param clazz        Class of the value to be converted to (must not be null)
     * @return a object representation of the configuration property requested
     */
    <T> T getProperty(String key, T defaultValue, Class<T> clazz);

    /**
     * Set a property in runtime configuration, this will replace any previously set values.
     *
     * @param key   The key of the property to change
     * @param value The new value
     */
    void setProperty(String key, Object value);

    /**
     * Remove a property from the runtime configuration.
     *
     * @param key the key to remove along with corresponding value.
     */
    void clearProperty(String key);

    /**
     * Remove all properties from the runtime configuration.
     */
    void clear();

    /**
     * Get a string associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated string.
     */
    String getString(String key);

    /**
     * Get a string associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated string if key is found and has valid
     * format, default value otherwise.
     */
    String getString(String key, String defaultValue);

    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated double.
     */
    double getDouble(String key);

    /**
     * Get a double associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated double if key is found and has valid
     * format, default value otherwise.
     */
    double getDouble(String key, double defaultValue);

    /**
     * Get an int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated int.
     */
    int getInt(String key);

    /**
     * Get an int associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated int if key is found and has valid
     * format, default value otherwise.
     */
    int getInt(String key, int defaultValue);

    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated boolean.
     */
    boolean getBoolean(String key);

    /**
     * Get a boolean associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean if key is found and has valid
     * format, default value otherwise.
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Get an array of strings associated with the given configuration key.
     * If the key doesn't map to an existing object an empty array is returned
     *
     * @param key The configuration key.
     * @return The associated string array if key is found.
     */
    String[] getStringArray(String key);

    /**
     * Check if the configuration contains the specified key.
     *
     * @param key the key whose presence in this configuration is to be tested
     * @return {@code true} if the configuration contains a value for this
     * key, {@code false} otherwise
     */
    boolean containsKey(String key);


    /**
     * Return description of configuration containing this key.
     * For keys coming from file configuration source would be file path.
     * There is no specific format for return value.
     * This method is used for informative purposes
     *
     * @param key
     * @return
     */
    String getSource(String key);

    /**
     * Reload all configuration
     */
    void reload();
}
