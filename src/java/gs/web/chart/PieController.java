package gs.web.chart;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class PieController implements Controller {
    public static final String BEAN_ID = "/chart/pie.page";
    public static final Log _log = LogFactory.getLog(PieController.class);

    private static final Color [] PIE_COLORS = {
            Color.decode("#55bbcc"),
            Color.decode("#99cc66"),
            Color.decode("#cc5566"),
            Color.decode("#ffbb00"),
            Color.decode("#ddeeee"),
            Color.decode("#639c31"),
            Color.decode("#bfbfbf"),
            Color.decode("#dd9999"),
    };

    public static final int MAX_SLICES = PIE_COLORS.length;
    public static final String PARAM_DELIMITED_PERCENTAGES = "p";

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String delimPercents = request.getParameter(PARAM_DELIMITED_PERCENTAGES);

        if (StringUtils.isBlank(delimPercents)) {
            delimPercents = "";
        }

        final JFreeChart chart = ChartFactory.createPieChart(
                null,
                creatDataset(delimPercents),
                false,
                false,
                false
        );               

        final PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(null);
        plot.setCircular(true);
        plot.setInteriorGap(0);
        plot.setShadowXOffset(0);
        plot.setShadowYOffset(0);

        plot.setShadowPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);

        for (int i=0; i < MAX_SLICES; i++) {
            plot.setSectionPaint(new Integer(i), PIE_COLORS[i]);
        }

        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream(1024*7);
        ChartUtilities.writeChartAsPNG(outBuffer, chart, 160, 160, true, 9);

        byte[] buf = outBuffer.toByteArray();
        response.setContentType("image/png");
        response.setContentLength(buf.length);

        OutputStream out = response.getOutputStream();
	    out.write(buf, 0, buf.length);

	    outBuffer.close();
	    outBuffer = null;
	    buf = null;
        out.close();

        return null;
    }

    protected PieDataset creatDataset(final String delimPcts) {
        String percents [] = delimPcts.split("_");
        final DefaultPieDataset dataset = new DefaultPieDataset();

        int length = percents.length;

        if (length > MAX_SLICES) {
            length = MAX_SLICES;
        }

        for (int i=0; i < length; i++) {
            Double pct = convert(percents[i]);

            if (null == pct) {
                continue;
            }

            dataset.setValue(new Integer(i), pct);
        }
        return dataset;
    }

    /**
     * Return a Double from a string or null if input is invalid.
     * Negative input will return null.  0 is ok.
     *
     * Trims trailing and leading spaces.
     *
     * Adds or subtracts 0.1 '<' or '>' values.
     *
     * Ex:   convert("0.5") = Double.valueOf("0.5")
     *       convert("> 1") = Double.valueOf("1.1")
     *       convert(">1.1") = Double.valueOf("1.2")
     *       convert("< 1") = Double.valueOf("0.9")
     *       convert("< 2") = Double.valueOf("-1")
     *       convert("-1.1") = null
     *       convert("Hi") = null
     *
     * @param pctAsString string to convert to double
     * @return null when a double cannot be determined or a double value otherwise
     */
    protected Double convert(final String pctAsString) {
        String percentage = pctAsString.trim();

        double offset = 0;
        String replace = "";
        if (percentage.startsWith("<")) {
            offset = -0.1;
            replace = "<";
        } else if (percentage.startsWith(">")) {
            offset = 0.1;
            replace = ">";
        }

        Double retVal = null;
        try {
            if (offset == 0) {
                retVal = new Double(percentage);
            } else {
                percentage = percentage.replaceFirst(replace, "").trim();
                retVal = new Double (Double.valueOf(percentage).doubleValue() + offset);
            }

            if (retVal.doubleValue() < 0) {
                _log.warn("negative number: " + pctAsString);
                retVal = null;
            }

        } catch(NumberFormatException e) {
            _log.warn("bad input: " + pctAsString);
        }

        return retVal;
    }
}
