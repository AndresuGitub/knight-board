FROM openjdk:17
WORKDIR /app
COPY . /app
RUN chmod +x build.sh && ./build.sh
CMD ["java", "-cp", "out:lib/json-20210307.jar", "KnightPathJR"]