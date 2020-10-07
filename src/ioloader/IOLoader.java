/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ioloader;

import java.util.Date;

/**
 *
 * @author rmfaller
 */
public class IOLoader extends Thread {

    private static long writeiterations = 100;
    private static float writethreshold = (float) 0.5;
    private static long writebuffer = 8192;
    private static long readiterations = 100;
    private static float readthreshold = (float) 0.1;
    private static long readbuffer = 8192;
    private static String workingdirectory = "./tmp";
    private static long lapsedtime = 0;
    private static long minthreads = 1;
    private static long maxthreads = 128;
    private static String comment = "ioloader";
    private static boolean writetest = false;
    private static boolean readtest = false;
    private static boolean forever = false;
    private static boolean summary = false;
    private static long maxfilesize = 0;
    private static long previoustime = 0;

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        long threads;
        if (args.length < 1) {
            help();
        }
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-w":
                case "--writetest":
                    writetest = true;
                    break;
                case "-r":
                case "--readtest":
                    readtest = true;
                    break;
                case "-f":
                case "--forever":
                    forever = true;
                    break;
                case "-c":
                case "--comment":
                    comment = (args[i + 1]);
                    break;
                case "-i":
                case "--writeiterations":
                    writeiterations = Long.parseLong(args[i + 1]);
                    break;
                case "-t":
                case "--writethreshold":
                    writethreshold = Float.parseFloat(args[i + 1]);
                    break;
                case "-b":
                case "--writebuffer":
                    writebuffer = Long.parseLong(args[i + 1]);
                    break;
                case "-l":
                case "--readiterations":
                    readiterations = Long.parseLong(args[i + 1]);
                    break;
                case "-s":
                case "--readthreshold":
                    readthreshold = Float.parseFloat(args[i + 1]);
                    break;
                case "-e":
                case "--readbuffer":
                    readbuffer = Long.parseLong(args[i + 1]);
                    break;
                case "-d":
                case "--workingdirectory":
                    workingdirectory = (args[i + 1]);
                    break;
                case "-m":
                case "--minthreads":
                    minthreads = Long.parseLong(args[i + 1]);
                    break;
                case "-x":
                case "--maxthreads":
                    maxthreads = Long.parseLong(args[i + 1]);
                    break; 
                case "-z":
                case "--maxfilesize":
                    String mfsz = (args[i + 1]).substring(args[i + 1].length() - 1);
                    if ((mfsz.compareToIgnoreCase("m") == 0) || (mfsz.compareToIgnoreCase("g") == 0)) {
                        if (mfsz.compareToIgnoreCase("m") == 0) {
                            maxfilesize = (Long.parseLong((args[i + 1]).substring(0, args[i + 1].length() - 1))) * 1000000;
                        } else {
                            maxfilesize = (Long.parseLong((args[i + 1]).substring(0, args[i + 1].length() - 1))) * 1000000000;
                        }
                    } else {
                        System.err.println("ERROR: maxfilesize value of " + args[i + 1] + " is not in the correct format. Proper format example 500m or 2g.");
                        System.err.println("Value of 0 is being used which means the file(s) grow without limit");
                    }
                    break;
                case "-?":
                case "-h":
                case "--help":
                    help();
                    break;
                default:
                    break;
            }
        }
        int loops = 0;
        previoustime = (long) new Date().getTime();
        while (loops <= 1) {
            if (writetest) {
                if (loops == 0) {
                    printheader("write");
                }
                threads = minthreads;
                lapsedtime = 0;
                while ((writethreshold >= (lapsedtime / (float) (writeiterations * threads))) && (maxthreads >= threads)) {
                    Loaders[] loaders = new Loaders[(int) threads];
                    for (int i = 0; i < threads; i++) {
                        loaders[i] = new Loaders(i, writeiterations, writebuffer, workingdirectory, "w", maxfilesize);
                        loaders[i].start();
                    }
                    lapsedtime = 0;
                    for (int i = 0; i < threads; i++) {
                        loaders[i].join();
                        lapsedtime = loaders[i].getLapsedTime() + lapsedtime;
                    }
                    printdata(threads, lapsedtime, writeiterations, writebuffer);
                    threads++;
                }
            }
            if (readtest) {
                if (loops == 0) {
                    printheader("read");
                }
//                printheader("read");
                threads = minthreads;
                lapsedtime = 0;
                while ((readthreshold >= (lapsedtime / (float) (readiterations * threads)) && (maxthreads >= threads))) {
                    Loaders[] loaders = new Loaders[(int) threads];
                    for (int i = 0; i < threads; i++) {
                        loaders[i] = new Loaders(i, readiterations, readbuffer, workingdirectory, "r", maxfilesize);
                        loaders[i].start();
                    }
                    lapsedtime = 0;
                    for (int i = 0; i < threads; i++) {
                        loaders[i].join();
                        lapsedtime = loaders[i].getLapsedTime() + lapsedtime;
                    }
                    printdata(threads, lapsedtime, readiterations, readbuffer);
                    threads++;
                }
            }
            if (!forever) {
                loops = 2;
            } else {
                loops = 1;
            }
        }
    }

    private static void help() {
        String help = "\nIOLoader usage:"
                + "\njava -jar ./dist/IOLoader.jar"
                + "\nwith one or more of the following options:"
                + "\n\t--workingdirectory | -d {default = ./tmp} location to write and read files"
                + "\n\t--writetest        | -w invokes write testing"
                + "\n\t--readtest         | -r invokes read testing; assumes write test has run prior to create file to read from"
                + "\n\t--writethreshold   | -t {default = 0.5ms} average write operation time to exceed and terminate test"
                + "\n\t--readthreshold    | -s {default = 0.1ms} average read operation time to exceed and terminate test"
                + "\n\t--writeiterations  | -i {default = 100} number of writes operations per thread"
                + "\n\t--readiterations   | -l {default = 100} number of read operations per thread"
                + "\n\t--writebuffer      | -b {default = 8192} buffer size of write"
                + "\n\t--readbuffer       | -e {default = 8192} buffer size of read"
                + "\n\t--comment          | -c {default = ioloader} appends this string to column headers to help with comparisons"
                + "\n\t--minthreads       | -m {default = 1} minimum threads to start with"
                + "\n\t--maxthreads       | -x {default = 128} maximum threads limit before stopping"
                + "\n\t--maxfilesize      | -z {no size limit} maximum size an individual file (each thread is associated to one file) can grow to"
                + "\n\t                        integer value must be appended with a \"m\" for megabytes or \"g\" for gigabytes i.e. 500m or 2g"
                + "\n\t--forever          | -f {default is to NOT run forever}"
                + "\n\t--help             | -h this output\n"
                + "\nExamples: \n"
                + "  java -jar ./dist/IOLoader.jar --writetest --writeiterations 10000  --forever --workingdirectory /tmp/test\n"
                + "  java -jar ./dist/IOLoader.jar --readtest --maxthreads 8 --workingdirectory /tmp/test\n"
                + "  java -jar ./dist/IOLoader.jar --writetest --maxthreads 2 --minthreads 2 --forever --workingdirectory ./tmp --maxfilesize 1m --writeiterations 1024 --comment t0 --writethreshold 2\n"
                + "  java -jar ./dist/IOLoader.jar --writetest --writethreshold 1 --writeiterations 4000 --workingdirectory /tmp/test\n";
        System.out.println(help);
    }

    private static void printheader(String optype) {
        if (forever) {
            System.out.print("timestamp,clock-lapsedtime-ms,");
        }
        System.out.print(optype + "-threads,buffer,"); // 1,2
        System.out.print(comment + "~combined-ops,"); //3
        System.out.print(comment + "~combined-time-ms,"); //4
        System.out.print(comment + "~avr-T-lapsedtime-ms,");  //5
        System.out.print(comment + "~avr-T-op-sec,");  //6
        System.out.print(comment + "~combined-ops-sec,");  //7
        System.out.print(comment + "~avr-T-MB-sec,");  //8
        System.out.print(comment + "~total-MB-sec,");  //9
        System.out.print(comment + "~avr-ms-op-sec,");  //10
        System.out.print(comment + "~throughput-per-T-opsxMB,");  //11
        System.out.print(comment + "~throughput-average-opsxMB");  //12
        System.out.println();
    }

    private static void printdata(long threads, long lapsedtime, long iterations, long buffer) {
        float lapsed;
        long presenttime = 0;
        if (forever) {
            presenttime = (long) new Date().getTime();
            System.out.print(presenttime + "," + (presenttime - previoustime) + ",");
            previoustime = presenttime;
        }
        //threads and buffer 1, 2
        System.out.print(threads + "," + buffer + ",");
        //combined-ops 3
        long combinedops = (iterations * threads);
        System.out.print(combinedops + ",");
        //combined-time-ms 4
        System.out.print(lapsedtime + ",");
        //avr-T-lapsedtime-ms  5
        System.out.print((lapsedtime / threads) + ",");

        if (lapsedtime == 0) {
            lapsed = 1;
        } else {
            lapsed = (float) lapsedtime;
        }
        //avr-T-op-sec  6
        System.out.print((((float) iterations / (lapsedtime / threads)) * 1000) + ",");
        //combined-ops-sec  7
        System.out.print(((((float) iterations / (lapsedtime / threads)) * 1000) * threads) + ",");
        //avr-T-MB-sec  8
        System.out.print((((((float) iterations / (lapsedtime / threads)) * 1000) * buffer) / 1048576) + ",");
        //total-MB-s  9
        System.out.print((((((float) iterations / (lapsedtime / threads)) * 1000) * buffer) / 1048576) * threads + ",");
        //avr-ms-op  10
        System.out.print(((float) lapsed / combinedops) + ",");
        //throughput-per-T  11
        System.out.print((((float) iterations / (lapsedtime / threads)) * 1000) * (((((float) iterations / (lapsedtime / threads)) * 1000) * buffer) / 1048576) + ",");
        //throughput-average  12
        System.out.print(((((float) iterations / (lapsedtime / threads)) * 1000) * (((((float) iterations / (lapsedtime / threads)) * 1000) * buffer) / 1048576)) * threads);
        System.out.println();
    }
}
