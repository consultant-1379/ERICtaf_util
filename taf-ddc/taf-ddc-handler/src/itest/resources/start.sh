#!/bin/bash
ln -s /usr/java/jdk1.7.0_17/lib/tools.jar lib/tools.jar
java -cp taf-ddc-handler.jar:lib/* -Dcom.ericsson.cifwk.diagmon.util.common.torvernum=7 com.ericsson.cifwk.diagmon.util.instr.Instr -metrics sut.xml -defaultPollInter  5 $@
