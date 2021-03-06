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
package org.clueminer.clustering.preview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

/**
 * Generates legend for chart coloring
 *
 * @author Tomas Barton
 */
public class ChartLegend extends JPanel {

    private Map<Integer, Color> colors;
    private BufferedImage buffImg;
    private Graphics2D g2d;
    private Dimension size = new Dimension(0, 0);
    private final Insets insets = new Insets(0, 50, 0, 0);
    private int lineHeight = 15;
    private int fontSize = 12;
    private Font defaultFont = new Font("verdana", Font.PLAIN, fontSize);
    private int width;
    private int height;
    private int maxWidth = 50;
    private int tickWidth = 20;
    private int spaceBetweenTickAndText = 20;
    private int maxComponentWidth = 100;
    private List<Integer> sortedKeys;

    public ChartLegend() {
        setBackground(Color.WHITE);
    }

    public void updateChart() {
        if (!hasData()) {
            return;
        }
        int w = insets.left + tickWidth + spaceBetweenTickAndText + maxWidth + insets.right;
        if (w > maxComponentWidth) {
            width = w;
        } else {
            width = maxComponentWidth;
        }
        height = insets.top + numLines() * lineHeight + insets.bottom;
        //nodes on right, 90 deg rot
        setSizes(width, height);
        invalidateCache();
    }

    private int numLines() {
        if (!hasData()) {
            return 0;
        }
        return colors.size();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (buffImg == null) {
            drawData();
        }

        Graphics2D g2 = (Graphics2D) g;
        if (g2d != null) {
            g2.drawImage(buffImg,
                         0, 0,
                         size.width, size.height,
                         null);
        }
        g2.dispose();
    }

    public boolean hasData() {
        return colors != null;
    }

    /**
     * Set component size
     *
     * @param width
     * @param height
     */
    public void setSizes(int width, int height) {
        size.width = width;
        size.height = height;
        setPreferredSize(size);
        setSize(size);
        setMinimumSize(size);
    }

    private void drawData() {
        if (!hasData()) {
            return;
        }
        buffImg = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_ARGB);
        g2d = buffImg.createGraphics();
        double annY;
        g2d.setColor(Color.black);
        g2d.setFont(defaultFont);
        FontRenderContext frc = g2d.getFontRenderContext();
        FontMetrics fm = g2d.getFontMetrics();
        int ascent = fm.getMaxAscent();
        int descent = fm.getDescent();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setStroke(new BasicStroke(3));

        int i = 0;
        String s;
        double offset = (lineHeight / 2.0) + ((ascent - descent) / 2.0);
        double lineY;
        int textStart = insets.left + tickWidth + spaceBetweenTickAndText;

        for (Integer row : sortedKeys) {
            annY = i * lineHeight + offset;
            lineY = i * lineHeight + (lineHeight / 2.0) + 1.0;
            g2d.setColor(colors.get(row));
            g2d.draw(new Line2D.Double(insets.left, lineY, insets.left + tickWidth, lineY));

            s = String.valueOf(row);

            int w = (int) (g2d.getFont().getStringBounds(s, frc).getWidth());
            checkMax(w);
            g2d.setColor(Color.BLACK);
            g2d.drawString(s, textStart, (float) annY);
            i++;
        }
    }

    private void checkMax(int width) {
        if (width > maxWidth) {
            maxWidth = width;
            updateChart();
        }
    }

    public Map<Integer, Color> getColors() {
        return colors;
    }

    public void setColors(Map<Integer, Color> colors) {
        sortedKeys = new ArrayList<Integer>(colors.keySet());
        Collections.sort(sortedKeys);

        this.colors = colors;
        updateChart();
    }

    /**
     * We try to avoid complete repainting as much as possible
     */
    public void invalidateCache() {
        //invalidate cache
        buffImg = null;
        repaint();
    }

    public void setMaxWidth(int width) {
        this.maxComponentWidth = width;
    }

}
