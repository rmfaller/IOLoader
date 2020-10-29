#!/bin/bash
java -jar /Users/robert.faller/projects/IOLoader/dist/IOLoader.jar \
  --writetest \
  --writeiterations 4096 \
  --writethreshold 2 \
  --maxfilesize 2g \
  --minthreads 1 \
  --maxthreads 20 \
  --comment hd \
  --workingdirectory /Volumes/twoTBdrive/tmp/tmp \
  --forever
