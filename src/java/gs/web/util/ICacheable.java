package gs.web.util;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public interface ICacheable<KEY, VALUE> {
    public static final long HOUR = 3600000;
    public static final long DAY = 86400000;

    public VALUE getFromCache(KEY key);

    public void putIntoCache(KEY key, VALUE value, long timeToExpiration);

    public void clearCacheKey(KEY key);

    public void clearCache();

}
