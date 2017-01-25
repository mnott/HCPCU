package com.sap.hcpcu.servlets;

import com.sap.hcpcu.application.Service;
import com.sap.hcpcu.tools.ThreadRunner;
import com.sap.hcpcu.tools.ThreadRunner.ThreadCaller;
import com.sap.hcpcu.worker.Worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class Collector
 */
public class Collector extends HttpServlet implements ThreadCaller {
  private static final long   serialVersionUID = 1L;

  private static final int    batchSize        = 10000;

  /**
   * Logger for this class
   */
  private static final Logger log              = LoggerFactory.getLogger(Collector.class);

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }


  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    final Worker         worker        = Service.getWorker();
    final BufferedReader reader        = request.getReader();
    List<String>         batch         = new ArrayList<String>(batchSize);
    int                  numberOfLines = 0;
    int                  batchCount    = 0;
    int                  maxLineSize   = 0;
    String               line;

    /*
     * TODO: We are actually parallelizing the batches,
     *       as we fire off all of them in parallel
     *       threads. We should review this strategy
     *       and potentially be less aggressive.
     */
    while ((line = reader.readLine()) != null) {
      batch.add(line);

      int curLineSize = line.length();

      if (curLineSize > maxLineSize) {
        maxLineSize = curLineSize;
      }

      if ((++batchCount % batchSize) == 0) {
        /*
         * Hand over to persistence thread runner
         */
        new ThreadRunner(this, "persist", Arrays.asList(batch, worker)).run();

        /*
         * Implicitly remove the hard reference
         */
        batch = new ArrayList<String>(batchSize);
      }

      numberOfLines++;
    }

    /*
     * Add the remaining lines
     */
    final int rest = batch.size();
    if (rest > 0) {
      new ThreadRunner(this, "persist", Arrays.asList(batch, worker)).run();
      batch = new ArrayList<String>(batchSize);
    }

    reader.close();

    log.debug("+ Added " + (numberOfLines + rest - 2) + " transactions to the backend. Max line size: " + maxLineSize);
  }


  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override public Object threadStart(String callName, Object callArgument) {
    log.debug("> Start: " + callName);

    final List<String> batch  = (ArrayList<String>) ((List) callArgument).get(0);

    final Worker       worker = (Worker) ((List) callArgument).get(1);

    /*
     * Not perfect but allows us to safely insert. If we don't
     * synchronize here, we're running out of pool space, and
     * if we massively parallelize, we don't recover fast
     * enough and will risk dropping inserts.
     */
    synchronized (worker) {
      int batchCount = 0;

      try {
        worker.prepareBatch();

        for (final String transaction : batch) {
          ++batchCount;
          worker.addToBatch(transaction);
        }

        worker.executeBatch();

        worker.closeBatch();
      } catch (Exception e) {
        log.error("! Error executing batch: " + e.getMessage());
      } finally {
        if (worker != null) {
          try {
            worker.closeBatch();
          } catch (Exception e) {
            log.error("! Error closing batch: " + e.getMessage());
          }
        }
      }

      return "Inserted " + batchCount + " entries.";
    }
  }


  @Override public void threadEnd(String callName, Object callArgument, Object callResult) {
    log.debug("< Done: " + callName + ". Result: " + callResult);
  }
}
