/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ioloader;

import java.io.File;
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
    private final long maxfilesize;
    private long transactions = 0;
    private long bytes = 0;
    private final boolean append;
    private boolean dirtyread = false;

    Loaders(int t, long iterations, long buffersize, String workingdirectory, String optype, Long maxfilesize, boolean append, boolean dirtyread) {
        this.threadid = t;
        this.optype = optype;
        this.append = append;
        this.dirtyread = dirtyread;
        Integer randomvalue;
        this.maxfilesize = maxfilesize;
        if (this.optype.compareTo("w") == 0) {
            this.filename = workingdirectory + "/ioloaderfile" + threadid;
        } else {
            File dir = new File(workingdirectory);
            File[] files = dir.listFiles((File dir1, String name1) -> name1.toLowerCase().startsWith("ioloader"));
            randomvalue = files.length - 1;
            if (randomvalue < 1) {
                randomvalue = 1;
            }
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

    private void setTransactions(long i) {
        this.transactions = i;
    }

    private void setBytes(long i) {
        this.bytes = i;
    }

    public long getTransactions() {
        return (this.transactions);
    }

    public long getBytes() {
        return (this.bytes);
    }

    public long getLapsedTime() {
        return (this.lapsedtime);
    }

    @Override
    public void run() {
        long totalbites = 0;
        RandomAccessFile raf;
        boolean bitset = true;
        Long filepoint;
        Long filesize;
        Integer randomvalue;
        byte bites[];
        long bitesread;
        long optime = 0;
        try {
            raf = new RandomAccessFile(this.filename, "rwd");
            for (int i = 0; i < this.loops; i++) {
//                File tf = new File(this.filename);
//                tf.setLastModified((long) new Date().getTime());
                long startop = (long) new Date().getTime();
                filesize = raf.length();
                if (filesize != 0) {
                    if (append) {
                        filepoint = filesize;
                    } else {
                        randomvalue = filesize.intValue();
                        filepoint = (long) randomgen.nextInt(randomvalue);
                    }
                } else {
                    filepoint = Long.valueOf("0");
                }
                if ((this.maxfilesize != 0) && (filepoint >= this.maxfilesize)) {
                    filepoint = filepoint - (this.maxfilesize - this.buffersize);
                }
                if (this.optype.compareTo("w") == 0) {
                    raf.seek(filepoint);
                    raf.write(this.bitearray);
                    totalbites = this.bitearray.length + totalbites;
                } else {
                    if (dirtyread) {
                        raf.seek(i);
                        raf.writeBoolean(bitset);
                        if (bitset) {
                            bitset = false;
                        } else {
                            bitset = true;
                        }
                    }
                    bites = new byte[(int) this.buffersize];
                    raf.seek(filepoint);
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
        this.setTransactions(loops);
        this.setBytes(totalbites);
//        System.out.println(optime + " .... " + loops + " bits = " + totalbites);
    }

}
