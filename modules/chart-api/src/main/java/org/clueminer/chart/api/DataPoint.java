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
package org.clueminer.chart.api;

import java.awt.Shape;
import org.clueminer.chart.points.PointData;
import org.clueminer.chart.util.PointND;

/**
 * Class for storing points of a plot.
 */
public class DataPoint {

    /**
     * Axes and data values that were used to create the data point.
     */
    public final PointData data;
    /**
     * Position of the data point (n-dimensional).
     */
    public final PointND<Double> position;
    /**
     * Drawable that will be used to render the data point.
     */
    public final Drawable drawable;
    /**
     * Shape describing the data point.
     */
    public final Shape shape;
    /**
     * Drawable that will be used to render the value label.
     */
    public final Drawable labelDrawable;

    /**
     * Creates a new {@code DataPoint} object with the specified position,
     * {@code Drawable}, and shape.
     *
     * @param data          Data that this point was created from.
     * @param position      Coordinates in view/screen units.
     * @param drawable      Visual representation.
     * @param shape         Geometric shape of the point.
     * @param labelDrawable Visual representation of the value label.
     */
    public DataPoint(PointData data, PointND<Double> position,
            Drawable drawable, Shape shape, Drawable labelDrawable) {
        this.data = data;
        this.position = position;
        this.drawable = drawable;
        this.shape = shape;
        this.labelDrawable = labelDrawable;
    }
}
