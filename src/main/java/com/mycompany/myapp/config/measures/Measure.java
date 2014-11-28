package com.mycompany.myapp.config.measures;

import org.joda.time.DateTime;

import java.io.Serializable;

public class Measure implements Serializable {

    private String name;
    private String metric;
    private String timestamp;

    public Measure(String name, String metric, String timestamp) {
        this.name = name;
        this.metric = metric;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Measure{" +
            "name='" + name + '\'' +
            ", metric='" + metric + '\'' +
            ", timestamp=" + timestamp +
            '}';
    }
}
