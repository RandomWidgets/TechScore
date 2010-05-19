#!/bin/bash
# Adds the HEADER file to the specified file

if [[ $# -eq 0 ]]; then
    echo -e "usage: addheader.sh File.java"
    echo -e "\n      Add contents of HEADER to File.java"
    exit 1;
fi

# find the location of the line
# " * Created"
loc=`grep -m 1 -n "^ *\* Created" $1 | cut -d ":" -f 1`
let "loc_b = loc - 1"

# splice files together
head -n$loc_b $1 | cat - HEADER > $1.new
tail -n+$loc  $1 | cat $1.new - > $1
