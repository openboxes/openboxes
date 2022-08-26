#!/bin/bash
set -eo pipefail
start_tm=$(date '+%s')
export TIME="%C %E"

usage()
{
	set +xv
	echo >&2 "$0 - clone a remote mysql database to a local machine for testing"
	echo >&2 ""
	echo >&2 "Usage: $0 [-fsP] [-l <LOCAL_DB>] [-p <PATH> ] [-r <REMOTE_DB>] REMOTE_HOST"
	echo >&2 "REMOTE_HOST -- name or ip of remote host (passed to ssh)"
	echo >&2 "LOCAL_DB -- name of new database to create locally (default: 'openboxes')"
	echo >&2 "REMOTE_DB -- name of database to clone from REMOTE_HOST (default: 'openboxes')"
	echo >&2 "PATH -- path under which mysql is installed on remote host"
	echo >&2 ""
	echo >&2 "Flags:"
	echo >&2 "  -f Completely delete \$LOCAL_DB, if it exists, before cloning -- use with care!"
	echo >&2 "  -s use sudo when accessing local database (if using auth_socket for root)"
	echo >&2 "  -P Copy product_demand tables (will fail if refreshProductDemandData is running on remote)"
	exit 1
}

#
# Pretty-print the difference between two timestamps, $1 and $2.
#
elapsed_tm()
{
	set +e
	# guess whether we're running GNU date: thanks for nothing, POSIX
	if date -j >/dev/null 2>&-
	then
		# macOS/BSD has the -j flag
		date -juf "%s" $(($2 - $1)) "+%H:%M:%S"
	else
		# otherwise, assume we're running GNU/linux
		date -ud@$(($2 - $1)) "+%H:%M:%S"
	fi
	set -e
}

LOCAL_DB="openboxes"
LOCAL_DB_USERNAME="root"
REMOTE_DB="openboxes"
REMOTE_DB_USERNAME="root"
local_sudo=

MYSQL_PATH=/usr/bin:/usr/local/mysql/bin

if [ ! "$LOCAL_DB_PASSWORD" ]
then
	echo >&2 "please set the LOCAL_DB_PASSWORD environment variable"
	exit 1
elif [ ! "$REMOTE_DB_PASSWORD" ]
then
	echo >&2 "please set the REMOTE_DB_PASSWORD environment variable"
	exit 1
fi

set -u

while getopts 'fhl:p:r:sP' o
do
	case "${o}" in
	f)
		do_clobber=1
		;;
	h)
		usage
		;;
	l)
		LOCAL_DB=${OPTARG}
		[ "$LOCAL_DB" ] || usage
		;;
	p)
		${OPTARG} || usage
		MYSQL_PATH=${OPTARG}:${MYSQL_PATH}
		;;
	r)
		REMOTE_DB=${OPTARG}
		[ "$REMOTE_DB" ] || usage
		;;
	s)
		local_sudo=sudo
		;;
	P)
		do_copy_product_demand=1
		;;
	*)
		usage
		;;
	esac
done
shift $((OPTIND-1))

export PATH=$PATH:$MYSQL_PATH

[ "$1" ] || usage
[ "$#" -eq 1 ] || usage

REMOTE_HOST="$1"
sql_basename="$REMOTE_HOST-$REMOTE_DB-$LOCAL_DB"
declare -a ignored_remote_tables

echo -n "Counting products in remote database $REMOTE_HOST:$REMOTE_DB ..."
remote_product_cnt=$(ssh "$REMOTE_HOST" "PATH=$MYSQL_PATH mysql -u '$REMOTE_DB_USERNAME' -p'$REMOTE_DB_PASSWORD' '$REMOTE_DB' -Nse 'select count(id) from product;'")
echo " $remote_product_cnt"

if [ ! "${do_copy_product_demand:-}" ]
then
	#
	# ReportService.refreshProductDemandData() drops tables while it works,
	# which can generate "ERROR 1146 (42S02) at line XXXX: Table doesn't exist"
	# errors and make dependent views un-reconstructable.
	#
	declare -a nocreate_tables=(
		"product_demand_details"
		"product_demand_details_tmp"
		"product_expiry_summary"	# this view is invalid when product_demand_details is dropped
	)
	echo "Skipping tables overwritten by ReportService.refreshProductDemandData() ..."
	for it in "${nocreate_tables[@]}"
	do
		echo " - will not create \`$it\`"
		ignored_remote_tables+=("--ignore-table=$REMOTE_DB.$it")
	done
fi

#
# Some views may refer to the remote database by name; if so, the sed
# expression in this command will map them to the local database's name.
#
echo "Exporting schema (ignoring ${#ignored_remote_tables[@]} tables) from remote database $REMOTE_HOST:$REMOTE_DB ..."
/usr/bin/time ssh "$REMOTE_HOST" \
    "PATH=$MYSQL_PATH mysqldump -u '$REMOTE_DB_USERNAME' -p'$REMOTE_DB_PASSWORD' \
    --opt --allow-keywords --single-transaction --no-data \
    ${ignored_remote_tables[*]:-} '$REMOTE_DB' | gzip -cf" | \
    gunzip -c | \
    sed "/SQL SECURITY DEFINER/! s/\`$REMOTE_DB\`/\`$LOCAL_DB\`/g" > \
    "${sql_basename}-schema.sql"

