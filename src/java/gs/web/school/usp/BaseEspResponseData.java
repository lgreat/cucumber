package gs.web.school.usp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gs.data.school.EspResponse;
import org.joda.time.DateTime;

import java.util.*;


/**
 * Decorator pattern. A List of EspResponses that wraps an underlying List of EspResponses and adds additional
 * functionality
 * Not thread safe
 */
public class BaseEspResponseData implements IEspResponseData {

    // the wrapped list of responses
    private List<EspResponse> _responses;

    private Map<String, List<EspResponse>> _responsesByKey = null;
    private Date _oldestResponseDate = null;

    /**
     * sets instance variables back to null, so that the methods that populate them will be re-executed. Should be
     * called when underlying List of responses is modified
     */
    protected void clearCache() {
        _responsesByKey = null;
        _oldestResponseDate = null;
    }

    public BaseEspResponseData(List<EspResponse> responses) {
        if (responses == null) {
            responses = new ArrayList<EspResponse>();
        }
        _responses = responses;
    }

    public BaseEspResponseData() {
        _responses = new ArrayList<EspResponse>();
    }

    /**
     * I had to add this constructor to avoid mayhem in SchoolProfileDataHelper when attempting to expose the underlying
     * List of EspResponses from DB to the caller (in this case, the controller). Data helper only exposes a Map, and
     * the SchoolProfileDataHelper patterns only expose/return a single attribute/object
     * @param responsesByKey
     */
    public BaseEspResponseData(Map<String, List<EspResponse>> responsesByKey) {
        if (responsesByKey == null) {
            _responses = new ArrayList<EspResponse>();
            return;
        }

        List<EspResponse> responses = new ArrayList<EspResponse>();
        Iterator<Map.Entry<String, List<EspResponse>>> iterator= responsesByKey.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, List<EspResponse>> entry = iterator.next();
            responses.addAll(entry.getValue());
        }
        _responses = responses;
        _responsesByKey = responsesByKey;
    }

    /**
     * Given a list of EspResponses, determines the Date of the oldest response. The result is "cached" within
     * an instance variable, so that multiple accesses on the same List of responses will not require iterating again
     * @return a Date
     */
    public Date getOldestResponseDate() {
        if (_responses == null || _responses.isEmpty()) {
            return null;
        }

        if (_oldestResponseDate == null) {
            Date oldestResponse = null;
            for (EspResponse response : _responses) {
                if (oldestResponse == null || response.getCreated().before(oldestResponse)) {
                    oldestResponse = response.getCreated();
                }
            }
            _oldestResponseDate = oldestResponse;
        }
        return _oldestResponseDate;
    }

    /**
     * Determines if the date from the oldest EspResponse was a year ago or sooner. The result is "cached" within
     * an instance variable, so that multiple accesses on the same List of responses will not require iterating again
     * @return true if oldest date >= a year ago
     */
    public boolean hasRecentYearOfData() {
        if (getOldestResponseDate() == null) {
            return false;
        }

        DateTime yearAgo = new DateTime().minusYears(1);
        DateTime oldestResponseDate = new DateTime(getOldestResponseDate());

        return yearAgo.isBefore(oldestResponseDate);
    }

    public List<EspResponse> getResponses() {
        return _responses;
    }

    /**
     * @return A map of Response key (string) to a List of the associated responses
     */
    public Map<String, List<EspResponse>> getResponsesByKey() {
        if (_responses == null || _responses.isEmpty()) {
            return new HashMap<String, List<EspResponse>>();
        }

        if (_responsesByKey == null) {
            _responsesByKey = new HashMap<String, List<EspResponse>>();
        }

        Iterator<EspResponse> iterator = iterator();

        while (iterator.hasNext()) {
            EspResponse response = iterator.next();

            List<EspResponse> responses = _responsesByKey.get(response.getKey());

            if (responses == null) {
                responses = new ArrayList<EspResponse>();
                _responsesByKey.put(response.getKey(), responses);
            }
            responses.add(response);
        }

        return _responsesByKey;
    }

    @Override
    public String toString() {
        return _responses.toString();
    }

    /**
     * Use this if you'd rather have a multimap of Response Keys -> Response Values. Underlying Map is ArraysListMultimap
     */
    public Multimap<String,String> getMultimap() {
        Multimap<String,String> map = ArrayListMultimap.create();

        Iterator<EspResponse> iterator = iterator();

        while (iterator.hasNext()) {
            EspResponse response = iterator.next();
            map.put(response.getKey(), response.getValue());
        }

        return map;
    }

    // delegate methods to _responses generated by IntelliJ
    // methods which modify the underlying List must call clearCache(), so that when the caller asks
    // for information about the underlying ESP responses, the answers are up-to-date

    public int size() {
        return _responses.size();
    }

    public boolean isEmpty() {
        return _responses.isEmpty();
    }

    public boolean contains(Object o) {
        return _responses.contains(o);
    }

    public Iterator<EspResponse> iterator() {
        return _responses.iterator();
    }

    public Object[] toArray() {
        return _responses.toArray();
    }

    public <T> T[] toArray(T[] ts) {
        return _responses.toArray(ts);
    }

    public final boolean add(EspResponse espResponse) {
        clearCache();
        return _responses.add(espResponse);
    }

    public final boolean remove(Object o) {
        clearCache();
        return _responses.remove(o);
    }

    public boolean containsAll(Collection<?> objects) {
        return _responses.containsAll(objects);
    }

    public final boolean addAll(Collection<? extends EspResponse> espResponses) {
        clearCache();
        return _responses.addAll(espResponses);
    }

    public final boolean addAll(int i, Collection<? extends EspResponse> espResponses) {
        clearCache();
        return _responses.addAll(i, espResponses);
    }

    public final boolean removeAll(Collection<?> objects) {
        clearCache();
        return _responses.removeAll(objects);
    }

    public final boolean retainAll(Collection<?> objects) {
        clearCache();
        return _responses.retainAll(objects);
    }

    public final void clear() {
        clearCache();
        _responses.clear();
    }

    @Override
    public boolean equals(Object o) {
        return _responses.equals(o);
    }

    @Override
    public int hashCode() {
        return _responses.hashCode();
    }

    public EspResponse get(int i) {
        return _responses.get(i);
    }

    public EspResponse set(int i, EspResponse espResponse) {
        return _responses.set(i, espResponse);
    }

    public final void add(int i, EspResponse espResponse) {
        clearCache();
        _responses.add(i, espResponse);
    }

    public final EspResponse remove(int i) {
        clearCache();
        return _responses.remove(i);
    }

    public int indexOf(Object o) {
        return _responses.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return _responses.lastIndexOf(o);
    }

    public ListIterator<EspResponse> listIterator() {
        return _responses.listIterator();
    }

    public ListIterator<EspResponse> listIterator(int i) {
        return _responses.listIterator(i);
    }

    public List<EspResponse> subList(int i, int i2) {
        return _responses.subList(i, i2);
    }
}
