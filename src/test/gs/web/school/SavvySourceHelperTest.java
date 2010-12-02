package gs.web.school;

import gs.data.geo.City;
import gs.data.state.State;
import junit.framework.TestCase;

public class SavvySourceHelperTest extends TestCase {

    public void testGetSavvyCityPageUrl() {
        assertEquals("http://www.savvysource.com/preschools/c_preschools_in_san_diego_ca", SavvySourceHelper.getSavvyCityPageUrl(State.CA, "San Diego"));
        City city = new City();
        city.setName("San Diego");
        city.setState(State.CA);
        assertEquals("http://www.savvysource.com/preschools/c_preschools_in_san_diego_ca", SavvySourceHelper.getSavvyCityPageUrl(city));
        assertEquals("http://www.savvysource.com/preschools/c_preschools_in_new_york_ny", SavvySourceHelper.getSavvyCityPageUrl(State.NY, "New York"));
        assertEquals("http://www.savvysource.com/preschools/c_preschools_in_washington_dc", SavvySourceHelper.getSavvyCityPageUrl(State.DC, "Washington"));
        // http://www.greatschools.org/idaho/coeur-d'alene/
        assertEquals("http://www.savvysource.com/preschools/c_preschools_in_coeur_d_alene_id", SavvySourceHelper.getSavvyCityPageUrl(State.ID, "Coeur d'Alene"));
        assertEquals("http://www.savvysource.com/preschools/c_preschools_in_winston_salem_nc", SavvySourceHelper.getSavvyCityPageUrl(State.NC, "Winston-Salem"));
    }

    public void testGetSavvyStatePageUrl() {
        assertEquals("http://www.savvysource.com/preschools/s_preschools_in_california_ca", SavvySourceHelper.getSavvyStatePageUrl(State.CA));
        assertEquals("http://www.savvysource.com/preschools/s_preschools_in_new_york_ny", SavvySourceHelper.getSavvyStatePageUrl(State.NY));
        assertEquals("http://www.savvysource.com/preschools/s_preschools_in_washington_dc", SavvySourceHelper.getSavvyStatePageUrl(State.DC));
    }
}
