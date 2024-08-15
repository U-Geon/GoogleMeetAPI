# Google Meet API 사용

구글 미트의 자체적인 API가 없어서 Google Calendar API를 사용해서 화상 회의를 생성할 수 있다.

1. Google OAuth2 2.0 인증
2. Google Calendar API를 사용하여 Event 생성
3. Google Meet URL을 발급

---
# 필요

- application-db.yml
```
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/{db이름}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useUnicode=true
    username: {username}
    password: {password}
  jpa:
    database: mysql
    hibernate:
      ddl-auto: create # create none update
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: debug
```

- application-jwt.yml
```
jwt:
  secret-key: {key}
  access-token:
    expiration: 12
  refresh-token:
    expiration: 24
```

- application-oauth.yml
```
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: {client-id}
            client-secret: {secret-key}
            redirect-uri: "http://localhost:8080/login/oauth2/code/google"
            client-name: Google
            scope:
              - email
              - profile
              - https://www.googleapis.com/auth/calendar
```

- credential.json
```
google cloud platform에서 생성
```
