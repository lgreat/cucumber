package gs.web.auth;


import facebook4j.*;
import facebook4j.auth.AccessToken;
import facebook4j.auth.Authorization;
import facebook4j.conf.Configuration;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONObject;
import gs.data.community.User;
import gs.data.util.CmsUtil;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class FacebookSession implements Facebook {

    public static final String REQUEST_ATTRIBUTE = "facebookSession";
    private Logger _log = Logger.getLogger(FacebookSession.class);

    public static class GsFacebookSessionBuilder {
        private FacebookSession _facebookSession = new FacebookSession();
        private boolean hasAccessToken = false;
        private boolean hasAuthorizationCode = false;

        public GsFacebookSessionBuilder authorizationCode(String authorizationCode) {
            hasAuthorizationCode = true;
            _facebookSession.setAuthorizationCode(authorizationCode);
            return this;
        }

        public GsFacebookSessionBuilder accessToken(String accessToken) {
            boolean hasAccessToken = true;
            _facebookSession.setAccessToken(accessToken);
            return this;
        }

        public GsFacebookSessionBuilder userId(String userId) {
            _facebookSession.setUserId(userId);
            return this;
        }

        public GsFacebookSessionBuilder callbackUrl(String callbackUrl) {
            // yeah, unfortunately the get method actually modifies the underlying state
            _facebookSession.getOAuthAuthorizationURL(callbackUrl);
            return this;
        }

        public FacebookSession build() {
            if (!hasAccessToken && _facebookSession.getAuthorizationCode() != null) {
                //_facebookSession.loadAccessToken();
            }
            return _facebookSession;
        }
    }

    public static GsFacebookSessionBuilder builder() {
        return new GsFacebookSessionBuilder();
    }

    private String _userId;

    private Facebook _facebook;

    private User _user;

    private String _authorizationCode;

    private FacebookSession() {
        _facebook = new FacebookFactory().getInstance();
        _facebook.setOAuthAppId(CmsUtil.getFacebookAppId(), CmsUtil.getFacebookSecret());
    }

    public boolean loadAccessToken() {
        boolean success = false;

        try {
            AccessToken token = getOAuthAccessToken(_authorizationCode);
            success = token != null;
        } catch (Exception e) {
            _log.debug("Problem getting access token: ", e);
            // fail
        }
        return success;
    }

    public String getUserId() {
        if (_userId == null) {
            try {
                _userId = _facebook.getId();
            } catch (Exception e) {
                _log.debug("Could not get userId from Facebook");
                // fail
            }
        }

        return _userId;
    }

    public void setUserId(String userId) {
        _userId = userId;
    }

    public boolean isOwnedBy(User user) {
        return (getUserId() != null && getUserId().equals(user.getFacebookId()));
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        _user = user;
    }

    public void setAuthorizationCode(String authorizationCode) {
        _authorizationCode = authorizationCode;
    }

    public String getAuthorizationCode() {
        return _authorizationCode;
    }

    public void setAccessToken(String accessToken) {
        setOAuthAccessToken(new AccessToken(accessToken, null));
    }

    public boolean isSignedIn() {
        String userId = getUserId();

        return (userId != null);
    }

    public boolean isValid() {
        boolean valid = false;

        if (_authorizationCode == null) {
            valid = false;
            return valid;
        }

        try {
            valid = getMe() != null;
        } catch (Exception e) {
        }

        return valid;
    }

    //////////////////////////////////////////


    public ResponseList<Tag> getTagsOnPhoto(String s, Reading reading) throws FacebookException {
        return _facebook.getTagsOnPhoto(s, reading);
    }

    public void setOAuthAppId(String s, String s2) {
        _facebook.setOAuthAppId(s, s2);
    }

    public void setOAuthPermissions(String s) {
        _facebook.setOAuthPermissions(s);
    }

    public String getOAuthAuthorizationURL(String s) {
        return _facebook.getOAuthAuthorizationURL(s);
    }

    public String getOAuthAuthorizationURL(String s, String s2) {
        return _facebook.getOAuthAuthorizationURL(s, s2);
    }

    public AccessToken getOAuthAccessToken() {
        return _facebook.getOAuthAccessToken();
    }

    public AccessToken getOAuthAccessToken(String s) throws FacebookException {
        return _facebook.getOAuthAccessToken(s);
    }

    public AccessToken getOAuthAppAccessToken() throws FacebookException {
        return _facebook.getOAuthAppAccessToken();
    }

    public void setOAuthAccessToken(AccessToken accessToken) {
        _facebook.setOAuthAccessToken(accessToken);
    }

    public String getId() throws FacebookException, IllegalStateException {
        return _facebook.getId();
    }

    public String getName() throws FacebookException, IllegalStateException {
        return _facebook.getName();
    }

    public String getEmail() throws FacebookException, IllegalStateException {
        return _facebook.getEmail();
    }

    public Authorization getAuthorization() {
        return _facebook.getAuthorization();
    }

    public Configuration getConfiguration() {
        return _facebook.getConfiguration();
    }

    public <T> ResponseList<T> fetchNext(Paging<T> tPaging) throws FacebookException {
        return _facebook.fetchNext(tPaging);
    }

    public <T> ResponseList<T> fetchPrevious(Paging<T> tPaging) throws FacebookException {
        return _facebook.fetchPrevious(tPaging);
    }

    public void shutdown() {
        _facebook.shutdown();
    }

    public facebook4j.User getMe() throws FacebookException {
        return _facebook.getMe();
    }

    public facebook4j.User getMe(Reading reading) throws FacebookException {
        return _facebook.getMe(reading);
    }

    public facebook4j.User getUser(String s) throws FacebookException {
        return _facebook.getUser(s);
    }

    public facebook4j.User getUser(String s, Reading reading) throws FacebookException {
        return _facebook.getUser(s, reading);
    }

    public URL getPictureURL() throws FacebookException {
        return _facebook.getPictureURL();
    }

    public URL getPictureURL(PictureSize pictureSize) throws FacebookException {
        return _facebook.getPictureURL(pictureSize);
    }

    public URL getPictureURL(String s) throws FacebookException {
        return _facebook.getPictureURL(s);
    }

    public URL getPictureURL(String s, PictureSize pictureSize) throws FacebookException {
        return _facebook.getPictureURL(s, pictureSize);
    }

    public List<facebook4j.User> getUsers(String... strings) throws FacebookException {
        return _facebook.getUsers(strings);
    }

    public ResponseList<Account> getAccounts() throws FacebookException {
        return _facebook.getAccounts();
    }

    public ResponseList<Account> getAccounts(Reading reading) throws FacebookException {
        return _facebook.getAccounts(reading);
    }

    public ResponseList<Account> getAccounts(String s) throws FacebookException {
        return _facebook.getAccounts(s);
    }

    public ResponseList<Account> getAccounts(String s, Reading reading) throws FacebookException {
        return _facebook.getAccounts(s, reading);
    }

    public ResponseList<Activity> getActivities() throws FacebookException {
        return _facebook.getActivities();
    }

    public ResponseList<Activity> getActivities(Reading reading) throws FacebookException {
        return _facebook.getActivities(reading);
    }

    public ResponseList<Activity> getActivities(String s) throws FacebookException {
        return _facebook.getActivities(s);
    }

    public ResponseList<Activity> getActivities(String s, Reading reading) throws FacebookException {
        return _facebook.getActivities(s, reading);
    }

    public ResponseList<Album> getAlbums() throws FacebookException {
        return _facebook.getAlbums();
    }

    public ResponseList<Album> getAlbums(Reading reading) throws FacebookException {
        return _facebook.getAlbums(reading);
    }

    public ResponseList<Album> getAlbums(String s) throws FacebookException {
        return _facebook.getAlbums(s);
    }

    public ResponseList<Album> getAlbums(String s, Reading reading) throws FacebookException {
        return _facebook.getAlbums(s, reading);
    }

    public String createAlbum(AlbumCreate albumCreate) throws FacebookException {
        return _facebook.createAlbum(albumCreate);
    }

    public String createAlbum(String s, AlbumCreate albumCreate) throws FacebookException {
        return _facebook.createAlbum(s, albumCreate);
    }

    public Album getAlbum(String s) throws FacebookException {
        return _facebook.getAlbum(s);
    }

    public Album getAlbum(String s, Reading reading) throws FacebookException {
        return _facebook.getAlbum(s, reading);
    }

    public ResponseList<Photo> getAlbumPhotos(String s) throws FacebookException {
        return _facebook.getAlbumPhotos(s);
    }

    public ResponseList<Photo> getAlbumPhotos(String s, Reading reading) throws FacebookException {
        return _facebook.getAlbumPhotos(s, reading);
    }

    public String addAlbumPhoto(String s, Media media) throws FacebookException {
        return _facebook.addAlbumPhoto(s, media);
    }

    public String addAlbumPhoto(String s, Media media, String s2) throws FacebookException {
        return _facebook.addAlbumPhoto(s, media, s2);
    }

    public ResponseList<Comment> getAlbumComments(String s) throws FacebookException {
        return _facebook.getAlbumComments(s);
    }

    public ResponseList<Comment> getAlbumComments(String s, Reading reading) throws FacebookException {
        return _facebook.getAlbumComments(s, reading);
    }

    public String commentAlbum(String s, String s2) throws FacebookException {
        return _facebook.commentAlbum(s, s2);
    }

    public ResponseList<Like> getAlbumLikes(String s) throws FacebookException {
        return _facebook.getAlbumLikes(s);
    }

    public ResponseList<Like> getAlbumLikes(String s, Reading reading) throws FacebookException {
        return _facebook.getAlbumLikes(s, reading);
    }

    public boolean likeAlbum(String s) throws FacebookException {
        return _facebook.likeAlbum(s);
    }

    public boolean unlikeAlbum(String s) throws FacebookException {
        return _facebook.unlikeAlbum(s);
    }

    public URL getAlbumCoverPhoto(String s) throws FacebookException {
        return _facebook.getAlbumCoverPhoto(s);
    }

    public ResponseList<Checkin> getCheckins() throws FacebookException {
        return _facebook.getCheckins();
    }

    public ResponseList<Checkin> getCheckins(Reading reading) throws FacebookException {
        return _facebook.getCheckins(reading);
    }

    public ResponseList<Checkin> getCheckins(String s) throws FacebookException {
        return _facebook.getCheckins(s);
    }

    public ResponseList<Checkin> getCheckins(String s, Reading reading) throws FacebookException {
        return _facebook.getCheckins(s, reading);
    }

    public String checkin(CheckinCreate checkinCreate) throws FacebookException {
        return _facebook.checkin(checkinCreate);
    }

    public String checkin(String s, CheckinCreate checkinCreate) throws FacebookException {
        return _facebook.checkin(s, checkinCreate);
    }

    public Checkin getCheckin(String s) throws FacebookException {
        return _facebook.getCheckin(s);
    }

    public Checkin getCheckin(String s, Reading reading) throws FacebookException {
        return _facebook.getCheckin(s, reading);
    }

    public ResponseList<Comment> getCheckinComments(String s) throws FacebookException {
        return _facebook.getCheckinComments(s);
    }

    public ResponseList<Comment> getCheckinComments(String s, Reading reading) throws FacebookException {
        return _facebook.getCheckinComments(s, reading);
    }

    public String commentCheckin(String s, String s2) throws FacebookException {
        return _facebook.commentCheckin(s, s2);
    }

    public ResponseList<Like> getCheckinLikes(String s) throws FacebookException {
        return _facebook.getCheckinLikes(s);
    }

    public ResponseList<Like> getCheckinLikes(String s, Reading reading) throws FacebookException {
        return _facebook.getCheckinLikes(s, reading);
    }

    public boolean likeCheckin(String s) throws FacebookException {
        return _facebook.likeCheckin(s);
    }

    public boolean unlikeCheckin(String s) throws FacebookException {
        return _facebook.unlikeCheckin(s);
    }

    public Comment getComment(String s) throws FacebookException {
        return _facebook.getComment(s);
    }

    public boolean deleteComment(String s) throws FacebookException {
        return _facebook.deleteComment(s);
    }

    public ResponseList<Like> getCommentLikes(String s) throws FacebookException {
        return _facebook.getCommentLikes(s);
    }

    public ResponseList<Like> getCommentLikes(String s, Reading reading) throws FacebookException {
        return _facebook.getCommentLikes(s, reading);
    }

    public boolean likeComment(String s) throws FacebookException {
        return _facebook.likeComment(s);
    }

    public boolean unlikeComment(String s) throws FacebookException {
        return _facebook.unlikeComment(s);
    }

    public Domain getDomain(String s) throws FacebookException {
        return _facebook.getDomain(s);
    }

    public Domain getDomainByName(String s) throws FacebookException {
        return _facebook.getDomainByName(s);
    }

    public List<Domain> getDomainsByName(String... strings) throws FacebookException {
        return _facebook.getDomainsByName(strings);
    }

    public ResponseList<Event> getEvents() throws FacebookException {
        return _facebook.getEvents();
    }

    public ResponseList<Event> getEvents(Reading reading) throws FacebookException {
        return _facebook.getEvents(reading);
    }

    public ResponseList<Event> getEvents(String s) throws FacebookException {
        return _facebook.getEvents(s);
    }

    public ResponseList<Event> getEvents(String s, Reading reading) throws FacebookException {
        return _facebook.getEvents(s, reading);
    }

    public String createEvent(EventUpdate eventUpdate) throws FacebookException {
        return _facebook.createEvent(eventUpdate);
    }

    public String createEvent(String s, EventUpdate eventUpdate) throws FacebookException {
        return _facebook.createEvent(s, eventUpdate);
    }

    public boolean editEvent(String s, EventUpdate eventUpdate) throws FacebookException {
        return _facebook.editEvent(s, eventUpdate);
    }

    public boolean deleteEvent(String s) throws FacebookException {
        return _facebook.deleteEvent(s);
    }

    public Event getEvent(String s) throws FacebookException {
        return _facebook.getEvent(s);
    }

    public Event getEvent(String s, Reading reading) throws FacebookException {
        return _facebook.getEvent(s, reading);
    }

    public ResponseList<Post> getEventFeed(String s) throws FacebookException {
        return _facebook.getEventFeed(s);
    }

    public ResponseList<Post> getEventFeed(String s, Reading reading) throws FacebookException {
        return _facebook.getEventFeed(s, reading);
    }

    public String postEventFeed(String s, PostUpdate postUpdate) throws FacebookException {
        return _facebook.postEventFeed(s, postUpdate);
    }

    public String postEventLink(String s, URL url) throws FacebookException {
        return _facebook.postEventLink(s, url);
    }

    public String postEventLink(String s, URL url, String s2) throws FacebookException {
        return _facebook.postEventLink(s, url, s2);
    }

    public String postEventStatusMessage(String s, String s2) throws FacebookException {
        return _facebook.postEventStatusMessage(s, s2);
    }

    public ResponseList<RSVPStatus> getRSVPStatusAsNoreply(String s) throws FacebookException {
        return _facebook.getRSVPStatusAsNoreply(s);
    }

    public ResponseList<RSVPStatus> getRSVPStatusAsNoreply(String s, String s2) throws FacebookException {
        return _facebook.getRSVPStatusAsNoreply(s, s2);
    }

    public ResponseList<RSVPStatus> getRSVPStatusAsInvited(String s) throws FacebookException {
        return _facebook.getRSVPStatusAsInvited(s);
    }

    public ResponseList<RSVPStatus> getRSVPStatusAsInvited(String s, String s2) throws FacebookException {
        return _facebook.getRSVPStatusAsInvited(s, s2);
    }

    public boolean inviteToEvent(String s, String s2) throws FacebookException {
        return _facebook.inviteToEvent(s, s2);
    }

    public boolean inviteToEvent(String s, String[] strings) throws FacebookException {
        return _facebook.inviteToEvent(s, strings);
    }

    public boolean uninviteFromEvent(String s, String s2) throws FacebookException {
        return _facebook.uninviteFromEvent(s, s2);
    }

    public ResponseList<RSVPStatus> getRSVPStatusInAttending(String s) throws FacebookException {
        return _facebook.getRSVPStatusInAttending(s);
    }

    public ResponseList<RSVPStatus> getRSVPStatusInAttending(String s, String s2) throws FacebookException {
        return _facebook.getRSVPStatusInAttending(s, s2);
    }

    public boolean rsvpEventAsAttending(String s) throws FacebookException {
        return _facebook.rsvpEventAsAttending(s);
    }

    public ResponseList<RSVPStatus> getRSVPStatusInMaybe(String s) throws FacebookException {
        return _facebook.getRSVPStatusInMaybe(s);
    }

    public ResponseList<RSVPStatus> getRSVPStatusInMaybe(String s, String s2) throws FacebookException {
        return _facebook.getRSVPStatusInMaybe(s, s2);
    }

    public boolean rsvpEventAsMaybe(String s) throws FacebookException {
        return _facebook.rsvpEventAsMaybe(s);
    }

    public ResponseList<RSVPStatus> getRSVPStatusInDeclined(String s) throws FacebookException {
        return _facebook.getRSVPStatusInDeclined(s);
    }

    public ResponseList<RSVPStatus> getRSVPStatusInDeclined(String s, String s2) throws FacebookException {
        return _facebook.getRSVPStatusInDeclined(s, s2);
    }

    public boolean rsvpEventAsDeclined(String s) throws FacebookException {
        return _facebook.rsvpEventAsDeclined(s);
    }

    public URL getEventPictureURL(String s) throws FacebookException {
        return _facebook.getEventPictureURL(s);
    }

    public URL getEventPictureURL(String s, PictureSize pictureSize) throws FacebookException {
        return _facebook.getEventPictureURL(s, pictureSize);
    }

    public boolean updateEventPicture(String s, Media media) throws FacebookException {
        return _facebook.updateEventPicture(s, media);
    }

    public boolean deleteEventPicture(String s) throws FacebookException {
        return _facebook.deleteEventPicture(s);
    }

    public ResponseList<Photo> getEventPhotos(String s) throws FacebookException {
        return _facebook.getEventPhotos(s);
    }

    public ResponseList<Photo> getEventPhotos(String s, Reading reading) throws FacebookException {
        return _facebook.getEventPhotos(s, reading);
    }

    public String postEventPhoto(String s, Media media) throws FacebookException {
        return _facebook.postEventPhoto(s, media);
    }

    public String postEventPhoto(String s, Media media, String s2) throws FacebookException {
        return _facebook.postEventPhoto(s, media, s2);
    }

    public ResponseList<Video> getEventVideos(String s) throws FacebookException {
        return _facebook.getEventVideos(s);
    }

    public ResponseList<Video> getEventVideos(String s, Reading reading) throws FacebookException {
        return _facebook.getEventVideos(s, reading);
    }

    public String postEventVideo(String s, Media media) throws FacebookException {
        return _facebook.postEventVideo(s, media);
    }

    public String postEventVideo(String s, Media media, String s2, String s3) throws FacebookException {
        return _facebook.postEventVideo(s, media, s2, s3);
    }

    public ResponseList<Family> getFamily() throws FacebookException {
        return _facebook.getFamily();
    }

    public ResponseList<Family> getFamily(Reading reading) throws FacebookException {
        return _facebook.getFamily(reading);
    }

    public ResponseList<Family> getFamily(String s) throws FacebookException {
        return _facebook.getFamily(s);
    }

    public ResponseList<Family> getFamily(String s, Reading reading) throws FacebookException {
        return _facebook.getFamily(s, reading);
    }

    public ResponseList<Book> getBooks() throws FacebookException {
        return _facebook.getBooks();
    }

    public ResponseList<Book> getBooks(Reading reading) throws FacebookException {
        return _facebook.getBooks(reading);
    }

    public ResponseList<Book> getBooks(String s) throws FacebookException {
        return _facebook.getBooks(s);
    }

    public ResponseList<Book> getBooks(String s, Reading reading) throws FacebookException {
        return _facebook.getBooks(s, reading);
    }

    public ResponseList<Game> getGames() throws FacebookException {
        return _facebook.getGames();
    }

    public ResponseList<Game> getGames(Reading reading) throws FacebookException {
        return _facebook.getGames(reading);
    }

    public ResponseList<Game> getGames(String s) throws FacebookException {
        return _facebook.getGames(s);
    }

    public ResponseList<Game> getGames(String s, Reading reading) throws FacebookException {
        return _facebook.getGames(s, reading);
    }

    public ResponseList<Movie> getMovies() throws FacebookException {
        return _facebook.getMovies();
    }

    public ResponseList<Movie> getMovies(Reading reading) throws FacebookException {
        return _facebook.getMovies(reading);
    }

    public ResponseList<Movie> getMovies(String s) throws FacebookException {
        return _facebook.getMovies(s);
    }

    public ResponseList<Movie> getMovies(String s, Reading reading) throws FacebookException {
        return _facebook.getMovies(s, reading);
    }

    public ResponseList<Music> getMusic() throws FacebookException {
        return _facebook.getMusic();
    }

    public ResponseList<Music> getMusic(Reading reading) throws FacebookException {
        return _facebook.getMusic(reading);
    }

    public ResponseList<Music> getMusic(String s) throws FacebookException {
        return _facebook.getMusic(s);
    }

    public ResponseList<Music> getMusic(String s, Reading reading) throws FacebookException {
        return _facebook.getMusic(s, reading);
    }

    public ResponseList<Television> getTelevision() throws FacebookException {
        return _facebook.getTelevision();
    }

    public ResponseList<Television> getTelevision(Reading reading) throws FacebookException {
        return _facebook.getTelevision(reading);
    }

    public ResponseList<Television> getTelevision(String s) throws FacebookException {
        return _facebook.getTelevision(s);
    }

    public ResponseList<Television> getTelevision(String s, Reading reading) throws FacebookException {
        return _facebook.getTelevision(s, reading);
    }

    public ResponseList<Interest> getInterests() throws FacebookException {
        return _facebook.getInterests();
    }

    public ResponseList<Interest> getInterests(Reading reading) throws FacebookException {
        return _facebook.getInterests(reading);
    }

    public ResponseList<Interest> getInterests(String s) throws FacebookException {
        return _facebook.getInterests(s);
    }

    public ResponseList<Interest> getInterests(String s, Reading reading) throws FacebookException {
        return _facebook.getInterests(s, reading);
    }

    public ResponseList<Friend> getFriends() throws FacebookException {
        return _facebook.getFriends();
    }

    public ResponseList<Friend> getFriends(Reading reading) throws FacebookException {
        return _facebook.getFriends(reading);
    }

    public ResponseList<Friend> getFriends(String s) throws FacebookException {
        return _facebook.getFriends(s);
    }

    public ResponseList<Friend> getFriends(String s, Reading reading) throws FacebookException {
        return _facebook.getFriends(s, reading);
    }

    public ResponseList<Friend> getBelongsFriend(String s) throws FacebookException {
        return _facebook.getBelongsFriend(s);
    }

    public ResponseList<Friend> getBelongsFriend(String s, Reading reading) throws FacebookException {
        return _facebook.getBelongsFriend(s, reading);
    }

    public ResponseList<Friend> getBelongsFriend(String s, String s2) throws FacebookException {
        return _facebook.getBelongsFriend(s, s2);
    }

    public ResponseList<Friend> getBelongsFriend(String s, String s2, Reading reading) throws FacebookException {
        return _facebook.getBelongsFriend(s, s2, reading);
    }

    public ResponseList<Friendlist> getFriendlists() throws FacebookException {
        return _facebook.getFriendlists();
    }

    public ResponseList<Friendlist> getFriendlists(Reading reading) throws FacebookException {
        return _facebook.getFriendlists(reading);
    }

    public ResponseList<Friendlist> getFriendlists(String s) throws FacebookException {
        return _facebook.getFriendlists(s);
    }

    public ResponseList<Friendlist> getFriendlists(String s, Reading reading) throws FacebookException {
        return _facebook.getFriendlists(s, reading);
    }

    public String createFriendlist(String s) throws FacebookException {
        return _facebook.createFriendlist(s);
    }

    public String createFriendlist(String s, String s2) throws FacebookException {
        return _facebook.createFriendlist(s, s2);
    }

    public Friendlist getFriendlist(String s) throws FacebookException {
        return _facebook.getFriendlist(s);
    }

    public Friendlist getFriendlist(String s, Reading reading) throws FacebookException {
        return _facebook.getFriendlist(s, reading);
    }

    public boolean deleteFriendlist(String s) throws FacebookException {
        return _facebook.deleteFriendlist(s);
    }

    public ResponseList<Friend> getFriendlistMembers(String s) throws FacebookException {
        return _facebook.getFriendlistMembers(s);
    }

    public boolean addFriendlistMember(String s, String s2) throws FacebookException {
        return _facebook.addFriendlistMember(s, s2);
    }

    public boolean removeFriendlistMember(String s, String s2) throws FacebookException {
        return _facebook.removeFriendlistMember(s, s2);
    }

    public ResponseList<FriendRequest> getFriendRequests() throws FacebookException {
        return _facebook.getFriendRequests();
    }

    public ResponseList<FriendRequest> getFriendRequests(Reading reading) throws FacebookException {
        return _facebook.getFriendRequests(reading);
    }

    public ResponseList<FriendRequest> getFriendRequests(String s) throws FacebookException {
        return _facebook.getFriendRequests(s);
    }

    public ResponseList<FriendRequest> getFriendRequests(String s, Reading reading) throws FacebookException {
        return _facebook.getFriendRequests(s, reading);
    }

    public ResponseList<Friend> getMutualFriends(String s) throws FacebookException {
        return _facebook.getMutualFriends(s);
    }

    public ResponseList<Friend> getMutualFriends(String s, Reading reading) throws FacebookException {
        return _facebook.getMutualFriends(s, reading);
    }

    public ResponseList<Friend> getMutualFriends(String s, String s2) throws FacebookException {
        return _facebook.getMutualFriends(s, s2);
    }

    public ResponseList<Friend> getMutualFriends(String s, String s2, Reading reading) throws FacebookException {
        return _facebook.getMutualFriends(s, s2, reading);
    }

    public ResponseList<Achievement> getAchievements() throws FacebookException {
        return _facebook.getAchievements();
    }

    public ResponseList<Achievement> getAchievements(Reading reading) throws FacebookException {
        return _facebook.getAchievements(reading);
    }

    public ResponseList<Achievement> getAchievements(String s) throws FacebookException {
        return _facebook.getAchievements(s);
    }

    public ResponseList<Achievement> getAchievements(String s, Reading reading) throws FacebookException {
        return _facebook.getAchievements(s, reading);
    }

    public String postAchievement(URL url) throws FacebookException {
        return _facebook.postAchievement(url);
    }

    public String postAchievement(String s, URL url) throws FacebookException {
        return _facebook.postAchievement(s, url);
    }

    public boolean deleteAchievement(URL url) throws FacebookException {
        return _facebook.deleteAchievement(url);
    }

    public boolean deleteAchievement(String s, URL url) throws FacebookException {
        return _facebook.deleteAchievement(s, url);
    }

    public ResponseList<Score> getScores() throws FacebookException {
        return _facebook.getScores();
    }

    public ResponseList<Score> getScores(Reading reading) throws FacebookException {
        return _facebook.getScores(reading);
    }

    public ResponseList<Score> getScores(String s) throws FacebookException {
        return _facebook.getScores(s);
    }

    public ResponseList<Score> getScores(String s, Reading reading) throws FacebookException {
        return _facebook.getScores(s, reading);
    }

    public boolean postScore(int i) throws FacebookException {
        return _facebook.postScore(i);
    }

    public boolean postScore(String s, int i) throws FacebookException {
        return _facebook.postScore(s, i);
    }

    public boolean deleteScore() throws FacebookException {
        return _facebook.deleteScore();
    }

    public boolean deleteScore(String s) throws FacebookException {
        return _facebook.deleteScore(s);
    }

    public ResponseList<Group> getGroups() throws FacebookException {
        return _facebook.getGroups();
    }

    public ResponseList<Group> getGroups(Reading reading) throws FacebookException {
        return _facebook.getGroups(reading);
    }

    public ResponseList<Group> getGroups(String s) throws FacebookException {
        return _facebook.getGroups(s);
    }

    public ResponseList<Group> getGroups(String s, Reading reading) throws FacebookException {
        return _facebook.getGroups(s, reading);
    }

    public Group getGroup(String s) throws FacebookException {
        return _facebook.getGroup(s);
    }

    public Group getGroup(String s, Reading reading) throws FacebookException {
        return _facebook.getGroup(s, reading);
    }

    public ResponseList<Post> getGroupFeed(String s) throws FacebookException {
        return _facebook.getGroupFeed(s);
    }

    public ResponseList<Post> getGroupFeed(String s, Reading reading) throws FacebookException {
        return _facebook.getGroupFeed(s, reading);
    }

    public String postGroupFeed(String s, PostUpdate postUpdate) throws FacebookException {
        return _facebook.postGroupFeed(s, postUpdate);
    }

    public String postGroupLink(String s, URL url) throws FacebookException {
        return _facebook.postGroupLink(s, url);
    }

    public String postGroupLink(String s, URL url, String s2) throws FacebookException {
        return _facebook.postGroupLink(s, url, s2);
    }

    public String postGroupStatusMessage(String s, String s2) throws FacebookException {
        return _facebook.postGroupStatusMessage(s, s2);
    }

    public ResponseList<GroupMember> getGroupMembers(String s) throws FacebookException {
        return _facebook.getGroupMembers(s);
    }

    public ResponseList<GroupMember> getGroupMembers(String s, Reading reading) throws FacebookException {
        return _facebook.getGroupMembers(s, reading);
    }

    public URL getGroupPictureURL(String s) throws FacebookException {
        return _facebook.getGroupPictureURL(s);
    }

    public ResponseList<GroupDoc> getGroupDocs(String s) throws FacebookException {
        return _facebook.getGroupDocs(s);
    }

    public ResponseList<GroupDoc> getGroupDocs(String s, Reading reading) throws FacebookException {
        return _facebook.getGroupDocs(s, reading);
    }

    public ResponseList<Like> getUserLikes() throws FacebookException {
        return _facebook.getUserLikes();
    }

    public ResponseList<Like> getUserLikes(Reading reading) throws FacebookException {
        return _facebook.getUserLikes(reading);
    }

    public ResponseList<Like> getUserLikes(String s) throws FacebookException {
        return _facebook.getUserLikes(s);
    }

    public ResponseList<Like> getUserLikes(String s, Reading reading) throws FacebookException {
        return _facebook.getUserLikes(s, reading);
    }

    public Link getLink(String s) throws FacebookException {
        return _facebook.getLink(s);
    }

    public Link getLink(String s, Reading reading) throws FacebookException {
        return _facebook.getLink(s, reading);
    }

    public ResponseList<Comment> getLinkComments(String s) throws FacebookException {
        return _facebook.getLinkComments(s);
    }

    public ResponseList<Comment> getLinkComments(String s, Reading reading) throws FacebookException {
        return _facebook.getLinkComments(s, reading);
    }

    public String commentLink(String s, String s2) throws FacebookException {
        return _facebook.commentLink(s, s2);
    }

    public ResponseList<Like> getLinkLikes(String s) throws FacebookException {
        return _facebook.getLinkLikes(s);
    }

    public ResponseList<Like> getLinkLikes(String s, Reading reading) throws FacebookException {
        return _facebook.getLinkLikes(s, reading);
    }

    public boolean likeLink(String s) throws FacebookException {
        return _facebook.likeLink(s);
    }

    public boolean unlikeLink(String s) throws FacebookException {
        return _facebook.unlikeLink(s);
    }

    public ResponseList<Location> getLocations() throws FacebookException {
        return _facebook.getLocations();
    }

    public ResponseList<Location> getLocations(Reading reading) throws FacebookException {
        return _facebook.getLocations(reading);
    }

    public ResponseList<Location> getLocations(String s) throws FacebookException {
        return _facebook.getLocations(s);
    }

    public ResponseList<Location> getLocations(String s, Reading reading) throws FacebookException {
        return _facebook.getLocations(s, reading);
    }

    public InboxResponseList<Inbox> getInbox() throws FacebookException {
        return _facebook.getInbox();
    }

    public InboxResponseList<Inbox> getInbox(Reading reading) throws FacebookException {
        return _facebook.getInbox(reading);
    }

    public InboxResponseList<Inbox> getInbox(String s) throws FacebookException {
        return _facebook.getInbox(s);
    }

    public InboxResponseList<Inbox> getInbox(String s, Reading reading) throws FacebookException {
        return _facebook.getInbox(s, reading);
    }

    public ResponseList<Message> getOutbox() throws FacebookException {
        return _facebook.getOutbox();
    }

    public ResponseList<Message> getOutbox(Reading reading) throws FacebookException {
        return _facebook.getOutbox(reading);
    }

    public ResponseList<Message> getOutbox(String s) throws FacebookException {
        return _facebook.getOutbox(s);
    }

    public ResponseList<Message> getOutbox(String s, Reading reading) throws FacebookException {
        return _facebook.getOutbox(s, reading);
    }

    public ResponseList<Message> getUpdates() throws FacebookException {
        return _facebook.getUpdates();
    }

    public ResponseList<Message> getUpdates(Reading reading) throws FacebookException {
        return _facebook.getUpdates(reading);
    }

    public ResponseList<Message> getUpdates(String s) throws FacebookException {
        return _facebook.getUpdates(s);
    }

    public ResponseList<Message> getUpdates(String s, Reading reading) throws FacebookException {
        return _facebook.getUpdates(s, reading);
    }

    public Message getMessage(String s) throws FacebookException {
        return _facebook.getMessage(s);
    }

    public Message getMessage(String s, Reading reading) throws FacebookException {
        return _facebook.getMessage(s, reading);
    }

    public ResponseList<Note> getNotes() throws FacebookException {
        return _facebook.getNotes();
    }

    public ResponseList<Note> getNotes(Reading reading) throws FacebookException {
        return _facebook.getNotes(reading);
    }

    public ResponseList<Note> getNotes(String s) throws FacebookException {
        return _facebook.getNotes(s);
    }

    public ResponseList<Note> getNotes(String s, Reading reading) throws FacebookException {
        return _facebook.getNotes(s, reading);
    }

    public String createNote(String s, String s2) throws FacebookException {
        return _facebook.createNote(s, s2);
    }

    public String createNote(String s, String s2, String s3) throws FacebookException {
        return _facebook.createNote(s, s2, s3);
    }

    public Note getNote(String s) throws FacebookException {
        return _facebook.getNote(s);
    }

    public Note getNote(String s, Reading reading) throws FacebookException {
        return _facebook.getNote(s, reading);
    }

    public ResponseList<Comment> getNoteComments(String s) throws FacebookException {
        return _facebook.getNoteComments(s);
    }

    public ResponseList<Comment> getNoteComments(String s, Reading reading) throws FacebookException {
        return _facebook.getNoteComments(s, reading);
    }

    public String commentNote(String s, String s2) throws FacebookException {
        return _facebook.commentNote(s, s2);
    }

    public ResponseList<Like> getNoteLikes(String s) throws FacebookException {
        return _facebook.getNoteLikes(s);
    }

    public ResponseList<Like> getNoteLikes(String s, Reading reading) throws FacebookException {
        return _facebook.getNoteLikes(s, reading);
    }

    public boolean likeNote(String s) throws FacebookException {
        return _facebook.likeNote(s);
    }

    public boolean unlikeNote(String s) throws FacebookException {
        return _facebook.unlikeNote(s);
    }

    public ResponseList<Notification> getNotifications() throws FacebookException {
        return _facebook.getNotifications();
    }

    public ResponseList<Notification> getNotifications(boolean b) throws FacebookException {
        return _facebook.getNotifications(b);
    }

    public ResponseList<Notification> getNotifications(Reading reading) throws FacebookException {
        return _facebook.getNotifications(reading);
    }

    public ResponseList<Notification> getNotifications(Reading reading, boolean b) throws FacebookException {
        return _facebook.getNotifications(reading, b);
    }

    public ResponseList<Notification> getNotifications(String s) throws FacebookException {
        return _facebook.getNotifications(s);
    }

    public ResponseList<Notification> getNotifications(String s, boolean b) throws FacebookException {
        return _facebook.getNotifications(s, b);
    }

    public ResponseList<Notification> getNotifications(String s, Reading reading) throws FacebookException {
        return _facebook.getNotifications(s, reading);
    }

    public ResponseList<Notification> getNotifications(String s, Reading reading, boolean b) throws FacebookException {
        return _facebook.getNotifications(s, reading, b);
    }

    public boolean markNotificationAsRead(String s) throws FacebookException {
        return _facebook.markNotificationAsRead(s);
    }

    public List<Permission> getPermissions() throws FacebookException {
        return _facebook.getPermissions();
    }

    public List<Permission> getPermissions(String s) throws FacebookException {
        return _facebook.getPermissions(s);
    }

    public boolean revokePermission(String s) throws FacebookException {
        return _facebook.revokePermission(s);
    }

    public boolean revokePermission(String s, String s2) throws FacebookException {
        return _facebook.revokePermission(s, s2);
    }

    public ResponseList<Photo> getPhotos() throws FacebookException {
        return _facebook.getPhotos();
    }

    public ResponseList<Photo> getPhotos(Reading reading) throws FacebookException {
        return _facebook.getPhotos(reading);
    }

    public ResponseList<Photo> getPhotos(String s) throws FacebookException {
        return _facebook.getPhotos(s);
    }

    public ResponseList<Photo> getPhotos(String s, Reading reading) throws FacebookException {
        return _facebook.getPhotos(s, reading);
    }

    public String postPhoto(Media media) throws FacebookException {
        return _facebook.postPhoto(media);
    }

    public String postPhoto(Media media, String s, String s2, boolean b) throws FacebookException {
        return _facebook.postPhoto(media, s, s2, b);
    }

    public String postPhoto(String s, Media media) throws FacebookException {
        return _facebook.postPhoto(s, media);
    }

    public String postPhoto(String s, Media media, String s2, String s3, boolean b) throws FacebookException {
        return _facebook.postPhoto(s, media, s2, s3, b);
    }

    public boolean deletePhoto(String s) throws FacebookException {
        return _facebook.deletePhoto(s);
    }

    public Photo getPhoto(String s) throws FacebookException {
        return _facebook.getPhoto(s);
    }

    public Photo getPhoto(String s, Reading reading) throws FacebookException {
        return _facebook.getPhoto(s, reading);
    }

    public ResponseList<Comment> getPhotoComments(String s) throws FacebookException {
        return _facebook.getPhotoComments(s);
    }

    public ResponseList<Comment> getPhotoComments(String s, Reading reading) throws FacebookException {
        return _facebook.getPhotoComments(s, reading);
    }

    public String commentPhoto(String s, String s2) throws FacebookException {
        return _facebook.commentPhoto(s, s2);
    }

    public ResponseList<Like> getPhotoLikes(String s) throws FacebookException {
        return _facebook.getPhotoLikes(s);
    }

    public ResponseList<Like> getPhotoLikes(String s, Reading reading) throws FacebookException {
        return _facebook.getPhotoLikes(s, reading);
    }

    public boolean likePhoto(String s) throws FacebookException {
        return _facebook.likePhoto(s);
    }

    public boolean unlikePhoto(String s) throws FacebookException {
        return _facebook.unlikePhoto(s);
    }

    public URL getPhotoURL(String s) throws FacebookException {
        return _facebook.getPhotoURL(s);
    }

    public ResponseList<Tag> getTagsOnPhoto(String s) throws FacebookException {
        return _facebook.getTagsOnPhoto(s);
    }

    public boolean addTagToPhoto(String s, String s2) throws FacebookException {
        return _facebook.addTagToPhoto(s, s2);
    }

    public boolean addTagToPhoto(String s, List<String> strings) throws FacebookException {
        return _facebook.addTagToPhoto(s, strings);
    }

    public boolean addTagToPhoto(String s, TagUpdate tagUpdate) throws FacebookException {
        return _facebook.addTagToPhoto(s, tagUpdate);
    }

    public boolean updateTagOnPhoto(String s, String s2) throws FacebookException {
        return _facebook.updateTagOnPhoto(s, s2);
    }

    public boolean updateTagOnPhoto(String s, TagUpdate tagUpdate) throws FacebookException {
        return _facebook.updateTagOnPhoto(s, tagUpdate);
    }

    public ResponseList<Poke> getPokes() throws FacebookException {
        return _facebook.getPokes();
    }

    public ResponseList<Poke> getPokes(Reading reading) throws FacebookException {
        return _facebook.getPokes(reading);
    }

    public ResponseList<Poke> getPokes(String s) throws FacebookException {
        return _facebook.getPokes(s);
    }

    public ResponseList<Poke> getPokes(String s, Reading reading) throws FacebookException {
        return _facebook.getPokes(s, reading);
    }

    public ResponseList<Post> getFeed() throws FacebookException {
        return _facebook.getFeed();
    }

    public ResponseList<Post> getFeed(Reading reading) throws FacebookException {
        return _facebook.getFeed(reading);
    }

    public ResponseList<Post> getFeed(String s) throws FacebookException {
        return _facebook.getFeed(s);
    }

    public ResponseList<Post> getFeed(String s, Reading reading) throws FacebookException {
        return _facebook.getFeed(s, reading);
    }

    public ResponseList<Post> getHome() throws FacebookException {
        return _facebook.getHome();
    }

    public ResponseList<Post> getHome(Reading reading) throws FacebookException {
        return _facebook.getHome(reading);
    }

    public ResponseList<Link> getLinks() throws FacebookException {
        return _facebook.getLinks();
    }

    public ResponseList<Link> getLinks(Reading reading) throws FacebookException {
        return _facebook.getLinks(reading);
    }

    public ResponseList<Link> getLinks(String s) throws FacebookException {
        return _facebook.getLinks(s);
    }

    public ResponseList<Link> getLinks(String s, Reading reading) throws FacebookException {
        return _facebook.getLinks(s, reading);
    }

    public ResponseList<Post> getPosts() throws FacebookException {
        return _facebook.getPosts();
    }

    public ResponseList<Post> getPosts(Reading reading) throws FacebookException {
        return _facebook.getPosts(reading);
    }

    public ResponseList<Post> getPosts(String s) throws FacebookException {
        return _facebook.getPosts(s);
    }

    public ResponseList<Post> getPosts(String s, Reading reading) throws FacebookException {
        return _facebook.getPosts(s, reading);
    }

    public ResponseList<Post> getStatuses() throws FacebookException {
        return _facebook.getStatuses();
    }

    public ResponseList<Post> getStatuses(Reading reading) throws FacebookException {
        return _facebook.getStatuses(reading);
    }

    public ResponseList<Post> getStatuses(String s) throws FacebookException {
        return _facebook.getStatuses(s);
    }

    public ResponseList<Post> getStatuses(String s, Reading reading) throws FacebookException {
        return _facebook.getStatuses(s, reading);
    }

    public ResponseList<Post> getTagged() throws FacebookException {
        return _facebook.getTagged();
    }

    public ResponseList<Post> getTagged(Reading reading) throws FacebookException {
        return _facebook.getTagged(reading);
    }

    public ResponseList<Post> getTagged(String s) throws FacebookException {
        return _facebook.getTagged(s);
    }

    public ResponseList<Post> getTagged(String s, Reading reading) throws FacebookException {
        return _facebook.getTagged(s, reading);
    }

    public Post getPost(String s) throws FacebookException {
        return _facebook.getPost(s);
    }

    public Post getPost(String s, Reading reading) throws FacebookException {
        return _facebook.getPost(s, reading);
    }

    public ResponseList<Comment> getPostComments(String s) throws FacebookException {
        return _facebook.getPostComments(s);
    }

    public ResponseList<Comment> getPostComments(String s, Reading reading) throws FacebookException {
        return _facebook.getPostComments(s, reading);
    }

    public String commentPost(String s, String s2) throws FacebookException {
        return _facebook.commentPost(s, s2);
    }

    public ResponseList<Like> getPostLikes(String s) throws FacebookException {
        return _facebook.getPostLikes(s);
    }

    public ResponseList<Like> getPostLikes(String s, Reading reading) throws FacebookException {
        return _facebook.getPostLikes(s, reading);
    }

    public boolean likePost(String s) throws FacebookException {
        return _facebook.likePost(s);
    }

    public boolean unlikePost(String s) throws FacebookException {
        return _facebook.unlikePost(s);
    }

    public String postFeed(PostUpdate postUpdate) throws FacebookException {
        return _facebook.postFeed(postUpdate);
    }

    public String postFeed(String s, PostUpdate postUpdate) throws FacebookException {
        return _facebook.postFeed(s, postUpdate);
    }

    public String postLink(URL url) throws FacebookException {
        return _facebook.postLink(url);
    }

    public String postLink(URL url, String s) throws FacebookException {
        return _facebook.postLink(url, s);
    }

    public String postLink(String s, URL url) throws FacebookException {
        return _facebook.postLink(s, url);
    }

    public String postLink(String s, URL url, String s2) throws FacebookException {
        return _facebook.postLink(s, url, s2);
    }

    public String postStatusMessage(String s) throws FacebookException {
        return _facebook.postStatusMessage(s);
    }

    public String postStatusMessage(String s, String s2) throws FacebookException {
        return _facebook.postStatusMessage(s, s2);
    }

    public boolean deletePost(String s) throws FacebookException {
        return _facebook.deletePost(s);
    }

    public ResponseList<Question> getQuestions() throws FacebookException {
        return _facebook.getQuestions();
    }

    public ResponseList<Question> getQuestions(Reading reading) throws FacebookException {
        return _facebook.getQuestions(reading);
    }

    public ResponseList<Question> getQuestions(String s) throws FacebookException {
        return _facebook.getQuestions(s);
    }

    public ResponseList<Question> getQuestions(String s, Reading reading) throws FacebookException {
        return _facebook.getQuestions(s, reading);
    }

    public String createQuestion(String s) throws FacebookException {
        return _facebook.createQuestion(s);
    }

    public String createQuestion(String s, List<String> strings, boolean b) throws FacebookException {
        return _facebook.createQuestion(s, strings, b);
    }

    public String createQuestion(String s, String s2) throws FacebookException {
        return _facebook.createQuestion(s, s2);
    }

    public String createQuestion(String s, String s2, List<String> strings, boolean b) throws FacebookException {
        return _facebook.createQuestion(s, s2, strings, b);
    }

    public Question getQuestion(String s) throws FacebookException {
        return _facebook.getQuestion(s);
    }

    public Question getQuestion(String s, Reading reading) throws FacebookException {
        return _facebook.getQuestion(s, reading);
    }

    public boolean deleteQuestion(String s) throws FacebookException {
        return _facebook.deleteQuestion(s);
    }

    public ResponseList<Question.Option> getQuestionOptions(String s) throws FacebookException {
        return _facebook.getQuestionOptions(s);
    }

    public ResponseList<Question.Option> getQuestionOptions(String s, Reading reading) throws FacebookException {
        return _facebook.getQuestionOptions(s, reading);
    }

    public String addQuestionOption(String s, String s2) throws FacebookException {
        return _facebook.addQuestionOption(s, s2);
    }

    public ResponseList<QuestionVotes> getQuestionOptionVotes(String s) throws FacebookException {
        return _facebook.getQuestionOptionVotes(s);
    }

    public ResponseList<Subscribedto> getSubscribedto() throws FacebookException {
        return _facebook.getSubscribedto();
    }

    public ResponseList<Subscribedto> getSubscribedto(Reading reading) throws FacebookException {
        return _facebook.getSubscribedto(reading);
    }

    public ResponseList<Subscribedto> getSubscribedto(String s) throws FacebookException {
        return _facebook.getSubscribedto(s);
    }

    public ResponseList<Subscribedto> getSubscribedto(String s, Reading reading) throws FacebookException {
        return _facebook.getSubscribedto(s, reading);
    }

    public ResponseList<Subscriber> getSubscribers() throws FacebookException {
        return _facebook.getSubscribers();
    }

    public ResponseList<Subscriber> getSubscribers(Reading reading) throws FacebookException {
        return _facebook.getSubscribers(reading);
    }

    public ResponseList<Subscriber> getSubscribers(String s) throws FacebookException {
        return _facebook.getSubscribers(s);
    }

    public ResponseList<Subscriber> getSubscribers(String s, Reading reading) throws FacebookException {
        return _facebook.getSubscribers(s, reading);
    }

    public ResponseList<Video> getVideos() throws FacebookException {
        return _facebook.getVideos();
    }

    public ResponseList<Video> getVideos(Reading reading) throws FacebookException {
        return _facebook.getVideos(reading);
    }

    public ResponseList<Video> getVideos(String s) throws FacebookException {
        return _facebook.getVideos(s);
    }

    public ResponseList<Video> getVideos(String s, Reading reading) throws FacebookException {
        return _facebook.getVideos(s, reading);
    }

    public String postVideo(Media media) throws FacebookException {
        return _facebook.postVideo(media);
    }

    public String postVideo(Media media, String s, String s2) throws FacebookException {
        return _facebook.postVideo(media, s, s2);
    }

    public String postVideo(String s, Media media) throws FacebookException {
        return _facebook.postVideo(s, media);
    }

    public String postVideo(String s, Media media, String s2, String s3) throws FacebookException {
        return _facebook.postVideo(s, media, s2, s3);
    }

    public Video getVideo(String s) throws FacebookException {
        return _facebook.getVideo(s);
    }

    public Video getVideo(String s, Reading reading) throws FacebookException {
        return _facebook.getVideo(s, reading);
    }

    public ResponseList<Like> getVideoLikes(String s) throws FacebookException {
        return _facebook.getVideoLikes(s);
    }

    public ResponseList<Like> getVideoLikes(String s, Reading reading) throws FacebookException {
        return _facebook.getVideoLikes(s, reading);
    }

    public boolean likeVideo(String s) throws FacebookException {
        return _facebook.likeVideo(s);
    }

    public boolean unlikeVideo(String s) throws FacebookException {
        return _facebook.unlikeVideo(s);
    }

    public ResponseList<Comment> getVideoComments(String s) throws FacebookException {
        return _facebook.getVideoComments(s);
    }

    public ResponseList<Comment> getVideoComments(String s, Reading reading) throws FacebookException {
        return _facebook.getVideoComments(s, reading);
    }

    public String commentVideo(String s, String s2) throws FacebookException {
        return _facebook.commentVideo(s, s2);
    }

    public URL getVideoCover(String s) throws FacebookException {
        return _facebook.getVideoCover(s);
    }

    public ResponseList<Insight> getInsights(String s, String s2) throws FacebookException {
        return _facebook.getInsights(s, s2);
    }

    public ResponseList<Insight> getInsights(String s, String s2, Reading reading) throws FacebookException {
        return _facebook.getInsights(s, s2, reading);
    }

    public ResponseList<Post> searchPosts(String s) throws FacebookException {
        return _facebook.searchPosts(s);
    }

    public ResponseList<Post> searchPosts(String s, Reading reading) throws FacebookException {
        return _facebook.searchPosts(s, reading);
    }

    public ResponseList<facebook4j.User> searchUsers(String s) throws FacebookException {
        return _facebook.searchUsers(s);
    }

    public ResponseList<facebook4j.User> searchUsers(String s, Reading reading) throws FacebookException {
        return _facebook.searchUsers(s, reading);
    }

    public ResponseList<Event> searchEvents(String s) throws FacebookException {
        return _facebook.searchEvents(s);
    }

    public ResponseList<Event> searchEvents(String s, Reading reading) throws FacebookException {
        return _facebook.searchEvents(s, reading);
    }

    public ResponseList<Group> searchGroups(String s) throws FacebookException {
        return _facebook.searchGroups(s);
    }

    public ResponseList<Group> searchGroups(String s, Reading reading) throws FacebookException {
        return _facebook.searchGroups(s, reading);
    }

    public ResponseList<Place> searchPlaces(String s) throws FacebookException {
        return _facebook.searchPlaces(s);
    }

    public ResponseList<Place> searchPlaces(String s, Reading reading) throws FacebookException {
        return _facebook.searchPlaces(s, reading);
    }

    public ResponseList<Place> searchPlaces(String s, GeoLocation geoLocation, int i) throws FacebookException {
        return _facebook.searchPlaces(s, geoLocation, i);
    }

    public ResponseList<Place> searchPlaces(String s,
                                            GeoLocation geoLocation,
                                            int i,
                                            Reading reading) throws FacebookException {
        return _facebook.searchPlaces(s, geoLocation, i, reading);
    }

    public ResponseList<Checkin> searchCheckins() throws FacebookException {
        return _facebook.searchCheckins();
    }

    public ResponseList<Checkin> searchCheckins(Reading reading) throws FacebookException {
        return _facebook.searchCheckins(reading);
    }

    public ResponseList<Location> searchLocations(GeoLocation geoLocation, int i) throws FacebookException {
        return _facebook.searchLocations(geoLocation, i);
    }

    public ResponseList<Location> searchLocations(GeoLocation geoLocation,
                                                  int i,
                                                  Reading reading) throws FacebookException {
        return _facebook.searchLocations(geoLocation, i, reading);
    }

    public ResponseList<Location> searchLocations(String s) throws FacebookException {
        return _facebook.searchLocations(s);
    }

    public ResponseList<Location> searchLocations(String s, Reading reading) throws FacebookException {
        return _facebook.searchLocations(s, reading);
    }

    public ResponseList<JSONObject> search(String s) throws FacebookException {
        return _facebook.search(s);
    }

    public ResponseList<JSONObject> search(String s, Reading reading) throws FacebookException {
        return _facebook.search(s, reading);
    }

    public TestUser createTestUser(String s) throws FacebookException {
        return _facebook.createTestUser(s);
    }

    public TestUser createTestUser(String s, String s2, String s3, String s4) throws FacebookException {
        return _facebook.createTestUser(s, s2, s3, s4);
    }

    public List<TestUser> getTestUsers(String s) throws FacebookException {
        return _facebook.getTestUsers(s);
    }

    public boolean deleteTestUser(String s) throws FacebookException {
        return _facebook.deleteTestUser(s);
    }

    public boolean makeFriendTestUser(TestUser testUser, TestUser testUser2) throws FacebookException {
        return _facebook.makeFriendTestUser(testUser, testUser2);
    }

    public JSONArray executeFQL(String s) throws FacebookException {
        return _facebook.executeFQL(s);
    }

    public Map<String, JSONArray> executeMultiFQL(Map<String, String> stringStringMap) throws FacebookException {
        return _facebook.executeMultiFQL(stringStringMap);
    }
}
