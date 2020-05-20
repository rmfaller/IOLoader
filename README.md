# IOLoader

```
IOLoader usage:
java -jar ./dist/IOLoader.jar
with one or more of the following options:
	--writetest        | -w invokes write testing
	--readtest         | -r invokes read testing; assumes write test has run prior to create file to read from
	--workingdirectory | -d {default = ./tmp} location to write and read files
	--writethreshold   | -t {default = 0.5ms} average write operation time to exceed and terminate test
	--readthreshold    | -s {default = 0.1ms} average read operation time to exceed and terminate test
	--writeiterations  | -i {default = 100} number of writes operations per thread
	--readiterations   | -l {default = 100} number of read operations per thread
	--writebuffer      | -b {default = 8192} buffer size of write
	--readbuffer       | -e {default = 8192} buffer size of read
	--comment          | -c {default = ioloader} appends this string to column headers to help with comparisons
	--minthreads       | -m {default = 1} minimum threads to start with
	--maxthreads       | -x {default = 128} maximum threads limit before stopping
	--forever          | -f {default is to NOT run forever}
	--help             | -h this output

Examples:
  java -jar ./dist/IOLoader.jar --writetest --writeiterations 10000  --forever --workingdirectory /tmp/test
  java -jar ./dist/IOLoader.jar --readtest --maxthreads 8 --workingdirectory /tmp/test
  java -jar ./dist/IOLoader.jar --writetest --writethreshold 1 --writeiterations 4000 --workingdirectory /tmp/test
```