#!/bin/bash
java -jar /Users/robert.faller/projects/IOLoader/dist/IOLoader.jar \
  --readtest \
  --readiterations 4096 \
  --readthreshold .1 \
  --maxfilesize 2g \
  --minthreads 1 \
  --maxthreads 30 \
  --comment 2tb \
  --workingdirectory $HOME/tmp/tmp \
  --forever
