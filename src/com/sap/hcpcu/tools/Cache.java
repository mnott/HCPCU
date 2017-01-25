package com.sap.hcpcu.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.SoftReference;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * This class provides the user with an object cache.
 * <p>
 * This cache has a general structure of an optimized queue (FIFO). The
 * following rules apply:
 * <ul>
 * <li>When an entry is added to the cache, it is added to the head of the
 *     queue.</li>
 * <li>If the maximum number of entries in the queue is reached, adding an
 *     entry to the head at the same time moves out one object from the tail.
 *     The cache size can be controlled by the setCacheSize function. The
 *     default cache size is 1000 entries.</li>
 * <li>Hitting any entry in the cache moves it back to the head of the queue.
 *     This way, more frequently accessed entries are more likely to remain in the
 *     cache.</li>
 * <li>When a maximum number of cache modification has been reached, the entire
 *     cache is flushed. This can be controlled by the setResetAfter function.
 *     The default threshold is 1000000 structural cache modifications.</li>
 * <li>Each cache entry gets a standard lifetime which is "forever" if you do
 *     not use the setLifetime function. If you use that function or choose
 *     a particular setting for one given entry using the more detailed
 *     version of the setEntry function, the entries will be cleaned up
 *     either by a cleaner thread that is started automatically when you
 *     instantiate the cache, or by the fact that you access an expired
 *     item before the next run of the cleaner thread took place. The
 *     default interval for the cleaner is 1 minute. This can be controlled
 *     by the setUpdateInterval function.</li>
 * <li>Each cache entry is encapsulated in a "SoftReference". This means,
 *     that when running into an out of memory situation, some entries
 *     may be moved out of the cache by the garbage collector. As you
 *     have no control whatsoever over which entries are removed, it
 *     is important to understand that <b>you must not rely on the fact
 *     that an entry is in the cache only because you had just put it there.</b>
 *     Each time you access the cache using the getEntry function, you must
 *     check whether you received a null object, and if so, you have to
 *     take care yourself for recreating the object from scratch - and for
 *     adding it back to the cache if you want.</li>
 * <li>It is possible to compress the cache content if you have huge but
 *     redundant objects to store. To do so, use the setCompressed function
 *     to activate this setting for the entire cache, or activate it only
 *     for single calls to setEntry in its detailed form. When getting
 *     an object from the cache, the cache takes care of decompressing
 *     the entry, so you do not have to worry about whether any entry had
 *     been compressed previously. Please make sure to note that the
 *     compression takes place in memory, and may imply a severe load
 *     on the CPU.</li>
 * <li>When compressing, it is possible to activate a double buffer
 *     for single entries or for the entire cache using the setDoublebuffer
 *     function. When doing so, storing and hitting a cache entry will
 *     automatically try to maintain a SoftReference to the uncompressed
 *     form, thus considerably speeding up subsequent hits to the same
 *     entry. The downside is, however, that this decompressed entry
 *     now competes for memory space with all other cache entries,
 *     whether compressed or not. So make sure to monitor, e.g. using
 *     the getCacheHit  vs. getCacheMiss functions, whether the setting
 *     renders the entire cache inactive such that hitting a huge cache
 *     entry forces all other cache entries out of the cache immediately.
 *     The cache statistics can be reset by the resetStatistics function.</li>
 * <li>It is very probable that using the double buffer and the compression
 *     as an overall cache setting does not make sense. All tests that have
 *     been untertaken so far show that the doublebuffer takes up at least
 *     the space, if not more, that the uncompressed objects would take,
 *     thus moving objects out of the cache that should have been kept
 *     there. On the other hand, double buffering and compressing makes
 *     a lot of sense to use on isolated cache entries. Think of getting
 *     a list of values that <i>may</i> be bigger than a given threshold
 *     that you define; in this case, you use the detailed version of the
 *     setEntry function and store only this entry in a compressed form,
 *     activating the double buffer at the same time. If the object is
 *     accessed frequently, the double buffer will eliminate subsequent
 *     decompressions and trade this for space; if the object is accessed
 *     rarely, the double buffer will more likely be prone to be moved out of the
 *     cache by the garbage collector, as it can be considered to take more space
 *     as opposed to the "other" entries.</li>
 * <li>It is possible to persist a cache on disk using the save function.
 *     The cache is compressed when doing so. See the documentation for
 *     that function vs. the load function for considerations to make
 *     that affect the compression, doublebuffer and lifetime settings
 *     of a given cache entry if these are not inline with the overall
 *     settings of the cache. Basically, saving and restoring the cache
 *     applies the overall settings to all objects in the cache; in
 *     addition, due to the many factors that may influence the content
 *     of the cache, you cannot rely on the assumption that loading a cache
 *     will lead to exactly the same content as it had when it was saved.
 *     Loading the cache adds the entries back to the cache as fresh
 *     entries, with the cache wide compression and double buffering
 *     setting, so already while loading the cache, the garbage collector
 *     may step into the place and remove objects from the cache if
 *     running into an out of memory situation.</li>
 * <li>Particular emphasis was placed on making this cache thread safe. Every
 *     structural modification is synchronized. Since some modifications are
 *     detectable only when accessing the cache, the accessors are
 *     synchronized, as well, as when they detect the invalidity of
 *     a SoftReference pointer, they immediately move that SoftReference
 *     object out of the cache, as well.</li>
 * </ol>
 * @author Copyright (c) 2003 Matthias Nott, Business Objects
 */
