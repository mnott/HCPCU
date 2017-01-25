package com.sap.hcpcu.application;

import com.sap.hcpcu.tools.Cache;
import com.sap.hcpcu.worker.Worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Service {
  private static Service       instance      = null;
  private static Object        mutex         = new Object();

  /**
   * The configuration holder
   */
  private static Configuration configuration = null;


  /**
   * The cache
   */
  private static Cache         cache         = null;

  /**
   * Logger for this class
   */
  private final static Logger  log           = LoggerFactory.getLogger(Service.class);

  private Service() {}

  public static Service getInstance() {
    if (instance == null) {
      synchronized (mutex) {
        if (instance == null) {
          instance = new Service();
        }
      }
    }

    return instance;
  }


  public static String getAttribute(String attributeName) {
    return getConfiguration().getAttribute(attributeName);
  }


  public static Worker getWorker() {
    log.debug("> Instantiating the Worker");

    Worker       worker        = null;

    /*
     * Instantiate the worker
     */
    final String some          = getAttribute("some");
    final String configuration = getAttribute("configuration");
    final String parameters    = getAttribute("parameters");

    try {
      log.debug("+ Attempting to instantiate worker pool using " + some + " " + configuration + " " + parameters);
      worker = Worker.getInstance(some, configuration, parameters);
    } catch (Exception e) {
      log.error("! Error instantiating the Worker: " + e.getMessage());
    }

    log.debug("< Instantiating the Worker...");

    return worker;
  }


  public static Cache getCache() {
    if (cache == null) {
      cache = new Cache();
    }

    return cache;
  }


  public static void doWork(String what) {
    log.debug("> Doing work: " + what);

    getWorker().doSomething(what);

    log.debug("< Doing work");
  }


  public static Configuration getConfiguration() {
    if (configuration == null) {
      configuration = new Configuration();
      try {
        configuration.init();
      } catch (Exception e) {
        log.error("! Error reading the configuration: " + e.getMessage());
        e.printStackTrace();
      }
    }

    return configuration;
  }
}
