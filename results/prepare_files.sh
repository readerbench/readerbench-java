#!/bin/bash

rm ./*.csv

touch ./"community_"$1"_initiation.csv"
touch ./"community_"$1"_textualComplexity.csv"
touch ./"community_"$1"_discussedTopics.csv"
touch ./"community_"$1"_timeAnalysis.csv"
touch ./"community_"$1"_individualStats.csv"
touch ./"community_"$1"_individualThreadStatistics.csv"