public class Cache {
  /**
   * The Log4J Logger.
   */
  private static Logger           log            = LoggerFactory.getLogger(Cache.class);


  /** The Updator Thread. */
  private CacheUpdator            updator        = null;

  /** The Key/Cursor map. */
  private TreeMap<String, Object> CacheCursor    = null;

  /** The Cursor/Key map. */
  private TreeMap<String, Object> CursorCache    = null;

  /** The Cache: Cursor/Value map. */
  private TreeMap<String, Object> ObjectCache    = null;


  /** Store objects in a compressed way. */
  private boolean                 compressed     = false;

  /** Debug output. */
  private boolean                 debug          = false;

  /** Whether the cache is enabled. */
  private boolean                 enabled        = true;

  /** Doublebuffer compressed objects. */
  private boolean                 doublebuffer   = false;

  /** Modifying actions performed on the cached. */
  private int                     actions        = 0;

  /** Cache Hit Counter. */
  private int                     cacheHit       = 0;

  /** Cache Miss Counter. */
  private int                     cacheMiss      = 0;

  /** Size of the Cache (number of entries). */
  private int                     cacheSize      = 1000;

  /** Current Key in the Cache, max of cacheSize. */
  private int                     mapCursor      = 0;

  /** Reset Cache after this number of modifying actions. */
  private int                     resetAfter     = 1000000;

  /** Default lifetime in seconds for cache entries. -1: Endless life for cache entries. */
  private long                    lifetime       = -1;

  /** Update interval for the cache, in milliseconds. */
  private long                    updateInterval = 60000;

  /**
   * Whether the updator thread was started.
   * The updator thread should only be started
   * if there are any entries that have a timeout.
   */
  private boolean                 updatorStarted = false;

  /**
   * Constructor. Initializes the internal data structures.
   */
  public Cache() {
    super();
    ObjectCache  = new TreeMap<String, Object>();
    CacheCursor  = new TreeMap<String, Object>();
    CursorCache  = new TreeMap<String, Object>();
    this.updator = new CacheUpdator();
    this.updator.setDaemon(true);
    this.updator.setParent(this);
    //updator.start();
  }

  /**
   * Set whether the cache is enabled.
   * @param enabled True to enable the cache, false to disable it.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }


  /**
   * Get whether the cache is enabled.
   * @return True if the cache is enabled, else false.
   */
  public boolean isEnabled() {
    return this.enabled;
  }


  /**
   * Start the updator thread. This should only
   * be done if there are any entries that expire,
   * i.e. that have a lifetime >=0.
   */
  private void startUpdator() {
    if (!this.updatorStarted) {
      updator.start();
      this.updatorStarted = true;
    }
  }


  /**
   * Get the internal Cache Hit counter.
   *
   * @return The internal Cache Hit counter.
   */
  public synchronized int getCacheHit() {
    return this.cacheHit;
  }


  /**
   * Get the internal Cache Miss counter.
   *
   * @return The internal Cache Miss counter.
   */
  public synchronized int getCacheMiss() {
    return this.cacheMiss;
  }


  /**
   * Set the Cache Size. This has no immediate effect to the cache. Only when
   * new entries are added to the cache, the cache may be resized accordingly,
   * if the new cache size is lower than the number of entries that are
   * currently in the cache. The default value for this parameter is 1000. Make
   * sure to understand that this does not tell anything about the amount of
   * memory the cache will require; it just is the number of entries that will
   * fit in the cache.
   *
   * @param size Integer value containing the new cache size.
   */
  public synchronized void setCacheSize(int size) {
    this.cacheSize = size;
  }


  /**
   * Set the compression mode.
   * <p>
   * Pay attention to the CPU load this option implies. The objects are
   * compressed when they are stored, and decompressed when they are retrieved.
   * When they are retrieved, a SoftReference to the uncompressed version is
   * maintained, so that it is likely that two subsequent accesses to the same
   * uncompressed object will not lead to two subsequent decompressions. As this
   * cannot be guaranteed for, the internal routines take care themselves of
   * decompressing the object as required, i.e. when an uncompressed copy is not
   * available.
   *
   * @param compressed True if objects generally shall be stored in a compressed
   *          way; else false. Only serializable objects can be stored in a
   *          compressed way.
   */
  public synchronized void setCompressed(boolean compressed) {
    this.compressed = compressed;
  }


  /**
   * Set the debug mode. If the cache is persisted to disk, the debug mode is
   * not saved, as this is typically not interesting when reusing a given cache
   * at a later time. Keeping the debug flag would risk to clobber log files
   * unknowingly.
   *
   * @param ft Boolean (true or false) for the debug mode.
   */
  public synchronized void setDebug(boolean ft) {
    this.debug = ft;
  }


