package com.sap.hcpcu.application;

import com.sap.hcpcu.model.JSONModel;
import com.sap.hcpcu.tools.Cache;
import com.sap.hcpcu.tools.FileLoader;
import com.sap.hcpcu.tools.HttpRequestAnalyzer;
import com.sap.hcpcu.tools.StringUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Application extends HttpServlet {
  private static final long   serialVersionUID = 1L;


  /**
   * Logger for this class
   */
  private static final Logger log              = LoggerFactory.getLogger(Configuration.class);

  public Application() throws Exception {
    super();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }


  public String getAttribute(String attributeName) {
    return Service.getAttribute(attributeName);
  }


  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    final boolean debug      = "true".equals(request.getParameter("debug"));
    final String  type       = request.getParameter("type");
    final String  parameters = StringUtility.getNonNullValue(request.getParameter("parameters"));

    if (debug) {
      log.debug("+ Incoming request for " + parameters + " as " + type);
    }

    if (!"raw".equals(type) && !"cfg".equals(type)) {
      JSONModel model = null;
      try {
        if (type != null) {
          /*
           * We have a String parameter to the constructor,
           * so we need to call a constructor with a String
           * parameter, if any.
           */
          final Class    klass = Class.forName(type.contains(".") ? type : ("com.sap.hcpcu.model." + type));

          /*
           * We split our value parameter at the ; to allow
           * for passing multiple parameters. We only expect
           * those to be all strings, which is a reasonable
           * assumption in a get scenario; the receiving
           * constructor could convert those anyway, if
           * needed.
           *
           * Also, we need to pass in the Application (this)
           * to the class we lookup, as it may e.g. require
           * to use the DatabasePool. Therefore, we use, as
           * convention, the first parameter to always be the
           * Application.
           */
          final Object[] vargs = StringUtility.split(parameters, ";");
          final Object[] aargs = new Object[vargs.length + 3];

          final Class[]  kargs = new Class[aargs.length];
          kargs[0] = Application.class;
          aargs[0] = this;
          kargs[1] = HttpServletRequest.class;
          aargs[1] = request;
          kargs[2] = HttpServletResponse.class;
          aargs[2] = response;

          for (int i = 0; i < vargs.length; i++) {
            kargs[(i + 3)] = String.class;
            aargs[(i + 3)] = vargs[i];
          }

          final Constructor konstr = klass.getDeclaredConstructor(kargs);

          if (konstr != null) {
            /*
             * If the class has a constructor with a String parameter
             */
            model = (JSONModel) konstr.newInstance(aargs);
          } else {
            /*
             * We use the default constructor.
             */
            model = (JSONModel) Class.forName(type.contains(".") ? type : ("com.sap.ita.model." + type)).newInstance();
            model.setApplication(this);
            model.setRequest(request);
            model.setResponse(response);
          }
        }
      } catch (InstantiationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      String json = "" + model;

      if (json != null) {
        json = "{ \"response\" : " + json + ", \"success\" : \"true\" }";

        writeResponse(request, response, json, debug);
      }
    } else if ("raw".equals(type)) {
      /*
       * Get a raw file
       */
      final String result = new String(FileLoader.loadBytes(parameters));
      writeResponse(request, response, result, debug);
    } else if ("cfg".equals(type)) {
      /*
       * Get a configuration parameter
       */
      final String result = Service.getAttribute(parameters);
      writeResponse(request, response, result, debug);
    }
  }


  private void writeResponse(HttpServletRequest request, HttpServletResponse response, String what, boolean debug) throws ServletException, IOException {
    if (debug) {
      HttpRequestAnalyzer.printHeaderInfo(request, 200);
      System.out.println("Working Directory: " + this.getClass().getClassLoader().getResource("").getPath());
      System.out.println("------------------------------------------");
      System.out.println("Result:");
      System.out.println("------------------------------------------");
      System.out.println(what);
      System.out.println("------------------------------------------");
    }

    final PrintWriter out = response.getWriter();
    response.setContentType("text/html");
    response.setHeader("Cache-control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Methods", "POST");
    response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    response.setHeader("Access-Control-Max-Age", "86400");
    out.print(what);
    out.close();
  }
}
