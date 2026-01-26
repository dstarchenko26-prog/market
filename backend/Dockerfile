# Етап 1: Збірка (Build)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Копіюємо тільки файли залежностей (щоб закешувати цей шар)
COPY pom.xml .
RUN mvn dependency:go-offline

# Копіюємо вихідний код і збираємо
COPY src ./src
RUN mvn clean package -DskipTests

# Етап 2: Запуск (Run)
# Використовуємо легший образ JRE, бо Maven нам вже не треба
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копіюємо зібраний jar з першого етапу
COPY --from=build /app/target/*.jar app.jar

# Відкриваємо порт
EXPOSE 8080

# Запускаємо
ENTRYPOINT ["java", "-jar", "app.jar"]