package gs.web.util;

/**
 * Helpful data structure for caching.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CachedItem<X> {
    private X _data;
    private long _cacheDuration;
    private long _cachedAt;

    public X getData() {
        return _data;
    }

    public void setData(X data) {
        _data = data;
    }

    public long getCacheDuration() {
        return _cacheDuration;
    }

    public void setCacheDuration(long cacheDuration) {
        _cacheDuration = cacheDuration;
    }

    public long getCachedAt() {
        return _cachedAt;
    }

    public void setCachedAt(long cachedAt) {
        _cachedAt = cachedAt;
    }

    /**
     * @return if cacheDuration milliseconds hasn't transpired, return getData(), otherwise return null.
     */
    public X getDataIfNotExpired() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - _cachedAt > _cacheDuration) {
            return null;
        }
        return _data;
    }
}
