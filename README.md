# Prometheus test for remote-write/read

## Pre-requisite

- `docker`
- `docker-compose`
- Build sample app : `./sample-app/gradlew -p sample-app/ --no-daemon --no-scan --no-watch-fs jibDockerBuild`

## Basic configuration
Setup :
 - Prometheus Origin (9090) with no datas
 - Prometheus Target (9091) with no data
 - Sample application

Configuration :
 - Origin : [origin/config/prometheus.yml](origin/config/prometheus.yml) (Scrape application)
 - Target: [](target/config/prometheus.yml) (Scrape application)

 Execute : `docker-compose up`

 [http://localhost:9090/graph?g0.expr=coffee_drank_cl&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h](Query) sample in origin  
 [http://localhost:9091/graph?g0.expr=coffee_drank_cl&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h](Query) sample in target
## Remote-write

Setup :
 - Prometheus Origin (9090) with no datas
 - Prometheus Target (9091) with no data
 - Sample application

Configuration :
 - Origin : [origin/config/prometheus-remote-write.yml]() (Remote-write + Scrape application)
 - Target: [target/config/prometheus.yml]()

Execute : `docker-compose -f docker-compose-remote-write.yaml up`

### Result

Origin is scrapping metrics from sample application after some satrtup time  
Remote-writing is sending data scrapped to target  
Target have new datas

## Remote-write (Backfill)

Setup :
 - Prometheus Origin (9090) with existing datas
 - Prometheus Target (9091) with no data

Configuration :
 - Origin : [origin/config/prometheus-remote-write-backfill.yml]() (Remote-write)
 - Target: [target/config/prometheus.yml]()

Execute : `docker-compose -f docker-compose-remote-write-backfill.yaml up`

### Result
Origin have the original datas [query](http://localhost:9090/graph?g0.expr=coffee_drank_cl&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h&g0.end_input=2022-03-31%2009%3A00%3A00&g0.moment_input=2022-03-31%2009%3A00%3A00)  
Target doesn't have the datas [query](http://localhost:9091/graph?g0.expr=coffee_drank_cl&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h&g0.end_input=2022-03-31%2009%3A00%3A00&g0.moment_input=2022-03-31%2009%3A00%3A00)


## Remote-read with backfill

Setup :
 - Prometheus Origin (9090) with existing datas
 - Prometheus Target (9091) with no data
 - Sample application

Configuration :
 - Origin : [origin/config/prometheus-remote-write-backfill.yml]()
 - Target: [target/config/prometheus.yml]() (Remote-read + Scape application)

Execute : `docker-compose -f docker-compose-remote-read.yaml up`

### Result
Origin have some existing datas [query](http://localhost:9090/graph?g0.expr=coffee_drank_cl&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h&g0.end_input=2022-03-31%2009%3A00%3A00&g0.moment_input=2022-03-31%2009%3A00%3A00)  
Target will read from origin for exsiting datas [query](http://localhost:9091/graph?g0.expr=coffee_drank_cl&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h&g0.end_input=2022-03-31%2009%3A00%3A00&g0.moment_input=2022-03-31%2009%3A00%3A00)  
Target will read new datas from application [query](http://localhost:9091/graph?g0.expr=coffee_drank_cl)  

:warning: Target will not copy data from origin  
You can stop origin with `docker-compose -f docker-compose-remote-read.yaml stop origin` and [query](http://localhost:9090/graph?g0.expr=coffee_drank_cl&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h&g0.end_input=2022-03-31%2009%3A00%3A00&g0.moment_input=2022-03-31%2009%3A00%3A00) will not send back data  
Restart origin with `docker-compose -f docker-compose-remote-read.yaml start origin` and data will be back [query](http://localhost:9091/graph?g0.expr=coffee_drank_cl&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h&g0.end_input=2022-03-31%2009%3A00%3A00&g0.moment_input=2022-03-31%2009%3A00%3A00)  
