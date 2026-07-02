# Maven 빌드 스테이지
FROM maven:3-eclipse-temurin-17 AS builder

WORKDIR /app

# pom.xml만 복사하여 의존성 다운로드
COPY pom.xml .
RUN mvn dependency:go-offline

# 소스 코드 복사 및 빌드
COPY src ./src
RUN mvn clean package -DskipTests

# Tomcat 배포 스테이지
FROM tomcat:10-jre17-temurin

# Tomcat 기본 기동 포트를 8080에서 10000으로 내부 변경 및 셧다운 포트 비활성화
RUN sed -i 's/port="8080"/port="10000"/g' /usr/local/tomcat/conf/server.xml && \
    sed -i 's/port="8005"/port="-1"/g' /usr/local/tomcat/conf/server.xml

# 빌드된 WAR 파일을 Tomcat의 webapps 디렉토리에 복사
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 10000

CMD ["catalina.sh", "run"]