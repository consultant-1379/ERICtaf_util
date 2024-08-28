package com.ericsson.cifwk.taf.manual.data;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ericsson.cifwk.taf.data.DataHandler;

public class ReportConsumerTest {

	private ReportConsumer rc = new ReportConsumer();
	
	@Test
	public void shouldUseDefaultLocation() {
		assertEquals(new ReportConsumer().getLocation() , ReportConsumer.DEFAULT_LOCATION);
	}

	@Test
	public void shouldUseSetLocation(){
		String location = new File(".").getAbsolutePath();
		DataHandler.setAttribute(ReportConsumer.LOCATION_ARG, location);
		assertEquals(rc.getLocation(), location);
	}
	
	@Test
	public void shouldLoadDataFromSetLocation() throws IOException {
		String location = "src/test/resources";
		DataHandler.setAttribute(ReportConsumer.LOCATION_ARG, location);
		List<Map<String,Object>> data = rc.getTestReportData();
		assertEquals(4, data.size());
	}

}
