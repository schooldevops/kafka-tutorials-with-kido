# kafka-performance-metrics

from: https://www.datadoghq.com/blog/monitoring-kafka-performance-metrics/

## What is Kafka

- 카프카는 분산, 파티션, 복제, 로그 서비스이다. 
- 링크드인에서 2011년 오픈소스로 배포했다. 
- 대용량의 확장가능한 pub/sub 메시지 큐 아키텍처로 분산트랜잭션 로그를 처리한다. 
- 대기업이 가질 수 있는 모든 실시간 데이터 피드를 처리하기 위한 통합 플랫폼을 제공하기 위해서 개발되었다. 

- 카프카와 다른 큐 시스템에서는 몇가지 다른 차이점이 있다. 
  - 카프카는 기본적으로 복제된 로그 서비스이다. 
  - 커뮤니케이션을 위해서 AMPQ 혹은 사전에 정의된 프로토콜을 사용하지 않는다. 
  - 작은 클러스터에서도 매우 빠르다. 
  - 강력한 주문 의미와 내구성 보장이 있다. 
- kafka는 다양한 조직에서 사용되며, 링크드인, 핀터레스트, 트위터, 데이터독 등
- 가장 최근 버젼은 2.4.1이다. 

## 아키텍처 오버뷰

- 카프카 배포버젼의 중요한 아키텍처는 다음과 같다. 
- 각 배포판은 컴포넌트들로 이루어 져 있다. 

![kafka-diagram](./imgs/kafka-diagram.avif)

- kafka 브로커들은 프로듀서 어플리케이션 사이에서 동작한다. 이는 메시지를 전송하는 역할을 수행한다. 
- 컨슈머는 이러한 메시지를 수신받아 처리한다. 
- 생산자는 요청 수를 줄여 네트워크 오버헤드를 최소화 하기 위해 일괄적으로 Kafka 브로커에 메시지를 푸시한다. 
- 브로커는 소비자가 원하는 속도로 가져올 수 있도록 메시지를 저장한다. 

- 메시지는 메시지를 설명하는 메타 데이터로 구성된다. 메시지 페이로드 및 선잭적 임의 헤더(버젼 0.11.0기준)

- Kafka 의 메시지는 브로커가 수신한 순서대로 로그에 기록되며 변경할 수 없으며 읽기만 허용된다. 

![broker-topic-parition2](./imgs/broker-topic-partition2.avif)

- 카프마는 관련 메시지를 저장하는 주제로 메시지를 구성하고 소비자는 필요한 주제를 구독한다. 
- 토픽 자체가 파티션으로 나뉘고 파티션은 브로커에게 할당된다.
- 따라서 주제는 브로커 수준에서 데이터 분할을 실행한다. 파티션 수가 많을수록 주제가 지원할 수 있는 동시에 소비자 수가 늘어난다. 

- kafka를 처음 설정할 때 주제당 충분한 수의 파티션을 할당하고 브로커 간에 파티션을 공정하게 분할하도록 주의해야한다. 
- Kafka를 처음 배포할 때 그렇게 하면 향후 성장통을 최소화 할 수 있다. 
- 적절한 수의 토픽과 파티션을 선택하는 방법에 대한 자세한 내용은 Confluent의 Jun Rao가 작성한 글을 읽어라. http://www.confluent.io/blog/apache-kafka-supports-200k-partitions-per-cluster/

- kafka의 복제 기능은 선택적으로 각 파티션을 여러 브로커에 유지하여 고 가용성을 제공한다. 
- 복제된 파티션에 Kafka는 파티션 리더인 하나의 복제본에만 메시지를 쓴다. 
- 다른 복제본은 리더에서 메시지 복사본을 가져오는 팔로워이다. 
- 소비자는 Kafka 버젼 2.4에서 파티션 리더 또는 팔로워에서 일을 수 있다. (이전 버전에서는 소비자가 파티션 리더에서만 읽을 수 있었다.) 
- 이 아키텍처는 요청 로드를 복제본 집합에 분산한다. 

- 또한 현재 리더가 오프라인 상태가 되면 팔로워가 ISR(동기화 복제본)로 인식되는 경우 모든 팔로워가 파티션 리더 역할을 할 수 있다. 
- Kafka는 파티션 리더로 전송된 각 메시지를 성공적으로 가져오고 승인한 경우 팔로워가 동기화된 것으로 간주한다. 
- 만약 리더가 오프라인이라면 kafka는 새로운 리더를 ISR로 부터 선출한다. 
- 그러나 만약 브로커가 명확하지 않은 리더 선출을 허용하도록 설정되었다면 (unclean.leader.election.enable 값이 true인경우), 이는 싱크되지 않은 리더를 선택할 수 있다. 

- 마지막으로 ZooKeeper 없이는 Kafka 배포가 완료되지 않는다. 
- ZooKeeper는 모든것을 연결하며 다음 역할을 수행한다. 
  - 컨트롤러 선출 (kafka broker는 파티션 리더를 관리한다.)
  - 클러스터 멤버십 레코딩
  - 토픽 설정 관리 
  - 생산자와 소비자의 처리량을 제한히기 위해 설정한 할당량 적용 

## Kafka를 위한 키 메트릭 

- 제대로 작동하는 Kafka 클러스터는 상당한 양의 데이터를 처리할 수 있다. 
- Kafka배포에 의존하는 애플리케이션의 안정적인 성능을 유지하려면 kafka배포의 상태를 모니터링하는 것이 중요하다. 

- kafka metrics 는 3가지 카테고리에 브러코 다운을 알 수 있다.
  - kafka server 메트릭
  - producer metrics
  - consumer metrics

