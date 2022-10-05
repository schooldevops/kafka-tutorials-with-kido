# Monitoring Kafka with Datadog

from: https://www.datadoghq.com/blog/monitor-kafka-with-datadog/

- 카프카배포는 종종 kafka 코드베이스 자체에 포함되지 않은 소프트웨어 패키지, 특히 아파치 주키퍼에 의존한다. 
- 포괄적인 모니터링 구현에는 모든 계층이 포함되어 있으므로 카프카 클러스터와 주키퍼 앙상블은 물론 생산자 및 소비자 애플리케이션과 이를 모두 실행하는 호스트에 대한 가시성을 확보할 수 있다. 
- 지속적이고 의미있는 모니터링을 구현하려면 나머지 인프라의 모니터링 데이터와 함께 Kafka 메트릭, 로그 및 분산 요청 추적을 수집 및 분석할 수 있는 플랫폼이 필요하다. 

![dash](imgs/dash1.avif)

- 데이터독을 사용하면 kafka 배포에서 메트릭, 로그 및 추적을 수집하여 전체 kafka 스택의 성능을 시각화하고 경고할 수 있다. 
- 데이터독은 이 시리즈의 1부에서 논의된 많은 주요 메트릭을 자동으로 수집하고 위에서 본 것처럼 템플릿 대시보드에서 사용할 수 있도록 한다. 

## Integrating Datadog, Kafka, and ZooKeeper

- 이 섹션에서는 Datadog 에이전트를 설치하여 Kafka 배포에서 메트릭, 로그 및 추적을 수집하는 방법을 설명한다. 
- 먼저 카프카와 주키퍼가 JMX 데이터를 보내고 있는지 확인한 다음 각 생상자, 소비자 및 브로커에 데이터독 에이전트를 설치 및 구성해야한다. 

### Verify Kafka and Zookeeper

- 시작하기 전에 Kafka가 JMX를 통해 메트릭을 보고하도록 구성되었는지 확인해야 한다. 
- 카프카가 JMX 데이터를 선택한 JMX 포트로 보내고 있는지 확인한 다음 JConsole을 사용하여 해당 포트에 연결해야한다. 
- 마찬가지로 주키퍼가 JConsole과 연결하여 지정된 포트로 JMX 데이터를 보내고 있는지 확인한다. 1부에서 설명한 MBean의 데이터가 표시되어야 한다. 

### Install the Datadog Agent

- 데이터독 Agent는 오픈소스 소프트웨어로 메트릭을 수집하고, 로그와 분산된 요청을 트레이싱 한다. 
- 이를 통해서 데이터독에서 뷰와 모니터링을 수행할 수 있다. 
- Agent를 설치는 일반적으로 단 하나의 명령만 사용한다. 

- 배포의 각 호스트(Kafka 브로커, 생산자, 소비자는 물론 ZooKeeper 앙상블의 각 호스트)에 에이전트를 설치한다. 
- 에이전트가 시작되고 실행되면 Datadog 계정에서 각 호스트 보고 메트릭이 표시되어야 한다. 

![kafka-host](imgs/kafka-host.avif)

### Configure the Agent

