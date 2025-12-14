#!/bin/zsh

javac -cp "src:libs/*" -d out $(find src -name "*.java")
