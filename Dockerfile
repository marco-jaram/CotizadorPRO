# Usa una imagen base con una versión de Java que coincida con tu proyecto
FROM openjdk:17-jdk-slim

# Argumento para especificar la ruta del archivo JAR
ARG JAR_FILE=target/*.jar

# Copia el archivo JAR construido por Maven al contenedor
COPY ${JAR_FILE} app.jar

# Expone el puerto que usa tu aplicación Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación cuando el contenedor se inicie
ENTRYPOINT ["java","-jar","/app.jar"]