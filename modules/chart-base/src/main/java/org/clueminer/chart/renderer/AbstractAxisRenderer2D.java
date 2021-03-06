/*
 * Copyright (C) 2011-2017 clueminer.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.clueminer.chart.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.clueminer.chart.api.Axis;
import org.clueminer.chart.api.AxisRenderer;
import org.clueminer.chart.api.Tick;
import org.clueminer.chart.api.TickType;
import org.clueminer.chart.util.GeometryUtils;
import org.clueminer.chart.util.MathUtils;
import org.clueminer.chart.util.PointND;
import org.clueminer.chart.util.SerializationUtils;

/**
 *
 * @author deric
 */
public abstract class AbstractAxisRenderer2D implements AxisRenderer, Serializable {

    /**
     * Version id for serialization.
     */
    private static final long serialVersionUID = 5623525683845512624L;
    /**
     * Line segments approximating the shape of the axis.
     */
    private Line2D[] shapeLines;
    /**
     * Normals of the line segments approximating the axis.
     */
    private Point2D[] shapeLineNormals;
    /**
     * Lengths of the line segments approximating the axis.
     */
    private double[] shapeSegmentLengths;
    /**
     * Length of the the axis up to a certain approximating line segment.
     */
    private double[] shapeLengths;

    /**
     * Intersection point of the axis.
     */
    private Number intersection;
    /**
     * Shape used for drawing.
     */
    private Shape shape;
    /**
     * Decides whether the shape is drawn.
     */
    private boolean shapeVisible;
    /**
     * Decides whether the shape normals are orientated clockwise.
     */
    private boolean shapeNormalOrientationClockwise;
    /**
     * Paint used to draw axis shape, ticks, and labels.
     */
    private Paint shapeColor;
    /**
     * Stroke used for drawing the axis shape.
     */
    // Property will be serialized using a wrapper
    private transient Stroke shapeStroke;
    /**
     * Decides whether the axis direction will be changed.
     */
    private boolean shapeDirectionSwapped;

    /**
     * Decides whether major ticks are drawn.
     */
    private boolean ticksVisible;
    /**
     * Distance on axis in which major ticks are drawn.
     */
    private Number tickSpacing;
    /**
     * Decides whether automatic tick spacing is enabled.
     */
    private boolean ticksAutoSpaced;
    /**
     * Tick length relative to the font
     */
    private double tickLength;
    /**
     * Stroke which is used to draw all major ticks.
     */
    // Property will be serialized using a wrapper
    private transient Stroke tickStroke;
    /**
     * Alignment of major ticks relative to the axis.
     */
    private double tickAlignment;
    /**
     * Font used to display the text of major ticks.
     */
    private Font tickFont;
    /**
     * Paint used to draw the shapes of major ticks.
     */
    private Paint tickColor;
    /**
     * Decides whether tick labels will be shown.
     */
    private boolean tickLabelsVisible;
    /**
     * Format which converts the tick values to labels.
     */
    private Format tickLabelFormat;
    /**
     * Distance between labels and ticks relative to the font height.
     */
    private double tickLabelDistance;
    /**
     * Decides whether the tick labels are drawn outside of the plot.
     */
    private boolean tickLabelsOutside;
    /**
     * Tick label rotation in degrees.
     */
    private double tickLabelRotation;

    /**
     * Decides whether minor ticks are drawn.
     */
    private boolean minorTickVisible;
    /**
     * Number of minor ticks between two major ticks.
     */
    private int minorTicksCount;
    /**
     * Tick length relative to font height.
     */
    private double minorTickLength;
    /**
     * Stroke used to draw all minor ticks.
     */
    // Property will be serialized using a wrapper
    private transient Stroke minorTickStroke;
    /**
     * Minor tick alignment relative to the axis.
     */
    private double minorTickAlignment;
    /**
     * Paint used to draw the shapes of minor ticks.
     */
    private Paint minorTickColor;

