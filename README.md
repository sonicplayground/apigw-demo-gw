# apigateway

모듈명:
- apigateway
- 간략 설명: 테스트용 API Gateway 프로토타입입니다. 간단한 헤더 기반 토큰 파싱으로 인증을 처리하고 경로 재작성 예시를 포함합니다.

라우트별 규칙 설명

1) product-query-service
- path: /products/query/**
- predicate: Path=/products/query/**, Method=GET
- uri: http://localhost:8081
- filters: RewritePath=/products/query(?<segment>.*) -> /products${segment}
- 동작: 클라이언트의 GET 요청을 /products/query/...에서 수신하여 뒤쪽 서비스의 /products/...로 재작성 후 전달합니다.
- 예시: GET /products/query/123 -> Gateway는 내부적으로 http://localhost:8081/products/123 로 요청 포워딩

2) product-service
- path: /products/**
- predicate: Path=/products/**, Method=POST
- uri: http://localhost:8081
- filters: TokenParsingFilter
- 동작: POST 요청 시 Authorization 헤더를 확인하여 토큰을 파싱하고 유효성을 검사합니다. 유효한 경우 토큰에서 추출한 userId를 X-User-Id 헤더로 뒤쪽 서비스에 추가하여 전달합니다. 유효하지 않거나 없으면 401 Unauthorized 응답(JSON) 반환.
- 토큰 포맷 (필요한 형태): <prefix>-<userId>-<timestampMillis>
  - 예: Bearer-42-1690000000000
  - 타임스탬프는 밀리초 단위이며, 생성 시점으로부터 30분(= 30 * 60 * 1000ms) 이내여야 함
- 예시: POST /products  (헤더: Authorization: Bearer-42-1690000000000) -> Gateway는 X-User-Id: 42 헤더를 추가하여 http://localhost:8081/products 로 포워딩

참고: 프로젝트에 ReqRespLogFilter 클래스( src/main/java/com/avis/apigateway/filters/ReqRespLogFilter.java )가 GlobalFilter로 구현되어 있으며 @Component로 빈 등록되어 있으므로, application.yaml에 명시하지 않아도 모든 라우트에 전역 필터로 자동 적용됩니다. 또한 getOrder()가 -1로 설정되어 있어 라우트별 필터보다 우선적으로 실행됩니다.

기술 스택 (간단)
- 언어/플랫폼: Java 17
- 프레임워크: Spring Boot 3.5.5
- Spring Cloud Gateway (Spring Cloud BOM: 2025.0.0)
- 빌드: Gradle (wrapper 사용)

빌드/실행 (간단)
- 의존성/빌드: ./gradlew build
- 실행(개발): ./gradlew bootRun
- 실행(생성된 JAR): java -jar build/libs/*.jar

운영 주의사항
- 뒤쪽(백엔드) 서비스는 설정된 URI(http://localhost:8081)에서 실행되어 있어야 합니다.
- TokenParsingFilter 로직은 간단한 프로토타입용 검증을 수행하므로 실제 배포 전 보안 요구사항에 맞게 교체/강화해야 합니다.

간단한 파일 위치
- 애플리케이션 설정: src/main/resources/application.yaml
- 커스텀 필터: src/main/java/com/avis/apigateway/filters/TokenParsingFilter.java