- 다음으로 카프카와 주키퍼 모두에 대한 에이전트 구성 파일을 생성해야 한다. 
- 여기서 OS에 대한 에이전트 구성 디렉토리 위치를 찾을 수 있다. 
- 해당 디렉토리에서 카프카 및 주키퍼에 대한 샘플 구성 파일을 찾을 수 있다. 
- 데이터독으로 Kafka를 모니터링 하려면 카프카 및 카프카 컨수머 에이전트 통합 파일을 모두 편집해야한다. 
- 통합이 함께 작동하는 방식에 대한 자세한 내용은 다음을 참조하자. 
- https://docs.datadoghq.com/integrations/faq/troubleshooting-and-deep-dive-for-kafka/?_gl=1*tpi9ks*_gcl_aw*R0NMLjE2NjQyNTE2MjguQ2p3S0NBandtOFdaQmhCVUVpd0ExNzhVbkpEbmVsNVBWSWlld3A1dGhLY1htcDA1TUdoWlM2dVlHYTB0ak53cjZKX2lFUXdxTUJ3VjNob0NBWmdRQXZEX0J3RQ..*_ga*MTU2NjY1MTUzMC4xNjUyMzQ2Mzg0*_ga_KN80RDFSQK*MTY2NDk0NzA4OS4zMS4xLjE2NjQ5NDcwOTMuNTYuMC4w&_ga=2.112100999.240816678.1664848404-1566651530.1652346384&_gac=1.45680214.1664251629.CjwKCAjwm8WZBhBUEiwA178UnJDnel5PVIiewp5thKcXmp05MGhZS6uYGa0tjNwr6J_iEQwqMBwV3hoCAZgQAvD_BwE#datadog-kafka-integrations
- 카프카 통합을 위한 구성 파일은 kafa.d/ 하위 디렉토리에 있고 카프카 소비자 통합의 구성 파일은 kafka_consumer.d/ 하위 디렉토리에 있다. 
- 주키퍼 통합은 자체 설정 파일을 가지고 있으며 zk.d/ 디렉토리에 존재한다. 

- 각 호스트에서 관련 디렉토리(브로커의 kafka.a/ 및 kafka_consumer.d/ 디렉토리, 주키퍼 호스트의 zk.d/ 디렉토리)에 샘플 YAML 파일을 복사하고 conf.yaml 로 저장한다. 
- kafka.d/conf.yaml 파일은 에이전트가 수집할 카프카 메트릭 목록이 포함되어 있다. 이 파일을 사용하여 에이전트가 브로커, 생산자 및 소비자를 모니터링 하도록 구성할 수 있다. 
- 설정과 일치하도록 호스트 및 포트 값(및 필요한 경우 사용자 및 암호)을 변경한다. 

- yaml 파일에 태그를 추가하여 메트릭에 맞춤 측정기준을 적용할 수 있다. 
- 이를 통해 데이터독에서 카프카 모니터링 데이터를 검색하고 필터링 할 수 있다. 

- 카프카 배포는 여러개의 컴포넌트로 구성되어 있다. 브로커, 프로듀서, 컨슈머. 일부 태그를 사용하여 전체 배포를 식별하고 다른 태그를 사용하여 각 호스트의 역할을 구별하는 것이 도움이 될 수 있다. 
- 아래 샘플 코드에서 role 태그를 사용하여 이 메트릭들은 카프카 브로커로 부터 왓음을 알 수 있다. 
- 그리고 서비스 태그를 사용하여 브로커를 더 넓은 컨텍스트에 배치한다. 
- 서비스 값(signup_processor)은 이 배포의 생산자와 소비자가 공유할 수 있다. 

```go
    tags:
      - role:broker
      - service:signup_processor
```

- 그런 다음 브로커 및 소비자 오프셋 정보를 데이터독에 가져오려면 설정과 일치하도록 kafka_consumer/conf.yaml 파일을 수정한다. 
- 만약 카프카 엔드포인트가 기본과 다르다면 (localhost:9092), 이 파일에서 kafka_connect_str 값을 업데이트해야 한다. 
- 클러스터 내의 특정 소비자 그룹을 모니터링하려면 consumer_groups 값에 지정할 수 있다. 
- 그렇지 않을 경우 monitor_unlisted_consumer_groups를 true로 설정하여 에이전트가 모든 소비자 그룹에서 오프셋 값을 가져오도록 할 수 있다.

#### Collect Kafka and ZooKeeper logs

