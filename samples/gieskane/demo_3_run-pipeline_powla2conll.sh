#!/bin/bash
# Requires the swagger-server to be running.

# run Fintan conversion from POWLA format to CoNLL. 
# Adjust powla2conll.json for different column order.
cat data/powla/nietzsche.ttl | ./../../run.sh -c powla2conll.json > data/conll/nietzsche.conll