- kafka 는 주키퍼의 운영 상태에 의존한다. 이는 중요한 주키퍼 모니터링 정보이다. 
- kafka 및 Zookeeper메트릭 수집에 대해 자세히 알아보려면 이 시리즈의 2부를 살펴보자. 

- 이 문서에서는 메트릭 수집 및 경고를 위한 프레임워크를 제공하는 Monitoring 101 시리즈에 소개된 메트릭 용어를 참조한다. 

## Broker metrics

- 모든 메시지들은 반드시 kafka broker를 통해서 컨슘되기 때문에 모니터링과 알람이 중요하다. 
- 브로커 메트릭은 다음 3가지 사유로 발생된다. 
  - Kafka-emitted metrics
  - Host-level metrics
  - JVM garbage collection metrics

![kafka-diagram-broker](./imgs/kafka-diagram-broker.avif)

### Kafka-emitted metrics

|Name|	MBean name|	Description|	Metric type|
|---|---|---|---|
|UnderReplicatedPartitions|	kafka.server:type=ReplicaManager/name=UnderReplicatedPartitions|	Number of unreplicated partitions|	Resource: Availability|
|IsrShrinksPerSec/IsrExpandsPerSec|	kafka.server:type=ReplicaManager/name=IsrShrinksPerSec kafka.server:type=ReplicaManager/name=IsrExpandsPerSec|	Rate at which the pool of in-sync replicas (ISRs) shrinks/expands|	Resource: Availability|
|ActiveControllerCount|	kafka.controller:type=KafkaController/name=ActiveControllerCount|	Number of active controllers in cluster|	Resource: Error|
|OfflinePartitionsCount|	kafka.controller:type=KafkaController/name=OfflinePartitionsCount|	Number of offline partitions|	Resource: Availability|
|LeaderElectionRateAndTimeMs|	kafka.controller:type=ControllerStats/name=LeaderElectionRateAndTimeMs	Leader| election rate and latency|	Other|
|UncleanLeaderElectionsPerSec|	kafka.controller:type=ControllerStats/name=UncleanLeaderElectionsPerSec|	Number of “unclean” elections per second|	Resource: Error|
|TotalTimeMs|	kafka.network:type=RequestMetrics/name=TotalTimeMs/request={Produce|FetchConsumer|FetchFollower}|	Total time (in ms) to serve the specified request (Produce/Fetch)|	Work: Performance|
|PurgatorySize|	kafka.server:type=DelayedOperationPurgatory/name=PurgatorySize|delayedOperation={Produce|Fetch}|	Number of requests waiting in producer purgatory/Number of requests waiting in fetch purgatory|	Other|
|BytesInPerSec/BytesOutPerSec|	kafka.server:type=BrokerTopicMetrics|name={BytesInPerSec|BytesOutPerSec}|	Aggregate incoming/outgoing byte rate|	Work: Throughput|
|RequestsPerSecond|	kafka.network:type=RequestMetrics/name=RequestsPerSec/request={Produce|FetchConsumer|FetchFollower}/version={0|1|2|3|…}|	Number of (producer|consumer|follower) requests per second|	Work: Throughput|

#### Metric to watch: UnderReplicatedPartitions

- 정상 클러스터에서 ISR(동기화 복제본)의 수는 총 복제본 수와 정확히 일치한다.
- 파티션 복제본이 해당 리더보다 너무 뒤처지면 팔로워 파티션이 ISR 풀에서 제거되고 IsrShrinksPerSec에서 해당 증가가 표시되어야한다. 
- 만약 브로커를 사용할 수 없게되면 UnderReplicatedPartitions의 값이 급격히 증가한다. 
- 카프카의 고가용성 보장은 복제 없이는 충족될 수 없으므로 이 메트릭 값이 장기간 0을 초과하는 경우 조사가 확실히 보장된다. 

#### Metric to watch: IsrShrinksPerSec/IsrExpandsPerSec

- 브로커 클러스터를 확장하거나 파티션을 제거하는 경우를 제외하고 특정 파티션에 대한 ISR의 수는 상당히 정적으로 유지되어야한다. 
- 고가용성을 유지하기 위해 정상적인 Kafka클러스터에는 장애 조치를 위한 최소 수의 ISR이 필요하다. 
- 일정 시간 동안 리더에 연결하지 않은 경우 복제본이 ISR 풀에서 제거될 수 있다. (replica.socket.timeout.ms)
- 이러한 메트릭 값의 플랩과 그 직후 IsrExpandsPerSec의 해당 증가 없이 IsrShrinksPerSec의 증가를 조사해야한다. 

#### Metric to alert on: ActiveControllerCount

- 카프카 클러스터에서 부팅하는 첫 번째 노드는 자동으로 컨트롤러가 되며 하나만 있을 수 있다. 
- 카프카 클러스터의 컨트롤러는 파티션 리더 목록을 유지 관리하고 리더쉽 전환을 조정한다. (파티션 리더를 사용할 수 없는 경우)
- 컨트롤러를 교체해야 하는 경우 ZooKeeper는 브로커 풀에서 새 컨트롤러를 무작위로 선택한다. 
- 모든 브로커에 대한 ActiveControllerCount의 합계는 항상 1과 같아야 하며 1초 이상 지속되는 다른 값에 대해 경고해야한다. 

#### Metric to alert on: OfflinePartitionsCount (controller only)

- 이 메트릭은 활성 리더가 없는 파티션 수를 보고한다. 
- 모든 읽기 쓰기 작업은 파티션 리더에서만 수행되기 때문에 서비스 중단을 방지하려면 이 메트릭에 대해 0이 아닌 값에 대해 경고해야한다. 
- 활성 리더가 없는 파티션은 완전히 액세스할 수 없으며 해당 파티션의 소비자와 생산자는 리더를 사용할 수 있을땎지 차단된다.

