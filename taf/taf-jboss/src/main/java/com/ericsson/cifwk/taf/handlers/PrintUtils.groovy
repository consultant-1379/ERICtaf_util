package com.ericsson.cifwk.taf.handlers

import groovy.transform.PackageScope

@PackageScope
class PrintUtils {

	static String runAndCaptureOutput(Closure runner){
		PrintStream out = System.out
		ByteArrayOutputStream tempOut = new ByteArrayOutputStream()
		System.setOut(new PrintStream(tempOut))
		String result = ""
		try {
			runner()
		} finally {
			System.setOut(out)
		}
		List allLines = tempOut.toString().readLines()
		return result +  (allLines.last()=="")? allLines[0..-1].join("\n") : allLines.join("\n")
	}
}
