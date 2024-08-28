/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.annotations;

import com.ericsson.cifwk.meta.API;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * This approach to performance testing was replaced by TAF scenarios.
 */
@API(API.Quality.Deprecated)
@API.Since(2.6)
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(value={METHOD})
public @interface VUsers {
	int[] vusers();
}
