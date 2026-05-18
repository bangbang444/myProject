## 🛠 기술 스택

### 💻 백엔드 및 빌드
* **Language:** Java 17
* **Framework:** Spring Boot 3.5.6 (Web, JPA, WebFlux)
* **Build Tool:** Gradle

### 🗄️ 데이터베이스 및 보안
* **Main DB:** MySQL (Production), H2 (Test)
* **Auth & Cache:** Redis (Refresh Token & Session Management)
* **Security:** JWT (jjwt 0.11.5)
### 🏗️ 인프라 및 배포 (Self-Hosted Home Server)
* **CI/CD:** Jenkins
* **Deployment:** Docker Compose
* **Object Storage:** Garage (S3 Compatible Self-hosted Storage)
* **Monitoring:** Prometheus, Grafana, Node Exporter

### 🧪 테스트 전략
* **단위 테스트:** `Mockito` 기반 Service 레이어 단위 테스트
* **통합 테스트:** `H2` 인메모리 DB 기반 Spring 컨텍스트 통합 검증
* **동시성 테스트:** `CountDownLatch` & `ExecutorService` 활용 정합성 검증
* **부하 테스트:** `k6` 기반 부하 테스트를 통한 API 가용성(RPS) 및 응답 속도(Latency) 측정

### 🌐 외부 연동 및 자동화
* **Automation:** Playwright (Naver Maps Crawler)
* **Messaging:** Firebase (FCM을 활용한 실시간 푸시 알림 구현)
* **AI Engine:** Spring AI
* **Monitoring:** Discord Webhook (실시간 4xx, 5xx 에러 알림)

### 아키텍처
<img width="15460" height="8368" alt="Project  Architecture" src="https://github.com/user-attachments/assets/561ea15a-280b-492f-8997-c23196f66c1c" />


