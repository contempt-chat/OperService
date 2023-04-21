package com.ircnet.service.operserv;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ScannerThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerThread.class);
    private static ScannerThread instance;

    class QueueEntry {
        private Runnable runnable;

        /**
         * Contains the stack trace.
         */
        private Exception pseudoException;

        public QueueEntry(Runnable runnable) {
            this.runnable = runnable;
            this.pseudoException = new Exception("Stack trace");
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public Exception getPseudoException() {
            return pseudoException;
        }
    }

    private List<QueueEntry> queue;

    public static ScannerThread getInstance() {
        if(instance == null) {
            instance = new ScannerThread();
        }

        return instance;
    }

    private ScannerThread() {
        super("ScannerThread");
        this.queue = new ArrayList<>();
    }

    public void runOnThread(Runnable runnable) {
        if(runnable == null) {
            LOGGER.error("runnable is null!", new IllegalArgumentException());
            return;
        }

        queue.add(new QueueEntry(runnable));
    }

    @Override
    public void run() {
        while(true) {
            if(!queue.isEmpty()) {
                QueueEntry queueEntry = queue.get(0);

                try {
                    Instant begin = Instant.now();
                    queueEntry.getRunnable().run();
                    Instant end = Instant.now();
                    long secondsElapsed = Duration.between(begin, end).toMillis() / 1000;

                    if(secondsElapsed > 3) {
                        LOGGER.warn("Task took {} seconds", secondsElapsed, queueEntry.getPseudoException());
                    }
                }
                catch (Exception e) {
                    if(e != null) {
                        LOGGER.error("An error occurred {}", ExceptionUtils.getStackTrace(e));
                    }

                    LOGGER.error("An error occurred", queueEntry.getPseudoException());
                }
                finally {
                    queue.remove(0);
                }
            }
            else {
                try {
                    sleep(500);
                }
                catch (InterruptedException e) {
                }
            }
        }
    }
}
