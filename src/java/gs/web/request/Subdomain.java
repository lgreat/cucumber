package gs.web.request;

import java.util.EnumSet;

public enum Subdomain {
    PK("pk"),
    WWW("www"),
    CLONE("clone"),
    WILLOW("willow"),
    QA("qa"),
    QA_PREVIEW("qa-preview"),
    STAGING("staging"),
    DEV("dev"),
    DEV_PREVIEW("dev-preview"),
    ;

    private String _value;

    Subdomain(String value) {
        _value = value;
    }

    public String toString() {
        return _value;
    }

    public static Subdomain getByValue(String value){
       Subdomain match = null;
       for (Subdomain domain : EnumSet.allOf(Subdomain.class)) {
           if (domain.toString().equals(value)) {
               match = domain;
           }
       }
       return match;
   }
}
