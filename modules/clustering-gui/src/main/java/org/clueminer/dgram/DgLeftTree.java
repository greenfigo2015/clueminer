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
package org.clueminer.dgram;

import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.SwingConstants;
import org.clueminer.clustering.api.HierarchicalResult;
import org.clueminer.clustering.api.dendrogram.DendroNode;
import org.clueminer.clustering.api.dendrogram.DendroPane;
import org.clueminer.clustering.api.dendrogram.DendrogramDataEvent;
import org.clueminer.clustering.api.dendrogram.DendrogramMapping;

/**
 * Dendrogram tree with leaves on left and root on right side
 *
 * @author Tomas Barton
 */
public class DgLeftTree extends DgTree {

    private static final long serialVersionUID = 6752253584322193098L;

    public DgLeftTree(DendroPane panel) {
        super(panel);
        this.orientation = SwingConstants.HORIZONTAL;
    }

    @Override
    protected void drawSubTree(Graphics2D g2, DendroNode node) {
        int nx = (int) scaleDistance(node.getHeight());
        int ny = (int) (node.getPosition() * elementHeight + halfElem);

        drawNode(g2, node, nx, ny);

        if (!node.isLeaf()) {
            int lx = (int) scaleDistance(node.getLeft().getHeight());
            int ly = (int) (node.getLeft().getPosition() * elementHeight + halfElem);

            int rx = (int) scaleDistance(node.getRight().getHeight());
            int ry = (int) (node.getRight().getPosition() * elementHeight + halfElem);
            //we're drawing a U shape
            //straight line
            g2.drawLine(nx, ly, nx, ry);

            //left node
            g2.drawLine(nx, ly, lx, ly);

            //right node
            g2.drawLine(nx, ry, rx, ry);
        }
    }

    @Override
    public void datasetChanged(DendrogramDataEvent evt, DendrogramMapping dataset) {
        this.dendroData = dataset;
        HierarchicalResult clustering = dataset.getRowsResult();
        setTreeData(clustering.getTreeData());
        //to prevent tree flickering
        recalculate();
    }

    @Override
    public void cellWidthChanged(DendrogramDataEvent evt, int width, boolean isAdjusting) {
        //nothing to do
    }

    @Override
    public void cellHeightChanged(DendrogramDataEvent evt, int height, boolean isAdjusting) {
        if (!hasData()) {
            return;
        }
        if (elementHeight != height) {
            elementHeight = height;
            recalculate();
        }
    }

    @Override
    public DendroNode findSubTree(Point p) {
        DendroNode node = null;

        return node;
    }

    @Override
    public boolean isHorizontal() {
        return true;
    }

    @Override
    public boolean isVertical() {
        return false;
    }

}