#### Metric to watch: LeaderElectionRateAndTimeMs

- 파티션 리더가 죽으면, 새 리더 선출이 시작된다.
- 파티션 리더는 ZooKeeper와의 세션 유지에 실패하면 "죽은" 것으로 간주된다.
- ZooKeeper의 Zab와 달리 Kafka는 리더십 선출을 위해 다수결 알고리즘을 사용하지 않는다. 
- 대신에 카프카의 쿼럼은 특정 파티션에 대한 모든 ISR(동기화 복제본) 집합으로 구성된다.
- 복제본이 리더를 따라잡으면 동기화 된 것으로 간주된다. 즉 ISR의 모든 복제본이 리더로 승격될 수 있다. 

- LeaderElectionRateAndTimeMs는 리더 선택 비율(초당)과 클러스터에 리더가 없는 총 시간(밀리초)을 보고한다. 
- UncleanLeaderElectionsPerSec만큼 나쁘지는 않지만 이 메트릭을 주시하고 싶을 것이다. 
- 위에서 언급했듯이 리더 선출은 현재 리더와의 연락이 끊겼을 때 촉발되며, 이는 오프라인 브로커로 전환될 수 있다. 

![kafka-leader-elect](imgs/kafka-leader-elect.avif)

#### Metric to alert on: UncleanLeaderElectionsPerSec

- 명확하지 않은 리더 선택은 카프카 브로커 중 자격이 있는 파티션 리더가 없을 때 발생한다.
- 일반적으로 파티션의 리더인 브로커가 오프라인 상태가 되면 파티션의 ISR 집합에서 새 리더가 선출된다. 
- 명확하지 않은 리더 선택은 카프카 0.11 이상에서 기본적으로 비활성화 되어 있다. 
- 즉, 새 리더로 선택할 ISR이 없는 경우 파티션이 오프라인 상태가 된다. 
- 카프카가 부정확한 리더 선택을 허용하도록 구성된 경우 동기화되지 않은 복제본에서 리더가 선택되고 이전 리더가 손실되기 전에 동기화되지 않은 모든 영구적으로 손실된다. 
- 기본적으로 부정확한 리더 선택은 가용성을 위해 일관성을 희생한다. 
- 이 측정항목은 데이터 손실을 나타내므로 주의해야한다. 

#### Metric to watch: TotalTimeMs

- TotalTimeMs 메트릭 패밀리는 요청을 처리하는 데 걸린 총 시간을 측정한다. (생산, 가져오기-소비자 또는 가져오기-팔로워요청)
  - produce: 생산자로부터 데이터 전송 요청
  - fetch-consumer: 새로운 데이터를 가져오기 위해서 컨슈머에서 요청
  - fetch-follower: 새로운 데이터를 얻기 위해 파티션의 팔로워인 브로커로 부터 요청
- TotalTimeMs 측정 자체는 다음 네 가지 측정항목의 한계이다.
  - queue: 요청 큐에서 대기하는 데 소요된 시간
  - local: 리더가 처리하는 데 소요된 시간
  - remote: 팔로워 응답을 기다리는 데 소요된 시간(requests.required.acks=-1인 경우에만)
  - response: 응답을 보낸 시간

- 일반적인 상태에서 이 값은 최소한의 변동으로 매우 정적이다. 
- 비 정상적인 동작이 보이면 개별 대기열, 로컬, 원격 및 응답값을 확인하여 속도 저하를 일으키는 정확한 요청 세그먼트를 찾아낼 수 있다. 

![kafka-totaltime](imgs/kafka-totaltime.avif)

#### Metric to watch: PurgatorySize

- 요청  충족되기를 기다리는 농산물 및 가져오기 요청을 위한 임시 보관소 역할을 한다. 

The request purgatory serves as a temporary holding pen for produce and fetch requests waiting to be satisfied. Each type of request has its own parameters to determine if it will be added to purgatory:

fetch: Fetch requests are added to purgatory if there is not enough data to fulfill the request (fetch.min.bytes on consumers) until the time specified by fetch.wait.max.ms is reached or enough data becomes available
produce: If request.required.acks=-1, all produce requests will end up in purgatory until the partition leader receives an acknowledgment from all followers.
Keeping an eye on the size of purgatory is useful to determine the underlying causes of latency. Increases in consumer fetch times, for example, can be easily explained if there is a corresponding increase in the number of fetch requests in purgatory.


#### Metric to watch: BytesInPerSec/BytesOutPerSec

- 일반적으로 디스크 처리량은 Kafka 성능의 주요 병목 현상이 되는 경향이 있다. 
- 그러나 네트워크가 병목 현상이 절대 발생하지 않는다는 것이다. 
- 네트워크 처리량은 데이터 센터간에 메시지를 보내는 경우, 주제에 많은 수의 소비자가 있는 경우 또는 복제본이 리더를 따라잡는 경우 Kafka의 성능에 영향을 줄 수 있다. 
- 브로커에서 네트워크 처리량을 추적하면 잠재적인 병목 현상이 발생할 수 있는 위치에 대한 추가 정보를 얻을 수 있으며, 메시지의 종단 간 압축을 활성화해야 하는지 여부와 같은 결정을 내릴 수 있다. 

#### Metric to watch: RequestsPerSec

- 카프카 배포가 효율적으로 통신하고 있는지 확인하려면 생산자, 소비자 및 팔로워의 요청 비율을 모니터링 해야한다. 
- 생산자가 더 많은 트래픽을 보내거나 배포가 확장되어 메시지를 가져와야 하는 소비자 또는 팔로워가 추가됨에 따라 Kafka의 요청 속도가 증가할 것으로 예상할 수 있다. 
- 그러나 requestsPerSec 이 여전히 높으면 생상자, 소비자 및/또는 브로커의 배치 크리를 늘리는 것을 고려해야한다. 
- 이렇게 하면 요청 수를 줄여 불필요한 네트워크 오버헤드를 줄임으로써 Kafka 배포의 처리량을 향상시킬 수 있다. 

