package com.mycompany.myapp.config.measures;

import com.codahale.metrics.Snapshot;
import org.joda.time.DateTime;

public class SnapshotMeasure extends Measure {

    private Long max;
    private Long min;
    private Double mean;
    private Double median;
    private Double p75;
    private Double p95;
    private Double p98;
    private Double p99;
    private Double p999;
    private Double stdDev;

    public SnapshotMeasure(String name, String type, DateTime timestamp, Snapshot snapshot) {
        super(name, type, timestamp);
        this.max = snapshot.getMax();
        this.min = snapshot.getMin();
        this.mean = snapshot.getMean();
        this.median = snapshot.getMedian();
        this.p75 = snapshot.get75thPercentile();
        this.p95 = snapshot.get95thPercentile();
        this.p98 = snapshot.get98thPercentile();
        this.p99 = snapshot.get99thPercentile();
        this.p999 = snapshot.get999thPercentile();
        this.stdDev = snapshot.getStdDev();
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public Long getMin() {
        return min;
    }

    public void setMin(Long min) {
        this.min = min;
    }

    public Double getMean() {
        return mean;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public Double getMedian() {
        return median;
    }

    public void setMedian(Double median) {
        this.median = median;
    }

    public Double getP75() {
        return p75;
    }

    public void setP75(Double p75) {
        this.p75 = p75;
    }

    public Double getP95() {
        return p95;
    }

    public void setP95(Double p95) {
        this.p95 = p95;
    }

    public Double getP98() {
        return p98;
    }

    public void setP98(Double p98) {
        this.p98 = p98;
    }

    public Double getP99() {
        return p99;
    }

    public void setP99(Double p99) {
        this.p99 = p99;
    }

    public Double getP999() {
        return p999;
    }

    public void setP999(Double p999) {
        this.p999 = p999;
    }

    public Double getStdDev() {
        return stdDev;
    }

    public void setStdDev(Double stdDev) {
        this.stdDev = stdDev;
    }

    @Override
    public String toString() {
        return "SnapshotMeasure{" +
            "max=" + max +
            ", min=" + min +
            ", mean=" + mean +
            ", median=" + median +
            ", p75=" + p75 +
            ", p95=" + p95 +
            ", p98=" + p98 +
            ", p99=" + p99 +
            ", p999=" + p999 +
            ", stdDev=" + stdDev +
            '}';
    }
}
