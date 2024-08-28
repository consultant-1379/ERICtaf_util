package com.ericsson.cifwk.taf.utils.ssh;

import com.ericsson.cifwk.meta.API;
import com.maverick.ssh.SshAuthentication;

import groovy.transform.PackageScope;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Enum to specify the SshAuthenticationState states
 */
@API(Internal)
@PackageScope
enum SshAuthenticationState {
	COMPLETE(SshAuthentication.COMPLETE,"COMPLETE"),
	FAILED(SshAuthentication.FAILED,"FAILED"),
	PARTIAL(SshAuthentication.FURTHER_AUTHENTICATION_REQUIRED,"FURTHER_AUTHENTICATION_REQUIRED"),
	READY(SshAuthentication.PUBLIC_KEY_ACCEPTABLE,"PUBLIC_KEY_ACCEPTABLE"),
	CANCELLED(SshAuthentication.CANCELLED,"CANCELLED");

	private int authenticationProtocolState;
	private String stateName;

	SshAuthenticationState(int authenticationProtocolState,String stateName){
		this.authenticationProtocolState = authenticationProtocolState;
		this.stateName = stateName;
	}

	public int toInteger(){
		return authenticationProtocolState;
	}
	@Override
	public String toString(){
		return stateName;
	}
}