  /**
   * Activate the doublebuffer for the entire cache. Pay attention to this: When
   * you use compression, you are already likely to be in a scarce memory
   * situation. Activating the doublebuffer will make the cache make use <i>more
   * </i> memory. So each get to a compressed (potentially huge) object will
   * force other objects out of the cache. So you should use the doublebuffer
   * only if you generally have enough memory for your cache, but want to
   * activate compression anyways for isolated objects that you estimate to be
   * of bigger size. These objects will effectively be stored twice in the
   * cache: in an uncompressed version and in a compressed version, if you use
   * the doublebuffer, thus allocating <i>more </i> memory. You do not have
   * control over which of the two is thrown out first; if the compressed object
   * is thrown out, the uncompressed version will be thrown out at the same
   * time. If the uncompressed version will be thrown out, the compressed
   * version is likely to remain.
   *
   * @param doublebuffer True if you want to activate the doublebuffer for the
   *          entire cache, else false.
   */
  public synchronized void setDoublebuffer(boolean doublebuffer) {
    this.doublebuffer = doublebuffer;
  }


  /**
   * Adds an Entry to the Cache. The cache key and the object to be cached have
   * to be passed. Internally, the following data structures are constructed:
   * <p>
   * <ul>
   * <li><b>CursorCache: </b> This TreeMap contains an integer as primary key
   * and the cache key as value. It is used to keep the cache sorted as a FIFO /
   * queue buffer.</li>
   * <li><b>ObjectCache: </b> This TreeMap contains the cache key as primary
   * key and the actual object as value.</li>
   * <li><b>CacheCursor: </b> This TreeMap contains the cache key as primary
   * key and the integer from CursorCache as value. It is used to locate the
   * right entry in the CursorCache TreeMap upon removal of Objects from the
   * Cache.</li>
   * </ul>
   * <p>
   * When the resetAfter value is bypassed, the entire cache is invalidated.
   * This is used to keep the cache structure consistent in the long run.
   * <p>
   * If the size of the Cache bypasses cacheSize, the first entry in the Cache,
   * defined by the CursorCache TreeMap, is removed and the new entry is added
   * to the end of the Cache.
   * <p>
   * If we had a cache hit on a cache entry, the entry is reappended to the head
   * of the cache. This way, the cache acts in a self-optimizing way to maintain
   * the entries that are requested most.
   * <p>
   * If the cache runs into an out of memory situation, cache entries are
   * automatically released. Therefore, you can never rely on an object to be in
   * the cache just because you had put it there recently. You have to try to
   * retrieve the object, and if you get a null value, you must regenerate your
   * object.
   * <p>
   * If the object expires due to its lifetime (which defaults to -1, i.e. the
   * object will never expire, but you have the option of setting the lifetime
   * explicitely by calling the version of this function that accepts the
   * lifetime as a parameter), the object will be removed from the cache by the
   * cache updator which runs at regular intervals (you can set this interval by
   * setUpdateInterval()).
   *
   * @param aKey The Key under which the Object was cached.
   * @param obj The Object to be cached.
   */
  public synchronized void setEntry(String aKey, Object obj) {
    setEntry(aKey, obj, this.lifetime, this.compressed, this.doublebuffer);
  }


  /**
   * Adds an Entry to the Cache and in addition sets a maximum lifetime.
   *
   * @param aKey The Key under which the Object was cached.
   * @param obj The Object to be cached.
   * @param lifetime The lifetime in seconds that the object may remain in the
   *          cache.
   * @param compressed True if the object is to be stored compressed, else
   *          false. Only serializable objects can be comressed.
   * @param doublebuffer True if an attempt shall be made to keep a
   *          SoftReference to the object in its uncompressed form.
   */
  public synchronized void setEntry(String aKey, Object obj, long lifetime, boolean compressed, boolean doublebuffer) {
    log.debug("> Caching object for key: " + aKey + "; lifetime: " + lifetime + "; compressed: " + compressed + "; doublebuffer: " + doublebuffer);

    if (!this.enabled) {
      log.debug("! Cache is disabled.");
      log.debug("< setEntry");

      return;
    }

    ++this.mapCursor;
    ++this.actions;

    if (((this.actions > (this.resetAfter - 1)) && (this.resetAfter != -1)) || (this.actions > (Integer.MAX_VALUE - 1)) || (this.mapCursor > (Integer.MAX_VALUE - 1)) || (this.cacheMiss > (Integer.MAX_VALUE - 1)) || (this.cacheHit > (Integer.MAX_VALUE - 1))) {
      log.debug("! Cache has to be flushed because it hit boundary conditions");
      flush();
    } else {
      while (ObjectCache.size() >= this.cacheSize) {
        /*
         * Get the first Cursor Cache Entry.
         */
        Object CursorCacheFirstEntry = getCacheEntry(CursorCache, CursorCache.firstKey(), "CursorCache");

        if (CursorCacheFirstEntry == null) {
          continue;
        }

        /*
         * Then remove from the other caches and finally from the CursorCache.
         */
        ObjectCache.remove((String) CursorCacheFirstEntry);
        CacheCursor.remove((String) CursorCacheFirstEntry);
        CursorCache.remove(CursorCache.firstKey());
      }
    }

    /* Calculate Cursor key */
    String cKey = "" + this.mapCursor;

    /* Add Cache and Cursor Keys/Values to their Maps */
    setCacheEntry(ObjectCache, aKey, obj, lifetime, compressed, doublebuffer);
    setCacheEntry(CacheCursor, aKey, cKey, lifetime, false, false);
    setCacheEntry(CursorCache, cKey, aKey, lifetime, false, false);

    if (lifetime >= 0) {
      startUpdator();
    }

    log.debug("< Caching object for key: " + aKey);
  }


