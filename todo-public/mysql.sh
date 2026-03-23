#!/bin/bash
docker run --name mysql --rm -d -e MYSQL_USER=test -e MYSQL_ALLOW_EMPTY_PASSWORD=1 -e MYSQL_DATABASE=test -p 3306:3306 mysql
