package com.sap.hcpcu.tools;

import java.io.IOException;

import java.security.Principal;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * This class provides for a request analyzer.<p>
 *
 * You can either call the static method {@link HttpRequestAnalyzer#printHeaderInfo(HttpServletRequest)}
 * from any point in your web application, or you include the analyzer as a filter in <code>web.xml</code>:
 *
 * <xmp>
 *   <filter>
 *     <filter-name>HttpRequestAnalyzer</filter-name>
 *     <filter-class>com.bo.bointerface.tools.http.HttpRequestAnalyzer</filter-class>
 *     <init-param>
 *       <!-- Show user principal; default: false -->
 *       <param-name>user_principal</param-name>
 *       <param-value>false</param-value>
 *     </init-param>
 *     <init-param>
 *       <!-- Show request parameters; default: true -->
 *       <param-name>parameters</param-name>
 *       <param-value>true</param-value>
 *     </init-param>
 *     <init-param>
 *       <!-- Show request attributes; default: true -->
 *       <param-name>attributes</param-name>
 *       <param-value>true</param-value>
 *     </init-param>
 *     <init-param>
 *       <!-- Show request headers; default: true -->
 *       <param-name>header</param-name>
 *       <param-value>true</param-value>
 *     </init-param>
 *     <init-param>
 *       <!-- Show request cookies; default: true -->
 *       <param-name>cookies</param-name>
 *       <param-value>true</param-value>
 *     </init-param>
 *     <init-param>
 *       <!-- Show session beans; default: true -->
 *       <param-name>session</param-name>
 *       <param-value>true</param-value>
 *     </init-param>
 *     <init-param>
 *       <!-- Show application beans; default: true -->
 *       <param-name>application</param-name>
 *       <param-value>true</param-value>
 *     </init-param>
 *     <init-param>
 *       <!-- Show initialisation parameters; default: true -->
 *       <param-name>init</param-name>
 *       <param-value>true</param-value>
 *     </init-param>
 *     <init-param>
 *       <!-- Output only request endpoints; default: false -->
 *       <param-name>compressed</param-name>
 *       <param-value>false</param-value>
 *     </init-param>
 *   </filter>
 *   <filter-mapping>
 *     <filter-name>HttpRequestAnalyzer</filter-name>
 *     <url-pattern>/*</url-pattern>
 *   </filter-mapping>
 * </xmp>
 * </p>
 *
 * If you call the header analyzer as a filter, you can selectively
 * switch off information that you do not want.<p>
 *
 * The output is printed to System.out.
 */
public class HttpRequestAnalyzer implements Filter {
  /**
   * Maximum length of a string to output.
   * The idea is that for example the session
   * id from BOReport may be a pretty long string.
   */
  private int          maxLength = 200;

  /**
   * FilterConfig to get initialization parameters.
   */
  private FilterConfig config    = null;

  /**
   * Initialize the filter.
   */
  public void init(FilterConfig config) throws ServletException {
    String maxLength = config.getInitParameter("maxlength");
    if (maxLength != null) {
      this.maxLength = StringUtility.StringToInteger(maxLength, this.maxLength);
    }

    this.config = config;
  }


  /**
   * Destroy the filter.
   */
  public void destroy() {}


  /**
   * Apply the filter.
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest  req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    HttpRequestAnalyzer.printHeaderInfo(req, this.maxLength, this.config);
    chain.doFilter(req, res);
  }


  /**
   * Print the header, cookies, etc. info.
   *
   * @param request The HTTP request object.
   */
  public static void printHeaderInfo(HttpServletRequest request) {
    printHeaderInfo(request, 200);
  }


  /**
   * Print the header, cookies, etc. info.
   *
   * @param request The HTTP request object.
   * @param maxLength The max lenght of a value to be printed
   */
  public static void printHeaderInfo(HttpServletRequest request, int maxLength) {
    printHeaderInfo(request, maxLength, null);
  }


  /**
   * Print the header, cookies, etc. info.
   *
   * @param request The HTTP request object.
   * @param maxLength The max lenght of a value to be printed
   * @param config The Filter Config
   */
  @SuppressWarnings("rawtypes")
  public static void printHeaderInfo(HttpServletRequest request, int maxLength, FilterConfig config) {
    if (config !=  null && "true".equals(config.getInitParameter("compressed"))) {
      System.out.println(request.getRequestURL());

      return;
    }

    System.out.println("");
    System.out.println("------------------------------------------");
    System.out.println("[" + request.getRequestURL() + "]");
    System.out.println("------------------------------------------");
    System.out.println("Header Data: " + new java.util.Date());
    System.out.println("------------------------------------------");
    if (config ==  null || "true".equals(config.getInitParameter("user_principal"))) {
      final Principal userPrincipal = request.getUserPrincipal();
      if (userPrincipal != null) {
        final String userName = userPrincipal.getName();

        System.out.println("User Principal Name : [" + userName + "]");
      }

      System.out.println("------------------------------------------");
    }

    TreeMap<String, String> t      = new TreeMap<String, String>();
    String                  hdrstr;

    /*
     * Parameters
     */
    if ((config == null) || ((config != null) && ("true".equals(config.getInitParameter("parameters")) || (null == config.getInitParameter("parameters"))))) {
      for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
        hdrstr = (String) e.nextElement();

        String val = request.getParameter(hdrstr);
        if ((val != null) && (val.length() > 200)) {
          val = val.substring(0, 200);
        }

        t.put(hdrstr + " [" + request.getParameter(hdrstr).getClass().getName() + "]", val);
      }

      for (Iterator it = t.keySet().iterator(); it.hasNext();) {
        String tkey = (String) it.next();
        System.out.println("par: " + tkey + "=" + (String) t.get(tkey));
      }

      t.clear();
      System.out.println("------------------------------------------");
    }

    /*
     * Attributes
     */
    if ((config == null) || ((config != null) && ("true".equals(config.getInitParameter("attributes")) || (null == config.getInitParameter("attributes"))))) {
      for (Enumeration e = request.getAttributeNames(); e.hasMoreElements();) {
        hdrstr = (String) e.nextElement();

        String val = "" + request.getAttribute(hdrstr);
        if ((val != null) && (val.length() > 200)) {
          val = val.substring(0, 200);
        }

        t.put(hdrstr + " [" + request.getAttribute(hdrstr).getClass().getName() + "]", val);
      }

      for (Iterator it = t.keySet().iterator(); it.hasNext();) {
        String tkey = (String) it.next();
        System.out.println("att: " + tkey + "=" + (String) t.get(tkey));
      }

      t.clear();
      System.out.println("------------------------------------------");
    }

    /*
     * Header
     */
    if ((config == null) || ((config != null) && ("true".equals(config.getInitParameter("header")) || (null == config.getInitParameter("header"))))) {
      for (Enumeration s = request.getHeaderNames(); s.hasMoreElements();) {
        hdrstr = (String) s.nextElement();

        String val = request.getHeader(hdrstr);
        if ((val != null) && (val.length() > 200)) {
          val = val.substring(0, 200);
        }

        t.put(hdrstr + " [" + request.getHeader(hdrstr).getClass().getName() + "]", val);
      }

      for (Iterator it = t.keySet().iterator(); it.hasNext();) {
        String tkey = (String) it.next();
        System.out.println("hdr: " + tkey + "=" + (String) t.get(tkey));
      }

      t.clear();
      System.out.println("------------------------------------------");
    }

    /*
     * Cookies
     */
    if ((config == null) || ((config != null) && ("true".equals(config.getInitParameter("cookies")) || (null == config.getInitParameter("cookies"))))) {
      Cookie[] c = request.getCookies();
      if (c != null) {
        for (int i = 0; i < c.length; i++) {
          String val = c[i].getValue();
          if ((val != null) && (val.length() > 200)) {
            val = val.substring(0, 200);
          }

          t.put(c[i].getName() + " (Domain: " + c[i].getDomain() + ")(Path: " + c[i].getPath() + ")(Max Age: " + c[i].getMaxAge() + ")" + " [" + c[i].getValue().getClass().getName() + "]", val);
        }

        for (Iterator it = t.keySet().iterator(); it.hasNext();) {
          String tkey = (String) it.next();
          System.out.println("coo: " + tkey + "=" + t.get(tkey));
        }

        t.clear();
        System.out.println("------------------------------------------");
      }
    }

    /*
     * Session
     */
    if ((config == null) || ((config != null) && ("true".equals(config.getInitParameter("session")) || (null == config.getInitParameter("session"))))) {
      final HttpSession session = request.getSession();
      for (Enumeration s = session.getAttributeNames(); s.hasMoreElements();) {
        hdrstr = (String) s.nextElement();

        String val = "" + session.getAttribute(hdrstr);
        if ((val != null) && (val.length() > 200)) {
          val = val.substring(0, 200);
        }

        t.put(hdrstr + " [" + session.getAttribute(hdrstr).getClass().getName() + "]", val);
      }

      for (Iterator it = t.keySet().iterator(); it.hasNext();) {
        String tkey = (String) it.next();
        System.out.println("ses: " + tkey + "=" + t.get(tkey));
      }

      t.clear();
      System.out.println("------------------------------------------");
    }

    if (config != null) {
      /*
       * Application parameters
       */
      if ("true".equals(config.getInitParameter("application")) || (null == config.getInitParameter("application"))) {
        for (Enumeration s = config.getServletContext().getAttributeNames(); s.hasMoreElements();) {
          hdrstr = (String) s.nextElement();

          String val = "" + config.getServletContext().getAttribute(hdrstr);
          if ((val != null) && (val.length() > 200)) {
            val = val.substring(0, 200);
          }

          t.put(hdrstr + " [" + config.getServletContext().getAttribute(hdrstr).getClass().getName() + "]", val);
        }

        for (Iterator it = t.keySet().iterator(); it.hasNext();) {
          String tkey = (String) it.next();
          System.out.println("app: " + tkey + "=" + t.get(tkey));
        }

        t.clear();
        System.out.println("------------------------------------------");
      }

      /*
       * Init parameters
       */
      if ("true".equals(config.getInitParameter("init")) || (null == config.getInitParameter("init"))) {
        for (Enumeration s = config.getServletContext().getInitParameterNames(); s.hasMoreElements();) {
          hdrstr = (String) s.nextElement();

          String val = config.getServletContext().getInitParameter(hdrstr);
          if ((val != null) && (val.length() > 200)) {
            val = val.substring(0, 200);
          }

          t.put(hdrstr + " [" + config.getServletContext().getInitParameter(hdrstr).getClass().getName() + "]", val);
        }

        for (Iterator it = t.keySet().iterator(); it.hasNext();) {
          String tkey = (String) it.next();
          System.out.println("ini: " + tkey + "=" + t.get(tkey));
        }

        t.clear();
        System.out.println("------------------------------------------");
      }
    }
  }
}
