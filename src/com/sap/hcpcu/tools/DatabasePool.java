package com.sap.hcpcu.tools;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import javax.sql.DataSource;


public class DatabasePool {
  /**
   * The Log4J Logger.
   */
  static Logger                log              = LoggerFactory.getLogger(DatabasePool.class);

  private static DatabasePool  instance         = null;
  private static Object        mutex            = new Object();

  private DataSource           dataSource;
  private boolean              containerManaged = true;
  
  private String persistenceUnit = null;


  /**
   * The Entity Manager Factory
   */
  private EntityManagerFactory emf              = null;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public DatabasePool(final String dataSource, final String persistenceUnit, final String driver, final String url, final String username, final String password, final int initialConnections, final int maxConnections, boolean waitIfBusy) throws SQLException, NamingException {
    this.persistenceUnit = persistenceUnit;
    
    /*
     * Testing container managed scenario.
     *
     * dataSource should be given like "java:comp/env/jdbc/DefaultDB"
     */
    if (containerManaged && (dataSource != null) && !"".equals(dataSource)) {
      final InitialContext initialContext = new InitialContext();
      if (initialContext != null) {
        try {
          /*
           * The next line will fail with a NoInitialContextException if
           * we are not in a container managed scenario.
           */
          log.debug("+ Using data source " + dataSource);

          DataSource ds = (DataSource) initialContext.lookup(dataSource);
          try {
            ds.getConnection();
          } catch (SQLException e) {
            log.debug("+ Container managed data source definition does not deliver a valid data source. Will use plain JDBC connections.");
            containerManaged = false;
          }

          if (containerManaged) {
            this.dataSource = ds;

            if (persistenceUnit != null) {
              final Map properties = new HashMap();
              properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, ds);
              emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
            }

            log.debug("+ Database is container managed at " + dataSource);

            return;
          }
        } catch (NoInitialContextException nice) {
          log.debug("+ Database is not container managed. Will use plain JDBC connections.");
          containerManaged = false;
        }
      }
    } else {
      log.debug("+ No data source information given. Assuming database is not container managed. Will use plain JDBC connections.");
      containerManaged = false;
    }

    /*
     * Not container managed scenario
     */
    try {
      Class.forName(driver);
    } catch (ClassNotFoundException cnfe) {
      throw (new SQLException("Cannot find class for driver: " + driver));
    }

    /*
     * TODO: check whether initialSize really does take effect.
     *
     * MySQL variant:
     * show processlist;
     * SHOW STATUS WHERE `variable_name` = 'Threads_connected';
     *
     */
    final Properties properties = new Properties();

    properties.setProperty("url", url);
    properties.setProperty("user", username);
    properties.setProperty("password", password);
    properties.setProperty("initialSize", "" + initialConnections);
    properties.setProperty("maxTotal", "" + maxConnections);
    properties.setProperty("maxWaitMillis", waitIfBusy ? "-1 " : "0");
    properties.setProperty("accessToUnderlyingConnectionAllowed", "true");
    properties.setProperty("testOnBorrow", "true");

    final ConnectionFactory              connectionFactory         = new DriverManagerConnectionFactory(url, properties);

