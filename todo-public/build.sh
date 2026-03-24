#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

mvn -f "$SCRIPT_DIR/pom.xml" package -DskipTests

cp "$SCRIPT_DIR/app/target/todo-app.war" "$SCRIPT_DIR/../TD1/todo/todo-app.war"
