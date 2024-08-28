package com.ericsson.cifwk.taf.method;

import com.ericsson.cifwk.taf.datasource.DataRecord;

interface MyDataRecord extends DataRecord{

    int getX();
    int getY();
    int getZ();
}
