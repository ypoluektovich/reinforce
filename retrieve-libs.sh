#!/bin/sh

if [[ (( $# < 1 )) || (( $# > 2 )) ]]; then
	echo "Usage: $0 <module name> [<confs>]" >&2
	exit 1
fi

MODULE="$1"
if [[ ! -d "src/$MODULE" ]]; then
	echo "Invalid module: $MODULE" >&2
	exit 2
fi

if [[ -z "$2" ]]; then
	CONFS="default"
else
	CONFS="$2"
fi

ivy -settings ivysettings.xml -ivy "src/$MODULE/ivy.xml" -confs "$CONFS" -retrieve 'lib/[artifact]-[revision].[ext]'
(( $? == 0 )) && exit 0 || exit 3
