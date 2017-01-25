package com.sap.hcpcu.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * This class provides for a request wrapper that allows to modify
 * the collection of request variables.<p>
 */
public class HttpModifiableRequest extends HttpServletRequestWrapper {
  private Map<String, String[]> params;
  private Map<String, String[]> headers;
  private Map<String, Cookie>   cookies;

  /**
   * Constructor.
   */
  public HttpModifiableRequest(HttpServletRequest request) {
    super(request);
    this.params  = new HashMap<String, String[]>(request.getParameterMap());
    this.headers = new HashMap<String, String[]>();
    this.cookies = new HashMap<String, Cookie>();

    for (final Enumeration<String> s = request.getHeaderNames(); s.hasMoreElements();) {
      String             var   = s.nextElement();
      final List<String> lVals = new ArrayList<String>();

      for (final Enumeration<String> eVals = request.getHeaders(var); eVals.hasMoreElements();) {
        lVals.add(eVals.nextElement());
      }

      final String[] sVals = new String[lVals.size()];
      int            i     = 0;

      for (final String val : lVals) {
        sVals[i++] = val;
      }

      this.headers.put(var, sVals);
    }

    final Cookie[] cookies = super.getCookies();

    if (cookies != null) {
      for (int i = 0; i < cookies.length; i++) {
        this.cookies.put(cookies[i].getName(), cookies[i]);
      }
    }
  }


  /**
   * Get the original (unwrapped) request.
   *
   * @return The original (unwrapped) request.
   */
  public HttpServletRequest getRequest() {
    /*
     * Instead of returning the wrapped
     * request (and caching it locally,
     * we leave it to the super class to
     * return the request. This solved
     * the problem with jsp:include not
     * being included if the wrapper was
     * passed to it, inside the struts
     * application.
     */
    return (HttpServletRequest) super.getRequest();
      //return this.request;
  }


  /**
   * Get a given parameter value.
   *
   * @see javax.servlet.ServletRequest#getParameter(String)
   */
  public String getParameter(String name) {
    String   returnValue = null;
    String[] paramArray  = getParameterValues(name);

    if ((paramArray != null) && (paramArray.length > 0)) {
      returnValue = paramArray[0];
    }

    return returnValue;
  }


  /**
   * Get a given header value.
   *
   * @see javax.servlet.http.HttpServletRequest#getHeader(String)
   */
  public String getHeader(String name) {
    final String[] vals = this.headers.get(name);

    if (vals == null) {
      return null;
    }

    final StringBuffer sb    = new StringBuffer();
    boolean            added = false;

    for (int i = 0; i < vals.length; i++) {
      String val = vals[i];

      if (added) {
        sb.append("; ");
      }

      sb.append(val);
      added = true;
    }

    return sb.toString();
  }


  /**
   * Get the map of parameter names and values.
   *
   * @see javax.servlet.ServletRequest#getParameterMap()
   */
  public Map<String, String[]> getParameterMap() {
    return Collections.unmodifiableMap(this.params);
  }


  /**
   * Get the parameter names.
   *
   * @see javax.servlet.ServletRequest#getParameterNames()
   */
  public Enumeration<String> getParameterNames() {
    return Collections.enumeration(this.params.keySet());
  }


  /**
   * Get the header names.
   */
  public Enumeration<String> getHeaderNames() {
    return Collections.enumeration(this.headers.keySet());
  }


  /**
   * Get the parameter values.
   *
   * @see javax.servlet.ServletRequest#getParameterValues(String)
   */
  public String[] getParameterValues(String name) {
    final String[] temp = (String[]) this.params.get(name);

    if (temp == null) {
      return null;
    }

    final String[] result = new String[temp.length];
    System.arraycopy(temp, 0, result, 0, temp.length);

    return result;
  }


  /**
   * Get the header values for a given header name.
   *
   * @see javax.servlet.http.HttpServletRequest#getHeaders(String)
   */
  public Enumeration<String> getHeaders(String name) {
    final String[] vals = (String[]) this.headers.get(name);

    if (vals == null) {
      return null;
    }

    final Vector<String> v = new Vector<String>(vals.length);

    for (int i = 0; i < vals.length; i++) {
      v.add(vals[i]);
    }

    return v.elements();
  }


  /**
   * Get the cookies.
   *
   * @see javax.servlet.http.HttpServletRequest#getCookies()
   */
  public Cookie[] getCookies() {
    final Cookie[] cookies = new Cookie[this.cookies.size()];
    int            i       = 0;

    for (Cookie cookie : this.cookies.values()) {
      cookies[i++] = cookie;
    }

    return cookies;
  }


  /**
   * Sets a single value for the parameter.  Overwrites any current values.
   * @param name Name of the parameter to set
   * @param value Value of the parameter
   */
  public void setParameter(String name, String value) {
    String[] oneParam = { value };
    setParameter(name, oneParam);
  }


  /**
   * Sets a single value for the header. Overwrites any current values.
   * @param name Name fo the header to set
   * @param value Value of the header
   */
  public void setHeader(String name, String value) {
    String[] oneParam = { value };
    setHeader(name, oneParam);
  }


  /**
   * Sets a single value for the cookies. Overwrites any given
   * cookie of the same name.
   *
   * @param name The name of the cookie
   * @param value The value of the cookie
   */
  public void setCookie(String name, String value) {
    this.cookies.put(name, new Cookie(name, value));
    setCookiesToHeader();
  }


  /**
   * Add a given cookie to the list of cookies.
   * Overwrites any given cookie of the same name.
   *
   * @param cookie The cookie to add
   */
  public void setCookie(Cookie cookie) {
    this.cookies.put(cookie.getName(), cookie);
    setCookiesToHeader();
  }


  /**
   * Add the current list of cookies to the header.
   */
  private void setCookiesToHeader() {
    final StringBuffer sb    = new StringBuffer();
    boolean            added = false;

    for (final Cookie cookie : this.cookies.values()) {
      if (added) {
        sb.append("; ");
      }

      sb.append(cookie.getName());
      sb.append("=");
      sb.append(cookie.getValue());
      added = true;
    }

    setHeader("cookie", sb.toString());
  }


  /**
   * Sets multiple values for a parameter.
   * Overwrites any current values.
   * @param name Name of the parameter to set
   * @param values String[] of values
   */
  public void setParameter(String name, String[] values) {
    this.params.put(name, values);
  }


  /**
   * Sets multiple values for a header.
   * Overwrites any current values.
   * @param name Name of the header to set
   * @param values String[] of values.
   */
  public void setHeader(String name, String[] values) {
    this.headers.put(name, values);
  }


  /**
   * Remove a request parameter.
   *
   * @param name Name of the parameter to remove
   */
  public void removeParameter(String name) {
    if (this.params.containsKey(name)) {
      this.params.remove(name);
    }
  }


  /**
   * Remove a request header.
   *
   * @param name Name of the header to remove
   */
  public void removeHeader(String name) {
    if (this.headers.containsKey(name)) {
      this.headers.remove(name);
    }
  }


  /**
   * Remove a cookie.
   *
   * @param name The name of the cookie to
   * remove
   */
  public void removeCookie(String name) {
    if (this.cookies.containsKey(name)) {
      this.cookies.remove(name);
      setCookiesToHeader();
    }
  }
}
