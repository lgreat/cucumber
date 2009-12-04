package gs.web;

import junit.framework.TestCase;

/**
 * author thuss
 */
public interface IntegrationTestCase {
    /** Use localhost on the greatschools domain so cookies will work */
    static final String INTEGRATION_HOST = "http://localhost.greatschools.org:9000";
}
