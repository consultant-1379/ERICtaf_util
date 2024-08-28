package com.ericsson.cifwk.diagmon.util.instr.outputhandlers.taf;

import com.ericsson.cifwk.diagmon.util.instr.OutputHandler;
import com.ericsson.cifwk.diagmon.util.instr.config.ConfigTreeNode;
import com.ericsson.cifwk.taf.ddc.DDCHandler;
import com.ericsson.cifwk.taf.ddc.GraphiteDDCHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * DDC OutputHandlerImpl
 * <p/>-- [Warning!!!] -- <br/>
 * Class name must be OutputHandlerImpl<br/>
 * package must be "com.ericsson.cifwk.diagmon.util.instr.outputhandlers.&lt;type&gt;<br/>"
 * &lt;type&gt - type attribute in &lt;outputHandler&gt; xmlnode from xml file, passed as parameter -metric in command<br/>
 *
 * @see OutputHandler#get
 */
public class OutputHandlerImpl extends OutputHandler {

    static {
        // m_Log = org.apache.log4j.Logger.getLogger(OutputHandlerImpl.class);;  // uncomment if need log in OutputHandler
    }

    List<DDCHandler> handlers = new ArrayList<>();

    {
        handlers.add(new GraphiteDDCHandler());
    }

    /**
     * @param config &lt;outputHandler&gt; xmlnode from xml file, passed as parameter -metric in command line
     */
    public OutputHandlerImpl(ConfigTreeNode config) {
        init(config);
    }


    public void init(ConfigTreeNode node) {
        Config config = new Config(node);
        for (DDCHandler handler : handlers) {
            handler.init(config);
        }
    }

    @Override
    protected void recordValues(ConfigTreeNode node) {
        for (DDCHandler handler : handlers) {
            handler.recordValues(node);
        }
    }

    @Override
    public void shutdown() {
        for (DDCHandler handler : handlers) {
            handler.shutdown();
        }
    }

}