  public synchronized Set<String> getKeys() {
    return this.CacheCursor.keySet();
  }


  /**
   * Gets an Entry from the Cache. The cache key has to be passed. The Entry is
   * returned as java.lang.Object, so you have to cast it to the required type.
   * If the object was found in the cache, the cache position of the object is
   * changed as if the object had just been added to the cache. The net result
   * is that this object will stay in the cache longer, as the cache itself has
   * the structure of a FIFO / queue.
   *
   * @param aKey The Key under which the Object was cached.
   * @return Object The cached Object or null, if none was found.
   */
  public synchronized Object getEntry(String aKey) {
    if (!this.enabled) {
      log.debug("! Cache is disabled.");

      return null;
    }

    Object obj = getCacheEntry(ObjectCache, aKey, "ObjectCache");

    if (obj == null) {
      if (this.debug) {
        log.debug("- Cache Miss: [" + aKey + "]");
      }

      ++this.cacheMiss;
    } else {
      String cKey = (String) getCacheEntry(CacheCursor, aKey, "CacheCursor");

      if (cKey == null) {
        if (this.debug) {
          log.debug("- Cache Miss: [" + aKey + "]");
        }

        ++this.cacheMiss;

        return null;
      }

      long lifetimeCKey = getCacheEntryLifetime(CacheCursor, aKey);
      long lifetimeAKey = getCacheEntryLifetime(CursorCache, cKey);

      if (this.debug) {
        log.debug("+ Cache Hit : [" + cKey + "][" + aKey + "]");
      }

      ++this.mapCursor;
      ++this.cacheHit;

      /* Calculate Cursor key */
      CacheCursor.remove(aKey);
      CursorCache.remove(cKey);
      cKey = "" + this.mapCursor;
      setCacheEntry(CacheCursor, aKey, cKey, lifetimeCKey, false, false);
      setCacheEntry(CursorCache, cKey, aKey, lifetimeAKey, false, false);
    }

    return obj;
  }


  /**
   * Set the default lifetime for new objects that are added to the cache.
   *
   * @param lifetime The lifetime in seconds.
   */
  public synchronized void setLifetime(long lifetime) {
    this.lifetime = lifetime;
  }


  /**
   * Set the Reset After Threshold. When this number of modifications was made
   * to the cache (an addition is a modification) the cache is expired. The
   * default value for this parameter is 1000000. If set to -1, the cache will
   * only be flushed after about 2147483647 modifications.
   *
   * @param threshold Integer value containing the reset after threshold.
   */
  public synchronized void setResetAfter(int threshold) {
    this.resetAfter = threshold;
  }


  /**
   * Set the default update interval for the cache.
   *
   * @param updateInterval The update interval in seconds.
   */
  public synchronized void setUpdateInterval(long updateInterval) {
    this.updateInterval = updateInterval * 1000;
    this.updator.setUpdateInterval(this.updateInterval);
  }


  /**
   * Destroy the cache and stop the update thread. Call this method when the
   * cache is no longer needed, to avoid wasting memory and cpu time. If you
   * attempt to use the cache after calling the destroy method, a null pointer
   * exception will be thrown.
   */
  public synchronized void destroy() {
    log.debug("> Destroy");
    this.updator.setActive(false);
    this.updator.setDaemon(false);
    this.updator.interrupt();
    this.updator = null;
    flush();
    log.debug("< Destroy");
  }


  /**
   * Flush the entire content of the cache.
   */
  public synchronized void flush() {
    log.debug("> flush");
    this.mapCursor = 1;
    this.actions   = 1;
    this.cacheMiss = 0;
    this.cacheHit  = 0;
    ObjectCache.clear();
    CacheCursor.clear();
    CursorCache.clear();
    log.debug("< flush");
  }


