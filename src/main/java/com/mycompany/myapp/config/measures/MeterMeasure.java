package com.mycompany.myapp.config.measures;

import com.codahale.metrics.Metered;
import org.joda.time.DateTime;

public class MeterMeasure extends Measure {

    private Long count;
    private Double m1Rate;
    private Double m5Rate;
    private Double m15Rate;
    private Double meanRate;

    public MeterMeasure(String name, DateTime timestamp, Metered meter) {
        super(name, "meter", timestamp);
        this.count = meter.getCount();
        this.m1Rate = meter.getOneMinuteRate();
        this.m5Rate = meter.getFiveMinuteRate();
        this.m15Rate = meter.getFifteenMinuteRate();
        this.meanRate = meter.getMeanRate();
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getM1Rate() {
        return m1Rate;
    }

    public void setM1Rate(Double m1Rate) {
        this.m1Rate = m1Rate;
    }

    public Double getM5Rate() {
        return m5Rate;
    }

    public void setM5Rate(Double m5Rate) {
        this.m5Rate = m5Rate;
    }

    public Double getM15Rate() {
        return m15Rate;
    }

    public void setM15Rate(Double m15Rate) {
        this.m15Rate = m15Rate;
    }

    public Double getMeanRate() {
        return meanRate;
    }

    public void setMeanRate(Double meanRate) {
        this.meanRate = meanRate;
    }

    @Override
    public String toString() {
        return "Meter{" +
            "count=" + count +
            ", m1Rate=" + m1Rate +
            ", m5Rate=" + m5Rate +
            ", m15Rate=" + m15Rate +
            ", meanRate=" + meanRate +
            '}';
    }
}