### Host-level broker metrics

|Name|	Description|	Metric type|
|---|---|---|
|Page cache reads ratio|	Ratio of reads from page cache vs reads from disk|	Resource: Saturation|
|Disk usage|	Disk space currently consumed vs. available|	Resource: Utilization|
|CPU usage|	CPU use|	Resource: Utilization|
|Network bytes sent/received|	Network traffic in/out|	Resource: Utilization|

#### Metric to watch: Page cache read ratio

- 카프카는 처음부터 커널의 페이지 캐시를 활용하여 안정적(디스크 지원) 및 성능(메모리 내) 메시지 파이프라인을 제공하도록 설계 되었다. 
- 페이지 캐시 읽기 비율은 데이터베이스의 캐시 적중률과 유사하다. 값이 높을수록 읽기 속도가 빨라져 성능이 향상된다. 
- 이 메트릭은 복제본이 리더를 따라잡는 경우 (새 브로커가 생성될 때와 같이) 잠시 떨어지지만 페이지 캐시 읽기 비율이 80% 미만으로 유지되면 추가 브로커를 프로비저닝 하는 것이 좋다. 

![kafka-pagecache](imgs/kafka-pagecache-read.avif)

#### Metric to alert on: Disk usage

- 카프카는 모든 데이터를 디스크에 유지하기 때문에 kafka에 사용 가능한 디스크 여유 공간의 양을 모니터링 해야한다. 
- 카프카는 디스크가 가득 차면 실패하므로 시간이 지남에 따라 디스크 성장을 추적하고 디스크 공간이 거의 다 소모되기 전에 적절한 시간에 관리자에게 알리도록 경고를 설정하는 것이 매우 중요하다. 

![kafka-disk-space](imgs/kafka-disk-free.avif)

#### Metric to watch: CPU usage

- Kafka의 주요 병목 현상은 일반적으로 메모리지만, CPU사용량을 주시하는 것은 나쁘지 않다. 
- GZip 압축이 활성화된 사용 사례에서도 CPU가 성능 문제의 원인이 되는 경우는 거의 없다. 
- 따라서 CPU 사용률이 급증하는 경우 조사할 가치가 있다. 

#### Network bytes sent/received

- 카프카의 바이트 입/출력 메트릭을 모니터링하는 경우 kafka 의 측면을 이해하고 있는 것이다. 
- 호스트의 네트워크 사용량에 대한 전체 그림을 얻으려면 특히 Kafka 브로커가 다른 네트워크 서비스를 호스팅하는 경우 호스트 수준 네트워크 처리량을 모니터링 해야한다. 
- 높은 네트워크 사용량은 성능 저하의 증상일 수 있다.
- 네트워크 사용량이 많은 경우 TCP 재전송 및 패킷 손실 오류와 관련하여 성능 문제가 네트워크와 관련된 것인지 판단하는 데 도움이 될 수 있다. 

### JVM garbage collection metrics

- 카프카는 Scala로 작성되고 JVM(Java Virtual Machine)에서 실행되기 때문에 Java가비지 수집 프로세스에 의존하여 메모리를 확보한다. 
- 카프카 클러스터의 활동이 많을수록 가비지 수집이 더 자주 실행된다. 

![jvm-gc-per-min](imgs/jvm-gc-per-min.avif)

- Java 응용 프로그램에 익숙한 사람은 가비지 수집이 높은 성능 비용을 수반할 수 있다는 것을 알고 있다. 
- 가비지 수집으로 인한 긴 일시 중지의 가장 눈에 띄는 효과는 버려진 ZooKeeper세션의 증가이다. (세션 시간 초과로 인해)
- 가비지 수집 유형은 젋은 세대(새 개체)또는 이전 세대(오래 지속된 개체)가 수집되는지 여부에 따라 다르다. 
- Java 가비지 수집에 대한 좋은 입문서는 이 페이지를 참조하자. https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/gc01/index.html
- 가비지 수집 중에 과도한 일시 중지가 발생하는 경우 JDK 버전 또는 가비지 수집기를 업그레이드 하는 것을 고려할 수 있다. (또는 zookeeper.session.timeout.ms에 대한 시간 초과 값을 확장)
- 또한 가비지 수집을 최소화 하도록 Java 런타임을 조정할 수 있다. 
- LinkedIn의 엔지니어는 JVM 가비지 수집 최적화에 대해 자세히 썼다. 
- 물론 kafka 문서에서 몇 가지 권장 사항을 확인할 수 있다. 

|JMX attribute|	MBean name|	Description|	Type|
|---|---|---|---|
|CollectionCount|	java.lang:type=GarbageCollector/name=G1 (Young|Old) Generation|	JVM에 의해 실행된 젋거나 오래된 가비지 수집 프로세스의 총 수|	Other|
|CollectionTime|	java.lang:type=GarbageCollector/name=G1 (Young|Old) Generation|	JVM이 젋거나 오래된 가비지 수집 프로세스를 실행하는 데 소비한 총 시간(밀리초) |	Other|

![young-generation](imgs/young-generation-time-broker.avif)

#### Metric to watch: Old generation garbage collection count/time

- 구세대 가비지 수집은 구세대 힙에서 사용하지 않는 메모리를 해제한다. 
- 이것은 일시 중지가 낮은 가비지 수집이다. 
- 즉, 응용 프로그램 스레드를 일시적으로 중지하지만 간헐적으로만 중지한다. 
- 이 프로세스를 완료하는 데 몇 초가 걸리거나 빈도가 증가하는 경우 클러스터에 메모리가 부족하여 효율적으로 작동할 수 없는것이다. 


