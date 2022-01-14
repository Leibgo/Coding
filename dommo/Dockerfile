FROM java:8

COPY *.jar /app.jar

CMD ["--server.port=8383--"]

EXPOSE 8383

ENTRYPOINT ["java","-jar","app.jar"]
