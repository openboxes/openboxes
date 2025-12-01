# This image is built on Ubuntu 22.04, which has LTS support until April 2027
FROM azul/zulu-openjdk:8-jre

LABEL maintainer="support@openboxes.com"
EXPOSE 8080

# Install curl for healthcheck
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

# Default flags for the JVM. These can be replaced at the runtime.
ENV JAVA_TOOL_OPTIONS="-Xms1024m -Xmx1024m -XX:+UseParallelGC -Djava.awt.headless=true"

# Create openboxes user and group
RUN useradd --user-group --shell /bin/false  --home-dir /app openboxes
WORKDIR /app

# Create and chown 'uploads' directory for the OB app
RUN mkdir uploads && chown openboxes:openboxes uploads/

# Copy and chown the WAR file
COPY --chown=openboxes:openboxes build/libs/openboxes.war openboxes.war

USER openboxes

# Directly execute the WAR file, which runs the application with an embedded Tomcat servlet
CMD ["java","-Dgrails.env=prod","-jar","/app/openboxes.war"]
