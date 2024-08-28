package scripts

import com.ericsson.cifwk.taf.osgi.agent.ApiOperator

class AddOperator implements ApiOperator {

    public String add(String x, String y) {
        return x.toInteger() + y.toInteger()
    }

}
