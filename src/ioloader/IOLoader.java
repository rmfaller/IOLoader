/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ioloader;

import java.io.File;
import java.io.FilenameFilter;
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
    private static long totallapsedtime = 0;
    private static long totalbytes = 0;
    private static long totaltransactions = 0;
    private static long minthreads = 1;
    private static long maxthreads = 128;
    private static String comment = "ioloader";
    private static boolean writetest = false;
    private static boolean readtest = false;
    private static boolean forever = false;
    private static boolean summary = false;
    private static boolean filecount = true;
    private static long maxfilesize = 0;
    private static long previoustime = 0;
    private static float writebestiops = 0;
    private static float writeworseiops = 1048576;
    private static float readbestiops = 0;
    private static float readworseiops = 1048576;
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
                case "-y":
                case "--summary":
                    summary = true;
                    break;
                case "-g":
                case "--filecount":
                    filecount = false;
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
                        System.err.println("ERROR: --maxfilesize value of " + args[i + 1] + " is not in the correct format. Proper format example 500m or 2g.");
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
                totallapsedtime = 0;
                totalbytes = 0;
                totaltransactions = 0;
                while ((writethreshold >= (totallapsedtime / (float) (writeiterations * threads))) && (maxthreads >= threads)) {
                    Loaders[] loaders = new Loaders[(int) threads];
                    for (int i = 0; i < threads; i++) {
                        loaders[i] = new Loaders(i, writeiterations, writebuffer, workingdirectory, "w", maxfilesize);
                        loaders[i].start();
                    }
                    totallapsedtime = 0;
                    totalbytes = 0;
                    totaltransactions = 0;
                    for (int i = 0; i < threads; i++) {
                        loaders[i].join();
                        totallapsedtime = loaders[i].getLapsedTime() + totallapsedtime;
                        totalbytes = loaders[i].getBytes() + totalbytes;
                        totaltransactions = loaders[i].getTransactions() + totaltransactions;
                    }
                    float iops = printdata(threads, totallapsedtime, totaltransactions, totalbytes, writebuffer, writethreshold);
                    if (iops > writebestiops) {
                        writebestiops = iops;
                    }
                    if (iops < writeworseiops) {
                        writeworseiops = iops;
                    }
                    threads++;
//                    totallapsedtime = 0;
//                    totalbytes = 0;
//                    totaltransactions = 0;
                }
            }
            if (readtest) {
                File dir = new File(workingdirectory);
                File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().startsWith("ioloader");
                    }
                });
                if ((files.length < maxthreads) && (filecount)) {
                    System.err.println("\nRead test ERROR: --maxthreads exceeds the number of files to read from in --workingdirectory which can lead to invalid read results.");
                    System.err.println("Options:");
                    System.err.println("   reduce the number of --threads to equal the number of files in the --workingdirectory to read from - or -");
                    System.err.println("   use the --filecount switch to override this check but the results will not be valid - or -");
                    System.err.println("   run a write test with a higher --maxthreads count and higher --writethreshold to generate enough files to match the number of threads.");
                } else {
                    if (loops == 0) {
                        printheader("read");
                    }
                    threads = minthreads;
                    totallapsedtime = 0;
                    totalbytes = 0;
                    totaltransactions = 0;
                    while ((readthreshold >= (totallapsedtime / (float) (readiterations * threads)) && (maxthreads >= threads))) {
                        Loaders[] loaders = new Loaders[(int) threads];
                        for (int i = 0; i < threads; i++) {
                            loaders[i] = new Loaders(i, readiterations, readbuffer, workingdirectory, "r", maxfilesize);
                            loaders[i].start();
                        }
                        totallapsedtime = 0;
                        totalbytes = 0;
                        totaltransactions = 0;
                        for (int i = 0; i < threads; i++) {
                            loaders[i].join();
                            totallapsedtime = loaders[i].getLapsedTime() + totallapsedtime;
                            totalbytes = loaders[i].getBytes() + totalbytes;
                            totaltransactions = loaders[i].getTransactions() + totaltransactions;
                        }
                        float iops = printdata(threads, totallapsedtime, totaltransactions, totalbytes, readbuffer, readthreshold);
                        if (iops > readbestiops) {
                            readbestiops = iops;
                        }
                        if (iops < readworseiops) {
                            readworseiops = iops;
                        }
                        threads++;
//                        totallapsedtime = 0;
                    }
                }
            }
            if (!forever) {
                loops = 2;
            } else {
                loops = 1;
            }
        }
        if (summary) {
            printsummary(writebestiops, writeworseiops, readbestiops, readworseiops);
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
        System.out.print("timestamp,"); //A
        System.out.print("clock-lapsedtime-ms,"); ///b
        System.out.print(optype + "-threads," + optype + "-threshold,buffer,"); // C, D, E
        System.out.print(optype + "-total-transactions," + optype + "-total-MB,"); // F, G
        System.out.print(comment + "~avr-bytes-tx,");  //H
        System.out.print(comment + "~avr-time-tx-T-ms,");  //I
        System.out.print(comment + "~avr-tx-sec-T-IOPs,");  //J
        System.out.print(comment + "~avr-MB-per-T-sec,");  //K
        System.out.print(comment + "~total-tx-sec-IOPs,");  //L
        System.out.print(comment + "~total-MB-sec,");  //M
        System.out.print(comment + "~combined-time-ms"); //N
        System.out.println();
    }

    private static float printdata(long threads, long totallapsedtime, long iterations, long totalbytes, long buffer, float threshold) {
        float lapsed;
        long presenttime = (long) new Date().getTime();
        if (totallapsedtime == 0) {
            lapsed = 1;
        } else {
            lapsed = (float) totallapsedtime;
        }
        float averbytespertx = ((float) totalbytes / iterations);
        float avertimepertxperT = ((float) (lapsed / iterations));
        float avertxpersecperT = (((float) 1000 / avertimepertxperT) / threads);
        float averMBperTpersec = ((averbytespertx * avertxpersecperT) / 1048576);
        float iops = (avertxpersecperT * threads);
        long totalops = (iterations * threads);

        System.out.print(presenttime + "," + (presenttime - previoustime) + ","); // A, B
        previoustime = presenttime;
        System.out.print(threads + "," + threshold + "," + buffer + ","); //threads and buffe C, D, E
        System.out.print(iterations + "," + ((float) totalbytes / 1048576) + ","); //transactions and MB F, G
        System.out.print(averbytespertx + ","); //avr-bytes-tx H
        System.out.print(avertimepertxperT + ","); //avr-time-tx-T-ms I
        System.out.print(avertxpersecperT + ","); //avr-tx-sec-T-IOPs J
        System.out.print(averMBperTpersec + ","); //avr-MB-per-T-sec K
        System.out.print(iops + ","); //total-tx-sec-IOPs L
        System.out.print((averMBperTpersec * threads) + ","); //total-MB-sec M
        System.out.print(lapsed ); //combined-time-ms N
        System.out.println();
        return(iops);
    }

    private static void printsummary(float writebestiops, float writeworseiops, float readbestiops, float readworseiops) {
        if (writebestiops > 0) {
            System.out.println("\nIOLoader write best IOPs = " + writebestiops + " and worse IOPs = " + writeworseiops);
        }
        if (readbestiops > 0) {
            System.out.println("\nIOLoader read best IOPS = " + readbestiops + " and worse IOPs = " + readworseiops);
        }
    }
}