    /**
     * Custom labels containing their respective position and text.
     */
    private final Map<Double, String> customTicks;
    /**
     * Label text of the axis.
     */
    private String label;
    /**
     * Distance relative to font height.
     */
    private double labelDistance;
    /**
     * Axis label rotation in degrees.
     */
    private double labelRotation;
    /**
     * Font for axis label text.
     */
    private Font labelFont;
    /**
     * Paint used to draw the axis label.
     */
    private Paint labelColor;

    /**
     * Initializes a new instance with default settings.
     */
    public AbstractAxisRenderer2D() {
        intersection = 0.0;
        // The direction must defined as swapped before the shape is evaluated.
        shapeDirectionSwapped = false;
        shape = new Line2D.Double(0.0, 0.0, 1.0, 0.0);
        evaluateShape(shape);

        shapeVisible = true;
        shapeNormalOrientationClockwise = false;
        shapeStroke = new BasicStroke();
        shapeColor = Color.BLACK;

        ticksVisible = true;
        tickSpacing = 0.0;
        ticksAutoSpaced = false;
        tickLength = 1.0;
        tickStroke = new BasicStroke();
        tickAlignment = 0.5;
        tickFont = Font.decode(null);
        tickColor = Color.BLACK;

        tickLabelsVisible = true;
        tickLabelFormat = NumberFormat.getInstance();
        tickLabelDistance = 1.0;
        tickLabelsOutside = true;
        tickLabelRotation = 0.0;

        customTicks = new HashMap<>();

        minorTickVisible = true;
        minorTicksCount = 1;
        minorTickLength = 0.5;
        minorTickStroke = new BasicStroke();
        minorTickAlignment = 0.5;
        minorTickColor = Color.BLACK;

        label = null;
        labelDistance = 1.0;
        labelRotation = 0.0;
        labelFont = Font.decode(null);
        labelColor = Color.BLACK;
    }

    /**
     * Returns a list of all tick element on the axis.
     *
     * @param axis Axis
     * @return A list of {@code Tick} instances
     */
    @Override
    public List<Tick> getTicks(Axis axis) {
        List<Tick> ticks = new LinkedList<>();

        if (!axis.isValid()) {
            return ticks;
        }

        double min = axis.getMin().doubleValue();
        double max = axis.getMax().doubleValue();

        Set<Double> tickPositions = new HashSet<>();

        createTicksCustom(ticks, axis, min, max, tickPositions);

        boolean isAutoSpacing = isTicksAutoSpaced();
        // If the spacing is invalid, use auto spacing
        if (!isAutoSpacing) {
            Number tickSpacing = getTickSpacing();
            if (tickSpacing == null) {
                isAutoSpacing = true;
            } else {
                double tickSpacingValue = tickSpacing.doubleValue();
                if (tickSpacingValue <= 0.0 || !MathUtils.isCalculatable(tickSpacingValue)) {
                    isAutoSpacing = true;
                }
            }
        }

        createTicks(ticks, axis, min, max, tickPositions, isAutoSpacing);

        return ticks;
    }

    /**
     * Returns the absolute length of a major tick.
     *
     * @return Major tick length in pixels.
     */
    @Override
    public double getTickLengthAbsolute() {
        double fontSize = getTickFont().getSize2D();
        return getTickLength() * fontSize;
    }

    /**
     * Returns the absolute length of a minor tick.
     *
     * @return Minor tick length in pixels.
     */
    public double getTickMinorLengthAbsolute() {
        double fontSize = getTickFont().getSize2D();
        return getMinorTickLength() * fontSize;
    }

    /**
     * Returns the absolute distance between ticks and labels.
     *
     * @return Distance in pixels.
     */
    public double getTickLabelDistanceAbsolute() {
        double fontSize = getTickFont().getSize2D();
        return getTickLabelDistance() * fontSize;
    }

