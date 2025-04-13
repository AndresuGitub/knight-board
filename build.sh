#!/bin/bash
rm -rf out
mkdir -p out
javac -cp "lib/json-20210307.jar" -d out src/KnightPathJR.java
