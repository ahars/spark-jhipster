package com.mycompany.myapp.config;

import com.codahale.metrics.*;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.config.measures.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which publishes metric values to a Spark Receiver.
 *
 */
public class SparkReporter extends ScheduledReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparkReporter.class);

    private final SocketFactory socketFactory;
    private final InetSocketAddress address;
    private Socket socket;
    private ObjectMapper mapper;
    private PrintWriter writer;
    private String json;

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    private SparkReporter(MetricRegistry registry, InetSocketAddress address, TimeUnit rateUnit,
                          TimeUnit durationUnit, MetricFilter filter) {
        super(registry, "spark-reporter", filter, rateUnit, durationUnit);
        this.address = address;
        this.socketFactory = SocketFactory.getDefault();
        this.mapper = new ObjectMapper();

        try {
            this.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws IllegalStateException, IOException {
        if (this.socket != null) {
            throw new IllegalStateException("Already connected");
        } else {
            this.socket = this.socketFactory.createSocket(this.address.getAddress(), this.address.getPort());
            this.writer = new PrintWriter(this.socket.getOutputStream());
        }
    }

    public void connectionClose() throws IOException {
        if(this.socket != null) {
            this.socket.close();
        }
        this.socket = null;
        this.json = null;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        try {

            // nothing to do if we don't have any metrics to report
            if (gauges.isEmpty() && counters.isEmpty() && histograms.isEmpty() &&
                meters.isEmpty() && timers.isEmpty()) {
                return;
            }

            if (!gauges.isEmpty()) {
                for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                    reportGauge(entry.getKey(), entry.getValue());
                }
            }

            if (!counters.isEmpty()) {
                for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                    reportCounter(entry.getKey(), entry.getValue());
                }
            }

            if (!histograms.isEmpty()) {
                for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                    reportHistogram(entry.getKey(), entry.getValue());
                }
            }

            if (!meters.isEmpty()) {
                for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                    reportMetered(entry.getKey(), entry.getValue());
                }
            }

            if (!timers.isEmpty()) {
                for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                    reportTimer(entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException var18) {
            LOGGER.warn("Unable to report to Spark ", var18);
        }
    }

    private void reportGauge(String name, Gauge gauge) throws IOException {
        if (this.isANumber(gauge.getValue()) == true) {
            this.json = mapper.writeValueAsString(new GaugeMeasure(name, gauge));
            this.writer.println(json);
        }
    }

    private void reportCounter(String name, Counter counter) throws IOException {
        this.json  = mapper.writeValueAsString(new CounterMeasure(name, counter));
        this.writer.println(json);
    }

    private void reportHistogram(String name, Histogram histogram) throws IOException {
        this.json  = mapper.writeValueAsString(new HistogramMeasure(name, histogram));
        this.writer.println(json);
    }

    private void reportMetered(String name, Metered meter) throws IOException {
        this.json  = mapper.writeValueAsString(new MeterMeasure(name, meter));
        this.writer.println(json);
    }

    private void reportTimer(String name, Timer timer) throws IOException {
        this.json  = mapper.writeValueAsString(new TimerMeasure(name, timer));
        this.writer.println(json);
    }

    private Boolean isANumber(Object object) {
        if (object instanceof Float || object instanceof  Double ||
            object instanceof Integer || object instanceof Long) {
            return true;
        } else {
            return false;
        }
    }

    public static class Builder {

        private final MetricRegistry registry;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
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

        public SparkReporter build(InetSocketAddress address) throws IOException {
            return new SparkReporter(registry, address, rateUnit, durationUnit, filter);
        }
    }
}