    /**
     * Adds minor and major ticks to a list of ticks.
     *
     * @param ticks List of ticks
     * @param axis Axis
     * @param min Minimum value of axis
     * @param max Maximum value of axis
     * @param tickPositions Set of tick positions
     * @param isAutoSpacing Use automatic scaling
     */
    public abstract void createTicks(List<Tick> ticks, Axis axis,
            double min, double max, Set<Double> tickPositions,
            boolean isAutoSpacing);

    /**
     * Adds custom ticks to a list of ticks.
     *
     * @param ticks List of ticks
     * @param axis Axis
     * @param min Minimum value of axis
     * @param max Maximum value of axis
     * @param tickPositions Set of tick positions
     */
    protected void createTicksCustom(List<Tick> ticks, Axis axis,
            double min, double max, Set<Double> tickPositions) {
        Map<? extends Number, String> labelsCustom = getCustomTicks();
        if (labelsCustom != null) {
            for (Number tickPositionWorldObj : labelsCustom.keySet()) {
                double tickPositionWorld = tickPositionWorldObj.doubleValue();
                if (tickPositionWorld < min || tickPositionWorld > max) {
                    continue;
                }
                Tick tick = getTick(
                        TickType.CUSTOM, axis, tickPositionWorld);
                ticks.add(tick);
                tickPositions.add(tickPositionWorld);
            }
        }
    }

    /**
     * Returns the point of the tick mark (in pixel coordinates) on the
     * specified axis with the specified value.
     *
     * @param type Type of tick mark.
     * @param axis Axis containing the tick mark.
     * @param tickPositionWorld Displayed value on the axis.
     * @return Object describing the desired tick mark.
     */
    protected Tick getTick(TickType type, Axis axis, double tickPositionWorld) {
        // Calculate position of tick on axis shape
        PointND<Double> tickPoint = getPosition(axis, tickPositionWorld, false, false);

        // Calculate tick normal
        PointND<Double> tickNormal = getNormal(axis, tickPositionWorld, false, false);

        // Retrieve tick label
        String tickLabel;
        Map<Double, String> labelsCustom = getCustomTicks();
        if (labelsCustom != null && labelsCustom.containsKey(tickPositionWorld)) {
            tickLabel = labelsCustom.get(tickPositionWorld);
        } else {
            Format labelFormat = getTickLabelFormat();
            if (labelFormat != null) {
                tickLabel = labelFormat.format(tickPositionWorld);
            } else {
                tickLabel = String.valueOf(tickPositionWorld);
            }
        }

        Tick tick = new Tick(type, tickPoint, tickNormal, null, null, tickLabel);
        return tick;
    }

    /**
     * Returns the normal vector at the position of the specified value.
     * The vector is normalized.
     *
     * @param axis Axis
     * @param value World coordinate value to convert
     * @param extrapolate Option to activate extrapolation value that are not
     * on the axis
     * @param forceLinear Force linear interpolation.
     * @return N-dimensional normal vector at the position
     */
    @Override
    public PointND<Double> getNormal(Axis axis, Number value,
            boolean extrapolate, boolean forceLinear) {
        double valueView;
        if (forceLinear) {
            valueView = (value.doubleValue() - axis.getMin().doubleValue())
                    / axis.getRange() * getShapeLength();
        } else {
            valueView = worldToView(axis, value, extrapolate);
        }

        int segmentIndex = MathUtils.binarySearchFloor(shapeLengths, valueView);
        if (segmentIndex < 0 || segmentIndex >= shapeLines.length) {
            return null;
        }

        segmentIndex = MathUtils.limit(
                segmentIndex, 0, shapeLineNormals.length - 1);
        boolean normalOrientationClockwise = AbstractAxisRenderer2D.this
                .isShapeNormalOrientationClockwise();
        double normalOrientation
                = normalOrientationClockwise ? 1.0 : -1.0;
        PointND<Double> tickNormal = new PointND<Double>(
                normalOrientation * shapeLineNormals[segmentIndex].getX(),
                normalOrientation * shapeLineNormals[segmentIndex].getY()
        );

        return tickNormal;
    }