  /**
   * Load the Cache from a ZIP compresed format. Using the
   * {@link FileLoader}, the file is
   * retrieved either from the WEB-INF directory of a web application or from
   * the current directory of the .jar file.
   * <p>
   * Only objects that are serializable must be added to the Cache if you want
   * to persist the Cache. Otherwise, the Exception
   * java.io.NotSerializableException will be thrown.
   * <p>
   * If you were using compression or doublebuffering on some of the cached
   * objects, these settings will not be restored on a per object basis.
   * Instead, only the cache-wide settings are applied to all objects.
   * This as well aplies to the object lifetime.
   * <p>
   * You must understand that saving and then loading the cache does not
   * lead to the same cache object. This is due to the complex inner
   * mechanisms of the cache. As the cache wide compression, doublebuffering
   * and lifetime are applied, reloading the cache from disk may at the same
   * time, while adding the entries back to the cache, already trigger
   * cache cleaning operations that lead to a different view of the cache.
   * But this only underlines the fact that you can never rely on the
   * existence of an object in the cache only because you put it there,
   * because there are many reasons why such an object could have been
   * moved out of the cache in the meantime.
   * <p>
   * The upside is that if you want to e.g. compress your entire cache,
   * you can easily do so by just saving and restoring it with a different
   * compression setting.
   *
   * @param FileName The filename under which the Cache had been saved
   *          previously.
   */
  @SuppressWarnings("unchecked")
  public synchronized void load(String FileName) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException {
    InputStream       fis = FileLoader.load(FileName);
    GZIPInputStream   gis = new GZIPInputStream(fis);
    ObjectInputStream ois = new ObjectInputStream(gis);

    flush();

    TreeMap<String, Object> myObjectCache = (TreeMap<String, Object>) ois.readObject();

    this.cacheSize      = ((Integer) ois.readObject()).intValue();
    this.mapCursor      = ((Integer) ois.readObject()).intValue();
    this.actions        = ((Integer) ois.readObject()).intValue();
    this.resetAfter     = ((Integer) ois.readObject()).intValue();
    this.cacheMiss      = ((Integer) ois.readObject()).intValue();
    this.cacheHit       = ((Integer) ois.readObject()).intValue();
    this.debug          = ((Boolean) ois.readObject()).booleanValue();
    this.lifetime       = ((Long) ois.readObject()).longValue();
    this.updateInterval = ((Long) ois.readObject()).longValue();
    this.compressed     = ((Boolean) ois.readObject()).booleanValue();
    this.doublebuffer   = ((Boolean) ois.readObject()).booleanValue();
    ois.close();

    if (this.lifetime >= 0) {
      startUpdator();
    }

    this.updator.setUpdateInterval(this.updateInterval);

    String[] tKey = new String[myObjectCache.size()];
    int      i    = 0;

    for (final String s : myObjectCache.keySet()) {
      tKey[i++] = s;
    }

    for (int j = 0; j < myObjectCache.size(); j++) {
      Object o = myObjectCache.get(tKey[j]);

      if (o == null) {
        continue;
      }

      setEntry(tKey[j], o, this.lifetime, this.compressed, this.doublebuffer);
    }
  }


  /**
   * Removes an Entry from the Cache. The cache key has to be passed and hence
   * to be known precisely.
   *
   * @param aKey The Key under which the Object was cached.
   */
  public synchronized void removeEntry(String aKey) {
    String cKey = (String) getCacheEntry(CacheCursor, aKey, "CacheCursor");
    ++this.actions;

    if (ObjectCache.containsKey(aKey)) {
      ObjectCache.remove(aKey);
    }

    if (CacheCursor.containsKey(aKey)) {
      CacheCursor.remove(aKey);
    }

    if (CursorCache.containsKey(aKey)) {
      CursorCache.remove(cKey);
    }
  }


  /**
   * Removes an Entry or a Set of Entries from the Cache. It is sufficient to
   * know a fraction of the cache key. For example, if you have cached entries
   * that have a prefix "document-" in common, and you want to uncache all
   * documents, pass "document-" as parameter to this function.
   *
   * @param aKey Part of the Key under which the Objects were cached.
   */
  public synchronized void removeLike(String aKey) {
    boolean found = false;
    String  tKey  = "";

    do {
      found = false;

      if ((ObjectCache != null) && (ObjectCache.size() > 0)) {
        for (final String xKey : ObjectCache.keySet()) {
          if (xKey.indexOf(aKey) != -1) {
            found = true;
            tKey  = xKey;

            break;
          }
        }
      }

      if (found) {
        ++this.actions;
        removeEntry(tKey);
      }
    } while (found);
  }


  /**
   * Reset the internal Cache Miss and Cache Hit counters.
   */
  public synchronized void resetStatistics() {
    this.cacheMiss = this.cacheHit = 0;
  }


