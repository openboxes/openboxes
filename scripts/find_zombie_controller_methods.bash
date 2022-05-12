#!/bin/bash

#
# Copyright (c) 2022 Partners In Health.  All rights reserved.
# The use and distribution terms for this software are covered by the
# Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
# which can be found in the file epl-v10.html at the root of this distribution.
# By using this software in any fashion, you are agreeing to be bound by
# the terms of this license.
# You must not remove this notice, or any other, from this software.
#

#
# This brief script looks for methods declared in controller classes,
# then looks in a few common places for references to those methods.
#
# For each controller method, this script will print one line to stdout:
# - If nothing is found, "no references" is printed in red text.
# - If a confirmed reference is found, it prints "confirmed references" in green.
# - Otherwise, it prints "uncertain" in yellow.
#
# Things that look like references are printed to stderr.
# A handy little summary is printed at the end.
#
# Of the three outcomes, "confirmed references" is probably the most
# reliable. Don't take the script's word for it before deleting anything!
#

set -uo noglob pipefail

CLEAR='\033[0m'
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'

if ! rg --help >/dev/null
then
	echo "This script requires ripgrep."
	exit 2
fi

contestants=$(
	# find all controller-class containing files in the project
	find . -name '*Controller.groovy' -print0 | \
	# look for all method/closure declarations in each file
	xargs -0 egrep -n '^(    |\t)[^ ].*{' | \
	# strip everything after the putative method/closure name
	sed -e 's/(.*//' -e 's/ *= *{.*//' | \
	# the first column is the file path; the last, the method/closure name
	awk '{
		sub(/.*\//, "", $1);  				# strip preceding path
		sub(/\.groovy:[0-9]*:*/, "", $1); 	# strip file extension and line no.
		# camel-case the basename and append the method/closure name with a dot
		print tolower(substr($1,1,1)) substr($1, 2) "." $(NF)
	}' | sort)

declare -a victims
declare -a maybes

echo "Inspecting $(echo "$contestants" | wc -l | tr -d ' ') entry points..."

for c in $contestants
do
	split_name=($(echo "$c" | tr '.' ' '))
	class_name="${split_name[0]}"
	method_name="${split_name[1]}"
	gsp_class_name=${class_name%Controller}
	matches=""

	printf "finding references to $class_name.$method_name... "

	# quick check: see how many times the name appears anywhere in the project
	symbol_cnt=$(git grep -Ichnp "\b$method_name\b" | awk '{s+=$1} END {print s}')

	# then look for implicit references in views named after the method
	file_root="grails-app/views/${gsp_class_name}"
	file_glob="${method_name}.gsp"
	matches+=$(find "$file_root" -name "$file_glob" 2>&- | sort)

	#
	# If the symbol appears exactly once, it's definitely safe to remove.
	#
	# TODO xxYyZz really should count as a use of getXxYyZz, but getters
	# TODO in controllers are so uncommon I haven't yet bothered to handle
	# TODO them properly: for now, the script will never report any getter
	# TODO as being safe to remove.
	#
	if [ "$symbol_cnt" -eq 1 ] && [ ! "$matches" ] && [[ ! "$method_name" =~ ^get ]]
	then
		printf "${RED}no references${CLEAR}\n"
		victims+=("$class_name.$method_name")
		continue
	fi

	#
	# At this point, the symbol appears more than once. If we find it in
	# a known location then we can definitively say it cannot be removed.
	#

	# look for references in gsp files
	explicit_pattern='controller.*\b'"$gsp_class_name"'\b.*action.*\b'"$method_name"'\b'
	matches+=$(git grep -EIpn "$explicit_pattern")
	implicit_pattern='\b.*action.*\b'"$method_name"'\b'
	matches+=$(grep -Rn "$implicit_pattern" "grails-app/views/$gsp_class_name" 2>&-)

	# look for references in grails mappings and configuration
	url_pattern='\b'"$gsp_class_name"'\b.*\n.*action *=#\[[A-Z]+ *:.*\b'"$method_name"'\b'
	matches+=$(rg -U "$url_pattern" grails-app/conf/UrlMappings.groovy)
	role_pattern='^[ \t]+.\b'"${gsp_class_name}"'\b.[ \t]*:.*\b'"$method_name"'\b'
	matches+=$(grep -n "$role_pattern" grails-app/conf/RoleFilters.groovy)

	# look for references in REST call declarations
	restful_pattern="/${gsp_class_name}/${method_name}\b"
	matches+=$(git grep -EIpn "$restful_pattern")

	if [ "${matches}" ]
	then
		# high-confidence access of this symbol
		printf "${GREEN}confirmed references${CLEAR}\n"
		echo "${matches}" >&2
	else
		# the hits we found may be text matches, not true references
		printf "${YELLOW}uncertain${CLEAR}\n"
		maybes+=("$class_name.$method_name")
	fi
done

set +u

echo "found ${#victims[@]} controller method(s) with no references."
for v in "${victims[@]}"
do
	echo "- $v"
done

echo "a further ${#maybes[@]} method(s) may need manual review."
for m in "${maybes[@]}"
do
	echo "- $m"
done