    /**
     * Returns the length of the shape path which is used to render axes.
     *
     * @return Shape length.
     */
    protected double getShapeLength() {
        if (shapeLengths == null || shapeLengths.length == 0) {
            return 0.0;
        }
        return shapeLengths[shapeLengths.length - 1];
    }

    /**
     * Returns the position of the specified value on the axis.
     * The value is returned in view coordinates.
     *
     * @param axis Axis
     * @param value World coordinate value to convert
     * @param extrapolate Option to activate extrapolation value that are not
     * on the axis
     * @param forceLinear Force linear interpolation.
     * @return N-dimensional point of the value
     */
    public PointND<Double> getPosition(Axis axis, Number value,
            boolean extrapolate, boolean forceLinear) {
        if (shapeLines == null || shapeLines.length == 0 || value == null) {
            return null;
        }

        double valueView;
        if (forceLinear) {
            valueView = (value.doubleValue() - axis.getMin().doubleValue())
                    / axis.getRange() * getShapeLength();
        } else {
            valueView = worldToView(axis, value, extrapolate);
        }

        if (Double.isNaN(valueView)) {
            return null;
        }

        // TODO Check if this is a valid way to allow infinite values
        if (valueView == Double.NEGATIVE_INFINITY) {
            valueView = 0.0;
        } else if (valueView == Double.POSITIVE_INFINITY) {
            valueView = getShapeLength();
        }

        if (valueView <= 0.0 || valueView >= getShapeLength()) {
            if (extrapolate) {
                // do linear extrapolation if point lies outside of shape
                int segmentIndex = (valueView <= 0.0) ? 0 : shapeLines.length - 1;
                Line2D segment = shapeLines[segmentIndex];
                double segmentLen = shapeSegmentLengths[segmentIndex];
                double shapeLen = shapeLengths[segmentIndex];
                double relLen = (valueView - shapeLen) / segmentLen;
                return new PointND<>(
                        segment.getX1() + (segment.getX2() - segment.getX1()) * relLen,
                        segment.getY1() + (segment.getY2() - segment.getY1()) * relLen
                );
            } else {
                if (valueView <= 0.0) {
                    Point2D p2d = shapeLines[0].getP1();
                    return new PointND<>(p2d.getX(), p2d.getY());
                } else {
                    Point2D p2d = shapeLines[shapeLines.length - 1].getP2();
                    return new PointND<>(p2d.getX(), p2d.getY());
                }
            }
        }

        // Determine to which segment the value belongs using a binary search
        int i = MathUtils.binarySearchFloor(shapeLengths, valueView);

        if (i < 0 || i >= shapeLines.length) {
            return null;
        }
        Line2D line = shapeLines[i];

        double posRel = (valueView - shapeLengths[i]) / shapeSegmentLengths[i];
        PointND<Double> pos = new PointND<>(
                line.getX1() + (line.getX2() - line.getX1()) * posRel,
                line.getY1() + (line.getY2() - line.getY1()) * posRel
        );
        return pos;
    }

    /**
     * Calculates important aspects of the specified shape.
     *
     * @param shape Shape to be evaluated.
     */
    protected final void evaluateShape(Shape shape) {
        boolean directionSwapped = isShapeDirectionSwapped();
        shapeLines = GeometryUtils.shapeToLines(shape, directionSwapped);
        shapeSegmentLengths = new double[shapeLines.length];
        // First length is always 0.0, last length is the total length
        shapeLengths = new double[shapeLines.length + 1];
        shapeLineNormals = new Point2D[shapeLines.length];

        if (shapeLines.length == 0) {
            return;
        }

        for (int i = 0; i < shapeLines.length; i++) {
            Line2D line = shapeLines[i];

            // Calculate length of axis shape at each shape segment
            double segmentLength = line.getP1().distance(line.getP2());
            shapeSegmentLengths[i] = segmentLength;
            shapeLengths[i + 1] = shapeLengths[i] + segmentLength;

            // Calculate a normalized vector perpendicular to the current
            // axis shape segment
            shapeLineNormals[i] = new Point2D.Double(
                    (line.getY2() - line.getY1()) / segmentLength,
                    -(line.getX2() - line.getX1()) / segmentLength
            );
        }
    }

