# 실행방법.

## 카프카 컨테이너 실행

```go
docker-compose -f docker-compose.yaml up
```

## 카프카 컨테이너 종료 

```go
docker-compose -f docker-compose.yaml down
```

## kafka-ui 보기

- [localhost:8989](http://localhost:8989/)

## kafka-connect 조회하기

- http://localhost:8083/connectors

```go
["source-mysql-user-00","sink-mysql-user-00"]
```
- http://localhost:8083/connectors/sink-mysql-user-00

```go
{"name":"sink-mysql-user-00","config":{"connector.class":"io.confluent.connect.jdbc.JdbcSinkConnector","connection.password":"1234","auto.evolve":"true","connection.user":"sink_user","task.max":"1","name":"sink-mysql-user-00","auto.create":"true","connection.url":"jdbc:mysql://mysql:3306/sink","topics.regex":"^jdbc-connector-.*","insert.mode":"upsert","pk.mode":"record_value","pk.fields":"id"},"tasks":[{"connector":"sink-mysql-user-00","task":0}],"type":"sink"}
```

- http://localhost:8083/connectors/sink-mysql-user-00/status

```go
{"name":"sink-mysql-user-00","connector":{"state":"RUNNING","worker_id":"kafka-connect:8083"},"tasks":[{"id":0,"state":"RUNNING","worker_id":"kafka-connect:8083"}],"type":"sink"}
```

## 관련자료 

- https://www.confluent.io/blog/kafka-connect-deep-dive-jdbc-source-connector/
- https://github.com/schooldevops/AWS_Tutorials_by_kido/blob/main/ElastiCache/redis_cluster.md
- https://github.com/lettuce-io/lettuce-core/wiki/ReadFrom-Settings
- https://github.com/lettuce-io/lettuce-core/wiki/Redis-Cluster