    final PoolableConnectionFactory      poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);

    final ObjectPool<PoolableConnection> connectionPool            = new GenericObjectPool<PoolableConnection>(poolableConnectionFactory);

    poolableConnectionFactory.setPool(connectionPool);

    final PoolingDataSource<PoolableConnection> ds = new PoolingDataSource<PoolableConnection>(connectionPool);

    this.dataSource = ds;

    if (persistenceUnit != null) {
      final Map properties2 = new HashMap();
      properties2.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, ds);
      emf = Persistence.createEntityManagerFactory(persistenceUnit, properties2);
    }
  }

  public static DatabasePool getInstance(final String dbSource, final String persistenceUnit, final String driver, final String url, final String username, final String password, final int initialConnections, final int maxConnections, final boolean waitIfBusy) throws SQLException, NamingException {
    if (instance == null) {
      synchronized (mutex) {
        if (instance == null) {
          instance = new DatabasePool(dbSource, persistenceUnit, driver, url, username, password, initialConnections, maxConnections, waitIfBusy);
        }
      }
    }

    return instance;
  }


  public DataSource getDataSource() {
    return this.dataSource;
  }


  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }


  public EntityManager getEntityManager() {
    return this.emf.createEntityManager();
  }

  public EntityManagerFactory getEntityManagerFactory() {
    return this.emf;
  }
  
  public String getPersistenceUnit() {
    return this.persistenceUnit;
  }

  /**
   * Lookup something from the Database.
   *
   * @param sql The SQL Statement
   * @return List of Lists of Strings holding the values.
   */
  @SuppressWarnings("rawtypes")
  public List selectFromDB(String sql) {
    return selectFromDB(sql, null, null, false, 0);
  }


  /**
   * Lookup something from the database, return it as JSON array.
   *
   * @param sql The SQL Statement.
   * @return The JSON array, with table headers as labels.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public String selectJSONFromDB(final String sql) {
    final List<String> headers = new ArrayList<String>();
    final List<List>   tbl     = selectFromDB(sql, null, headers, false, 0);

    final int          cols    = headers.size();
    final int          rows    = tbl.size();

    final StringBuffer sb      = new StringBuffer();
    sb.append("[");

    for (int i = 0; i < rows; i++) {
      final List row = tbl.get(i);

      sb.append("{");

      for (int j = 0; j < cols; j++) {
        sb.append("\"");
        sb.append(headers.get(j));
        sb.append("\": \"");
        sb.append(row.get(j));
        sb.append("\"");
        if (j < (cols - 1)) {
          sb.append(",");
        }
      }

      sb.append("}");
      if (i < (rows - 1)) {
        sb.append(",");
      }
    }

    sb.append("]");

    return sb.toString();
  }


  /**
   * Lookup something from the Database.
   *
   * @param sql The SQL Statement
   * @param parameters A List containing in each row an object that shall be parsed
   *        into the statement. Null, if no such objects are needed.
   * @return List of Lists of Strings holding the values.
   */
  @SuppressWarnings("rawtypes")
  public List selectFromDB(final String sql, final List parameters) {
    return selectFromDB(sql, parameters, null, false, 0);
  }


  /**
   * Lookup something from the Database
   *
   * @param sql The SQL Statement
   * @param asObjects True if the result sets are to be returned as
   *        their original object types, false if they are to be returned as Strings.
   * @return List of Lists of Objects or Strings, depending on the asObjects parameter,
   *         holding the values.
   */
  @SuppressWarnings("rawtypes")
  public List selectFromDB(final String sql, final boolean asObjects) {
    return selectFromDB(sql, null, null, asObjects, 0);
  }


  /**
   * Lookup something from the Database
   *
   * @param sql The SQL Statement
   * @param parameters A List containing in each row an object that shall be parsed
   *        into the statement. Null, if no such objects are needed.
   * @param asObjects True if the result sets are to be returned as
   *        their original object types, false if they are to be returned as Strings.
   * @return List of Lists of Objects or Strings, depending on the asObjects parameter,
   *         holding the values.
   */
  @SuppressWarnings("rawtypes")
  public List selectFromDB(final String sql, final List parameters, final boolean asObjects) {
    return selectFromDB(sql, parameters, null, asObjects, 0);
  }


  /**
   * Update statement for the Database
   * @param sql The update SQL statement
   */
  public void updateDB(final String sql) {
    updateDB(sql, null, 0);
  }


  /**
   * Update statement for the Database.
   * @param sql The update SQL statement
   * @param parameters A list containing in each row an object that shall be parsed into the statement.
   *                   Null, if no such objects are needed.
   */
  @SuppressWarnings("rawtypes")
  public void updateDB(final String sql, final List parameters) {
    updateDB(sql, parameters, 0);
  }


  /**
   * Lookup something from the Database
   *
   * @param sql The SQL Statement
   * @param parameters A List containing in each row an object that shall be parsed
   *        into the statement. Null, if no such objects are needed.
   * @param columnHeaders An optional List that will be filled with the column headers.
   *        If not needed, pass null
   * @param asObjects True if the result sets are to be returned as
   *        their original object types, false if they are to be returned as Strings.
   * @param attempt The selection attempt. See {@link #maxAttempts}.
   * @return List of Lists of Objects or Strings, depending on the asObjects parameter,
   *         holding the values.
   */
  @SuppressWarnings({ "rawtypes" })
  private List selectFromDB(final String sql, final List parameters, final List<String> columnHeaders, final boolean asObjects, final int attempt) {
    log.debug("> Selecting from Database");

    Connection                 con    = null;
    PreparedStatement          pst    = null;
    ResultSet                  rs     = null;
    final ArrayList<ArrayList> result = new ArrayList<ArrayList>();

    try {
      /*
       * Get the connection.
       */
      log.debug("+ Get Connection");
      con = getConnection();

      /*
       * Prepare the statement.
       */
      log.debug("+ Preparing Statement: " + sql);
      pst = con.prepareStatement(sql);

      /*
       * If we have parameters to set, we do
       * so now.
       */
      if (parameters != null) {
        int i = 1;
        for (final Object object : parameters) {
          final String type = object.getClass().getName();

          /*
           * Decide, what to do. this list can be
           * extended in the future.
           */
          if ("java.lang.String".equals(type)) {
            pst.setString(i, "" + object);
          } else if ("java.util.Date".equals(type)) {
            pst.setTimestamp(i, new Timestamp(((java.util.Date) object).getTime()));
          } else if ("java.sql.Array".equals(type)) {
            pst.setArray(i, (java.sql.Array) object);
          } else if ("java.math.BigDecimal".equals(type)) {
            pst.setBigDecimal(i, (java.math.BigDecimal) object);
          } else if ("java.sql.Blob".equals(type)) {
            pst.setBlob(i, (java.sql.Blob) object);
          } else if ("java.lang.Boolean".equals(type)) {
            pst.setBoolean(i, ((Boolean) object).booleanValue());
          } else if ("java.sql.Clob".equals(type)) {
            pst.setClob(i, (java.sql.Clob) object);
          } else if ("java.sql.Date".equals(type)) {
            pst.setDate(i, (java.sql.Date) object);
          } else if ("java.lang.Double".equals(type)) {
            pst.setDouble(i, ((Double) object).doubleValue());
          } else if ("java.lang.Float".equals(type)) {
            pst.setFloat(i, ((Float) object).floatValue());
          } else if ("java.lang.Integer".equals(type)) {
            pst.setInt(i, ((Integer) object).intValue());
          } else if ("java.lang.Long".equals(type)) {
            pst.setLong(i, ((Long) object).longValue());
          } else if ("java.sql.Ref".equals(type)) {
            pst.setRef(i, (java.sql.Ref) object);
          } else if ("java.lang.Short".equals(type)) {
            pst.setShort(i, ((Short) object).shortValue());
          } else if ("java.sql.Time".equals(type)) {
            pst.setTime(i, (java.sql.Time) object);
          } else if ("java.sql.Timestamp".equals(type)) {
            pst.setTimestamp(i, (java.sql.Timestamp) object);
          } else if ("java.lang.Object".equals(type)) {
            pst.setObject(i, object);
          } else {
            pst.setString(i, "" + object);
          }

          i++;
        }
      }

      /*
       * Execute the query.
       */
      try {
        log.debug("+ Execute Query: " + sql);
        rs = pst.executeQuery();
      } catch (SQLException e) {
        final String msg = e.getMessage();
        if ((msg != null) && msg.contains("No ResultSet set was produced")) {
          ; // workaround for MS SQL Server stupidity
        } else {
          throw (e);
        }
      }

      /*
       * Get the metadata, if any.
       */
      ResultSetMetaData rsmd = null;
      try {
        if (rs != null) {
          rsmd = rs.getMetaData();
        }
      } catch (SQLException sqle) {
        /*
         * We assume that this exception is
         * caused by a delete, insert or
         * update query.
         */
      }

      /*
       * Get the data.
       */
      if (rsmd != null) {
        final int cols = rsmd.getColumnCount();

        if (columnHeaders != null) {
          for (int j = 1; j <= cols; j++) {
            /*
             * 20150613 MN:
             * Adding toLowerCase, as HANA reports all column names
             * in all caps.
             */
            columnHeaders.add(rsmd.getColumnName(j).toLowerCase());
          }
        }

        while (rs.next()) {
          final ArrayList<Object> row = new ArrayList<Object>(cols);

          for (int j = 1; j <= cols; j++) {
            if (asObjects) {
              row.add(rs.getObject(j));
            } else {
              /*
               * 20150613 MN:
               * Adding .trim, as HANA does not trim. Should be
               * a nicer solution around, this is a hack.
               */
              row.add(rs.getString(j).trim());
            }
          }

          result.add(row);
        }
      }
    } catch (SQLException sqle) {
      /*
       * If we had an error, we may
       * want to retry.
       */
      log.error("! Error selecting from the database: " + sqle.getMessage());
      log.error("! The SQL statement was: \n\t" + sql);
    } finally {
      /*
       * Close the open cursors.
       */
      try {
        if (rs != null) {
          rs.close();
        }

        if (pst != null) {
          pst.close();
        }
      } catch (SQLException sqle) {
        log.error("! Error closing the result set: " + sqle.getMessage());
      }

      try {
        con.close();
      } catch (SQLException e) {
        log.error("! Error closing the connection: " + e.getMessage());
      }
    }

    log.debug("< Selecting from Database");

    return (result);
  }


  /**
   * Update something in the Database
   *
   * @param sql The update SQL Statement
   * @param parameters A List containing in each row an object that shall be parsed
   *        into the statement. Null, if no such objects are needed.
   * @param attempt The selection attempt. See {@link #maxAttempts}.
   */
  @SuppressWarnings("rawtypes")
  private void updateDB(final String sql, final List parameters, final int attempt) {
    log.debug("> Updating the Database");

    Connection        con = null;
    PreparedStatement pst = null;

    try {
      /*
       * Get the connection.
       */
      log.debug("+ Get Connection");
      con = getConnection();

      /*
       * Prepare the statement.
       */
      pst = con.prepareStatement(sql);

      /*
       * If we have parameters to set, we do
       * so now.
       */
      if (parameters != null) {
        int i = 1;
        for (final Object object : parameters) {
          final String type = object.getClass().getName();

          /*
           * Decide, what to do. this list can be
           * extended in the future.
           */
          if ("java.lang.String".equals(type)) {
            pst.setString(i, "" + object);
          } else if ("java.util.Date".equals(type)) {
            pst.setTimestamp(i, new Timestamp(((java.util.Date) object).getTime()));
          } else if ("java.sql.Array".equals(type)) {
            pst.setArray(i, (java.sql.Array) object);
          } else if ("java.math.BigDecimal".equals(type)) {
            pst.setBigDecimal(i, (java.math.BigDecimal) object);
          } else if ("java.sql.Blob".equals(type)) {
            pst.setBlob(i, (java.sql.Blob) object);
          } else if ("java.lang.Boolean".equals(type)) {
            pst.setBoolean(i, ((Boolean) object).booleanValue());
          } else if ("java.sql.Clob".equals(type)) {
            pst.setClob(i, (java.sql.Clob) object);
          } else if ("java.sql.Date".equals(type)) {
            pst.setDate(i, (java.sql.Date) object);
          } else if ("java.lang.Double".equals(type)) {
            pst.setDouble(i, ((Double) object).doubleValue());
          } else if ("java.lang.Float".equals(type)) {
            pst.setFloat(i, ((Float) object).floatValue());
          } else if ("java.lang.Integer".equals(type)) {
            pst.setInt(i, ((Integer) object).intValue());
          } else if ("java.lang.Long".equals(type)) {
            pst.setLong(i, ((Long) object).longValue());
          } else if ("java.sql.Ref".equals(type)) {
            pst.setRef(i, (java.sql.Ref) object);
          } else if ("java.lang.Short".equals(type)) {
            pst.setShort(i, ((Short) object).shortValue());
          } else if ("java.sql.Time".equals(type)) {
            pst.setTime(i, (java.sql.Time) object);
          } else if ("java.sql.Timestamp".equals(type)) {
            pst.setTimestamp(i, (java.sql.Timestamp) object);
          } else if ("java.lang.Object".equals(type)) {
            pst.setObject(i, object);
          } else {
            pst.setString(i, "" + object);
          }

          i++;
        }
      }

      /*
       * Execute the query.
       */
      try {
        log.debug("+ Execute Query: " + sql);
        pst.executeUpdate();
      } catch (SQLException e) {
        final String msg = e.getMessage();
        if ((msg != null) && msg.contains("No ResultSet set was produced")) {
          ; // workaround for MS SQL Server stupidity
        } else {
          throw (e);
        }
      }
    } catch (SQLException sqle) {
      /*
       * If we had an error, we may
       * want to retry.
       */
      log.error("! Error updating the database: " + sqle.getMessage());
      log.error("! The SQL statement was: \n\t" + sql);
    } finally {
      /*
       * Close the open cursors.
       */
      try {
        if (pst != null) {
          pst.close();
        }
      } catch (SQLException sqle) {
        log.error("! Error closing the result set: " + sqle.getMessage());
      }

      try {
        con.close();
      } catch (SQLException e) {
        log.error("! Error closing the connection: " + e.getMessage());
      }
    }

    log.debug("< Updating the Database");
  }


  @SuppressWarnings("rawtypes")
  public static void main(final String[] args) throws Exception {
    final DatabasePool db = DatabasePool.getInstance(null, null, "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/hcpcu", "hcpcu", "hcpcu", 2, 5, true);

    final List         l  = db.selectFromDB("select count(id) from transactions");

    System.out.println(l);
  }
}
