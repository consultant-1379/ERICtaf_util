package com.ericsson.cifwk.taf

import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock

import java.util.concurrent.locks.ReentrantReadWriteLock

import org.apache.log4j.Logger

/**
 * This class it to synchronize work of suites 
 *
 */
class SuiteExecutionHelper {
	private static Logger log = Logger.getLogger(SuiteExecutionHelper)
	private static ReentrantReadWriteLock request = new ReentrantReadWriteLock()
	private static Queue currentyRunningBlock = [] as Queue
	/**
	 * This method is used in default @link {@link TorNgSuiteListener} on every suite 
	 */
	public static void requestParallelRun(){
		requestRun(true)
	}


	/**
	 * This method is to be used in BeforeSuite block if suite should be executed by itself
	 */
	public static void requestExclusiveRun(){
		log.debug "Exclusive run requested"
		removeSuite()
		requestRun(false)
	}

	/**
	 * This methods is mandatory to be used after the exclusive run was requested; best to put in BeforeSuite section 
	 */
	public static void releaseExclusiveRun(){
		log.debug "Exclusive run for" + getSuiteName() + " finished"
		request.writeLock().unlock()
	}


	@WithWriteLock
	private static void removeSuite(){
		if (currentyRunningBlock.contains(getSuiteName())){
			currentyRunningBlock.remove(getSuiteName())
		}
	}
	@WithWriteLock
	private static void addSuite(){
		if (! currentyRunningBlock.contains(getSuiteName()))
			currentyRunningBlock << getSuiteName()

	}

	@WithReadLock
	private static String peekNextSuite(){
		currentyRunningBlock.peek()
	}
	/**
	 * Common method to request a run; if run is exclusive, other suites will be added to lock queue
	 * @param parallel
	 */
	public static void requestRun(boolean parallel){
		log.debug "Requesting run for " + getSuiteName() + " with parallel: $parallel"
		request.writeLock().lock()
		addSuite()
		if (parallel){
			log.debug "Parallel run for " + getSuiteName() + " in progress"
			request.writeLock().unlock()
		}else{
			waitForFinish()
			log.debug "Exclusive run for" + getSuiteName() + " in progress"
		}
	}

	/**
	 * Method to block thread until all test cases from currently running suites finishes the execution
	 */
	private static void waitForFinish(){
		synchronized (currentyRunningBlock) {
			while (getSuiteName() != peekNextSuite()){
				log.debug "Waiting for other suites to finish: $currentyRunningBlock"
				currentyRunningBlock.wait()
			}
		}
	}

	/**
	 * Finish execution of a suite. Method is called from @link {@link TorNgSuiteListener}
	 */
	public static void finishExecution(){
		log.debug "Finished execution of $suiteName"
		synchronized (currentyRunningBlock) {
			removeSuite()
			currentyRunningBlock.notifyAll()
		}
	}

	/**
	 * Method to get suite name as name of current thread
	 * @return
	 */
	private static String getSuiteName(){
		return Thread.currentThread().getName()
	}

}
