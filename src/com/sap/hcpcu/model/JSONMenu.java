package com.sap.hcpcu.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.sap.hcpcu.application.Application;
import com.sap.hcpcu.application.Configuration;
import com.sap.hcpcu.application.Service;
import com.sap.hcpcu.tools.DatabasePool;
import com.sap.hcpcu.tools.FileLoader;
import com.sap.hcpcu.tools.StringUtility;


public class JSONMenu extends JSONModel {
  /**
   * Logger for this class
   */
  private static final transient Logger log  = LoggerFactory.getLogger(Configuration.class);

  @SuppressWarnings("unused")
  private JsonArray                     menu;

  public JSONMenu(Application application, HttpServletRequest request, HttpServletResponse response) {
    this(application, request, response, "");
  }


  public JSONMenu(Application application, HttpServletRequest request, HttpServletResponse response, String filename) {
    super(application, request, response);

    final DatabasePool db     = Service.getDatabasePool();

    String             json   = null;

    String             result = null;

    if (db != null) {
      json   = db.selectJSONFromDB("select id, targetPage, targetPageType, title, icon, info, infoState, number, numberUnit from menu order by id");

      result = json;

      final Pattern p = Pattern.compile("\"@(.*?)@\"");
      final Matcher m = p.matcher(json);

      while (m.find()) {
        final int c = m.groupCount();
        for (int i = 1; i <= c; i++) {
          final String queryReference = m.group(i);
          
          final String       valueFromDB    = ""+Service.getCachedValueFromDB(queryReference);

          result = StringUtility.replace(result, "@" + queryReference + "@", valueFromDB, true);
        }
      }

      log.debug("+ Menu before parsing: " + json);
      log.debug("+ Menu after  parsing: " + result);

      this.menu = new JsonParser().parse(result).getAsJsonArray();
      // System.out.println("Loaded from DB: " + json);
    } else {
      json      = new String(FileLoader.loadBytes(filename));
      this.menu = new JsonParser().parse(json).getAsJsonObject().getAsJsonArray("menu");
      // System.out.println("Loaded from file: " + json);
    }
  }
}
