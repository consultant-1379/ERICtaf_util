package com.ericsson.cifwk.taf.handlers.netsim;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class NetSimCommandHandlerConcurrencyTest {
	private static final Logger log = LoggerFactory.getLogger(NetSimContextRegistry.class);

	@Test
	public void testContextsHaveToBeThreadSpecific() {
		NetSimContextRegistry registry = new NetSimContextRegistry();
		registry = spy(registry);

		final Host host1 = mock(Host.class);
		final Host host2 = mock(Host.class);

		final List<NetSimCommand> slowCmds = Lists.newArrayList(NetSimCommands.start(), NetSimCommands.showFs());
		final List<NetSimCommand> fastCmds = Lists.<NetSimCommand>newArrayList(NetSimCommands.open("blahBlah"));
		
		SshNetSimContext context11 = createContextStub(host1, slowCmds, fastCmds);
		SshNetSimContext context12 = createContextStub(host2, slowCmds, fastCmds);
		
		doReturn(context11).when(registry).createContextForHost(host1);
		doReturn(context12).when(registry).createContextForHost(host2);

		NetSimCommandHandler.setContextRegistry(registry);
		final NetSimContext c1 = NetSimCommandHandler.getContext(host1);
		final NetSimContext c2 = NetSimCommandHandler.getContext(host2);
		
		Runnable slowRunner = new Runnable() {
			@Override
			public void run() {
				log.debug("Running slowCmds");
				c1.exec(slowCmds);
				log.debug("Done slowCmds");
				log.debug("Running fastCmds");
				c1.exec(fastCmds);
				log.debug("Done fastCmds");
				NetSimCommandHandler.closeAllContexts();
			}
		};

		Runnable fastRunner = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				log.debug("Running fastCmds");
				c2.exec(fastCmds);
				log.debug("Done fastCmds");
				NetSimCommandHandler.closeAllContexts();
			}
		};
		
		Thread thread1 = new Thread(slowRunner);
		Thread thread2 = new Thread(fastRunner);
		
		StringBuilder errorContainer = new StringBuilder();
		registerExceptionBuffer(errorContainer);
		
		startThreadsAndWaitUntilFinish(thread1, thread2);
		
		if (errorContainer.length() > 0) {
			fail("Execution failed: " +errorContainer.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	private SshNetSimContext createContextStub(final Host host1, final List<NetSimCommand> slowCmds, final List<NetSimCommand> fastCmds) {
		SshNetSimContext context = spy(new SshNetSimContext(host1));
		doReturn("").when(context).executeCommandSetAndGetOutput(anyString());
		doReturn(mock(NetSimResult.class)).when(context).processTextResult(anyString());
		doNothing().when(context).verifyResult(any(List.class), anyString(), any(NetSimResult.class));

		doAnswer(new DelayingAnswer(2000)).when(context).preparedCommandSet(slowCmds);
		doAnswer(new DelayingAnswer(500)).when(context).preparedCommandSet(fastCmds);
		
		return context;
	}

	private void startThreadsAndWaitUntilFinish(Thread... threads) {
		for (Thread thread : threads) {
			thread.setDaemon(true);
			thread.start();
		}
		try {
			for (Thread thread : threads) {
				thread.join();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

	private void registerExceptionBuffer(final StringBuilder errorContainer) {
		UncaughtExceptionHandler eh = new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.error("Thread " + t + " failed with exception", e);
				errorContainer.append(String.format("Thread %s failed with exception '%s'", t.toString(), e.getMessage()));
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(eh);
	}

	private class DelayingAnswer implements Answer<String> {
		private final int delay;
		
		DelayingAnswer(int delay) {
			this.delay = delay;
		}
		
		@Override
		public String answer(InvocationOnMock invocation) throws Throwable {
			Thread.sleep(delay);
			return UUID.randomUUID().toString();
		}
	};
}
