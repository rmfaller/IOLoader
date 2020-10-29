#!/bin/bash
java -jar /Users/robert.faller/projects/IOLoader/dist/IOLoader.jar \
  --writetest \
  --writeiterations 16384 \
  --writethreshold 1 \
  --maxfilesize 2g \
  --minthreads 1 \
  --maxthreads 10 \
  --comment hd \
  --workingdirectory $HOME/tmp/tmp \
  --forever
