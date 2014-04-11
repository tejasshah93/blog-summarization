#!/bin/bash

if [ "$#" -ne 1 ];
then
	echo "USAGE: sh run.sh <URL To Crawl and summarize>"
else
	python tCrawler.py "$1"
	javac -cp gson-2.2.4.jar:stanford-postagger.jar src/*.java 
	java -cp gson-2.2.4.jar:stanford-postagger.jar:src/ XMLParser
fi