  /**
   * Save the Cache in a ZIP compressed format to Disk.
   * <p>
   * Only objects that are serializable must be added to the Cache if you want
   * to persist the Cache. Otherwise, the Exception
   * java.io.NotSerializableException will be thrown.
   * <p>
   * If you were using compression or doublebuffering on some of the cached
   * objects, these settings will not be stored on a per object basis. Instead,
   * only the cache-wide settings will be stored. When reloading the cache,
   * each object is entered, with standard lifetime, into the cache.
   * <p>
   * You must understand that saving and then loading the cache does not
   * lead to the same cache object. This is due to the complex inner
   * mechanisms of the cache. As the cache wide compression, doublebuffering
   * and lifetime are applied, reloading the cache from disk may at the same
   * time, while adding the entries back to the cache, already trigger
   * cache cleaning operations that lead to a different view of the cache.
   * But this only underlines the fact that you can never rely on the
   * existence of an object in the cache only because you put it there,
   * because there are many reasons why such an object could have been
   * moved out of the cache in the meantime.
   * <p>
   * The upside is that if you want to e.g. compress your entire cache,
   * you can easily do so by just saving and restoring it with a different
   * compression setting.
   *
   * @param FileName The filename under which the Cache is to be saved. In
   *   earlier versions of the cache, the DirectoryLocator was immediately
   *   applied to the filename, creating potential incompatibilities with
   *   some environments / application servers. For this reason, the pure
   *   filename is given here, i.e. it is your own responsibility to make
   *   sure that filename points to a valid location.
   */
  @SuppressWarnings("rawtypes")
  public synchronized void save(String FileName) throws java.io.FileNotFoundException, java.io.IOException {
    FileOutputStream        fos           = new FileOutputStream( /* DirectoryLocator.getBasePath() + */FileName);
    GZIPOutputStream        gos           = new GZIPOutputStream(fos);
    ObjectOutputStream      oos           = new ObjectOutputStream(gos);

    TreeMap<String, Object> myObjectCache = new TreeMap<String, Object>();

    String[]                tKey          = new String[ObjectCache.size()];
    int                     i             = 0;

    for (final String s : ObjectCache.keySet()) {
      tKey[i++] = s;
    }

    for (int j = 0; j < ObjectCache.size(); j++) {
      SoftReference ref = (SoftReference) ObjectCache.get(tKey[j]);

      if (ref == null) {
        continue;
      }

      CachedObject cachedObject = (CachedObject) ref.get();

      if ((cachedObject == null) || cachedObject.expired()) {
        ObjectCache.remove(tKey[j]);

        continue;
      }

      boolean tempDoubleBuffer = cachedObject.getDoublebuffer();
      cachedObject.setDoublebuffer(false);

      Object o = cachedObject.getObject();

      if (o != null) {
        myObjectCache.put(tKey[j], o);
      }

      cachedObject.setDoublebuffer(tempDoubleBuffer);
    }

    oos.writeObject(myObjectCache);
    oos.writeObject(new Integer(this.cacheSize));
    oos.writeObject(new Integer(this.mapCursor));
    oos.writeObject(new Integer(this.actions));
    oos.writeObject(new Integer(this.resetAfter));
    oos.writeObject(new Integer(this.cacheMiss));
    oos.writeObject(new Integer(this.cacheHit));
    oos.writeObject(Boolean.valueOf("" + this.debug));
    oos.writeObject(new Long(this.lifetime));
    oos.writeObject(new Long(this.updateInterval));
    oos.writeObject(Boolean.valueOf("" + this.compressed));
    oos.writeObject(Boolean.valueOf("" + this.doublebuffer));
    oos.flush();
    oos.close();
  }


  /**
   * Put an entry in the SoftReferenced Cache.
   *
   * @param cache The Cache to put the object in.
   * @param key The key that shall point to the object.
   * @param obj The object to store in the cache.
   * @param lifetime The lifetime.
   * @param compressed True if the object is to be stored compressed.
   * @param doublebuffer True if an attempt shall be made to keep a
   *          SoftReference to the object in its uncompressed form.
   */
  private synchronized void setCacheEntry(TreeMap<String, Object> cache, String key, Object obj, long lifetime, boolean compressed, boolean doublebuffer) {
    cache.put(key, new SoftReference<Object>(new CachedObject(obj, Calendar.getInstance().getTime(), lifetime /* * 1000*/, compressed, doublebuffer)));
  }


  /**
   * Get an entry from the SoftReferenced Cache.
   *
   * @param cache The Cache to get an object from.
   * @param key The Cache key pointing to the object.
   * @return The object. Null, if the object was not available.
   */
  @SuppressWarnings("rawtypes")
  private synchronized Object getCacheEntry(TreeMap cache, Object key, String name) {
    if (key == null) {
      return null;
    }

    SoftReference ref = (SoftReference) cache.get(key);

    if (ref == null) {
      return null;
    }

    CachedObject cachedObject = (CachedObject) ref.get();

    if ((cachedObject == null) || cachedObject.expired()) {
      cache.remove(key);

      return null;
    }

    //cachedObject.resetTimer();

    return cachedObject.getObject();
  }


  /**
   * Get the lifetime for an entry in the SoftReferenced Cache.
   *
   * @param cache The Cache to get the lifetime for an object from.
   * @param key The Cache key pointing to the object.
   * @return The lifetime. 0 if not available.
   */
  @SuppressWarnings("rawtypes")
  private synchronized long getCacheEntryLifetime(TreeMap cache, Object key) {
    if (key == null) {
      return 0;
    }

    SoftReference ref = (SoftReference) cache.get(key);

    if (ref == null) {
      return 0;
    }

    CachedObject cachedObject = (CachedObject) ref.get();

    if ((cachedObject == null) || cachedObject.expired()) {
      cache.remove(key);

      return 0;
    }

    return cachedObject.getLifeTime();
  }


