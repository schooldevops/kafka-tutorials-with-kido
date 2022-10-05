# Collecting Kafka performance metrics

from: https://www.datadoghq.com/blog/collecting-kafka-performance-metrics/

- 주요 Kafka 성능 측정항목에 대한 가이드를 이미 읽었다면 카프카 성능 및 리소스 활용도에 대한 광범위한 측정 항목을 제공한다는 것을 알았고 다양한 방법으로 사용할 수 있다.
- 또한 주키퍼를 모니터링하지 않고서는 카프카 성능 모니터링 솔루션이 완성되지 않는다는 것도 확인했다. 
- 이 게시물에서 필요에 따라 카프카 및 주키퍼 메트릭을 수집하기 위한 몇 가지 다른 옵션을 다룬다. 

- Tomcat, Cassandra및 기타 자바 어플리케이션과 마찬가지로 Kafka와 ZooKeeper는 JMX(Java Management Extensions)를 통해 가용성 및 성능에 대한 메트릭을 노출한다. 

## Collect native Kafka Performance metrics

- 이 게시물에서 다음 도구를 사용하여 Kafka및 주키퍼에서 메트릭을 수집하는 방법을 보여준다. 
  - JConsole: JDK와 함께 제공되는 GUI이다.
  - JMX: 외부 그래프 작성 및 모니터링 도구 및 서비스 
  - Burrow: 컨슈머 헬스 모니터링 도구 

- JConsole과 JMX는 이 시리즈의 1부에서 설명한 모든 기본 Kafka 성능 메트릭을 수집할 수 있으며 Burrow는 모든 소비자의 상태와 오프셋을 모니터링할 수 있는 보다 전문화된 도구이다. 
- 호스트 수준 메트릭의 경우 모니터링 에이전트 설치를 고려해야한다. 

### Collect Kafka performance metrics with JConsole

- JConsole은 JDK와 함께 제공되는 간단한 Java GUI이다.
- Kafka가 JMX를 통해 내보낸 메트릭의 전체 범위를 탐색하기 위한 인터페이스를 제공한다. 
- JConsole은 리소스 집약적일 수 있으므로 전용 호스트에서 실행하고 원격으로 Kafka 메트릭을 수집해야한다.

- 우선 JConsole이 Kafka 호스트에서 JMX 메트릭을 수집하는 데 사용할 수 있는 포트를 지정해야한다. 
- KAFKA_JMX_OPTS 변수에 다음 매개변수를 추가하여 JMX 포트 값을 포함하도록 Kafka의 시작 스크립트(bin/kafka-run-class.sh)를 편집한다. 

```js
-Dcom.sun.management.jmxremote.port=<MY_JMX_PORT> -Dcom.sun.management.jmxremote.rmi.port=<MY_JMX_PORT> -Djava.rmi.server.hostname=<MY_IP_ADDRESS>
```

- 이러한 변경 사항을 적용하려면 Kafka를 다시 시작하라. 
- 다음으로 전용 모니터링 호스트에서 JConsole을 시작한다. 
- JDK가 시스템 경로의 디렉토리에 설치된 경우 jconsole명령으로 JConsole을 시작할 수 있다. 
- 그렇지 않으면 JDK 설치의 bin/ 하위 디렉토리에서 JConsole 실행 파일을 찾아라. 

- JConsole UI에서 Kafka호스트의 IP주소와 JMX 포트를 지정한다. 
- 아래 예는 192.0.0.1 포트 999에서 카프카 호스트에 연결하는 JConsole을 보여준다. 

![jconsole-remote2](imgs/jconsole-remote2.avif)

- MBeans 탭은 사용 가능한 모든 JMX 경로를 표시한다. 

![mbeans-tab](imgs/mbeans-tab.avif)

- 위의 스크린샷에서 볼 수 있듯이 kafka는 소스별로 메트릭을 집계한다. 
- 카프카의 주요 메트릭에 대한 모든 JMX 경로는 이 시리즈의 1부에서 찾을 수 있다. 

#### Consumer and producers

- 소비자 및 생산자로부터 JMX 메트릭을 수집하려면 위에서 설명한 것과 동일한 단계를 수행하여 포트 9999를 생산자 또는 소비자의 JMX 포트 및 노드의 IP 주소로 교체한다. 