## Kafka producer metrics

- 카프카 프로듀서들은 소비를 위해 메시지를 브로커 토픽으로 푸시하는 독립적인 프로세스이다. 
- 생산자가 실패하면 소비자는 새 메시지 없이 남게 된다.
- 다음은 들어오는 데이터의 안정적인 흐름을 보장하기 위해 모니터링할 수 있는 가장 유용한 생산자 메트릭이다. 

![kafka-diagram-producer](imgs/kafka-diagram-producer.avif)


|JMX attribute|	MBean name|	Description|	Metric type|
|---|---|---|---|
|compression-rate-avg|	kafka.producer:type=producer-metrics/client-id=([-.w]+)|보낸 배치의 평균 압축률	|Work: Other|
|response-rate|	kafka.producer:type=producer-metrics/client-id=([-.w]+)|	초당 수신된 평균 응답 수	|Work: Throughput|
|request-rate|	kafka.producer:type=producer-metrics/client-id=([-.w]+)|	초당 전송된 평균 요청 수|	Work: Throughput|
|request-latency-avg|	kafka.producer:type=producer-metrics/client-id=([-.w]+)|	평균 요청 지연 시간 (밀리초)|	Work: Throughput|
|outgoing-byte-rate|	kafka.producer:type=producer-metrics/client-id=([-.w]+)|	초당 평균 나가는/들어오는 바이트 수|	Work: Throughput|
|io-wait-time-ns-avg|	kafka.producer:type=producer-metrics/client-id=([-.w]+)|	I/O 쓰레드가 소켓을 기다리는 데 소비한 평균 시간 |	Work: Throughput|
|batch-size-avg|	kafka.producer:type=producer-metrics/client-id=([-.w]+)|	요청당 파티션당 전송된 평균 바이트 수|	Work: Throughput|

### Metric to watch: Compression rate

- 이 메트릭은 생상자가 브로커에게 보내는 데이터 배치의 데이터 압축 비율을 반영한다. 
- 압축률이 높을수록 효율성이 높아진다. 
- 이 메트릭이 떨어지면, 데이터 형태에 문제가 있거나 불량 생산자가 압축되지 않는 데이터를 보내고 있음을 나타낼 수 있다. 

### Metric to watch: Response rate

- 생상자에서 응답율은 브로커로 부터 받은 응답률을 나타낸다. 
- 브로커는 데이터가 수신되면 생산자에게 응답한다. 
- 구성에 따라 "received"는 다음 세가지 의미중에 하나이다. 
  - 메시지가 수신되었으나 아직 커밋되지 않았다. (request.required.ack == 0)
  - 리더가 메시지를 디스크에 썼다.(request.required.ack == 1)
  - 리더는 모든 복제본이 데이터를 디스크에 썼다는 것을 확인 받았다.(request.required.acks == all)

- 생산자 데이터는 필요한 수의 승인을 받을 때까지 사용할 수없다. 
- 응답률이 낮다면 여러가지 요인이 작용할 수 있다. 
- 시작하기 좋은 장소는 브로커에서 request.required.acks 구성 지시문을 확인하는 것이다. 
- request.required.ack에 대한 올바른 값을 선택하는 것은 전적으로 사용 사례에 따라 다르다. 
- 일관성을 위해 가용성을 교환할지 여부는 사용자에게 달려있다. 

![request-response-rate](imgs/request-response-rate.avif)

### Metric to watch: Request rate

- 요청 속도는 생산자가 브로커에게 데이터를 보내는 속도이다. 
- 물론 정상적인 요청 비율을 구성하는 것은 사용 사례에 따라 크게 달라진다. 
- 지속적인 서비스 가용성을 보장하려면 피크와 드롭을 주시하는 것이 필수적이다. 
- 속도 제한이 활성화되지 않은 경우 트래픽 급증 시 브로커가 빠른 데이터 유입을 처리하는 데 어려움을 격기 때문에 크롤링 속도가 느려질 수 있다. 

### Metric to watch: Request latency average

- 평균 요청 대기 시간은 생산자가 브로커로 부터 응답을 받을 때까지 KafkaProducer.send()가 호출된 시간 사이의 측정값이다. 
- 이 컨텍스트에서 "받음"은 응답률에 대한 단락에서 설명한 것처럼 여러 가지를 의미할 수 있다. 

- 생산자는 메시지가 생성되자마다 각 메시지를 보낼 필요는 없다. 
- 생산자의 linger.ms 값은 메시지 일괄 처리를 보내기 전에 대기할 최대 시간을 결정하므로 단일 요청으로 보내기 전에 더 많은 메시지 일괄 처리를 누적할 수 있다. 
- linger.ms의 기본값은 0ms 이다. 
- 이 값을 더 높은 값으로 설정하면 대기 시간이 늘어날 수 있지만 생산자가 각 메시지에 대해 네트워크 오버헤드를 발생시키지 않고 여러 메시지를 보낼 수 있으므로 처리량을 개선하는 데도 도움이 될 수 있다. 
- 카프카 배포의 처리량을 개선하기 위해 linger.ms를 늘리는 경우 요청 대기 시간을 모니터링하여 허용 가능한 한도를 초과하지 않도록 해야한다. 

- 대기 시간은 처리량과 강한 상관 관계가 있으므로 생산자 구성에서 batch.size를 수정하면 처리량이 크게 향상될 수 있다는 점을 언급할 가치가 있다. 
- 최적의 배치 크기를 결정하는 것은 사용 사례에 따라 다르지만 일반적으로 사용 가능한 메모리가 있는 경우 배치 크기를 늘려야 한다. 
- 구성하는 배치 크기는 상한선임을 명심하라. 
- 소규모 배치에는 더 많은 네트워크 왕복이 필요하므로 처리량이 감소할 수 있다. 

