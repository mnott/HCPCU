package com.sap.hcpcu.clients;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class CollectorClient {
  /**
   * Logger for this class
   */
  private static final Logger   log   = LoggerFactory.getLogger(CollectorClient.class);

  private static final String   url   = "https://hcpcuad0dc7c13.hana.ondemand.com/hcpcu/collector"; // "http://localhost:8080/HCPCU/collector";

  private static final String   dir   = "tests/";

  private static final String[] files =  {"sample.csv"};

  public static void main(String[] args) throws Exception {
  	String [] loadFiles;
  	
  	if (args.length > 0) {
  		loadFiles = args;
  	} else {
  		loadFiles = files;
  	}
  	
    log.debug("> Processing " + loadFiles.length + " files...");

    for (int i = 0; i < loadFiles.length; i++) {
      post(loadFiles[i]);
    }

    log.debug("< Processing done.");
  }


  private static void post(String fileName) throws ClientProtocolException, IOException {
    log.debug("> Processing " + dir + fileName);

    final CloseableHttpClient httpclient = HttpClients.createDefault();
    try {
      final HttpPost  httppost        = new HttpPost(url);

      final File      file            = new File(dir + fileName);

      FileInputStream fileInputStream;
      try {
        fileInputStream = new FileInputStream(file);
      } catch (FileNotFoundException e) {
        log.error("! Error reading " + dir + fileName + ": " + e.getMessage());

        return;
      }

      final InputStreamEntity reqEntity = new InputStreamEntity(fileInputStream, -1, ContentType.APPLICATION_OCTET_STREAM);
      reqEntity.setChunked(true);
      httppost.setEntity(reqEntity);

      log.debug("> Executing request: " + httppost.getRequestLine());

      CloseableHttpResponse response = httpclient.execute(httppost);
      try {
        log.debug("+ ----------------------------------------");
        log.debug("+ " + response.getStatusLine());
        log.debug("+ " + EntityUtils.toString(response.getEntity()));
        log.debug("+ ----------------------------------------");
      } finally {
        response.close();
      }
    } finally {
      httpclient.close();
      log.debug("< Executing Request.");
      log.debug("< Processed " + dir + fileName);
    }
  }
}