- 카프카 및 주키퍼에서 로그를 수집하도록 데이터독 에이전트를 구성할 수 있다. 
- 에이전트의 로그 수집은 기본적으로 비활성화 되어 있으므로 먼저 에이전트의 구성 파일을 수정하여 logs_enabled:true 로 설정해야한다. 
- 다음으로 카프마 config.yaml 파일에 로그 섹션의 주석을 제거하고 필요한 경우 브로커의 구성과 일치하도록 수정한다. 
- 주키퍼의 conf.yaml파일에서 동일한 작업을 수행하고, 태그 섹션과 로그 섹션을 업데이트하여 에이전트가 주키퍼 로그를 수집하고 태그를 지정하여 데이터독으로 보내지도록 지시한다. 
- conf.yaml 파일의 양쪽에서 공통 값을 사용하도록 서비스 태그를 수정하여 데이터독이 카프카 배포의 모든 구성요소에서 로그 수집을 할 수 있도록 해야한다. 
- 다음은 이전 섹션에서 카프카 메트릭에 적용한 것과 동일한 서비스 태그를 사용하는 카프카 구성 파일에서 이것이 어떻게 보이는지에 대한 예이다. 

```go
logs:
  - type: file
    path: /var/log/kafka/server.log
    source: kafka
    service: signup_processor
```

- 카프카 구성 파일의 기본 소스값은 카프카이다. 
- 마찬가지로 주키퍼의 구성 파일에는 소스: 주키퍼가 포함되어 있다. 이를 통해 데이터독은 적절한 통합 파이프라인을 적용하여 로그를 구문 분석하고 주요 속성을 추출할 수 있다. 
- 그런다음 signup_processor 서비스의 로그만 표시하도록 로그를 필터링할 수 있으므로 배포에 있는 여러 구성 요소의 로그를 쉽게 상호 연관시켜 신속하게 문제를 해결할 수 있다. 

#### Collect distributed traces

- 데이터독 APM 및 분산 추적은 요청 볼륨 및 대기 시간을 측정하여 서비스 성능에 대한 확장된 가시성을 제공한다. 
- 그래프와 경고를 생성하여 APM 데이터를 모니터링 하고 아래 표시된 것과 같은 플레임 그래프에서 단일 요청의 활동을 시각화 하여 데이 시간및 모너터링하고 아래 표시된 것과 같은 프레임 그래프에서 단일 요청의 활동을 시각화하여 대기 시간 및 오류의 원인을 더 잘 이해할 수 있다 

![datadog-kafka-traces](imgs/datadog-kafka-traces.avif)

- 데이터독 APM은 kafka 클라이언트에 대한 요청을 추적할 수 있으며 인기 있는 언어 및 웹 프레임워크를 자동으로 계측한다. 
- 즉, 생산자와 소비자의 소그 코드를 수정하지 않고도 추적을 수집할 수 있다. APM 및 분산 추적 시작에 대한 지침은 설명서를 참조하라. 


### Verify configuration settings

- 데이터독, 카프카, 주키퍼가 제대로 통합되었는지 확인하려면 먼제 Agent를 다시 시작한 후 status명령을 실행하라. 
- 만약 실정이 올바르다면, 출력에 아래와 유사한 섹션이 포함된다. 

```go
	Running Checks
	======

	  [...]



    kafka_consumer (2.3.0)
    ----------------------
      Instance ID: kafka_consumer:55722fe61fb7f11a [OK]
      Configuration Source: file:/etc/datadog-agent/conf.d/kafka_consumer.d/conf.yaml
      Total Runs: 1
      Metric Samples: Last Run: 0, Total: 0
      Events: Last Run: 0, Total: 0
      Service Checks: Last Run: 0, Total: 0
      Average Execution Time : 13ms


	  [...]

    zk (2.4.0)
    ----------
      Instance ID: zk:8cd6317982d82def [OK]
      Configuration Source: file:/etc/datadog-agent/conf.d/zk.d/conf.yaml
      Total Runs: 1,104
      Metric Samples: Last Run: 29, Total: 31,860
      Events: Last Run: 0, Total: 0
      Service Checks: Last Run: 1, Total: 1,104
      Average Execution Time : 6ms
      metadata:
        version.major: 3
        version.minor: 5
        version.patch: 7
        version.raw: 3.5.7-f0fdd52973d373ffd9c86b81d99842dc2c7f660e
        version.release: f0fdd52973d373ffd9c86b81d99842dc2c7f660e
        version.scheme: semver

========
JMXFetch
========

  Initialized checks
  ==================
    kafka
      instance_name : kafka-localhost-9999
      message : 
      metric_count : 61
      service_check_count : 0
      status : OK
```

