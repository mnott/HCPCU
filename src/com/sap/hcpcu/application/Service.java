package com.sap.hcpcu.application;

import com.sap.hcpcu.tools.Cache;
import com.sap.hcpcu.tools.DatabasePool;
import com.sap.hcpcu.tools.StringUtility;
import com.sap.hcpcu.worker.Worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import java.text.DecimalFormat;

import java.util.List;

import javax.naming.NamingException;


public class Service {
  private static Service       instance      = null;
  private static Object        mutex         = new Object();


  /**
   * The configuration holder
   */
  private static Configuration configuration = null;


  /**
   * The database pool
   */
  private static DatabasePool  databasePool  = null;


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


  public static DatabasePool getDatabasePool() {
    if ((databasePool == null) && "true".equals(getAttribute("dbuse"))) {
      log.debug("> Instantiating the Database Pool");

      /*
       * Instantiate the database pool
       *
       * dbsource=java:comp/env/jdbc/DefaultDB
       * dbdriver=com.mysql.jdbc.Driver
       * dburl=jdbc:mysql://localhost/hcpcu
       * dbuse=true
       * dbuser=hcpcu
       * dbpass=hcpcu
       * dbinitial=2
       * dbmax=5
       * dbwait=true
       */
      final String  dbSource          = getAttribute("dbsource");
      final String  dbPersistenceUnit = getAttribute("dbpersistenceunit");
      final String  dbDriver          = getAttribute("dbdriver");
      final String  dbUrl             = getAttribute("dburl");
      final String  dbUser            = getAttribute("dbuser");
      final String  dbPass            = getAttribute("dbpass");
      final int     dbInitial         = StringUtility.StringToInteger(getAttribute("dbinitial"), 1);
      final int     dbMax             = StringUtility.StringToInteger(getAttribute("dbmax"), 1);
      final boolean dbWait            = "true".equals(getAttribute("dbwait"));

      try {
        log.debug("+ Attempting to instantiate database pool using " + dbUrl);
        databasePool = DatabasePool.getInstance(dbSource, dbPersistenceUnit, dbDriver, dbUrl, dbUser, dbPass, dbInitial, dbMax, dbWait);
      } catch (SQLException e2) {
        log.error("! Error instantiating the Database Pool: " + e2.getMessage());
      } catch (NamingException e2) {
        log.error("! Error instantiating the Database Pool: " + e2.getMessage());
      }

      log.debug("< Instantiating the Database Pool...");
    }

    return databasePool;
  }


  public static Cache getCache() {
    if (cache == null) {
      cache = new Cache();
    }

    return cache;
  }


  /**
   * Get some cached value from the database.
   *
   * <pre>
   * db_transactions=select count(id) from transactions
   * db_transactions_lifetime=60
   * db_transactions_default=4,723,132
   * db_transactions_numberformat=#,###,###,##0.00
   * </pre>
   *
   * @param queryReference
   * @return
   */
  @SuppressWarnings("rawtypes")
  public static String getCachedValueFromDB(String queryReference) {
    String valueFromDB = (String) getCache().getEntry(queryReference);

    log.debug("+ Resolving: " + queryReference + ". Cached value: " + valueFromDB);

    if (valueFromDB == null) {
      final String query = getAttribute(queryReference);

      if (query != null) {
        final List tbl = getDatabasePool().selectFromDB(query);

        if ((tbl != null) && (tbl.size() > 0)) {
          final List row = (List) tbl.get(0);

          if ((row != null) && (row.size() > 0)) {
            valueFromDB = "" + row.get(0);

            final String numberFormat = getAttribute(queryReference + "_numberformat");
            if (numberFormat != null) {
              final DecimalFormat df = new DecimalFormat(numberFormat);
              valueFromDB = df.format(StringUtility.StringToInteger(valueFromDB));
            }

            final long lifetime = StringUtility.StringToInteger(getAttribute(queryReference + "_lifetime"), 120);

            getCache().setEntry(queryReference, valueFromDB, lifetime, false, false);
          }
        }
      }
    }

    if (valueFromDB == null) {
      valueFromDB = getAttribute(queryReference + "_default");
    }

    return valueFromDB;
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
