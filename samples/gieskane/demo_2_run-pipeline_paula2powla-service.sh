#!/bin/bash
# Requires the swagger-server to be running.

# run Fintan conversion from Paula format to POWLA
./../../run.sh -c paula2powla-service.json -p data/gieskane/bauernleben.zip | \
# fix buggy URIs
sed -E "s/([^:])\/\//\1\//g" > data/powla/bauernleben.ttl