### Enable the integration

- 그런다음 데이터독 계정 내 카프카 통합 설정 및 주키퍼 통합 설정의 구성 탭에서 카프카 및 주키퍼 통합 설치 버튼을 클릭한다. 

## Monitoring your Kafka deployment in Datadog

- 에이전트가 배포에서 메트릭 보고를 시작하면 데이터독의 사용 가능한 대시보드 목록에 포괄적인 카프카 대시보드가 표시된다. 
- 이 기사의 맨 위에 있는 기본 카프카 대시보드에는 카프카 모니터링 방법에 대한 소개에서 강조 표시된 주요 메트릭이 표시된다. 
- 다른 시스템의 그래프와 메트릭을 추가하여 전체 웹스택을 모니터링 하는 보다 포괄적인 대시보드를 쉽게 만들 수 있다. 
- 예를 들어 메모리 사용량과 같은 호스트 수준 메트릭 또는 HAProxy의 메트릭과 함께 Kafka 메트릭을 그래프로 표시할 수 있다. 
- 이 대시보드 사용자 지정을 시작하려면 오른쪽 상단의 톱니바퀴를 클릭하고 대시보드 복제.. 를 선택하여 대시보드를 복제한다. 

![clone-dashboard](imgs/clone-dashboard.avif)

- 대시보드에서 그래프를 클릭하면 관련 로그 또는 추적을 빠르게 볼 수 있다. 
- Log Explorer로 이동하여 데이터독으로 모니터링 중인 다른 기술의 로그와 함께 kafka및 주키퍼 로그를 검색하고 필터링 할 수 있다. 
- 아래 스크린샷은 카프카 배포의 로그 스트림을 보여주고 카프카가 구성된 보전 장책에 따라 삭제할 로그 세그먼트를 식별하는 로그를 강조 표시한다. 
- 데이터독 로그 분석을 사용하고 로그 기반 메트릭을 생성하여 전체 기술 스택의 성능에 대한 통찰력을 얻을 수 있다. 

![datadog-kafka-zookeeper-logs](imgs/datadog-kafka-zookeeper-logs.avif)

- 데이터독이 메트릭, 로그 및 APM 데이터를 캡처하고 시각화하면 잠재적인 문제에 대해 자동으로 알림을 받도록 일부 경고를 설정할 수 있다. 
- 강력한 이상값 감지 기능을 사용하면 중요한 사항에 대해 경고를 받을 수 있다.
- 예를 들어 특정 생산자가 지연 시간이 증가하는 반면 다른 생산자는 정상적으로 작동하는 경우 이를 알리도록 경고를 설정할 수 있다. 

## Get started monitoring Kafka with Datadog

- 이 포스트에서 카프카와 대시보드를 통합하여 환경의 주요 메트릭, 로그 및 추적을 모니터링 하는 방법을 안내했다. 
- 자신의 데이터독 계정을 사용하여 따라했다면 이제 카프카 상태 및 성능에 대한 가시성이 향상되었을 뿐만 아니라 인프라, 사용 패턴 및 가장 중요한 데이터에 맞게 자동화된 경고를 생성하는 기능을 갖게 되었을 것이다. 
- 아직 데이터독 계정이 없는 경우 무료 평가판에 등록하고 즉시 카프카 모니터링을 시작할 수 있다. 

