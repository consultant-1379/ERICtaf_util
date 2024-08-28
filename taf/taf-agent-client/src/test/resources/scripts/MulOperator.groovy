package scripts

import com.ericsson.cifwk.taf.osgi.agent.ApiOperator

class MulOperator implements ApiOperator {

    public String mul(String x, String y) {
        return x.toInteger() * y.toInteger()
    }

}
