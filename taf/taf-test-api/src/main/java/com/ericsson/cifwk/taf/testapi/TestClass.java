package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Test class representation
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 22/09/2016
 */
@API(Internal)
public interface TestClass {

    /**
     * @return this test class name.  This is the name of the corresponding Java class.
     */
    String getName();

}
