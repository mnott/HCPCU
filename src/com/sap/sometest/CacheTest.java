package com.sap.sometest;

import com.sap.hcpcu.tools.Cache;


public class CacheTest {
  @SuppressWarnings("unused")
  public static void main(String[] args) throws Exception {
    String x = "abc";

    Cache  c = new Cache();

    //c.setEntry("key", x);

    // parameters: key (String), object to cache (Object), cache time (or -1) in seconds, compressed, double buffer
    //c.setEntry("0025", x, -1, true, false);

    /*
     * When you get your data you do this
     */

    //c.removeEntry("key");

    //Thread.currentThread().sleep(5000);

    //    String z = (String) c.getEntry("key");
    //    if(z == null) {
    //      System.out.println("Cache entry was null");
    //    }
    //

    //System.out.println(z);

    // cache size: keep track of most recent entities
    //
    // cache size 3
    // adding A => [A, , ]
    // adding B => [B, A, ]
    // adding C => [C, B, A]
    // adding D => [D, C, B] (A fell out
    // retrieving C => [C, D, B]
    // adding E => [E, C, D]

    c.setCacheSize(3);

    c.setEntry("A", "A");
    System.out.println(c.getKeys());
    c.setEntry("B", "B");
    System.out.println(c.getKeys());
    c.setEntry("C", "C");
    System.out.println(c.getKeys());
    c.setEntry("D", "D");
    System.out.println(c.getKeys());
    System.out.println("Got Entry: " + c.getEntry("B"));
    c.setEntry("E", "E");
    System.out.println(c.getKeys());

    // So to prefill your cache, do this asynchronously (independent of the user request):

    for (final String entry : c.getKeys()) {
      final String data = "get Data from Backend using " + entry; // fetch data from your backend, which is slow; entry is the Organizational Entity like 0025
      c.setEntry(entry, data);
    }

    // then on user request do this
    String myData = (String) c.getEntry("0025"); // <= 0025 is parameter from user
    if (myData == null) { // always do this if you get anything from the cache, it could be thrown out for out of memory reasons
      myData = "get Data from Backend using 0025"; // was not in the cache, so the first time now the user has to wait for like 5 minutes
      c.setEntry("0025", myData);
    }

    // what if we have multiple ways to look at the data (xls, data, xml)

    c.setEntry("0025-xml", "xml data");
    c.setEntry("0025-xls", "xls data");

    // starting the application
    // either read some OE from some configuration file

    // do the same as above on prefilling, but instead of iterating over cache keys, iterate over like OEs you have kept in a configuration file

    // or also, if you /restart/, you could

    c.save("filename");

    c.load("filename");

    //    Cache knownEntities = new Cache();
    //    knownEntities.setCacheSize(20);
    //
    //    String newEntity1 = "0025";
    //
    //    knownEntities.setEntry(newEntity1, newEntity1);
    //
    //
    //    String anEntity = (String) knownEntities.getEntry(newEntity1);
    //    if(anEntity == null) {
    //      // generate data for the entity
    //    }
    //

  }
}
