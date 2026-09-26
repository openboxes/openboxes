# OpenBoxes Docker Image
# Runs OpenBoxes (open-source supply chain management) on Tomcat 9 with JDK 11
#
# Build:
#   docker build -t openboxes .
#   docker build --build-arg OPENBOXES_VERSION=v0.9.6-hotfix1 -t openboxes:0.9.6 .
#
# Run (with external database):
#   docker run -d -p 8080:8080 \
#     -e OPENBOXES_DB_HOST=mariadb \
#     -e OPENBOXES_DB_NAME=openboxes \
#     -e OPENBOXES_DB_USERNAME=openboxes \
#     -e OPENBOXES_DB_PASSWORD=secret \
#     openboxes

FROM tomcat:9-jdk11-temurin

LABEL org.opencontainers.image.title="OpenBoxes"
LABEL org.opencontainers.image.description="Open-source supply chain management system"
LABEL org.opencontainers.image.url="https://openboxes.com"
LABEL org.opencontainers.image.source="https://github.com/openboxes/openboxes"
LABEL org.opencontainers.image.licenses="EPL-1.0"

# Remove default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Download OpenBoxes WAR from GitHub releases
ARG OPENBOXES_VERSION=v0.9.6-hotfix1
ADD https://github.com/openboxes/openboxes/releases/download/${OPENBOXES_VERSION}/openboxes.war \
    /usr/local/tomcat/webapps/ROOT.war

# Create directories for config, data, and logs
RUN mkdir -p /app/data/uploads /app/data/documents /app/logs && \
    chmod 644 /usr/local/tomcat/webapps/ROOT.war

# Copy configuration template and entrypoint
COPY openboxes-config.properties /app/config/openboxes-config.properties.template
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# JVM tuning for OpenBoxes
# OpenBoxes uses ~1.2-1.4GB during startup (Liquibase migrations), settling to ~1GB steady state.
# Adjust -Xmx based on available memory. Minimum recommended: 1.5GB.
ENV CATALINA_OPTS="-Xms512m -Xmx1536m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager"

# Database connection defaults (override at runtime)
ENV OPENBOXES_DB_HOST="localhost" \
    OPENBOXES_DB_PORT="3306" \
    OPENBOXES_DB_NAME="openboxes" \
    OPENBOXES_DB_USERNAME="openboxes" \
    OPENBOXES_DB_PASSWORD="" \
    OPENBOXES_SERVER_URL="http://localhost:8080"

EXPOSE 8080

# OpenBoxes takes 2-3 minutes to start on first run (Liquibase migrations).
# start-period accounts for this; subsequent health checks are faster.
HEALTHCHECK --interval=30s --timeout=10s --start-period=300s --retries=3 \
    CMD curl -sf http://localhost:8080/ || exit 1

VOLUME ["/app/data"]

ENTRYPOINT ["/docker-entrypoint.sh"]
