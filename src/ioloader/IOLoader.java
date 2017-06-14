/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ioloader;

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
    private static long maxthreads = 128;
    private static String comment = "ioloader";
    private static boolean writetest = false;
    private static boolean readtest = false;

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        int threads;
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
                case "--maxthreads":
                    maxthreads = Long.parseLong(args[i + 1]);
                    break;
                case "-h":
                case "--help":
                    help();
                    break;
                default:
                    break;
            }
        }
        if (writetest) {
            printheader("write");
            threads = 1;
            while ((writethreshold >= (lapsedtime / (float) (writeiterations * threads))) && (maxthreads > threads)) {
                Loaders[] loaders = new Loaders[threads];
                for (int i = 0; i < threads; i++) {
                    loaders[i] = new Loaders(i, writeiterations, writebuffer, workingdirectory, "w");
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
            printheader("read");
            threads = 1;
            lapsedtime = 0;
            while ((readthreshold >= (lapsedtime / (float) (readiterations * threads)) && (maxthreads > threads))) {
                Loaders[] loaders = new Loaders[threads];
                for (int i = 0; i < threads; i++) {
                    loaders[i] = new Loaders(i, readiterations, readbuffer, workingdirectory, "r");
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
    }

    private static void help() {
        String help = "\nIOLoader usage:"
                + "\njava -jar ./dist/IOLoader.jar"
                + "\nwith one or more of the following options:"
                + "\n\t--writetest        | -w invokes write testing"
                + "\n\t--readtest         | -r invokes read testing; assumes write test has run prior to create file to read from"
                + "\n\t--workingdirectory | -d {default = ./tmp} location to write and read files"
                + "\n\t--writethreshold   | -t {default = 0.5ms} average write operation time to exceed and terminate test"
                + "\n\t--readthreshold    | -s {default = 0.1ms} average read operation time to exceed and terminate test"
                + "\n\t--writeiterations  | -i {default = 100} number of writes operations per thread"
                + "\n\t--readiterations   | -l {default = 100} number of read operations per thread"
                + "\n\t--writebuffer      | -b {default = 8192} buffer size of write"
                + "\n\t--readbuffer       | -e {default = 8192} buffer size of read"
                + "\n\t--comment          | -c {default = ioloader} appends this string to column headers to help with comparisons"
                + "\n\t--maxthreads       | -m {default = 128} maximum threads limit before stopping"
                + "\n\t--help             | -h this output\n"
                + "\nExample: java -jar ./dist/IOLoader.jar --writetest --writethreshold 2 --writeiterations 4000 --workingdirectory /tmp/test\n";
        System.out.println(help);
    }

    private static void printheader(String optype) {
        System.out.print(optype + "-threads,lapsed-ms,total-ops,buffer,");
        System.out.print(comment + "~avr-ops-T-s,");
        System.out.print(comment + "~total-ops-s,");
        System.out.print(comment + "~MB-T-s,");
        System.out.print(comment + "~total-MB-s,");
        System.out.print(comment + "~avr-iterations-s,");
        System.out.print(comment + "~avr-ms-op,");
        System.out.println();
    }

    private static void printdata(int threads, long lapsedtime, long iterations, long buffer) {
        float lapsed;
        System.out.print(threads + ",");
        System.out.print(lapsedtime + ",");
        System.out.print((threads * iterations) + ",");
        System.out.print(buffer + ",");
        if (lapsedtime == 0) {
            lapsed = 1;
        } else {
            lapsed = (float) lapsedtime;
        }
        System.out.print(((float) (iterations * threads) / lapsed) * 1000 + ",");
        System.out.print(((float) (iterations * threads) / lapsed) * threads * 1000 + ",");
        System.out.print(((float) (buffer * threads) / lapsed) + ",");
        System.out.print(((float) (buffer * threads) / lapsed) * threads + ",");
        System.out.print(((float) iterations / lapsed) * 1000 + ",");
        System.out.print(((float) lapsedtime / (float) (iterations * threads)) + ",");
        System.out.println();
    }
}
