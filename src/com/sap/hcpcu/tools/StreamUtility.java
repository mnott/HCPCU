package com.sap.hcpcu.tools;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;


/**
 * Some Stream Utilities.
 */
public class StreamUtility {
  /**
   * Get all the content of the input stream as a String.<p>
   *
   * This function is useful for parsing URLConnection responses.
   *
   * @param in The input stream.
   * @return The content as a String.
   * @throws Exception
   */
  public static String getStreamToString(InputStream in) throws Exception {
    if (in == null) {
      throw (new Exception("Null stream passed"));
    }

    java.lang.StringBuffer  sb  = new StringBuffer();

    final InputStreamReader r   = new InputStreamReader(in, "UTF-8");
    int                     val;
    while ((val = r.read()) != -1)
      sb.append((char) val);

    return sb.toString();
  }


  /**
   * Get all the bytes from an input stream.
   * @param in The input stream.
   * @return The bytes from that input stream.
   */
  public static byte[] getBytesToEndOfStream( /*Buffered*/InputStream in) throws Exception {
    if (in == null) {
      throw (new Exception("Null stream passed"));
    }

    final int             chunkSize  = 2048;
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(chunkSize);
    int                   val;

    while ((val = in.read()) != -1)
      byteStream.write(val);

    return byteStream.toByteArray();
  }


  /**
   * Get all the bytes from an input stream
   * directly into an output stream.
   * @param in The input stream.
   * @param out The output stream.
   */
  public static void getBytesToEndOfStream(InputStream in, OutputStream out) throws Exception {
    if (in == null) {
      throw (new Exception("Null stream passed"));
    }

    int val;

    while ((val = in.read()) != -1)
      out.write(val);
  }


  /**
   * Get all the bytes from a reader.
   * @param r the reader.
   * @return The bytes from that reader.
   */
  public static byte[] getBytesToEndOfReader(Reader r) throws Exception {
    if (r == null) {
      throw (new Exception("Null stream passed"));
    }

    final int             chunkSize  = 2048;
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(chunkSize);
    int                   val;

    while ((val = r.read()) != -1)
      byteStream.write(val);

    return byteStream.toByteArray();
  }
}
