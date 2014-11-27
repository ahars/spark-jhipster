package com.mycompany.myapp.config.measures;

import java.io.Serializable;

public class Measure implements Serializable {

    private String name;
    private String type;
    private Long timestamp;

    public Measure(String name, String type, Long timestamp) {
        this.name = name;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Measure{" +
            "name='" + name + '\'' +
            ", type='" + type + '\'' +
            ", timestamp=" + timestamp +
            '}';
    }
}
