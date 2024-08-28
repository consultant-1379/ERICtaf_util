package com.ericsson.cifwk.taf.datasource;

class DataRecordModifierImpl implements DataRecordModifier {

    private final DataRecord dataRecord;

    public DataRecordModifierImpl(DataRecord dataRecord) {
        this.dataRecord = dataRecord;
    }

    public DataRecordModifier setField(String name, Object value) {
        ((DataRecordImpl) dataRecord).setFieldValue(name, value);
        return this;
    }

    @Override
    public DataRecordModifier setFields(DataRecord record) {
        ((DataRecordImpl) dataRecord).setFieldValues(record.getAllFields());
        return this;
    }

}
