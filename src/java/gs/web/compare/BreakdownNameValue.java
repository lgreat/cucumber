package gs.web.compare;

public class BreakdownNameValue {
    private String _value;
    private String _name;

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        this._value = value;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BreakdownNameValue that = (BreakdownNameValue) o;

        return _name != null ? _name.equals(that._name) : that._name == null;
    }

    @Override
    public int hashCode() {
        return _name != null ? _name.hashCode() : 0;
    }
}