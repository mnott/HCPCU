package com.sap.hcpcu.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * This class provides for a request wrapper that allows to modify
 * the response coming back from some endpoint.<p>
 */
public class HttpModifiableResponse extends HttpServletResponseWrapper {
  /**
   * Logger for this class
   */
  @SuppressWarnings("unused")
  private static final Logger log           = LoggerFactory.getLogger(HttpModifiableResponse.class);

  private int                 contentLength;
  private String              contentType;

  private HttpServletResponse response;

  private List<Cookie>        cookies       = new ArrayList<Cookie>();

  private StringWriter        stringWriter;

  /**
   * Constructor.
   */
  public HttpModifiableResponse(HttpServletResponse response) {
    super(response);
    this.response     = response;
    this.stringWriter = new StringWriter();
  }

  public void addCookie(Cookie cookie) {
    this.cookies.add(cookie);
    super.addCookie(cookie);
  }


  public Cookie[] getCookies() {
    final Cookie[] cookies = new Cookie[this.cookies.size()];
    int            i       = 0;

    for (final Cookie cookie : this.cookies) {
      cookies[i++] = cookie;
    }

    return cookies;
  }


  public PrintWriter getWriter() {
    return (new PrintWriter(this.stringWriter));
  }


  public ServletOutputStream getOutputStream() {
    return (new StringOutputStream(this.stringWriter));
  }


  public String toString() {
    return (this.stringWriter.toString());
  }


  public StringBuffer getBuffer() {
    return (this.stringWriter.getBuffer());
  }


  public void setContentType(String type) {
    this.contentType = type;
    super.setContentType(type);
  }


  public String getContentType() {
    return this.contentType;
  }


  public int getContentLength() {
    return this.contentLength;
  }


  public void setContentLength(int length) {
    this.contentLength = length;
    super.setContentLength(length);
  }


  /**
   * Modify the response.
   *
   * @param newContent The content to write to the response.
   * @throws ServletException
   */
  public void modify(String newContent) throws ServletException {
    try {
      this.response.setContentLength(newContent.getBytes(this.getCharacterEncoding()).length);

      final PrintWriter out = this.response.getWriter();
      out.write(newContent);
    } catch (IOException e) {
      throw (new ServletException(e));
    }
  }

  class StringOutputStream extends ServletOutputStream {
    private StringWriter stringWriter;

    public StringOutputStream(StringWriter stringWriter) {
      this.stringWriter = stringWriter;
    }

    public void write(int c) {
      this.stringWriter.write(c);
    }


    @Override public boolean isReady() {
      // Auto-generated method stub
      return false;
    }


    @Override public void setWriteListener(WriteListener arg0) {
      // Auto-generated method stub
    }
  }
}
