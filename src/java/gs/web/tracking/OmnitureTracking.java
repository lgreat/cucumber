package gs.web.tracking;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public abstract class  OmnitureTracking {

    /**
     * Objects that know how to represent themselves to omniture
     */
    protected static interface OmnitureInformation {
        public String toOmnitureString();
    }

    /**
     * An Omniture eVar, consisting of an EvarNumber and a value. For example, eVar 7 may be set to "foo".
     */
    public static class Evar implements OmnitureInformation {
        private EvarNumber _evarNumber;
        private String _value;

        public Evar(EvarNumber evarNumber, String value) {
            if (evarNumber == null) {
                throw new IllegalArgumentException("EvarNumber must not be null");
            }
            _evarNumber = evarNumber;
            _value = value;
        }

        public String toOmnitureString() {
            return _value;
        }

        public EvarNumber getOmnitureEvar() {
            return _evarNumber;
        }

        public int getNumber() {
            return _evarNumber.getNumber();
        }

        public String toString() {
            return "eVar" + _evarNumber.getNumber() + "=" + _value ;
        }
    }

    /**
     * Typesafe eVar number definitions.
     */
    public enum EvarNumber {
        CrossPromotion(5),
        RegistrationSegment(7);

        private int _number;

        EvarNumber(int num) {
            _number = num;
        }

        public int getNumber() {
            return _number;
        }
    }

    /**
     * Typesafe SuccessEvent definitions.
     */
    public enum SuccessEvent implements OmnitureInformation {
        CommunityRegistration(6),
        ArticleView(7),
        ParentRating(8),
        ParentReview(9),
        ParentSurvey(10),
        NewNewsLetterSubscriber(11),
        CommunityRaiseYourHand(15),
        CommunityDiscussionPost(16),
        CommunityDiscussionReplyPost(17),
        ChoicePackRequest(18),
        SweepstakesEntered(19),
        ArticleComment(20),
        TellAFriend(21),
        MSLAddSchool(27),
        MSLDeleteSchool(28),
        EmailVerified(29),
        CBTipDiscussionPost(30),
        CBTipReplyPost(31),
        CBAdviceDiscussionPost(32),
        CBAdviceReplyPost(33),
        EmailModuleSignup(37),
        FacebookLike(36),
        FacebookSend(38),
        TwitterTweet(39),
        CmsVideo(47),
        WorksheetView(48);

        private int _eventNumber;
        SuccessEvent(int eventNumber){
            _eventNumber = eventNumber;
        }

        public String toOmnitureString() {
            return "event" + _eventNumber + ";";
        }

        protected int getEventNumber(){
            return _eventNumber;
        }
    }
    protected String _events = "";

    /**
     * Add an evar to be tracked. This will overwrite any existing value for that evar.
     */
    public abstract void addEvar(Evar evar);

    /**
     * Add a success event . This will be added in addition to any other existing
     * success events.
     */
    public abstract void addSuccessEvent(SuccessEvent successEvent);

    /**
     * Null-safe method that appends info.toOmnitureString() to destination.
     */
    protected static String addOmnitureInformationToString(OmnitureInformation info, String destination) {
        if (info == null && destination == null){
            return "";
        } else if (info == null){
            return destination;
        } else if (destination == null){
            return info.toOmnitureString();
        }

        if (destination.contains(info.toOmnitureString())){
            return destination;
        } else {
            return destination + info.toOmnitureString();
        }
    }
}
