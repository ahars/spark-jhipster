package com.mycompany.myapp.config;

import com.codahale.metrics.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which publishes metric values to a Spark Receiver.
 *
 */
public class SparkReporter extends ScheduledReporter {

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static class Builder {
        private final MetricRegistry registry;
        private Clock clock;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private String host;
        private Integer port;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder send(String socket) {
            this.host = socket.split(":")[0];
            this.port = Integer.parseInt(socket.split(":")[1]);
            return this;
        }

        public SparkReporter build() throws IOException {
            return new SparkReporter(registry,
                host,
                port,
                clock,
                rateUnit,
                durationUnit,
                filter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SparkReporter.class);

    private final String host;
    private final Integer port;
    private final Clock clock;
    private Socket socket = null;

    private SparkReporter(MetricRegistry registry, String host, Integer port, Clock clock,
                          TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter) {
        super(registry, "spark-reporter", filter, rateUnit, durationUnit);
        this.host = host;
        this.port = port;
        this.clock = clock;

        try {
            socket = new Socket(host, port);
            LOGGER.info("Socket connection ", socket.toString());
        } catch (IOException e) {
            LOGGER.error("Failed connection socket ", e);
        }
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {

        // nothing to do if we don't have any metrics to report
        if (gauges.isEmpty() && counters.isEmpty() && histograms.isEmpty() && meters.isEmpty() && timers.isEmpty()) {
            LOGGER.info("Waiting for metrics..");
            return;
        }

        final Long timestamp = clock.getTime() / 1000;

        try {
            if (!gauges.isEmpty()) {
                for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                    reportGauge(entry.getKey(), entry.getValue(), timestamp);
                    LOGGER.info("Writing gauge.");
                }
            }
            if (!counters.isEmpty()) {
                for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                    reportCounter(entry.getKey(), entry.getValue(), timestamp);
                    LOGGER.info("Writing counter.");
                }
            }
            if (!histograms.isEmpty()) {
                for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                    reportHistogram(entry.getKey(), entry.getValue(), timestamp);
                    LOGGER.info("Writing histogram.");
                }
            }
            if (!meters.isEmpty()) {
                for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                    reportMetered(entry.getKey(), entry.getValue(), timestamp);
                    LOGGER.info("Writing meter.");
                }
            }
            if (!timers.isEmpty()) {
                for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                    reportTimer(entry.getKey(), entry.getValue(), timestamp);
                    LOGGER.info("Writing timer.");
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to report metrics ", e);
        }
    }

    private void reportTimer(String name, Timer timer, Long timestamp) throws IOException {

        JsonGenerator json = new JsonFactory().createGenerator(socket.getOutputStream());
        json.writeStartObject();
        json.writeStringField("metric", "timer");
        json.writeStringField("name", name);
        json.writeNumberField("timestamp", timestamp);

        final Snapshot snapshot = timer.getSnapshot();
        json.writeNumberField("count", timer.getCount());
        json.writeNumberField("m1_rate", timer.getOneMinuteRate());
        json.writeNumberField("m5_rate", timer.getFiveMinuteRate());
        json.writeNumberField("m15_rate", timer.getFifteenMinuteRate());
        json.writeNumberField("mean_rate", timer.getMeanRate());
        json.writeNumberField("max", snapshot.getMax());
        json.writeNumberField("mean", snapshot.getMean());
        json.writeNumberField("min", snapshot.getMin());
        json.writeNumberField("p50", snapshot.getMedian());
        json.writeNumberField("p75", snapshot.get75thPercentile());
        json.writeNumberField("p95", snapshot.get95thPercentile());
        json.writeNumberField("p98", snapshot.get98thPercentile());
        json.writeNumberField("p99", snapshot.get99thPercentile());
        json.writeNumberField("p999", snapshot.get999thPercentile());
        json.writeNumberField("stddev", snapshot.getStdDev());
        json.writeEndObject();
        json.flush();
    }

    private void reportMetered(String name, Metered meter, Long timestamp) throws IOException {

        JsonGenerator json = new JsonFactory().createGenerator(socket.getOutputStream());
        json.writeStartObject();
        json.writeStringField("metric", "meter");
        json.writeStringField("name", name);
        json.writeNumberField("timestamp", timestamp);
        json.writeNumberField("count", meter.getCount());
        json.writeNumberField("m1_rate", meter.getOneMinuteRate());
        json.writeNumberField("m5_rate", meter.getFiveMinuteRate());
        json.writeNumberField("m15_rate", meter.getFifteenMinuteRate());
        json.writeNumberField("mean_rate", meter.getMeanRate());
        json.writeEndObject();
        json.flush();
    }

    private void reportHistogram(String name, Histogram histogram, Long timestamp) throws IOException {

        JsonGenerator json = new JsonFactory().createGenerator(socket.getOutputStream());
        json.writeStartObject();
        json.writeStringField("metric", "histogram");
        json.writeStringField("name", name);
        json.writeNumberField("timestamp", timestamp);

        final Snapshot snapshot = histogram.getSnapshot();
        json.writeNumberField("count", histogram.getCount());
        json.writeNumberField("max", snapshot.getMax());
        json.writeNumberField("mean", snapshot.getMean());
        json.writeNumberField("min", snapshot.getMin());
        json.writeNumberField("p50", snapshot.getMedian());
        json.writeNumberField("p75", snapshot.get75thPercentile());
        json.writeNumberField("p95", snapshot.get95thPercentile());
        json.writeNumberField("p98", snapshot.get98thPercentile());
        json.writeNumberField("p99", snapshot.get99thPercentile());
        json.writeNumberField("p999", snapshot.get999thPercentile());
        json.writeNumberField("stddev", snapshot.getStdDev());
        json.writeEndObject();
        json.flush();
    }

    private void reportCounter(String name, Counter counter, Long timestamp) throws IOException {

        JsonGenerator json = new JsonFactory().createGenerator(socket.getOutputStream());
        json.writeStartObject();
        json.writeStringField("metric", "counter");
        json.writeStringField("name", name);
        json.writeNumberField("timestamp", timestamp);
        json.writeObjectField("counter", counter.getCount());
        json.writeEndObject();
        json.flush();
    }

    private void reportGauge(String name, Gauge gauge, Long timestamp) throws IOException {

        JsonGenerator json = new JsonFactory().createGenerator(socket.getOutputStream());
        json.writeStartObject();
        json.writeStringField("metric", "gauge");
        json.writeStringField("name", name);
        json.writeNumberField("timestamp", timestamp);
        json.writeObjectField("value", gauge.getValue());
        json.writeEndObject();
        json.flush();
    }
}
