package gs.web.hub;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 9/6/13
 * Time: 11:26 AM
 * To change this template use File | Settings | File Templates.
 */

import java.util.ArrayList;

/**
 * Model for Steps on  City Hub Choose  Pages.
 * @author sarora@greatschools.org Shomi Arora.
 */
public class StepModel {



    private Integer _stepNo;

    private String  _stepHeading;

    private String  _imageURL;

    private String   _stepDescription;


    ArrayList<FeaturedResourcesModel> _featuredResourcesModel;

    ArrayList<AdditionalResourcesModel> _additionalResourcesModel;


    public StepModel(final Integer stepNo , final String  stepHeading, final String  imageURL, final String  stepDescription) {
        _stepNo = stepNo;
        _stepHeading = stepHeading;
        _imageURL= imageURL;
        _stepDescription= stepDescription;
    }


    public Integer getStepNo() {
        return _stepNo;
    }

    public void setStepNo(final Integer stepNo) {
        this._stepNo = stepNo;
    }

    public String getStepHeading() {
        return _stepHeading;
    }

    public void setStepHeading(final String stepHeading) {
        this._stepHeading = stepHeading;
    }

    public String getImageURL() {
        return _imageURL;
    }

    public void setImageURL(final String imageURL) {
        this._imageURL = imageURL;
    }

    public String getStepDescription() {
        return _stepDescription;
    }

    public void setStepDescription(final String stepDescription) {
        this._stepDescription = stepDescription;
    }



    public ArrayList<AdditionalResourcesModel>  getAdditionalResourcesModel() {
        return _additionalResourcesModel;
    }

    public void setAdditionalResourcesModel(final ArrayList<AdditionalResourcesModel>  additionalResourcesModel) {
        this._additionalResourcesModel = additionalResourcesModel;
    }

    public ArrayList<FeaturedResourcesModel>  getFeaturedResourcesModel() {
        return _featuredResourcesModel;
    }

    public void setFeaturedResourcesModel(final ArrayList<FeaturedResourcesModel> featuredResourcesModel) {
        this._featuredResourcesModel = featuredResourcesModel;
    }
}
