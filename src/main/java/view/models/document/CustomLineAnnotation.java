/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package view.models.document;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.annotations.CategoryLineAnnotation;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;

/**
 * A line annotation drawing class for Gantt charts. Used to show dependencies
 * among tasks using the annotation framework. Pass the end value of the first
 * category and the start value of the second category as the connection points
 * in the constructor. This is just like <code>CategoryLineAnnotation</code>
 * except its drawing method draws rectilinear line segments rather than
 * diagonal lines. Use the <code>minimumLength</code> field to adjust the
 * minimum length of a line segment emerging from a connection point.
 *
 * @since Java 1.5 collections
 */
public class CustomLineAnnotation extends CategoryLineAnnotation {

    /**
     * generated serial version id
     */
    private static final long serialVersionUID = -2928265293236347594L;
    /**
     * minimum line segment length at a category connection
     */
    private static boolean straightLine = true;

    private double minimumLength = 10.0;

    /**
     * Sets the minimum segment length value.
     *
     * @param length
     */
    public void setMinimumLength(double length) {
        minimumLength = length;
    }

    /**
     * Gets the minimum segment length value.
     *
     * @return
     */
    public double getMinimumLength() {
        return minimumLength;
    }

    /**
     * Constructor merely calls the super class.
     *
     * @param category1 first category to connect
     * @param value1 end point of first category
     * @param category2 second category to connect
     * @param value2 start point of first category
     * @param paint paint object, same as super class
     * @param stroke stroke object, same as super class
     */
    public CustomLineAnnotation(@SuppressWarnings("rawtypes") Comparable category1, double value1,
            @SuppressWarnings("rawtypes") Comparable category2, double value2, Paint paint, Stroke stroke) {
        super(category1, value1, category2, value2, paint, stroke);
    }

    /**
     * Draws the annotation.
     *
     * @param g2 the graphics device.
     * @param plot the plot.
     * @param dataArea the data area.
     * @param domainAxis the domain axis.
     * @param rangeAxis the range axis.
     */
    @Override
    public void draw(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea,
            CategoryAxis domainAxis, ValueAxis rangeAxis) {

        CategoryDataset dataset = plot.getDataset();
        int catIndex1 = dataset.getColumnIndex(this.getCategory1());
        int catIndex2 = dataset.getColumnIndex(this.getCategory2());
        int catCount = dataset.getColumnCount();

        double pointX1;
        double pointY1;
        double pointX2;
        double pointY2;
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot
                .getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot
                .getRangeAxisLocation(), orientation);

        // create a list of lines to draw
        List<Line2D> lines = new ArrayList<>();

        if (orientation == PlotOrientation.HORIZONTAL) {
            pointY1 = domainAxis.getCategoryJava2DCoordinate(
                    CategoryAnchor.MIDDLE, catIndex1, catCount, dataArea,
                    domainEdge);
            pointX1 = rangeAxis.valueToJava2D(this.getValue1(), dataArea,
                    rangeEdge);
            pointY2 = domainAxis.getCategoryJava2DCoordinate(
                    CategoryAnchor.MIDDLE, catIndex2, catCount, dataArea,
                    domainEdge);
            pointX2 = rangeAxis.valueToJava2D(this.getValue2(), dataArea,
                    rangeEdge);

            // work out the line segments
            if (straightLine) {
                lines.add(new Line2D.Double(pointX1, pointY1, pointX2, pointY2));
            } else {
                if (Math.abs(pointX2 - pointX1) < (minimumLength / 2.0)) {
                    double halfway = pointY1 + (pointY2 - pointY1) / 2.0;
                    lines.add(new Line2D.Double(pointX1, pointY1, pointX1 + minimumLength, pointY1));
                    lines.add(new Line2D.Double(pointX1 + minimumLength, pointY1, pointX1 + minimumLength, halfway));
                    lines.add(new Line2D.Double(pointX1 + minimumLength, halfway, pointX2 - minimumLength, halfway));
                    lines.add(new Line2D.Double(pointX2 - minimumLength, halfway, pointX2 - minimumLength, pointY2));
                    lines.add(new Line2D.Double(pointX2 - minimumLength, pointY2, pointX2, pointY2));
                } else {
                    double halfway = pointX1 + (pointX2 - pointX1) / 2.0;
                    lines.add(new Line2D.Double(pointX1, pointY1, halfway, pointY1));
                    lines.add(new Line2D.Double(halfway, pointY1, halfway, pointY2));
                    lines.add(new Line2D.Double(halfway, pointY2, pointX2, pointY2));
                }
            }

        } else if (orientation == PlotOrientation.VERTICAL) {
            pointX1 = domainAxis.getCategoryJava2DCoordinate(
                    CategoryAnchor.MIDDLE, catIndex1, catCount, dataArea,
                    domainEdge);
            pointY1 = rangeAxis.valueToJava2D(this.getValue1(), dataArea,
                    rangeEdge);
            pointX2 = domainAxis.getCategoryJava2DCoordinate(
                    CategoryAnchor.MIDDLE, catIndex2, catCount, dataArea,
                    domainEdge);
            pointY2 = rangeAxis.valueToJava2D(this.getValue2(), dataArea,
                    rangeEdge);

            // work out the line segments
            if (Math.abs(pointX2 - pointX1) < (minimumLength * 2.0)) {
                // wrap-around case, add extra line segments
                double halfway = pointX1 + (pointX2 - pointX1) / 2.0;
                lines.add(new Line2D.Double(pointX1, pointY1, pointX1, pointY1 + minimumLength));
                lines.add(new Line2D.Double(pointX1, pointY1 + minimumLength, halfway, pointY1 + minimumLength));
                lines.add(new Line2D.Double(halfway, pointY1 + minimumLength, halfway, pointY2 - minimumLength));
                lines.add(new Line2D.Double(halfway, pointY2 - minimumLength, pointX2, pointY2 - minimumLength));
                lines.add(new Line2D.Double(pointX2, pointY2 - minimumLength, pointX2, pointY2));
            } else {
                // no wrap-around, use half the distance
                // could be fancier to avoid other shapes on the chart
                double halfway = pointY1 + (pointY2 - pointY1) / 2.0;
                lines.add(new Line2D.Double(pointX1, pointY1, pointX1, halfway));
                lines.add(new Line2D.Double(pointX1, halfway, pointX2, halfway));
                lines.add(new Line2D.Double(pointX2, halfway, pointX2, pointY2));
            }
        }

        g2.setPaint(this.getPaint());
        g2.setStroke(this.getStroke());

        // draw all the lines
        for (Line2D line : lines) {
            g2.drawLine((int) line.getP1().getX(), (int) line.getP1().getY(), (int) line.getP2().getX(), (int) line.getP2().getY());
        }
    }

    public static boolean isStraightLine() {
        return straightLine;
    }

    public static void setStraightLine(boolean straightLine) {
        CustomLineAnnotation.straightLine = straightLine;
    }
}
