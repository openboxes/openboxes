#!/usr/bin/env bash

set -e

while :; do
  echo -n "Provide openbox db name: "
  read DATABASE_NAME

  if mysql -Nse 'SHOW DATABASES;' | grep "${DATABASE_NAME}" -q; then
    break
  else
    echo "Database ${DATABASE_NAME} does not exist. Try again"
  fi
done

# User confirmation
echo "Are you sure you want to delete data from ${DATABASE_NAME}?"
echo -n "y/N: "
read ANSWER
if [[ ${ANSWER} != "y" && ${ANSWER} != "Y" ]]; then
  exit
fi

# Dump
DUMP_FILE=./${DATABASE_NAME}_$(date +%Y%m%d_%H%M%S_%Z).mysqldump.sql
echo "Dumping ${DATABASE_NAME}"
mysqldump "${DATABASE_NAME}" >"${DUMP_FILE}"
echo "Dump saved at ${DUMP_FILE}"

# Recreate
SQL="DROP DATABASE ${DATABASE_NAME};"
SQL="${SQL} CREATE DATABASE ${DATABASE_NAME} DEFAULT CHARSET utf8;"
mysql -e "${SQL}"
echo "Database recreated"

# Grant privileges
echo "Do you want grant all privileges to app db user?"
echo -n "y/N: "
read ANSWER

if [[ ${ANSWER} == "y" || ${ANSWER} == "Y" ]]; then
  echo -n "Login: "
  read LOGIN
  echo -n "Host: "
  read HOST

  APP_DATABASE_USER=${LOGIN}@${HOST}

  mysql -e "GRANT ALL ON ${DATABASE_NAME}.* to ${APP_DATABASE_USER};"
  echo "All privileges granted to ${APP_DATABASE_USER}"
fi

echo "Finished resetting. You can now restart openboxes."
echo "If you are using tomcat, run: sudo service tomcat restart"
