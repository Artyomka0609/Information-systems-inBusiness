#!/usr/bin/env bash
set -e
cd -- "$(dirname -- "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/ru/edu/pr05/*.java
java -Dfile.encoding=UTF-8 -cp out ru.edu.pr05.SelfCheck 
