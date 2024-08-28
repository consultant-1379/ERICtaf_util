package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Optional;

import java.io.Serializable;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Test group representation. Test group is a set of tests (for example, a suite in TestNG), or a set of other test groups.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 20/06/2016
 */
@API(Internal)
public interface TestGroup extends RuntimeAttributeHolder, Serializable {

    /**
     * @return group unique ID
     */
    String getId();

    /**
     * Retrieves a parameter from test group definition.
     * For example, if suite XML in TestNG contains a &lt;parameter&gt; tag, its value can be retrieved by this method call.
     *
     * @param parameterName
     * @return the value of the test group parameter if such parameter is defined for it, <code>null</code> otherwise.
     */
    String getDefinitionParameter(String parameterName);

    /**
     * If test group definition comes from a file, the file name is returned.
     * @return file name where test group is defined, <code>null</code> if group definition doesn't come from a file.
     */
    Optional<String> getDefinitionFileName();

    /**
     * Returns the results for this test group.
     * @return results for this test group.
     */
    List<TestGroupResult> getResults();

}
