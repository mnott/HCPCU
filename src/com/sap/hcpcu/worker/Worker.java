package com.sap.hcpcu.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class Worker {
  /**
   * The Log4J Logger
   */
  static Logger             log      = LoggerFactory.getLogger(Worker.class);

  private static Worker     instance = null;
  private static Object     mutex    = new Object();

  private ArrayList<String> batch;

  public Worker(final String some, final String config, final String parameters) {}

  public static Worker getInstance(final String some, final String config, final String parameters) {
    if (instance == null) {
      synchronized (mutex) {
        if (instance == null) {
          instance = new Worker(some, config, parameters);
        }
      }
    }

    return instance;
  }


  public void doSomething(String what) {
    log.debug("+ Doing something with " + what);
  }


  public void prepareBatch() {
    log.debug("+ Preparing Batch");

    this.batch = new ArrayList<String>();
  }


  public void addToBatch(String what) {
    log.debug("+ Adding to Batch: " + what);
    this.batch.add(what);
  }


  public void executeBatch() {
    log.debug("> ExecuteBatch");
    for (final String what : this.batch) {
      log.debug("+ Batch executing: " + what);
    }

    log.debug("< ExecuteBatch");
  }


  public void closeBatch() {
    log.debug("+ Closing Batch");
  }
}