### Collect Kafka performance metrics via JMX

- JConsole은 메트릭스 스냅샷을 매우 빠르게 제공할 수 있는 훌륭한 경량 도구이지만 프로덕션 환경에서 발생하는 큰 그림 유형의 질문에는 적합하지 않다. 
- 내 측정항목의 장기적 추세는 무엇인가? 
- 내가 알아야 할 대규모 패턴이 있는가?
- 성능 메트릭의 변경 사항이 내 환경의 다른 위치에 있는 작업이나 이벤트와 상관 관계가 있는가?

- 이러한 질문에 답하기 위해서는 보다 정교한 모니터링 시스템이 필요하다. 
- 다행히도 많은 모니터링 서비스와 도구가 JMX 플러그인을 통해 카프카에서 JMX 메트릭을 수집할 수 있다. 
- 플러그형 메트릭 리포터 라이브러리를 통해 또는 JMX 메트릭을 StatsD, Graphite또는 기타 시스템에 작성하는 커넥터를 통해 가능하다. 

- 구성 단계는 선택한 특정 모니터링 도구에 따라 크게 달라지지만 JMX는 이 시리즈 1부에서 언급한 MBean 이름을 사용하여 Kafka 성능 메트릭을 볼 수 있는 바른 경로이다. 

### Monitor consumer health with Burrow

- 이 시리즈의 1부에서 언급한 주요 메트릭 외에도 소비자에 대한 더 자세한 메트릭이 필요할 수 있다. 
- 이를 위한 도구로 burrow가 있다. 

- Burrow 는 LinkedIn에서 Kafka 소비자 모니터링을 위해 특별히 개발한 전문 모니터링 도구이다. 
- Burrow는 Kafka의 오프셋, 주제 및 소비자에 대한 가시성을 제공한다. 

- 특별한 내부 Kafka 주제 __consumer_offsets를 사용함으로써 Burrow는 단일 소비자와 분리된 중앙 집중식 서비스로 작동하여 커밋된 오프셋(주제 전반)과 브로커 상태를 기반으로 소비자에 대한 객관적인 보기를 제공할 수 있다. 

#### Installation and configuration

- 시작하기 전에 Go(v1.11+) 를 설치하고 구성해야한다. 
- 전용 머신을 사용하여 Burrow를 호스트 하거나 Kafka배포의 호스트 중 하나에서 실행할 수 있다. 
- Go 설치후 다음 커맨드를 실행하여 Burrow를 설치하자. 

```go
    go get github.com/linkedin/burrow
    cd $GOPATH/src/github.com/linkedin/burrow
    go mod tidy
    go install
```

- Burrow를 사용하기 전에 구성파일을 작성해야한다. 
- Burrow 구성은 Kafka 배포에 따라 다르다. 다음은 로컬 kafka배포를 위한 최소 구성방안이다. 

- burrow.cfg
  
```go
[zookeeper]
servers=["localhost:2181" ]

[httpserver.mylistener]
address=":8080"

[cluster.local]
class-name="kafka"
servers=[ "localhost:9091", "localhost:9092", "localhost:9093" ]

[consumer.myconsumers]
class-name="kafka"
cluster="local"
servers=[ "localhost:9091", "localhost:9092", "localhost:9093" ]
offsets-topic="__consumer_offsets"
```

- 구성 옵션에 대한 전체 개요는 Burrow wiki를 확인하라. 
- Burrow가 구성된 상태에서 다음 명령을 실행하여 소비자 상태 추적을 시작할 수 있다. 

```go
$GOPATH/bin/burrow --config-dir /path/to/config-directory
```

- 이제 Burrow의 HTTP엔드포인트 쿼리를 시작할 수 있다. 
- 예를 들어 Kafka 클러스터 목록을 보려면 http://localhost:8080/v3/kafka 를 누르고 다음과 같은 JSON응답을 볼 수 있다. 

```go
{
	"error": false,
	"message": "cluster list returned",
	"clusters": ["local"],
	"request": {
		"url": "/v3/kafka",
		"host": "mykafkahost"
	}
}
```

