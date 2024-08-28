package com.ericsson.cifwk.taf.performance.threshold;

import java.util.Arrays;

/**
 *
 */
public final class MetricSlice {

    String targetName;
    long startTimestamp;
    long endTimestamp;
    int seriesStep;
    Double[] data;

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public void setSeriesStep(int seriesStep) {
        this.seriesStep = seriesStep;
    }

    public void setData(Double[] data) {
        this.data = data;
    }

    public String getTargetName() {
        return targetName;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public int getSeriesStep() {
        return seriesStep;
    }

    public Double[] getData() {
        return data;
    }

    public int getSize() {
        return data.length;
    }

    @Override
    public String toString() {
        return "MetricSlice{" +
                "targetName='" + targetName + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", endTimestamp=" + endTimestamp +
                ", seriesStep=" + seriesStep +
                ", data=" + Arrays.toString(data) +
                '}';
    }

}
