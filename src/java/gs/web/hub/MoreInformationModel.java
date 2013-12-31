package gs.web.hub;

import gs.web.util.list.Anchor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 9/30/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoreInformationModel {

    public class InfoLinkSource {
        private String _contact;
        private Anchor _link;

        public String getContact() {
            return _contact;
        }

        public void setContact(final String contact) {
            this._contact = contact;
        }

        public Anchor getLink() {
            return _link;
        }

        public void setLink(final Anchor link) {
            this._link = link;
        }
    }

    public List<InfoLinkSource> getInfoLinkSources() {
        return _infoLinkSources;
    }

    public void setInfoLinkSources(List<InfoLinkSource> infoLinkSources) {
        _infoLinkSources = infoLinkSources;
    }

    private List<InfoLinkSource> _infoLinkSources = new ArrayList<InfoLinkSource>();

    public MoreInformationModel(final ArrayList<InfoLinkSource> infoLinkSources){
         _infoLinkSources = infoLinkSources;
    }

    public MoreInformationModel() {}
}