- 우리는 HTTP 또는 이메일을 통한 자동 알림을 포함하는 Burrow 기능의 겉핱기만 했다. 
- Burrow에 대해서 자세하게 알고자 하면 문서를 참조하자. https://github.com/linkedin/Burrow/wiki/

## Monitor Kafkas page cache

- 1부에서 식별된 대부분의 호스트 수준 메트릭은 표준 시스템 유틸리티를 사용하여 수집할 수 있다. 
- 페이지 캐시에는 더 많은 것이 필요하다. 3.13 이전의 리눅스 커널은 이 메트릭을 노출하기 위해 컴파일 타임 플래그가 필요할 수 있다. 
- 또한 Brendan Gregg가 만든 cachestat 스크립트를 다운로드 해야한다. 

```go
wget https://raw.githubusercontent.com/brendangregg/perf-tools/master/fs/cachestat
```

- 다음으로 스크립트를 실행가능하게 만든다. 

```go
chmod +x cachestat
```

- 그런 다음 ./cachestat<수집간격(초)> 을 사용하여 실행할 수 있다. 다음 예외 유사한 출력이 표시되어야 한다. 

```go
Counting cache functions... Output every 20 seconds.
	    HITS   MISSES  DIRTIES    RATIO   BUFFERS_MB   CACHE_MB
	    5352        0      234   100.0%          103        165
	    5168        0      260   100.0%          103        165
	    6572        0      259   100.0%          103        165
	    6504        0      253   100.0%          103        165
	[...]
```

- (DIRTIES열의 값은 페이지 캐시 입력 후 수정된 페이지 수를 나타낸다.)

## Collect ZooKeeper metrics

- 이 섹션에서는 ZooKeeper에서 메트릭을 수집하는 데 사용할 수 있는 세가지 도구인 JConsole, ZooKeeper의 "4글자 단어" 및 ZooKeeper Admin Server를 살펴보자. 
- 네글자 단어 또는 AdminServer만 사용하여 이 시리즈의 파트 1에 나열된 기본 ZooKeeper메트릭을 모두 수집할 수 있다. 
- 만약 JConsole을 이용한다면 팔로워 및 open_file_descriptor_count메트릭을 제외한 모든 메트릭을 수집할 수 있다. 

- 추가적으로 주키퍼에 대한 최상위 이넡페이스를 제공하는 zktop 유틸리티는 ZooKeeper앙상블을 모니터링 하는 데 유용한 도구이기도 하다. 
- 이 게시물에서는 zktop을 다루지는 않는다. 자세한 내용은 설명설르 참조하자. 

### Use JConsole to view JMX metrics

- JConsole에서 ZooKeeper메트릭을 보려면 로컬 주키퍼 서버를 모니터링 하는 경우 org.apache.zookeeper.server.quorum.QuorumPeerMain 프로세스를 선택할 수 있다. 
- 기본적으로 주키퍼는 로털 JMX 연결만 허용하므로 원격 서버를 모니터링 하려면 JMX 포트를 수동으로 지정해야한다. 
- 주키퍼의 bin/zkEnv.sh 파일에서 환경 변수로 추가하여 포트를 지정하거나 다음 예외 같이 zookeeper를 시작하는 데 사용하는 명령에 포함할 수 있다. 

```go
JMXPORT=9993 bin/zkServer.sh start
```

- Java 프로세스의 원격 모니터링을 활성화 하려면 java.rmi.server.hostname 속성을 설정해야한다. 

- 주키퍼가 실행되고 JMX를 통해 메트릭을 전송하면 다음과 같이 JConsole인스턴스를 원격 서버에 연결할 수 있다. 

![zookeeper-jconsole](imgs/zookeeper-jconsole-overview.avif)

- 메트릭에 대한 주키퍼의 정확한 JMX 경로는 구성에 따라 다르지만 항상 org.apache.ZooKeeperService MBean에서 찾을 수 있다. 
  
![zookeeper-jconsole-mbeans](imgs/zookeeper-jconsole-mbeans.avif)

- JMX를 사용하면 이 시리즈의 파트 1에 나열된 대부분의 메트릭을 수집할 수 있다. 
- 그것들을 모두 수집하려면 네글자 단어 또는 주키퍼 AdminServer를 사용해야한다. 

### The four-letter words

