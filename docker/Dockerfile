FROM openjdk:8u292-jre-slim-buster

LABEL maintainer="support@openboxes.com"
EXPOSE 8080

# Install curl for healthcheck
RUN apt-get update && apt-get install curl -y

# Default flags for the JVM. These can be replaced at the runtime.
ENV JAVA_TOOL_OPTIONS="-Xms1024m -Xmx1024m -XX:+UseParallelGC -Djava.awt.headless=true"

# Create openboxes user and group
RUN useradd --user-group --shell /bin/false  --home-dir /app openboxes
WORKDIR /app

# Create and chown 'uploads' directory for the OB app
RUN mkdir uploads && chown openboxes:openboxes uploads/

# Copy and chown the WAR file
COPY --chown=openboxes:openboxes openboxes.war openboxes.war

USER openboxes
CMD ["java","-Dgrails.env=prod","-jar","/app/openboxes.war"]