![request-avg-lat](imgs/request-avg-lat.avif)

### Metric to watch: Outgoing byte rate

- 카프카 브로커와 마찬가지로 생산자 네트워크 처리량을 모니터링하고 싶을 것이다. 
- 시간 경과에 따른 트래픽 양을 관찰하는 것은 네트워크 인프라를 변경해야 하는지 여부를 결정하는 데 필수적이다. 
- 생산자 네트워크 트래픽을 모니터링하면 인프라 변경에 대한 결정을 내리는데 도움이 될 뿐만 아니라 생산자의 생산 속도에 대한 창을 제공하고 과도한 트래픽의 소스를 식별하는데 도움이 된다. 

### Metric to watch: I/O wait time

- 생산자는 일반적으로 데이터 대기 및 데이터 전송의 두 가지 작업중 하나를 수행한다. 
- 생산자가 보낼 수 있는 것보다 더 많은 데이터를 생성하는 경우 네트워크 리소스를 기다리게 된다. 
- 그러나 생산자가 속도 제한을 받지 않거나 대역폭을 최대화 하지 않으면 병목 현상을 식별하기가 더 어려워진다. 
- 디스크 액세스는 처리 작업의 가장 느린 부분인 경향이 있으므로 생산자의 I/O 대기 시간을 확인하는 것이 좋은 출발점이다. 
- I/O 대기는 CPU가 유휴 상태인 동안 I/O를 수행하는 데 소요된 시간의 백분율을 나타낸다. 
- 대기 시간이 너무 많다는 것은 생산자가 필요한 데이터를 충분히 빨리 얻을 수 없다는 의미이다. 
- 스토리지 백엔드에 기존 하드 드라이브를 사용하는 경우 SSD를 고려할 수 있다. 

### Metric to watch: Batch size

- 네트워크 리소스를 보다 효율적으로 사용하기 위해 카프카 생산자는 메시지를 보내기 전에 일괄 처리로 그룹화 하려고 시도한다. 
- 생산자는 batch.size에 정의된 데이터 양(기본값은 16KB)을 누적하기 위해 기다리지만 linger.ms값(기본 값은 0밀리초)보다 오래 기다리지 않는다. 
- 생산자가 보낸 배치의 크기가 구성된 batch.size보다 지속적으로 작은 경우 생산자가 머뭇거릴 때마다 도착하지 않은 추가 데이터를 기다리는 시간이 낭비된다. 
- 배치 크기 값이 구성된 batch.size보다 작은 경우 linger.ms 설정을 줄이는 것이 좋다. 

## Kafka consumer metrics

![kafka-diagram-consumer](imgs/kafka-diagram-consumer.avif)

|JMX attribute|	MBean name|	Description|	Metric type|
|---|---|---|---|
|records-lag|	kafka.consumer:type=consumer-fetch-manager-metrics/client-id=([-.w]+)/topic=([-.w]+)/partition=([-.w]+)|	이 파티션의 생산자 뒤에 있는 메시지 소비자 수|	Work: Performance|
|records-lag-max|	kafka.consumer:type=consumer-fetch-manager-metrics/client-id=([-.w]+)/topic=([-.w]+)/partition=([-.w]+)|
|kafka.consumer:type=consumer-fetch-manager-metrics|client-id=([-.w]+)/	특정 파티션 또는 이 클라이언트의 모든 파티션에 대해 생산자 뒤에 있는 최대 메시지 소비자 수|	Work: Performance|
|bytes-consumed-rate|	kafka.consumer:type=consumer-fetch-manager-metrics/client-id=([-.w]+)/topic=([-.w]+)|
|kafka.consumer:type=consumer-fetch-manager-metrics|client-id=([-.w]+)/	특정 주제 또는 모든 주제에 대해 초당 소비된 평균 바이트 수이다.|	Work: Throughput|
|records-consumed-rate|	kafka.consumer:type=consumer-fetch-manager-metrics/client-id=([-.w]+)/topic=([-.w]+)|
|kafka.consumer:type=consumer-fetch-manager-metrics|client-id=([-.w]+)/	특정 주제 또는 모든 주제에 대해 초당 소비된 평균 레코드 수|	Work: Throughput|
|fetch-rate|	kafka.consumer:type=consumer-fetch-manager-metrics/client_id=([-.w]+)/	소비자의 초당 가져오기 요청 수|	Work: Throughput|

### Metrics to watch: Records lag/Records lag max

- 레코드 지연은 소비자의 현재 로그 오프셋과 생산자의 현재 로그 오프셋 간의 계산된 차이다. 
- 최대 레코드 지연은 관찰된 레코드 지연의 최대 값이다. 
- 이러한 측정항목 값의 중요성은 전적으로 소비자가 하는 일에 달려 있다. 
- 만약 오래된 메시지를 장기 저장소에 백업하는 소비자가 있는 경우 레코드 지연이 상당할 것으로 예상할 수 있다. 
- 그러나 소비자가 실시간 데이터를 처리하는 경우 지속적으로 높은 지연 값은 과부하된 소비자의 신호일 수 있다. 
- 이 경우 더 많은 소비자를 프로비저닝하고 더 많은 파티션에 주제를 분할하면 처리량을 늘리고 지연을 줄이는 데 도움이 될 수 있다. 

![kafka-consumer-lag](imgs/kafka-consumer-lag.avif)

### Metric to watch: bytes consumed rate

