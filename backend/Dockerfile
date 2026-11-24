# Etapa 1: compilar o projeto e gerar o .jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia só o pom primeiro: as dependências ficam em cache se o pom não mudar
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# Etapa 2: imagem final, só com o Java necessário para executar
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuário sem privilégios: a aplicação não roda como root
RUN useradd --system --create-home appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]