- 주키퍼는 4글자 단어로 알려진 제한된 명령 집합에 대한 응답으로 작동 데이터를 내보낸다. 
- 4글자 단어는 더 이상 사용되지 않으며 AdminServer를 선호하며 주키퍼 버젼 3.5부터는 구성에서 각 4글자 단어를 사용하기 전에 명시적으로 활성화 해야한다. 
- 하나 이상의 4글자 단어를 활성화 하려면 주키퍼 설치의 conf 하위 디렉토리에 있는 zoo.cfg파일에 지정한다. 

- 텔넷 또는 nc를 통해 주키퍼 네글자 단어를 발행할 수 있다. .예를 들어 구성에서 mntr을 활성화 한 경우 이 단어를 사용하여 주키퍼 서버에 대한 세부 정보를 얻을 수 있다. 

```go
echo mntr | nc localhost 2181
```

- 주키퍼는 여기에 표시된 예와 유사한 정보로 응답한다. 

```go
zk_version	3.5.7-f0fdd52973d373ffd9c86b81d99842dc2c7f660e, built on 02/10/2020 11:30 GMT
zk_avg_latency	0
zk_max_latency	0
zk_min_latency	0
zk_packets_received	12
zk_packets_sent	11
zk_num_alive_connections	1
zk_outstanding_requests	0
zk_server_state	standalone
zk_znode_count	5
zk_watch_count	0
zk_ephemerals_count	0
zk_approximate_data_size	44
zk_open_file_descriptor_count	67
zk_max_file_descriptor_count	1048576
```

### The Admin Server

- 주키퍼 버젼 3.5에서 4글자 단어 대신 AdminServer로 대체 되었다. 
- HTTP엔드포인트를 사용하여 주키퍼 앙상블에 대한 모든 동일한 정보에 액세스할 수 있다. 
- 사용 가능한 엔드포인트를 보려면 로컬 주키퍼 서버의 명령 끝점에 요청을 보낸다. 

```go
curl http://localhost:8080/commands
```

- 다음과 같이 URL엔드포인트 이름을 지정하여 유사항 명령을 사용하여 특정 엔드포인트에서 정보를 검색할 수 있다. 

```go
curl http://localhost:8080/<ENDPOINT>
```

- AdminServer는 출력을 JSON형식으로 보낸다. 
- 예를 들어 AdminServer의 모니터 끝점은 앞에서 호출한 mntr단어와 유사한 기능을 제공한다. 
- http://localhost:8080/commands/monitor에 요청을 보내면 다음과 같은 출력이 생성된다. 

```go
{
  "version" : "3.5.7-f0fdd52973d373ffd9c86b81d99842dc2c7f660e, built on 02/10/2020 11:30 GMT",
  "avg_latency" : 0,
  "max_latency" : 0,
  "min_latency" : 0,
  "packets_received" : 36,
  "packets_sent" : 36,
  "num_alive_connections" : 0,
  "outstanding_requests" : 0,
  "server_state" : "standalone",
  "znode_count" : 5,
  "watch_count" : 0,
  "ephemerals_count" : 0,
  "approximate_data_size" : 44,
  "open_file_descriptor_count" : 68,
  "max_file_descriptor_count" : 1048576,
  "last_client_response_size" : -1,
  "max_client_response_size" : -1,
  "min_client_response_size" : -1,
  "command" : "monitor",
  "error" : null

}
```

## Production-ready Kafka performance monitoring

- 이 포스트에서 간단하고 가벼운 도구를 사용하여 kafka와 zookeeper 메트릭에 액세스 하는 몇 가지 방법을 다루었다. 
- 프럳덕션 준비 모니터링의 경우 kafka 성능 메트릭과 스택에 있는 모든 기술의 주요 메트릭을 수집하는 동적 모니터링 시스템이 필요할 수 있다. 
- 이 시리즈 파트 3에서 우리는 데이터독을 사용하여 카프카 배포에서 메트릭과 로그 및 추적을 수집하고 보는 방법을 보여준다. 

- 데이터독은 카프카, 주키퍼 및 500개 이상의 기타 기술과 통합되므로 클러스터의 메트릭, 로그 및 분산요청 추적을 분석하고 경고할 수 있다. 