    /**
     * Custom deserialization method.
     *
     * @param in Input stream.
     * @throws ClassNotFoundException if a serialized class doesn't exist
     *                                anymore.
     * @throws IOException            if there is an error while reading data
     *                                from the
     *                                input stream.
     */
    private void readObject(ObjectInputStream in)
            throws ClassNotFoundException, IOException {
        // Default deserialization
        in.defaultReadObject();
        // Custom deserialization
        shapeStroke = (Stroke) SerializationUtils.unwrap((Serializable) in.readObject());
        tickStroke = (Stroke) SerializationUtils.unwrap((Serializable) in.readObject());
        minorTickStroke = (Stroke) SerializationUtils.unwrap((Serializable) in.readObject());
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // Default deserialization
        out.defaultWriteObject();
        // Custom serialization
        out.writeObject(SerializationUtils.wrap(shapeStroke));
        out.writeObject(SerializationUtils.wrap(tickStroke));
        out.writeObject(SerializationUtils.wrap(minorTickStroke));
    }

    @Override
    public Number getIntersection() {
        return intersection;
    }

    @Override
    public void setIntersection(Number intersection) {
        this.intersection = intersection;
    }

    @Override
    public Shape getShape() {
        return shape;
    }

    @Override
    public void setShape(Shape shape) {
        this.shape = shape;
        evaluateShape(shape);
    }

    @Override
    public boolean isShapeVisible() {
        return shapeVisible;
    }

    @Override
    public void setShapeVisible(boolean shapeVisible) {
        this.shapeVisible = shapeVisible;
    }

    @Override
    public boolean isShapeNormalOrientationClockwise() {
        return shapeNormalOrientationClockwise;
    }

    @Override
    public void setShapeNormalOrientationClockwise(boolean clockwise) {
        this.shapeNormalOrientationClockwise = clockwise;
    }

    @Override
    public Paint getShapeColor() {
        return shapeColor;
    }

    @Override
    public void setShapeColor(Paint color) {
        this.shapeColor = color;
    }

    @Override
    public Stroke getShapeStroke() {
        return shapeStroke;
    }

    @Override
    public void setShapeStroke(Stroke stroke) {
        this.shapeStroke = stroke;
    }

    @Override
    public boolean isShapeDirectionSwapped() {
        return shapeDirectionSwapped;
    }

    @Override
    public void setShapeDirectionSwapped(boolean directionSwapped) {
        this.shapeDirectionSwapped = directionSwapped;
    }

    @Override
    public boolean isTicksVisible() {
        return ticksVisible;
    }

    @Override
    public void setTicksVisible(boolean ticksVisible) {
        this.ticksVisible = ticksVisible;
    }

    @Override
    public Number getTickSpacing() {
        return tickSpacing;
    }

    @Override
    public void setTickSpacing(Number spacing) {
        this.tickSpacing = spacing;
    }

    @Override
    public boolean isTicksAutoSpaced() {
        return ticksAutoSpaced;
    }

    @Override
    public void setTicksAutoSpaced(boolean autoSpaced) {
        this.ticksAutoSpaced = autoSpaced;
    }

    @Override
    public double getTickLength() {
        return tickLength;
    }

    @Override
    public void setTickLength(double length) {
        this.tickLength = length;
    }

    @Override
    public Stroke getTickStroke() {
        return tickStroke;
    }

