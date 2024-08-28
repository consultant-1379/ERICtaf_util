package com.ericsson.cifwk.taf.handlers.netsim

import com.ericsson.cifwk.meta.API
import com.ericsson.cifwk.taf.handlers.netsim.implementation.SshNetsimHandler
import groovy.transform.InheritConstructors
/**
 * Class to communicate with and control a Netsim installation
 */
@API(API.Quality.Deprecated)
@API.Since(2.17d)
@Deprecated
@InheritConstructors
class NetsimHandler extends  SshNetsimHandler {

}