  /**
   * Print the content of the cache.
   *
   * @return The content of the cache.
   */
  @SuppressWarnings("rawtypes")
  public synchronized String toString() {
    final StringBuffer sb = new StringBuffer();

    if (ObjectCache == null) {
      return "";
    }

    final java.util.Set ks = ObjectCache.keySet();
    if (ks == null) {
      return "";
    }

    final String[] tKey = new String[ObjectCache.size()];
    int            i    = 0;

    for (final String xKey : ObjectCache.keySet()) {
      tKey[i++] = xKey;
    }

    for (int j = 0; j < ObjectCache.size(); j++) {
      final SoftReference ref = (SoftReference) ObjectCache.get(tKey[j]);
      if (ref != null) {
        final CachedObject obj = (CachedObject) ref.get();
        if (obj != null) {
          sb.append("\n[" + tKey[j] + "]\t=>\t" + obj.getObject());
        } else {
          sb.append("\n" + tKey[j] + "\t=>\tnull (object referenced softly was null)");
        }
      } else {
        sb.append("\n" + tKey[j] + "\t=>\tnull (soft reference was null)");
      }
    }

    return sb.toString();
  }


  /**
   * Updator thread used to update the cache at the specified interval.
   */
  private class CacheUpdator extends Thread {
    /**
     * The invoking (Cache) object.
     */
    private Cache   parent         = null;

    /**
     * Wheter or not the Thread is active.
     */
    private boolean active         = true;


    /** Update interval for the cache, in milliseconds. */
    private long    updateInterval = 5000;

    /**
     * Set the default update interval for the cache.
     *
     * @param updateInterval The update interval in seconds.
     */
    public synchronized void setUpdateInterval(long updateInterval) {
      this.updateInterval = updateInterval;
    }


    /**
     * Run the updator thread.
     */
    @SuppressWarnings("rawtypes")
    public void run() {
      while (true) {
        synchronized (this) {
          if ((ObjectCache != null) && (ObjectCache.size() > 0)) {
            synchronized (this.parent) {
              String[] tKey = new String[ObjectCache.size()];
              int      i    = 0;

              for (final String xKey : ObjectCache.keySet()) {
                tKey[i++] = xKey;
              }

              for (int j = 0; j < ObjectCache.size(); j++) {
                SoftReference ref = (SoftReference) ObjectCache.get(tKey[j]);

                if (ref == null) {
                  continue;
                }

                CachedObject cachedObject = (CachedObject) ref.get();

                if ((cachedObject == null) || cachedObject.expired()) {
                  ObjectCache.remove(tKey[j]);
                }

                continue;
              }
            }
          }

          if ((CursorCache != null) && (CursorCache.size() > 0)) {
            synchronized (this.parent) {
              String[] tKey = new String[CursorCache.size()];
              int      i    = 0;

              for (final String xKey : CursorCache.keySet()) {
                tKey[i++] = xKey;
              }

              for (int j = 0; j < CursorCache.size(); j++) {
                SoftReference ref = (SoftReference) CursorCache.get(tKey[j]);

                if (ref == null) {
                  continue;
                }

                CachedObject cachedObject = (CachedObject) ref.get();

                if ((cachedObject == null) || cachedObject.expired()) {
                  CursorCache.remove(tKey[j]);
                }

                continue;
              }
            }
          }

          if ((CacheCursor != null) && (CacheCursor.size() > 0)) {
            synchronized (this.parent) {
              String[] tKey = new String[CacheCursor.size()];
              int      i    = 0;

              for (final String xKey : CacheCursor.keySet()) {
                tKey[i++] = xKey;
              }

              for (int j = 0; j < CacheCursor.size(); j++) {
                SoftReference ref = (SoftReference) CacheCursor.get(tKey[j]);

                if (ref == null) {
                  continue;
                }

                CachedObject cachedObject = (CachedObject) ref.get();

                if ((cachedObject == null) || cachedObject.expired()) {
                  CacheCursor.remove(tKey[j]);
                }

                continue;
              }
            }
          }

          try {
            Thread.sleep(this.updateInterval);

            if (!active) {
              break;
            }
          } catch (InterruptedException ie) {}
        }
      }
    }


    /**
     * Activate or deactivate the updator thread.
     *
     * @param b True to activate the thread, false to deactivate the thread.
     */
    protected synchronized void setActive(boolean b) {
      log.debug("> setActive");
      active = b;
      log.debug("< setActive");
    }


    /**
     * Set the invoker of this thread. This is used to synchronize the thread
     * with its invoking object.
     *
     * @param parent The parent (Cache) object.
     */
    protected synchronized void setParent(Cache parent) {
      this.parent = parent;
    }
  }

