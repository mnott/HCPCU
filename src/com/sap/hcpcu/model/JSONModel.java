package com.sap.hcpcu.model;

import com.google.gson.Gson;
import com.sap.hcpcu.application.Application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class JSONModel {
	/**
	 * We declare Application as transient to avoid the implemeting
	 * classes, as they return themselves, to also return configuration
	 * information about the application.
	 */
  private transient Application application;

  private transient HttpServletRequest request;
  private transient HttpServletResponse response;

  public JSONModel() {}
  
  /**
   * Minimal Constructor: Needs the application parameter.
   * 
   * @param application
   */
  public JSONModel(Application application, HttpServletRequest request, HttpServletResponse response) {
    this.application = application;
    this.request = request;
    this.response = response;
  }

  public void setApplication(Application application) {
    this.application = application;
  }


  public Application getApplication() {
    return this.application;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public HttpServletRequest getRequest() {
    return this.request;
  }

  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }

  public HttpServletResponse getResponse() {
    return this.response;
  }


  /**
   * Return this
   */
  @Override public String toString() {
    final Gson   gson = new Gson();
    final String json = gson.toJson(this);

    return json;
  }
}
