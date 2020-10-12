# IOLoader

```
IOLoader usage:
java -jar ./dist/IOLoader.jar
with one or more of the following options:
	--workingdirectory | -d {default = ./tmp} location to write and read files
	--writetest        | -w invokes write testing
	--readtest         | -r invokes read testing; assumes write test has run prior to create file to read from
	--writethreshold   | -t {default = 0.5ms} average write operation time to exceed and terminate test
	--readthreshold    | -s {default = 0.1ms} average read operation time to exceed and terminate test
	--writeiterations  | -i {default = 100} number of writes operations per thread
	--readiterations   | -l {default = 100} number of read operations per thread
	--writebuffer      | -b {default = 8192} buffer size of write
	--readbuffer       | -e {default = 8192} buffer size of read
	--comment          | -c {default = ioloader} appends this string to column headers to help with comparisons
	--minthreads       | -m {default = 1} minimum threads to start with
	--maxthreads       | -x {default = 128} maximum threads limit before stopping
	--maxfilesize      | -z {no size limit} maximum size an individual file (each thread is associated to one file) can grow to
	                        integer value must be appended with a "m" for megabytes or "g" for gigabytes i.e. 500m or 2g
	--forever          | -f {default is to NOT run forever}
	--help             | -h this output

Examples:
  java -jar ./dist/IOLoader.jar --writetest --writeiterations 10000  --forever --workingdirectory /tmp/test
  java -jar ./dist/IOLoader.jar --readtest --maxthreads 8 --workingdirectory /tmp/test
  java -jar ./dist/IOLoader.jar --writetest --maxthreads 2 --minthreads 2 --forever --workingdirectory ./tmp --maxfilesize 1m --writeiterations 1024 --comment t0 --writethreshold 2
  java -jar ./dist/IOLoader.jar --writetest --writethreshold 1 --writeiterations 4000 --workingdirectory /tmp/test

Other option is to use the following on a Mac:
dd if=/dev/zero bs=1024k of=tstfile count=1024 && dd if=tstfile bs=1024k of=/dev/null count=1024 && rm tstfile
See: http://hints.macworld.com/article.php?story=20120704113548693

```