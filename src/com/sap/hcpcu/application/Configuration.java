package com.sap.hcpcu.application;

import com.sap.hcpcu.tools.FileLoader;
import com.sap.hcpcu.tools.StringUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;


public class Configuration {
  /**
   * Logger for this class
   */
  private static final Logger     log              = LoggerFactory.getLogger(Configuration.class);


  /**
   * Default config file name
   */
  public static final String      CONFIG_FILE_NAME = "app.properties";

  /**
   * The program parameters.
   */
  private HashMap<String, String> parameters       = new HashMap<String, String>();


  /**
   * The configuration file name, if any.
   */
  private String                  propertiesFile   = null;

  /**
   * The configuration parameters, if any.
   */
  @SuppressWarnings("unused")
  private String[]                args = null;


  /**
   * Default constructor.
   */
  public Configuration() {}

  /**
   * Get the properties file name.
   *
   * @return Returns the propertiesFile.
   */
  public String getPropertiesFile() {
    return propertiesFile;
  }


  /**
   * Initialize the application.
   */
  public void init() throws Exception {
    /*
     * Even if the constructor had set the name
     * of the properties file, we are able to
     * overwrite that name by the "cfg" command
     * line parameter.
     */
    this.propertiesFile = (String) getAttribute("cfg");
    if ((this.propertiesFile == null) || "".equals(this.propertiesFile)) {
      this.propertiesFile = Configuration.CONFIG_FILE_NAME;
    }

    readConfiguration(this.propertiesFile);
  }


  /**
   * Initialize the application.
   *
   * @param configFile The file name to read the configuration from.
   */
  public void init(String configFile) throws Exception {
    readConfiguration(configFile);
  }


  /**
   * Initialize the application.
   *
   * @param args The command line parameters.
   */
  public void init(String[] args) throws Exception {
    /*
     * Set this object's args
     */
    this.args = args;

    /*
     * Get the command line Parameters.
     */
    getCommandLineParameters(args);

    /*
     * Even if the constructor had set the name
     * of the properties file, we are able to
     * overwrite that name by the "cfg" command
     * line parameter.
     */
    this.propertiesFile = (String) getAttribute("cfg");
    if ((this.propertiesFile == null) || "".equals(this.propertiesFile)) {
      this.propertiesFile = Configuration.CONFIG_FILE_NAME;
    }

    readConfiguration(this.propertiesFile);
  }


  /**
   * Get the command line parameters into the
   * parameters HashMap.
   *
   * @param args The command line parameters.
   */
  protected void getCommandLineParameters(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String parametername  = StringUtility.getParameter(args[i], true, false);
      String parametervalue = StringUtility.getParameter(args[i], false, false);
      if ((parametername != null) && (parametervalue != null)) {
        setAttribute(parametername, parametervalue);
      }
    }
  }


  /**
   * Get an Application Attribute.
   * @param par The Attribute parameter name, not case sensitive.
   * @return The Attribute parameter value.
   */
  public String getAttribute(String par) {
    final String value = this.parameters.get(par.toUpperCase());

    return value;
  }


  /**
   * Get the names of the defined attributes.
   *
   * @return The names of the defined attributes.
   */
  @SuppressWarnings("rawtypes")
  public Enumeration getAttributeNames() {
    return Collections.enumeration(this.parameters.keySet());
  }


  /**
   * Set an Application Attribute.
   *
   * Use with great care. This can open a can of worms
   * in terms of concurrency.
   *
   * @param par The Attribute parameter name, not case sensitive.
   * @param val The Attribute parameter value.
   */
  public void setAttribute(String par, String val) {
    if ((null != par) && (null != val)) {
      this.parameters.put(par.toUpperCase(), val);
    }
  }


  /**
   * Read the configuration
   * properties file.
   *
   * @param filename The configuration file name.
   */
  @SuppressWarnings("rawtypes")
  protected void readConfiguration(String filename) {
    /*
     * Read the configuration file
     */
    log.debug("> readConfiguration");
    log.debug("> Loading configuration from " + filename);

    final Properties p = FileLoader.loadProperties(filename);
    if (p == null) {
      log.error("! Error reading Configuration from file " + filename);
      log.debug("< readConfiguration");
      log.debug("< Application Initialisation");

      return;
    }

    /*
     * Convert property names to uppercase
     */
    final TreeMap<String, String> uppercaseConfigurationParameters = new TreeMap<String, String>();
    for (final Iterator it = p.keySet().iterator(); it.hasNext();) {
      final String pName  = (String) it.next();
      final String pValue = (String) p.get(pName);
      uppercaseConfigurationParameters.put(pName.toUpperCase(), pValue);
    }

    /*
     * We save the parameters that already are in the
     * HashMap, i.e. which have been passed in as command
     * line parameters
     */
    final TreeMap<String, String> uppercaseCommandLineParameters = new TreeMap<String, String>();
    for (final Iterator it = this.parameters.keySet().iterator(); it.hasNext();) {
      final String pName  = (String) it.next();
      final String pValue = (String) this.parameters.get(pName);
      uppercaseCommandLineParameters.put(pName.toUpperCase(), pValue);
    }

    final HashMap<String, String> upperCaseParameters = new HashMap<String, String>();

    for (final String key : uppercaseConfigurationParameters.keySet()) {
      final String value = uppercaseConfigurationParameters.get(key);
      upperCaseParameters.put(key, value);
      log.debug("+ Configuration Parameter " + key + "=" + value);
      if ((key.length() > 4) && key.substring(0, 4).equalsIgnoreCase("SYS.")) {
        System.setProperty(key.substring(4), value);
        log.debug("+ Java System   Parameter " + key.substring(4) + "=" + value);
      }
    }

    for (final String key : uppercaseCommandLineParameters.keySet()) {
      final String value = uppercaseCommandLineParameters.get(key);
      upperCaseParameters.put(key, value);
      log.debug("+ Command Line  Parameter " + key + "=" + value);
      if ((key.length() > 4) && key.substring(0, 4).equalsIgnoreCase("SYS.")) {
        System.setProperty(key.substring(4), value);
        log.debug("+ Java System   Parameter " + key.substring(4) + "=" + value);
      }
    }

    this.parameters = upperCaseParameters;
    log.debug("< readConfiguration");
  }
}