- 생산자 및 브로커와 마찬가지로 소비자 네트워크 처리량을 모니터링 하고 싶을 것이다. 
- 예를 들어, 레코드 소비 비율(records-consumed-rate) 이 값자기 떨어지면 소비자가 실패했음을 나타낼 수 있지만 네트워크 처리량(bytes-consumed-rate)이 일정하게 유지된다면 여전히 헬시하다. (낮은 컨슈며, 큰 크기의 메시지)
- 다른 메트릭의 맥락에서 시간 경과에 따른 트래픽 볼륨을 관찰하는 것은 비정상적인 네트워크 사용을 진단하는 데 중요하다. 

### Metric to watch: records consumed rate

- 각 카프카 메시지는 단일 데이터 레코드이다.
- 메시지의 크기가 가변적일 수 있기 때문에 초당 소비되는 레코드 비율은 소비된 바이트 비율과 밀접한 상관관계가 없을 수 있다. 
- 생산자와 워크로드에 따라 일반적인 배포에서는 이 수치가 상당히 일정하게 유지될 것으로 예상해야한다. 
- 시간이 지남에 따라 이 지표를 모니터링 하여 데이터 소비 추세를 발견하고 경고할 수 있는 기준선을 만들 수 있다. 

![consumed-message](imgs/consumed-messages.avif)

### Metric to watch: fetch rate

- 소비자의 가져오기 비율은 전반적인 소비자 헬스 상태의 좋은 지표가 될 수 있다. 
- 0 값에 접근하는 최소 가져오기 속도는 잠재적인 소비자에게 문제를 알릴 수 있다. 
- 정상적인 소비자의 경우 최소 가져오기 비율은 일반적으로 0이 아니므로 이 값이 떨어질 경우 소비자 실패의 신호일 수 있다. 

## Why Zookeeper

- ZooKeeper는 Kafka 배포에서 중요한 역할을 한다. 
- 카프카의 브로커 및 주제에 대한 정보를 유지 관리하고, 배포를 통해 이동하는 트래픽 속도를 제어하기 위해 할댱량을 적용하고, 배포 상태가 변경될 때 Kafka가 파티션 리더를 선택할 수 있도록 복제본에 대한 정보를 저장한다. 
- ZooKeeper는 Kafka 배포의 중요한 구성 요소이며 ZooKeeper 가 중단되면 Kafka가 중지된다. 
- 안정적인 카프카 클러스터를 실행하려면 앙상블이라는 고가용성 구성으로 ZooKeeper를 배포해야한다. 
- 그러나 앙상블을 실행하든 단일 ZooKeeper 호스트를 실행하든 ZooKeeper를 모니터링하는 것은 건강한 Kafka클러스터를 유지하는 데 중요하다. 

![kafka-diagram-zookeeper](imgs/kafka-diagram-zookeeper.avif)

## ZooKeeper metrics

- ZooKeeper 는 MBean을 통해, 4글자 단어를 사용하는 명령줄 인터페이스를 통해, AdminServer에서 제공하는 HTTP 끝점으로 메트릭을 노출한다. 

|Name|	Description|	Metric type|	Availability|
|---|---|---|---|
|outstanding_requests|	대기중인 요청수|	Resource: Saturation|	Four-letter words/ AdminServer/ JMX|
|avg_latency|	클라이언트 요청에 응답하는 데 걸리는 시간(ms)|	Work: Throughput|	Four-letter words/ AdminServer/ JMX|
|num_alive_connections|	ZooKeeper에 연결된 클라이언트 수|	Resource: Availability|	Four-letter words/ AdminServer/ JMX|
|followers|	활성 팔로워 수|	Resource: Availability|	Four-letter words/ AdminServer|
|pending_syncs|	팔로워의 보류 중인 동기화 수|	Other|	Four-letter words/ AdminServer/ JMX|
|open_file_descriptor_count|	사용 중인 파일 설명자 수|	Resource: Utilization|	Four-letter words/ AdminServer|

### Metric to watch: Outstanding requests

![outstanding-requests](imgs/outstanding-requests.avif)

- 클라이언트는 ZooKeeper가 처리할 수 있는 것보다 더 빨리 요청을 제출할 수 있다. 
- 많은 수의 클라이언트가 있는 경우 이러한 일이 가끔 발생하는 것은 거의 당연하다. 
- 대기중인 요청으로 인해 사용 가능한 모든 메모리를 사용하는 것을 방지하기 위해 ZooKeeper는 ZooKeeper의 zookeeper.globalOutstandingLimit설정(기본값은 1,000)에 정의된 대기열 제한에 도달하면 클라이언트를 제한한다. 
- 요청이 처리되기를 기다리면 보고된 평균 지연 시간에 상관 관계가 표시된다. 
- 미해결 요청과 대기 시간을 모두 추적하면 성능 저하의 원인을 더 명확하게 파악할 수 있다. 

#### Metric to watch: Average latency

- 평균 요청 대기 시간은 ZooKeeper가 요청에 응답하는 데 걸리는 평균 시간(밀리초)이다.
- 주키퍼는 트랜잭션 로그에 트랜잭션을 기록할 때까지 요청에 응답하지 않는다. 
- ZooKeeper 앙상블의 성능이 저하되면 평균 지연 시간을 미해결 요청 및 보류 중인 동기화(아래에서 더 자세히 설명)와 연관시켜 속도 저하의 원인에 대한 통찰력을 얻을 수 있다. 
  
![zookeeper-average-latency](imgs/zookeeper-average-latency.avif)

#### Metric to watch: Number of alive connections

