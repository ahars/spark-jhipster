package com.mycompany.myapp.config;

import com.codahale.metrics.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private String sparkCluster = "localhost:9999";

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder prefixedWith(String prefix) {
            this.prefix = prefix;
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

        public Builder sparkCluster(String sparkCluster) {
            this.sparkCluster = sparkCluster;
            return this;
        }

        public SparkReporter build() {
            return new SparkReporter(registry,
                sparkCluster,
                clock,
                prefix,
                rateUnit,
                durationUnit,
                filter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SparkReporter.class);

    private final String sparkCluster;
    private final Clock clock;
    private final String prefix;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectWriter writer;

    private SparkReporter(MetricRegistry registry, String sparkCluster, Clock clock, String prefix,
                          TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter) {
        super(registry, "spark-reporter", filter, rateUnit, durationUnit);
        this.sparkCluster = sparkCluster;
        this.clock = clock;
        this.prefix = prefix;
        writer = mapper.writer();
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {

        // nothing to do if we don't have any metrics to report
        if (gauges.isEmpty() && counters.isEmpty() && histograms.isEmpty() && meters.isEmpty() && timers.isEmpty()) {
            LOGGER.info("Waiting for metrics...");
            return;
        }
        final Long timestamp = clock.getTime() / 1000;

        try {
            HttpURLConnection connection = openConnection();

            JsonGenerator json = new JsonFactory().createGenerator(connection.getOutputStream());
            json.writeStartObject();

            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                reportGauge(json, entry.getKey(), entry.getValue(), timestamp);
            }
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                reportCounter(json, entry.getKey(), entry.getValue(), timestamp);
            }
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                reportHistogram(json, entry.getKey(), entry.getValue(), timestamp);
            }
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                reportMetered(json, entry.getKey(), entry.getValue(), timestamp);
            }
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                reportTimer(json, entry.getKey(), entry.getValue(), timestamp);
            }
            json.writeEndObject();
            LOGGER.info("flush de : ", json.toString());
            json.flush();

            closeConnection(connection);
        } catch (IOException e) {
            LOGGER.error("Error report to Spark", e);
        }
    }

    private void reportTimer(JsonGenerator json, String name, Timer timer, Long timestamp) throws IOException {
        json.writeStartObject();
        json.writeStringField("name", name);
        json.writeStringField("type", "timer");
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
    }

    private void reportMetered(JsonGenerator json, String name, Metered meter, Long timestamp) throws IOException {
        json.writeStartObject();
        json.writeStringField("name", name);
        json.writeStringField("type", "meter");
        json.writeNumberField("timestamp", timestamp);
        json.writeNumberField("count", meter.getCount());
        json.writeNumberField("m1_rate", meter.getOneMinuteRate());
        json.writeNumberField("m5_rate", meter.getFiveMinuteRate());
        json.writeNumberField("m15_rate", meter.getFifteenMinuteRate());
        json.writeNumberField("mean_rate", meter.getMeanRate());
        json.writeEndObject();
    }

    private void reportHistogram(JsonGenerator json, String name, Histogram histogram, Long timestamp) throws IOException {
        json.writeStartObject();
        json.writeStringField("name", name);
        json.writeStringField("type", "histogram");
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
    }

    private void reportCounter(JsonGenerator json, String name, Counter counter, Long timestamp) throws IOException {
        json.writeStartObject();
        json.writeStringField("name", name);
        json.writeStringField("type", "counter");
        json.writeNumberField("timestamp", timestamp);
        json.writeNumberField("counter", counter.getCount());
        json.writeEndObject();
    }

    private void reportGauge(JsonGenerator json, String name, Gauge gauge, Long timestamp) throws IOException {
        json.writeStartObject();
        json.writeStringField("name", name);
        json.writeStringField("type", "gauge");
        json.writeNumberField("timestamp", timestamp);
        json.writeObjectField("value", gauge.getValue());
        json.writeEndObject();
    }

    private HttpURLConnection openConnection() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(sparkCluster).openConnection();
            connection.connect();
            return connection;

        } catch (MalformedURLException e) {
            LOGGER.error("Error not valid url {}: {}", sparkCluster, e);
        } catch (IOException e) {
            LOGGER.error("Error connecting to {}: {}", sparkCluster, e);
        }
        return null;
    }

    private void closeConnection(HttpURLConnection connection) throws IOException {
        connection.getOutputStream().close();
        connection.disconnect();
    }
}

