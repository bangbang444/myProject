## 🍽️ Gourmet
맛집 리뷰 SNS입니다. <br>
사용자는 맛집을 검색하고 리뷰를 등록할 수 있습니다. <br>
또한 식당 리뷰로 피드를 공유하고 사람들과 소통할 수 있습니다. <br>
식당 정보는 크롤링으로 맛집 데이터를 자동 수집합니다. <br>

## 주요 기능
- 카카오 OAuth 소셜 로그인
- 네이버 지도 크롤링 기반 맛집 자동 수집
- 맛집 검색, 리뷰 작성, 댓글
- 팔로우 · 좋아요 소셜 피드
- FCM 실시간 푸시 알림

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
* **Monitoring:** Promtail, Loki, Prometheus, Grafana, Node Exporter

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
<img width="15460" height="8368" alt="Project  Architecture (1)" src="https://github.com/user-attachments/assets/1052f93c-0b62-46ee-a165-52f92b305f3b" />


