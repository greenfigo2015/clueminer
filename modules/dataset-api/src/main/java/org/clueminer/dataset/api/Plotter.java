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
package org.clueminer.dataset.api;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Collection;

/**
 * Universal interface for dataset/cluster visualization.
 *
 * @author Tomas Barton
 * @param <E>
 */
public interface Plotter<E extends Instance> extends Serializable {

    /**
     * Configure component for give type of visualization.
     *
     * @param type
     */
    void prepare(DataType type);

    /**
     * Add data row to chart
     *
     * @param instance
     */
    void addInstance(E instance);

    /**
     * Add data with assignment to cluster
     *
     * @param instance
     * @param clusterName
     */
    void addInstance(E instance, String clusterName);

    void paint(Graphics g);

    /**
     * Sets the preferred size of this component
     *
     * @param preferredSize
     */
    void setPreferredSize(Dimension preferredSize);

    /**
     * Sets the minimum size of this component
     *
     * @param minimumSize
     */
    void setMinimumSize(Dimension minimumSize);

    Dimension getMinimumSize();

    Dimension getPreferredSize();

    int getWidth();

    int getHeight();

    /**
     * Repaint the component
     */
    void repaint();

    void revalidate();

    /**
     * Clear all currently painted data
     */
    void clearAll();

    /**
     * Set plot title
     * <p>
     * @param title
     */
    void setTitle(String title);

    /**
     *
     * @param min
     * @param max
     */
    void setXBounds(double min, double max);

    /**
     *
     * @param min
     * @param max
     */
    void setYBounds(double min, double max);

    /**
     * Check for visualization type support
     *
     * @param type
     * @return
     */
    boolean isSupported(DataType type);

    /**
     * Inverse search of displayed data by coordinates in the plot
     *
     * @param coord
     * @param maxK  maximum number of returned instances
     * @return set of closest items (if any)
     */
    Collection<E> instanceAt(double[] coord, int maxK);

    /**
     * Focus on given instance. Depending on implementation plotter might highlight
     * given data or display tooltip/status message.
     *
     * @param items to focus
     * @param e        mouse position
     */
    void focus(Collection<E> items, MouseEvent e);
}
