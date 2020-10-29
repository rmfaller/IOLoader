#!/bin/bash
java -jar /Users/robert.faller/projects/IOLoader/dist/IOLoader.jar \
  --writetest \
  --writeiterations 4096 \
  --writethreshold 2 \
  --maxfilesize 2g \
  --minthreads 1 \
  --maxthreads 30 \
  --comment hd \
  --workingdirectory $HOME/tmp/tmp \
  --forever
