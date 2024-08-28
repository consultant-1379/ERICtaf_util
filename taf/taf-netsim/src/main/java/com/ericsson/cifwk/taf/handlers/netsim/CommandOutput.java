package com.ericsson.cifwk.taf.handlers.netsim;


import java.util.List;
import java.util.Map;

/**
 * NetSim command output representation
 */
public final class CommandOutput {

	private final String rawOutput;

	/**
	 * @param rawOutput	output from NetSim binary as it is (including line breaks, command line echo, etc.)
	 */
	public CommandOutput(String rawOutput) {
		this.rawOutput = rawOutput;
	}

	/**
	 * @return raw output as received from NetSim
	 */
	public String getRawOutput() {
		return rawOutput;
	}

	/**
	 * Checks whether operation finished successfully or not. Not implemented yet
	 * @return
	 */
	public boolean isSuccess() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the text output, split into rows
	 * @return	text output, split into rows
	 */
	public List<String> asList() {
		return ResultProcessor.parseLines(rawOutput);
	}

	/**
	 * Returns the text output, split into rows, which are mapped to the headings.
	 * @return	text output, split into rows, which are mapped to the headings. 
	 * <p>First column is assumed to be the headings, so it's not 
	 * returned here as part of the list. All following content of the row is treated as set of columns 
	 * (if text is separated by 2 or more whitespaces, it's treated as 2 separate columns).
	 * <p><b>Example</b>
	 * <p>Text output, returned by a NetSim command:
		<pre>
			>> .mycommand
			fs        /netsim/netsim_dbdir/simdir/netsim/netsimdir/LTED1180-V2x10-FT-FDD-LTE01/LTE01ERBS00001/fs
			tmpfs     off one   two three
		</pre>
		<p>
		<code>asRows()</code> will return a Map with 2 entries that have "fs" and "tmpfs" keys respectively; first entry contains 
		a List with 1 record, second entry contains a List with 2 records ("off one" and "two three"). 
	 */
	public Map<String, List<String>> asRows() {
		return ResultProcessor.parseRows(rawOutput);
	}

	/**
	 * Returns the text output, split into columns.
	 * @return the text output, split into columns. First row in output is assumed to be the headings, and it's not returned here. 
	 * <p>All following rows are returned as this List of Maps - every Map represents one row, where column data is mapped to column name.  
	 * Use it to get the contents of the table.
	 * <p><b>Example</b>
	 * <p>Text output, returned by a NetSim command:
		<pre>
		NE Name                  Type              Server         In Address       Default dest.
		LTE01ERBS00001           LTE ERBS D1180-V2 netsim         192.168.100.2    
		LTE01ERBS00002           LTE ERBS D1180-V2 netsim         192.168.100.3    
		LTE01ERBS00003           LTE ERBS D1180-V2 netsim         192.168.100.4    
		LTE01ERBS00004           LTE ERBS D1180-V2 netsim         192.168.100.5    
		LTE01ERBS00005           LTE ERBS D1180-V2 netsim         192.168.100.6    
		LTE01ERBS00006           LTE ERBS D1180-V2 netsim         192.168.100.7    
		LTE01ERBS00007           LTE ERBS D1180-V2 netsim         192.168.100.8    
		LTE01ERBS00008           LTE ERBS D1180-V2 netsim         192.168.100.9    
		LTE01ERBS00009           LTE ERBS D1180-V2 netsim         192.168.100.10   
		LTE01ERBS00010           LTE ERBS D1180-V2 netsim         192.168.100.11   
		</pre>
		<p>
		<code>asColumns()</code> will return a List with 10 rows (10 Maps), where each column value is mapped to the name of the column. 
		So first row would contain mappings <code>{NE Name=LTE01ERBS00001, Type=LTE ERBS D1180-V2, ....}</code>
	 */
	public List<Map<String,String>> asColumns() {
		return ResultProcessor.parseColumns(rawOutput);
	}

	/**
	 * Returns the text output, split into sections. 
	 * @return text output, split into sections. Section is a set of homogeneous tables under a caption. 
	 * Section heading is the caption (key in the result Map), and the appropriate table data is mapped to it in the same structure 
	 * that is returned by <code>asColumns()</code> for a single table.
	 * <p><b>Example</b>
	 * <p>Text output, returned by a NetSim command:
		<pre>
			'server_00016_LTE_ERBS_D125-V2@netsim' for LTE ERBS D125-V2
			=================================================================
			    NE                       Address          Simulation/Commands 
			    LTE02ERBS00081           192.168.100.96   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00097           192.168.100.112  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00128           192.168.100.143  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00158           192.168.100.173  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00020           192.168.100.35   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00104           192.168.100.119  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00040           192.168.100.55   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00062           192.168.100.77   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00155           192.168.100.170  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00133           192.168.100.148  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00067           192.168.100.82   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00064           192.168.100.79   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00149           192.168.100.164  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00055           192.168.100.70   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00027           192.168.100.42   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00154           192.168.100.169  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00049           192.168.100.64   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00122           192.168.100.137  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00031           192.168.100.46   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00041           192.168.100.56   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00007           192.168.100.22   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00140           192.168.100.155  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00004           192.168.100.19   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00003           192.168.100.18   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00002           192.168.100.17   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02
			    LTE02ERBS00001           192.168.100.16   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02

			'server_00015_LTE_ERBS_D1180-V2@netsim' for LTE ERBS D1180-V2
			=================================================================
			    NE                       Address          Simulation/Commands 
			    LTE01ERBS00004           192.168.100.5    /netsim/netsimdir/LTED1180-V2x10-FT-FDD-LTE01
			    LTE01ERBS00010           192.168.100.11   /netsim/netsimdir/LTED1180-V2x10-FT-FDD-LTE01
			    LTE01ERBS00003           192.168.100.4    /netsim/netsimdir/LTED1180-V2x10-FT-FDD-LTE01
			    LTE01ERBS00007           192.168.100.8    /netsim/netsimdir/LTED1180-V2x10-FT-FDD-LTE01
			    LTE01ERBS00008           192.168.100.9    /netsim/netsimdir/LTED1180-V2x10-FT-FDD-LTE01
			    LTE01ERBS00001           192.168.100.2    /netsim/netsimdir/LTED1180-V2x10-FT-FDD-LTE01
			END
		</pre>
		<p>
		<code>asSections()</code> would return a Map with 2 entries, where each entry key is a section heading 
		(<code>'server_00016_LTE_ERBS_D125-V2@netsim'</code> for <code>LTE ERBS D125-V2</code> and <code>'server_00015_LTE_ERBS_D1180-V2@netsim'</code> 
		for <code>LTE ERBS D1180-V2</code>), and entry value is a List of appropriate table data (26 and 6 records).
	 */
	public Map<String, List<Map<String, String>>> asSections() {
		return ResultProcessor.parseSections(rawOutput);
	}

	@Override
	public String toString() {
		return rawOutput;
	}
}