  /**
   * A container for objects that are in the cache.
   */
  private static class CachedObject {
    /**
     * The expiry time for the Object.
     */
    private Date          expiryTime         = new Date();

    /**
     * The Object that is cached.
     */
    private Object        object             = null;

    /**
     * The SoftReference to the uncompressed object.
     */
    @SuppressWarnings("rawtypes")
    private SoftReference uncompressedObject = null;


    /**
     * Whether the object is stored compressed.
     */
    private boolean       compressed         = false;

    /**
     * True if an attempt shall be made to keep a SoftReference to the object in
     * its uncompressed form.
     */
    private boolean       doublebuffer       = false;

    /**
     * Whether or not an object may life forever. Caching the object with a
     * lifetime < 0 will leave the object forever in the cache. But it may still
     * fall out of the cache in an out of memory situation.
     */
    private boolean       livesForever       = false;

    /**
     * The actual lifetime of the object.
     */
    private long          lifeTime           = -1;


    /**
     * Constructor.
     *
     * @param o The object to cache.
     * @param birth The birthday of the object in the cache.
     * @param lifeTime The lifetime we allow this object to stay in the cache.
     * @param doublebuffer True if an attempt shall be made to keep a
     *          SoftReference to the object in its uncompressed form.
     */
    protected CachedObject(Object o, Date birth, long lifeTime, boolean compressed, boolean doublebuffer) {
      this.compressed   = compressed;
      this.doublebuffer = doublebuffer;

      if (this.compressed) {
        try {
          ByteArrayOutputStream fos = new ByteArrayOutputStream();
          GZIPOutputStream      gos = new GZIPOutputStream(fos);
          ObjectOutputStream    oos = new ObjectOutputStream(gos);
          oos.writeObject(o);
          oos.flush();
          oos.close();
          this.object = fos;

          if (this.doublebuffer) {
            this.uncompressedObject = new SoftReference<Object>(o);
          }
        } catch (java.io.IOException ioe) {
          ioe.printStackTrace();
        }
      } else {
        this.object = o;
      }

      if (lifeTime < 0) {
        this.livesForever = true;
      }

      this.expiryTime.setTime(birth.getTime() + (lifeTime * 1000));
      this.lifeTime = lifeTime;
    }


    /**
     * Get the compression status of the object in the cache.
     *
     * @return True if the object is compressed, else false.
     */
    @SuppressWarnings("unused")
    protected boolean getCompressed() {
      return this.compressed;
    }


    /**
     * Set the doublebuffer state for this object.
     *
     * @param doublebuffer True if the doublebuffer shall be activated, else
     *          false.
     */
    protected synchronized void setDoublebuffer(boolean doublebuffer) {
      this.doublebuffer = doublebuffer;
    }


    /**
     * Get the doublebuffer state of the object.
     *
     * @return True if the doublebuffer is active for this object, else false.
     */
    protected synchronized boolean getDoublebuffer() {
      return this.doublebuffer;
    }


    /**
     * Get the lifetime for the object in the cache.
     *
     * @return The lifetime, in milliseconds.
     */
    protected long getLifeTime() {
      return this.lifeTime;
    }


    /**
     * Get the object itself.
     *
     * @return The object itself.
     */
    protected synchronized Object getObject() {
      if (this.compressed) {
        if (this.uncompressedObject != null) {
          Object o = this.uncompressedObject.get();

          if (o != null) {
            return o;
          }
        }

        try {
          ByteArrayOutputStream fos = (ByteArrayOutputStream) this.object;
          ByteArrayInputStream  fis = new ByteArrayInputStream(fos.toByteArray());
          GZIPInputStream       gis = new GZIPInputStream(fis);
          ObjectInputStream     ois = new ObjectInputStream(gis);
          Object                o   = (Object) ois.readObject();
          ois.close();

          if (this.doublebuffer) {
            this.uncompressedObject = new SoftReference<Object>(o);
          }

          return o;
        } catch (java.io.IOException ioe) {
          ioe.printStackTrace();
        } catch (java.lang.ClassNotFoundException cnfe) {
          cnfe.printStackTrace();
        }
      }

      return this.object;
    }


    /**
     * Check whether the object has expired.
     *
     * @return True if it has expired, else false.
     */
    protected boolean expired() {
      if (this.livesForever) {
        return false;
      }

      Date currentTime = Calendar.getInstance().getTime();

      return this.expiryTime.before(currentTime);
    }


    /**
     * Reset the timer for the object in the
     * cache.
     *
     * This is needed when a cache hit occurs; in
     * this case, the object is reappended to the
     * queue, and hence it makes sense to reset
     * the timer for the object.
     *
     * TODO: Actually, it doesn't make that much
     * of sense to reset the timer. We removed the
     * call to this function, because we do want to
     * have a hard expiry time in order to actually
     * get to load new data. To be made configurable.
     */
    @SuppressWarnings("unused")
    protected synchronized void resetTimer() {
      this.expiryTime.setTime(Calendar.getInstance().getTime().getTime() + (this.lifeTime * 1000));
    }
  }
}
