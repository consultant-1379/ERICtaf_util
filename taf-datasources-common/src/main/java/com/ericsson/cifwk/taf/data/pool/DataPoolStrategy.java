package com.ericsson.cifwk.taf.data.pool;

import static com.ericsson.cifwk.meta.API.Quality.*;

import com.ericsson.cifwk.meta.API;

/**
 * Strategy on managing Data Pool data when all data is processed
 *
 */
@API(Stable)
public enum DataPoolStrategy {
	/**
	 * Reset iterator to point to first data when last one is reached
	 */
	RESET_ON_END, 
	/**
	 * Finish processing data when all values are delivered
	 */
	STOP_ON_END, 
	/**
	 * Reset iterator to point to first element when end is reached
	 */
	REPEAT_UNTIL_STOPPED
}