    @Override
    public void setTickStroke(Stroke stroke) {
        this.tickStroke = stroke;
    }

    @Override
    public double getTickAlignment() {
        return tickAlignment;
    }

    @Override
    public void setTickAlignment(double alignment) {
        this.tickAlignment = alignment;
    }

    @Override
    public Font getTickFont() {
        return tickFont;
    }

    @Override
    public void setTickFont(Font font) {
        this.tickFont = font;
    }

    @Override
    public Paint getTickColor() {
        return tickColor;
    }

    @Override
    public void setTickColor(Paint color) {
        this.tickColor = color;
    }

    @Override
    public boolean isTickLabelsVisible() {
        return tickLabelsVisible;
    }

    @Override
    public void setTickLabelsVisible(boolean tickLabelsVisible) {
        this.tickLabelsVisible = tickLabelsVisible;
    }

    @Override
    public Format getTickLabelFormat() {
        return tickLabelFormat;
    }

    @Override
    public void setTickLabelFormat(Format format) {
        this.tickLabelFormat = format;
    }

    @Override
    public double getTickLabelDistance() {
        return tickLabelDistance;
    }

    @Override
    public void setTickLabelDistance(double distance) {
        this.tickLabelDistance = distance;
    }

    @Override
    public boolean isTickLabelsOutside() {
        return tickLabelsOutside;
    }

    @Override
    public void setTickLabelsOutside(boolean labelsOutside) {
        this.tickLabelsOutside = labelsOutside;
    }

    @Override
    public double getTickLabelRotation() {
        return tickLabelRotation;
    }

    @Override
    public void setTickLabelRotation(double angle) {
        this.tickLabelRotation = angle;
    }

    @Override
    public boolean isMinorTicksVisible() {
        return minorTickVisible;
    }

    @Override
    public void setMinorTicksVisible(boolean minorTicksVisible) {
        this.minorTickVisible = minorTicksVisible;
    }

    @Override
    public int getMinorTicksCount() {
        return minorTicksCount;
    }

    @Override
    public void setMinorTicksCount(int count) {
        this.minorTicksCount = count;
    }

    @Override
    public double getMinorTickLength() {
        return minorTickLength;
    }

    @Override
    public void setMinorTickLength(double length) {
        this.minorTickLength = length;
    }

    @Override
    public Stroke getMinorTickStroke() {
        return minorTickStroke;
    }

    @Override
    public void setMinorTickStroke(Stroke stroke) {
        this.minorTickStroke = stroke;
    }

    @Override
    public double getMinorTickAlignment() {
        return minorTickAlignment;
    }

    @Override
    public void setMinorTickAlignment(double alignment) {
        this.minorTickAlignment = alignment;
    }

    @Override
    public Paint getMinorTickColor() {
        return minorTickColor;
    }

    @Override
    public void setMinorTickColor(Paint color) {
        this.minorTickColor = color;
    }

    @Override
    public Map<Double, String> getCustomTicks() {
        return Collections.unmodifiableMap(customTicks);
    }

    @Override
    public void setCustomTicks(Map<Double, String> positionsAndLabels) {
        customTicks.clear();
        customTicks.putAll(positionsAndLabels);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public double getLabelDistance() {
        return labelDistance;
    }

    @Override
    public void setLabelDistance(double distance) {
        this.labelDistance = distance;
    }

    @Override
    public double getLabelRotation() {
        return labelRotation;
    }

    @Override
    public void setLabelRotation(double angle) {
        this.labelRotation = angle;
    }

    @Override
    public Font getLabelFont() {
        return labelFont;
    }

    @Override
    public void setLabelFont(Font font) {
        this.labelFont = font;
    }

    @Override
    public Paint getLabelColor() {
        return labelColor;
    }

    @Override
    public void setLabelColor(Paint color) {
        this.labelColor = color;
    }
}
