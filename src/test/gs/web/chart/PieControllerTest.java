package gs.web.chart;

import gs.web.BaseControllerTestCase;
import org.jfree.data.general.PieDataset;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class PieControllerTest extends BaseControllerTestCase {
    PieController _controller;

    public void setUp() throws Exception {
        _controller = new PieController();
        super.setUp();
    }

    public void testHandleRequest() throws Exception {
        getRequest().addParameter("p", "34_33_32_>1_>1_>1_>1");
        _controller.handleRequest(getRequest(), getResponse());

        assertEquals(getResponse().getContentType(), "image/png");
        _log.warn(new Integer(getResponse().getContentLength()));
    }


    public void testCreateDataSet() {
        PieDataset dataset = _controller.creatDataset("34_33_32");
        assertEquals(3, dataset.getItemCount());
        assertEquals(Double.valueOf("34"), dataset.getValue(new Integer(0)));
        assertEquals(Double.valueOf("33"), dataset.getValue(new Integer(1)));
        assertEquals(Double.valueOf("32"), dataset.getValue(new Integer(2)));

        dataset = _controller.creatDataset("90_<10_<10");
        assertEquals(3, dataset.getItemCount());
        assertEquals(Double.valueOf("90"), dataset.getValue(new Integer(0)));
        assertEquals(Double.valueOf("9.9"), dataset.getValue(new Integer(1)));
        assertEquals(Double.valueOf("9.9"), dataset.getValue(new Integer(2)));        

        dataset = _controller.creatDataset("90_<10_<_");
        assertEquals(2, dataset.getItemCount());
        assertEquals(Double.valueOf("90"), dataset.getValue(new Integer(0)));
        assertEquals(Double.valueOf("9.9"), dataset.getValue(new Integer(1)));

        dataset = _controller.creatDataset("90_");
        assertEquals(1, dataset.getItemCount());
        assertEquals(Double.valueOf("90"), dataset.getValue(new Integer(0)));

        dataset = _controller.creatDataset(" ");
        assertEquals(0, dataset.getItemCount());

        dataset = _controller.creatDataset(">90_-1");
        assertEquals(1, dataset.getItemCount());
        assertEquals(Double.valueOf("90.1"), dataset.getValue(new Integer(0)));                
    }

    public void testConvert() {
        //run of the mill numbers
        assertEquals("negative returns null", null, _controller.convert("-1"));
        assertEquals("negative returns null", null, _controller.convert("-0.5"));
        assertEquals(Double.valueOf("0"), _controller.convert("0.00"));
        assertEquals(Double.valueOf("0.5"), _controller.convert("0.5"));
        assertEquals(Double.valueOf("1"), _controller.convert("1"));

        //numbers with spaces
        assertEquals("negative returns null", null, _controller.convert(" -1 "));
        assertEquals("negative returns null", null, _controller.convert(" -0.5"));
        assertEquals(Double.valueOf("0"), _controller.convert("0.00 "));
        assertEquals(Double.valueOf("0.5"), _controller.convert(" 0.5 "));
        assertEquals(Double.valueOf("1"), _controller.convert(" 1 "));

        //greater than numbers
        assertEquals(Double.valueOf("1.1"), _controller.convert(">1"));
        assertEquals(Double.valueOf("1.6"), _controller.convert(">1.5"));
        assertEquals(Double.valueOf("0.1"), _controller.convert(" >0"));
        assertEquals(Double.valueOf("1.1"), _controller.convert("> 1.0"));
        assertEquals(Double.valueOf("1.1"), _controller.convert(" > 1.0"));
        assertEquals(null, _controller.convert(" > -0.5"));
        assertEquals(Double.valueOf("0"), _controller.convert(" > -0.1"));

        //less than numbers
        assertEquals(Double.valueOf("0.9"), _controller.convert("<1"));
        assertEquals(Double.valueOf("1.4"), _controller.convert("<1.5"));
        assertEquals(Double.valueOf("0.9"), _controller.convert("< 1.0"));
        assertEquals(Double.valueOf("0.9"), _controller.convert(" < 1.0 "));
        assertEquals("<0 is same as negative", null, _controller.convert("<0"));

        //bogus numbers
        assertEquals(null, _controller.convert("hi"));
        assertEquals(null, _controller.convert(">hi"));
        assertEquals(null, _controller.convert("<3 hi"));
        assertEquals(null, _controller.convert("3<"));
    }
}
