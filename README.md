jhipster-sandbox
==========================

## Jhipster Sandbox

This is a sample Jhipster app implementing an Apache Spark Custom Reporter.
 
[Jhipster] (http://jhipster.github.io/) is a Yeoman Generator used to create a Spring + AngularJS project.


## Test

Test with the project [Metrics Spark] (https://github.com/ahars/metrics-spark) to report to a Spark Streaming test.

Run the Jhipster app with the Maven command :

```
$ mvn spring-boot:run
```

In order to test this reporting, you can :
* Run the Spark Streaming Test reporting into the Spark console.
* Run the Spark Streaming Test reporting into an ElasticSearch cluster.
* Run the Spark Streaming Test reporting into a Cassandra database.
