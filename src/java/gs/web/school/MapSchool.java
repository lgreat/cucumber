package gs.web.school;

import gs.data.school.NearbySchool;
import gs.data.school.review.Ratings;

/**
     * Extends NearbySchool as a convenience (but it really buys me nothing). The only used fields are:
     * neighbor (from super, for the school),
     * rating (from super, for the gs rating),
     * and parentRating (defined here, for the parent rating)
     */
public class MapSchool extends NearbySchool {
    private Ratings _parentRatings;

    public Ratings getParentRatings() {
        return _parentRatings;
    }

    public void setParentRatings(Ratings parentRatings) {
        _parentRatings = parentRatings;
    }
}