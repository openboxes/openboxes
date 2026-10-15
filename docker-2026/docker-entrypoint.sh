#!/bin/bash
set -e

# OpenBoxes reads its config from ~/.grails/openboxes-config.properties
CONFIG_DIR="${HOME}/.grails"
mkdir -p "$CONFIG_DIR"

# Process the config template, substituting environment variables
cp /app/config/openboxes-config.properties.template "$CONFIG_DIR/openboxes-config.properties"

sed -i "s|OPENBOXES_DB_HOST|${OPENBOXES_DB_HOST:-localhost}|g" "$CONFIG_DIR/openboxes-config.properties"
sed -i "s|OPENBOXES_DB_PORT|${OPENBOXES_DB_PORT:-3306}|g" "$CONFIG_DIR/openboxes-config.properties"
sed -i "s|OPENBOXES_DB_NAME|${OPENBOXES_DB_NAME:-openboxes}|g" "$CONFIG_DIR/openboxes-config.properties"
sed -i "s|OPENBOXES_DB_USERNAME|${OPENBOXES_DB_USERNAME:-openboxes}|g" "$CONFIG_DIR/openboxes-config.properties"
sed -i "s|OPENBOXES_DB_PASSWORD|${OPENBOXES_DB_PASSWORD:-}|g" "$CONFIG_DIR/openboxes-config.properties"
sed -i "s|OPENBOXES_SERVER_URL|${OPENBOXES_SERVER_URL:-http://localhost:8080}|g" "$CONFIG_DIR/openboxes-config.properties"

# Apply any additional connection pool settings
if [ -n "$OPENBOXES_DB_MAX_ACTIVE" ]; then
    sed -i "s|dataSource.properties.maxActive=.*|dataSource.properties.maxActive=${OPENBOXES_DB_MAX_ACTIVE}|g" "$CONFIG_DIR/openboxes-config.properties"
fi

# Allow CATALINA_OPTS override from environment
if [ -n "$EXTRA_CATALINA_OPTS" ]; then
    export CATALINA_OPTS="$CATALINA_OPTS $EXTRA_CATALINA_OPTS"
fi

echo "============================================"
echo "  OpenBoxes starting..."
echo "  Database: ${OPENBOXES_DB_HOST}:${OPENBOXES_DB_PORT}/${OPENBOXES_DB_NAME}"
echo "  Server URL: ${OPENBOXES_SERVER_URL:-http://localhost:8080}"
echo "============================================"

# Start Tomcat
exec catalina.sh run
