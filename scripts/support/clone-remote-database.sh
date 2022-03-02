#!/bin/bash
set -euo pipefail
start_tm=$(date '+%s')
export TIME="%C %E"

usage()
{
	set +xv
	echo 1>&2 "$0 - clone a remote mysql database to a local machine for testing"
	echo 1>&2 ""
	echo 1>&2 "Usage: $0 [-f] [-l <LOCAL_DB>] [-r <REMOTE_DB>] REMOTE_HOST"
	echo 1>&2 "REMOTE_HOST -- name or ip of remote host (passed to ssh)"
	echo 1>&2 "LOCAL_DB -- name of new database to create locally (default: 'openboxes')"
	echo 1>&2 "REMOTE_DB -- name of database to clone from REMOTE_HOST (default: 'openboxes')"
	echo 1>&2 ""
	echo 1>&2 "Use with care -- this script will destroy \$LOCAL_DB if it exists and you set -f!!"
	exit 1
}

#
# Pretty-print the difference between two timestamps, $1 and $2.
#
elapsed_tm()
{
	set +e
	# guess whether we're running GNU date: thanks for nothing, POSIX
	date -j >/dev/null 2>&-
	if [ $? -ne 0 ]
	then
		# GNU/linux
		date -ud@$(($2 - $1)) "+%H:%M:%S"
	else
		# macOS/BSD
		date -juf "%s" $(($2 - $1)) "+%H:%M:%S"
	fi
	set -e
}

DO_CLOBBER=0
LOCAL_DB="openboxes"
REMOTE_DB="openboxes"
while getopts "fhl:r:" o
do
	case "${o}" in
	f)
		DO_CLOBBER=1
		;;
	l)
		LOCAL_DB=${OPTARG}
		[ "$LOCAL_DB" ] || usage
		;;
	r)
		REMOTE_DB=${OPTARG}
		[ "$REMOTE_DB" ] || usage
		;;
	h)
		usage
		;;
	*)
		usage
		;;
	esac
done
shift $((OPTIND-1))

[ "$1" ] && [ "$#" -eq 1 ] || usage
REMOTE_HOST="$1"

SQL_BASENAME="$REMOTE_HOST-$REMOTE_DB-$LOCAL_DB"

echo -n "Counting products in remote database $REMOTE_HOST:$REMOTE_DB ..."
remote_product_cnt=$(ssh "$REMOTE_HOST" "mysql $REMOTE_DB -Nse 'select count(id) from product;'")
echo " $remote_product_cnt"

echo "Exporting schema from remote database $REMOTE_HOST:$REMOTE_DB ..."
/usr/bin/time ssh $REMOTE_HOST "mysqldump --allow-keywords --single-transaction --skip-comments --no-data --routines $REMOTE_DB | gzip -cf" | gunzip -c > $SQL_BASENAME-schema.sql

database_exists=$(mysql -Nse "select count(schema_name) from information_schema.schemata where schema_name = '$LOCAL_DB';")
if [ "$DO_CLOBBER" -eq 1 ]
then
	echo "Clobbering local database $LOCAL_DB ..."
	mysql -e "drop database if exists \`$LOCAL_DB\`;"
elif [ "$database_exists" -ne 0 ]
then
	echo >&2 "Database \`$LOCAL_DB\` exists and -f was not set!"
	exit 2
fi

echo "Initializing local database $LOCAL_DB ..."
mysql -e "create database \`$LOCAL_DB\` default charset utf8; grant all on \`$LOCAL_DB\`.* to 'openboxes'@'localhost' identified by 'openboxes';"
mysql $LOCAL_DB -e 'select 1' > /dev/null

echo "Importing schema into local database $LOCAL_DB ..."
/usr/bin/time mysql $LOCAL_DB < $SQL_BASENAME-schema.sql

echo "Listing tables in remote database $REMOTE_HOST:$REMOTE_DB ..."
IGNORED_TABLES=$(ssh "$REMOTE_HOST" "mysql -Nse 'select table_name from information_schema.tables where (table_schema like \"$REMOTE_DB\") and (table_name like \"%_dimension\" or table_name like \"%_fact\" or table_name like \"%_snapshot\");'")

declare -a IGNORED_REMOTE_TABLES
for it in $IGNORED_TABLES
do
	echo " - ignoring \`$it\`"
	IGNORED_REMOTE_TABLES+=("--ignore-table=$REMOTE_DB.$it")
done

echo "Exporting data (ignoring ${#IGNORED_REMOTE_TABLES[@]} tables) from remote database $REMOTE_HOST:$REMOTE_DB ..."
/usr/bin/time ssh $REMOTE_HOST "mysqldump --allow-keywords --single-transaction --skip-comments --no-create-info ${IGNORED_REMOTE_TABLES[@]} $REMOTE_DB | gzip -cf" | gunzip -c > $SQL_BASENAME-data.sql

echo "Importing data into local database $LOCAL_DB (this may take 10+ minutes) ..."
/usr/bin/time mysql $LOCAL_DB < $SQL_BASENAME-data.sql --reconnect --wait

echo -n "Counting products in local database $REMOTE_HOST:$LOCAL_DB ..."
local_product_cnt=$(mysql $LOCAL_DB -Nse 'select count(id) from product;')
echo " $local_product_cnt"

end_tm=$(date '+%s')

echo "Cloned data in $(elapsed_tm $start_tm $end_tm)"

if [ ! "$remote_product_cnt" -eq "$local_product_cnt" ]
then
	echo >&2 "Oh, no! Wrong number of product entries after copy! Do not use local DB!"
	exit 2
fi
