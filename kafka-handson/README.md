# RUN docker-compose

## Single docker compose

```go
docker-compose -f docker-compose-single.yaml up -d
```

## multi docker compose

```go
docker-compose -f docker-compose.yaml up -d
```

## kafka-ui 실행하기

- https://github.com/provectus/kafka-ui/blob/master/README.md

```go
docker run -p 8989:8080 \
	-e KAFKA_CLUSTERS_0_NAME=local \
	-e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=localhost:29092,localhost:39092,localhost:49092 \
	-d provectuslabs/kafka-ui:latest 
```

```go
docker run -p 8989:8080 \
	-e KAFKA_CLUSTERS_0_NAME=local \
	-e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=localhost:29092 \
	-d provectuslabs/kafka-ui:latest 
```

```go
podman run -p 8080:8080 --name ui_kafka --rm \
	-e KAFKA_CLUSTERS_0_NAME=local \
	-e KAFKA_CLUSTERS_0_ZOOKEEPER=172.24.118.82:2181,172.24.118.82:2182,172.24.118.82:2183 \
	-e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=172.24.118.82:9093,172.24.118.82:9094,172.24.118.82:9095 \
	-d provectuslabs/kafka-ui:latest
```

## 서버가 정상으로 수행되지 않은경우 


```go
ᐅ docker container ls -a
CONTAINER ID   IMAGE                              COMMAND                  CREATED              STATUS                       PORTS                                              NAMES
925073caec8c   confluentinc/cp-kafka:latest       "/etc/confluent/dock…"   About a minute ago   Exited (137) 5 seconds ago                                                      kafka-handson-kafka-3-1
dda0ee1e7cd8   confluentinc/cp-kafka:latest       "/etc/confluent/dock…"   About a minute ago   Exited (1) 40 seconds ago                                                       kafka-handson-kafka-2-1
70fdb09a5b0b   confluentinc/cp-kafka:latest       "/etc/confluent/dock…"   About a minute ago   Exited (1) 41 seconds ago                                                       kafka-handson-kafka-1-1
15adbebf7575   provectuslabs/kafka-ui             "/bin/sh -c 'java $J…"   13 minutes ago       Exited (143) 2 minutes ago                                                      kafka-ui
762596e2206b   confluentinc/cp-zookeeper:latest   "/etc/confluent/dock…"   13 minutes ago       Exited (143) 4 seconds ago                                                      kafka-handson-zookeeper-1-1
83e4f4f81bad   confluentinc/cp-zookeeper:latest   "/etc/confluent/dock…"   13 minutes ago       Exited (143) 4 seconds ago                                                      kafka-handson-zookeeper-2-1
7efc61819f9f   confluentinc/cp-zookeeper:latest   "/etc/confluent/dock…"   13 minutes ago       Exited (143) 4 seconds ago                                                      kafka-handson-zookeeper-3-1
3c5da9447d5e   memcached                          "docker-entrypoint.s…"   6 days ago           Up 6 days                    0.0.0.0:11211->11211/tcp                           reverent_euclid
36fb3ddb4543   mariadb:latest                     "docker-entrypoint.s…"   7 days ago           Up 7 days                    0.0.0.0:3306->3306/tcp                             my-mariadb
789a8c68174d   redis                              "docker-entrypoint.s…"   11 days ago          Up 7 days                    0.0.0.0:6379->6379/0:8999->8080/tcp   myjenkins

ᐅ docker container rm 925073caec8c dda0ee1e7cd8 70fdb09a5b0b 15adbebf7575 762596e2206b 83e4f4f81bad 83e4f4f81bad 7efc61819f9f 
```

