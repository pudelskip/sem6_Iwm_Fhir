import java.util.Date;

public class MyPair {

    private final Date key;
    private final String value;

    public MyPair(Date aKey, String aValue)
    {
        key   = aKey;
        value = aValue;
    }

    public Date key()   { return key; }
    public String value() { return value; }

}