- 주키퍼는 num_alive_connections 메트릭을 통해 연결된 클라이언트 수를 보고한다. 
- 이는 주키퍼가 아닌 노드에 대한 연결을 포함하여 모든 연결을 나타낸다.
- 대부분의 환경에서 이 숫자는 상당히 정적으로 유지 되어야한다. 
- 일반적으로 소비자, 생산자, 브로커 및 주키퍼 노드의 수는 비교적 안정적으로 유지되어야 한다. 
- 이 값의 예상치 못한 하락을 알고 있어야한다. 
- 카프카는 주키퍼를 사용하여 작업을 조정하기 때문에 주키퍼에 대한 연결이 끊어지면 연결이 끊긴 클라이언트에 따라 다양한 효과가 나타날 수 있다. 

#### Metric to watch: Followers (leader only)

- 팔로어 수는 주키퍼 앙상블의 총 크기에서 1을 뺀 것과 같아야한다. 
- (리더는 팔로워 수에 포함되지 않는다.) 앙상블의 크기는 사용자 개입으로 인해 변경되어야 하므로 (예: 관리자가 노드를 폐기하거나 위임한 경우) 이 값이 변경되면 경고해야한다.

#### Metric to alert on: Pending syncs (leader only)

- 트랜잭션 로그는 주키퍼에서 가장 성능이 중요한 부분이다. 
- 주키퍼는 응답을 반환하기 전에 트랜잭션을 디스크에 동기화해야 하므로 보류 중인 동기화가 많으면 지연 시간이 늘어난다. 
- 동기화가 수행될 때까지 주키퍼가 요청을 처리할 수 없기 때문에 장기간 미해결 동기화 후에 성능이 저하될 것이다. 
- 10보다 큰 pending_syncs값에 대한 경고를 고려해야한다. 
  
#### Metric to watch: Open file descriptor count

- 주키퍼는 파일 시스템의 상태를 유지하며 각 znode는 디스크의 하위 디렉토리에 해당한다. 
- 리눅스에는 제한된 수의 파일 디스크립터를 사용할 수 있다. 
- 이것은 구성 가능하므로 이 메트릭은 시스템의 구성된 제한과 비교하고 필요에 따라 제한을 늘려야한다. 
  
### ZooKeeper system metrics

- 주키퍼 자체에서 내보낸 메트릭 외에도 몇 가지 호스트 수준 주키퍼 메트릭을 모니터링할 가치가 있다. 
  
|Name|	Description|	Metric type|
|---|---|---|
|Bytes sent/received|	주키퍼 호스트에서 송수신한 바이트 수 |	Resource: Utilization|
|Usable memory|	주키퍼에서 사용 가능한 미사용 메모리 양|	Resource: Utilization|
|Swap usage|	주키퍼가 사용하는 스왑 공간의 양|	Resource: Saturation|
|Disk latency|	데이터 요청과 디스크에서 데이터 반환 사이의 시간지연|	Resource: Saturation|

#### Metric to watch: Bytes sent/received

- 많은 소비자와 파티션이 있는 대규모 배포에서 ZooKeeper는 클러스터의 변화하는 상태를 기록하고 전달하므로 병목 현상이 발생할 수 있다. 
- 시간 경과에 따라 주고받은 바이트 수를 추적하면 성능 문제를 진단하는 데 도움이 될 수 있다. 
- 주키퍼 앙상블의 트래픽이 증가하는 경우 더 많은 볼륨을 수용할 수 있도록 더 많은 노드를 프로비저닝 해야한다.

#### Metric to watch: Usable memory

- 주키퍼는 RAM에 완전히 상주해야 하며 디스크로 페이징해야 하는 경우 상당한 어려움을 겪을 것이다.
- 따라서 주키퍼가 최적의 성능을 발휘하도록 하려면 사용 가능한 메모리 양을 추적해야한다. 
- 주키퍼는 상태를 저장하는 데 사용되기 때문에 주키퍼 성능의 저하가 클러스터 전체에서 느껴진다. 
- 주키퍼 노드로 프로비저닝된 시스템에는 로드급증을 처리할 수 있는 충분한 메모리 버퍼가 있어야한다. 
  
#### Metric to alert on: Swap usage

- 주키퍼의 메모리가 부족하면 교체해야 하므로 속도가 느려진다. 
- 더 많은 메모리를 프로비저닝 할 수 있도록 스왑 사용에 대해 경고해야한다. 
  
#### Metric to watch: Disk latency

- 주키퍼는 RAM에 있어야 하지만 현재 상태를 주기적으로 스냅샷하고 모든 트랜잭션의 로그를 유지하기 위해 파일 시스템을 사용한다. 
- 업데이트가 발생하기 전에 ZooKeeper가 비휘발성 저장소에 트랜잭션을 작성해야 하므로 디스크 액세스가 잠재적인 병목 현산이 발생한다. 
- 디스크 대기 시간이 급증하면 주키퍼와 통신하는 모든 호스트의 서비스가 저하되므로 앙상블에 SSD를 장착하는 것 외에도 디스크 대기 시간을 확실히 주지해야한다. 
  
## Monitor your Kafka deployment

- 이 포스트에서 우리는 카프카 클러스터의 상태와 성능을 파악하기 위해 모니터링 해야하는 여러 주요 메트릭을 살펴 보았다. 
- 메시지 대기열로 카프카는 절대 진공 상태에서 실행되지 않는다. 
- 결국 자체 카프카 클러스터 및 해당 사용자와 특히 관련이 있는 보다 전문화된 추가 메트릭을 인식하게 될 것이다. 
- 이 시르즈에서 Datadog을 사용하여 Kafka클러스터의 상태에 대한 완전한 가시성을 얻을 수 있도록 추적 및 로그뿐만 아니라 중요한 Kafka메트릭을 수집하는 방법을 보여준다. 
- 이 기사에 설명된 모든 메트릭과 Kafka에서 노출된 기타 메트릭을 수집하는 포괄적인 가이드를 읽어라.