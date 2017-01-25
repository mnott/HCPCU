package com.sap.hcpcu.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class provides for a thread runner framework.
 */
public class ThreadRunner implements Runnable {
  /**
   * Logger for this class
   */
  private static final Logger log          = LoggerFactory.getLogger(ThreadRunner.class);

  private ThreadCaller        callBack;
  private String              callName;
  private Object              callArgument;
  private Thread              thread       = null;
  private boolean             running      = false;

  /**
   * Create a new ThreadRunner.
   *
   * @param callBack The object to call back.
   * @param callName The name of the callback.
   * @param callArgument The argument for the callback.
   */
  public ThreadRunner(ThreadCaller callBack, String callName, Object callArgument) {
    this.callBack     = callBack;
    this.callName     = callName;
    this.callArgument = callArgument;
  }

  /**
   * Interrupt the worker thread.
   */
  public void interrupt() {
    if ((this.thread != null) && this.running) {
      log.debug("! Interrupting: " + callName + " [" + callArgument + "]");
      this.running = false;
    }
  }


  /**
   * Get the actual worker thread object.
   *
   * @return The worker thread object.
   */
  public Thread getThread() {
    return this.thread;
  }


  /**
   * Run the thread.<p>
   *
   * If the thread was already running, it is interrupted.
   * Then a new thread is created. It calls the <code>threadStart</code>
   * method and then the <code>threadEnd</code> method of the caller.
   */
  public void run() {
    if ((this.thread != null) && this.running) {
      log.debug("! Interrupting: " + callName + " [" + callArgument + "]");
      this.thread.interrupt();
      this.thread = null;
    }

    this.thread = new Thread() {
      public void run() {
        running = true;

        Object callResult = callBack.threadStart(callName, callArgument);
        callBack.threadEnd(callName, callArgument, callResult);
        running = false;
      }
    };
    this.thread.start();
  }


  /**
   * A caller to the ThreadRunner needs to implement
   * this interface.
   */
  public interface ThreadCaller {
    public Object threadStart(String callName, Object callArgument);


    public void threadEnd(String callName, Object callArgument, Object callResult);
  }
}
