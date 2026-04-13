#!/bin/bash

############################################
##
## ISEL - System Virtualization Techniques
## Autumn/Winter 2025/26
##
## Coursework Assignment #1
##

RUN_DEMO=$(dirname "$0")/demo/test/bin/run_demo

if [[ -f "$RUN_DEMO" ]]; then
	$RUN_DEMO
else
	echo "ERROR: Missing demo file \"$RUN_DEMO\""
	echo First build the demo file with \"make\"
fi
