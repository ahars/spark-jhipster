package com.mycompany.myapp.config.measures;

import com.codahale.metrics.Histogram;

public class HistogramMeasure extends SnapshotMeasure {

    private Long count;

    public HistogramMeasure(String name, String timestamp, Histogram histogram) {
        super(name, "histogram", timestamp, histogram.getSnapshot());
        this.count = histogram.getCount();
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "HistogramMeasure{" +
            "count=" + count +
            '}';
    }
}
