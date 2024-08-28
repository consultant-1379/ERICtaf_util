package com.ericsson.cifwk.taf.assertions;

import com.ericsson.cifwk.meta.API;
import org.testng.asserts.Assertion;
import org.testng.asserts.IAssert;
import org.testng.collections.Maps;

import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
* When an assertion fails, don't throw an exception but record the failure.
* Calling {@code assertAll()} will cause an exception to be thrown if at
* least one assertion failed.
*/
@API(Internal)
public class ReusableSoftAssert extends Assertion {

    // LinkedHashMap to preserve the order
    private Map<AssertionError, IAssert> errors = Maps.newLinkedHashMap();

    @Override
    public void executeAssert(IAssert a) {
        try {
            a.doAssert();
        } catch(AssertionError ex) {
            onAssertFailure(a, ex);
            errors.put(ex, a);
        }
    }

    public void assertAll() {
        if (! errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("The following asserts failed:\n");
            boolean first = true;
            for (Map.Entry<AssertionError, IAssert> ae : errors.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(ae.getValue().getMessage());
            }
            errors.clear();
            throw new AssertionError(sb.toString());
        }
    }
}
