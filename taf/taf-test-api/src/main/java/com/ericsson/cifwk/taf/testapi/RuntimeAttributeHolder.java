package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 08/12/2016
 */
@API(Internal)
public interface RuntimeAttributeHolder {

    /**
     * Retrieves a runtime attribute value, if such attribute was set.
     *
     * @param attributeName
     * @return the value of a runtime attribute, if such attribute was set, <code>null</code> otherwise.
     */
    <T> T getAttribute(String attributeName);

    /**
     * Sets a runtime attribute value.
     *
     * @param attributeName
     * @param attributeValue
     */
    <T> void setAttribute(String attributeName, T attributeValue);

    /**
     * Removes the runtime attribute <code>attributeName</code>.
     * @param attributeName
     */
    void removeAttribute(String attributeName);

}