database_exists=$($local_sudo mysql -u "$LOCAL_DB_USERNAME" -p"$LOCAL_DB_PASSWORD" -Nse "select count(schema_name) from information_schema.schemata where schema_name = '$LOCAL_DB';")
if [ "${do_clobber:-}" ]
then
	echo "Clobbering local database $LOCAL_DB ..."
	$local_sudo mysql -u "$LOCAL_DB_USERNAME" -p"$LOCAL_DB_PASSWORD" -e "drop database if exists \`$LOCAL_DB\`;"
elif [ "$database_exists" -ne 0 ]
then
	echo >&2 "Database \`$LOCAL_DB\` exists and -f was not set!"
	exit 2
fi

echo "Initializing local database $LOCAL_DB ..."
$local_sudo mysql -u "$LOCAL_DB_USERNAME" -p"$LOCAL_DB_PASSWORD" -e "create database \`$LOCAL_DB\` default charset utf8; grant all on \`$LOCAL_DB\`.* to 'openboxes'@'localhost' identified by 'openboxes';"
$local_sudo mysql -u "$LOCAL_DB_USERNAME" -p"$LOCAL_DB_PASSWORD" "$LOCAL_DB" -e 'select 1' > /dev/null

echo "Inserting schema into local database $LOCAL_DB ..."
/usr/bin/time $local_sudo mysql -u "$LOCAL_DB_USERNAME" -p"$LOCAL_DB_PASSWORD" "$LOCAL_DB" < "${sql_basename}-schema.sql"

echo "Listing tables in remote database $REMOTE_HOST:$REMOTE_DB ..."
nocopy_tables=$(ssh "$REMOTE_HOST" "PATH=$MYSQL_PATH mysql -u '$REMOTE_DB_USERNAME' -p'$REMOTE_DB_PASSWORD' -Nse 'select table_name from information_schema.tables where (table_schema like \"$REMOTE_DB\") and (table_name like \"%_dimension\" or table_name like \"%_fact\" or table_name like \"%_snapshot\");'")

for it in $nocopy_tables
do
	echo " - will not copy \`$it\`"
	ignored_remote_tables+=("--ignore-table=$REMOTE_DB.$it")
done

echo "Exporting data (ignoring ${#ignored_remote_tables[@]} tables) from remote database $REMOTE_HOST:$REMOTE_DB ..."
/usr/bin/time ssh "$REMOTE_HOST" \
    "PATH=$MYSQL_PATH mysqldump -u '$REMOTE_DB_USERNAME' -p'$REMOTE_DB_PASSWORD' \
    --opt --allow-keywords --single-transaction --no-create-info \
    ${ignored_remote_tables[*]:-} '$REMOTE_DB' | gzip -cf" | \
    gunzip -c > "${sql_basename}-data.sql"

#
# Prevent "ERROR 2006 (HY000): MySQL server has gone away" by temporarily
# increasing max_allowed_packet, if needed. MySQL 5.7's default value of 4M
# may not be enough to consistently copy a production OB database, but if the
# local server has already increased it, we don't need, or want, to change it.
#
# see also https://stackoverflow.com/questions/10474922/error-2006-hy000-mysql-server-has-gone-away
#
curr_max_packet=$($local_sudo mysql -u "$LOCAL_DB_USERNAME" -p"$LOCAL_DB_PASSWORD" -Nse "select @@max_allowed_packet;")
if [ "$curr_max_packet" -lt 16777216 ]
then
	echo "Temporarily increasing max_allowed_packet to 16M ..."
	$local_sudo mysql -u "$LOCAL_DB_USERNAME" -p"$LOCAL_DB_PASSWORD" -e "set global max_allowed_packet=16*1024*1024;"
fi

echo "Inserting data into local database $LOCAL_DB (this may take several minutes) ..."
/usr/bin/time $local_sudo mysql -u "$LOCAL_DB_USERNAME" -p"$LOCAL_DB_PASSWORD" "$LOCAL_DB" < "${sql_basename}-data.sql" --max-allowed-packet=16M --reconnect --wait

if [ "$curr_max_packet" -lt 16777216 ]
then
	echo "Restoring previous max_allowed_packet ..."
	$local_sudo mysql -u "$LOCAL_DB_USERNAME" -p"$LOCAL_DB_PASSWORD" -e "set global max_allowed_packet=${curr_max_packet};"
fi

echo -n "Counting products in local database $REMOTE_HOST:$LOCAL_DB ..."
local_product_cnt=$($local_sudo mysql -u "$LOCAL_DB_USERNAME" -p"$LOCAL_DB_PASSWORD" "$LOCAL_DB" -Nse 'select count(id) from product;')
echo " $local_product_cnt"

end_tm=$(date '+%s')

echo "Cloned data in $(elapsed_tm "$start_tm" "$end_tm")"

if [ ! "$remote_product_cnt" -eq "$local_product_cnt" ]
then
	echo >&2 "Oh, no! Wrong number of product entries after copy! Do not use local DB $LOCAL_DB!"
	exit 2
fi
