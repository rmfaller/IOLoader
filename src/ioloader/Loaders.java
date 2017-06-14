/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ioloader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import static java.lang.Thread.yield;
import java.util.Date;
import java.util.Random;

/**
 *
 * @author rmfaller
 */
class Loaders extends Thread {

    private long lapsedtime;
    private final int threadid;
    private final String filename;
    private final long loops;
    private final Random randomgen = new Random();
    private final String optype;
    private final byte[] bitearray;
    private final long buffersize;

    Loaders(int t, long iterations, long buffersize, String workingdirectory, String optype) {
        this.threadid = t;
        this.optype = optype;
        Integer randomvalue;
        if (this.optype.compareTo("w") == 0) {
            this.filename = workingdirectory + "/ioloaderfile" + threadid;
        } else {
            File dir = new File(workingdirectory);
            File[] files = dir.listFiles((File dir1, String name1) -> name1.toLowerCase().startsWith("ioloader"));
            randomvalue = files.length - 1;
            this.filename = workingdirectory + "/" + files[randomgen.nextInt(randomvalue)].getName();
        }
        this.loops = iterations;
        this.buffersize = buffersize;
        this.setLapsedTime(0);
        this.bitearray = new byte[(int) this.buffersize];
        for (int i = 0; i < this.buffersize; i++) {
            this.bitearray[i] = 43;
        }
    }

    private void setLapsedTime(long i) {
        this.lapsedtime = i;
    }

    public long getLapsedTime() {
        return (this.lapsedtime);
    }

    @Override
    public void run() {
        long totalbites = 0;
        RandomAccessFile raf;
        Long filepoint;
        Long filesize;
        Integer randomvalue;
        byte bites[];
        long bitesread;
        long optime = 0;
        try {
            raf = new RandomAccessFile(this.filename, "rwd");
            for (int i = 0; i < this.loops; i++) {
                long startop = (long) new Date().getTime();
                filesize = raf.length();
                if (filesize != 0) {
                    randomvalue = filesize.intValue();
                    filepoint = new Long(randomgen.nextInt(randomvalue));
                } else {
                    filepoint = new Long("0");
                }
                raf.seek(filepoint);
                if (this.optype.compareTo("w") == 0) {
                    raf.write(this.bitearray);
                    totalbites = this.bitearray.length + totalbites;
                } else {
                    bites = new byte[(int) this.buffersize];
                    bitesread = raf.read(bites);
                    totalbites = bitesread + totalbites;
                }
                long endop = (long) new Date().getTime();
                optime = (endop - startop) + optime;
                yield();
            }
            raf.close();
        } catch (IOException | NumberFormatException e) {
            System.out.println("Exception: " + e);
        }
        this.setLapsedTime(optime);
    }

}
