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
package org.clueminer.chart.base;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import javax.swing.event.EventListenerList;
import org.clueminer.chart.api.AxisProperties;
import org.clueminer.chart.api.ChartProperties;
import org.clueminer.events.LogEvent;
import org.clueminer.events.LogListener;
import org.clueminer.gui.StrokeGenerator;
import org.clueminer.xml.XMLUtil;
import org.clueminer.xml.XMLUtil.XMLTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ChartPropertiesImpl extends AbstractPropertyListener implements XMLTemplate, ChartProperties {

    public static final double AXIS_TICK = 6;
    public static final double AXIS_DATE_STICK = 10;
    public static final double AXIS_PRICE_STICK = 5;
    public static final Color AXIS_COLOR = new Color(0x2e3436);
    public static final int AXIS_STROKE_INDEX = 0;
    public static final boolean AXIS_LOGARITHMIC_FLAG = false;
    public static final double BAR_WIDTH = 8;
    public static final Color BAR_COLOR = new Color(0x2e3436);
    public static final int BAR_STROKE_INDEX = 0;
    public static final boolean BAR_VISIBILITY = true;
    public static final Color BAR_DOWN_COLOR = new Color(0xef2929);
    public static final boolean BAR_DOWN_VISIBILITY = true;
    public static final Color BAR_UP_COLOR = new Color(0x73d216);
    public static final boolean BAR_UP_VISIBILITY = true;
    public static final Color GRID_HORIZONTAL_COLOR = new Color(0xeeeeec);
    public static final int GRID_HORIZONTAL_STROKE_INDEX = 0;
    public static final boolean GRID_HORIZONTAL_VISIBILITY = true;
    public static final Color GRID_VERTICAL_COLOR = new Color(0xeeeeec);
    public static final int GRID_VERTICAL_STROKE_INDEX = 0;
    public static final boolean GRID_VERTICAL_VISIBILITY = true;
    public static final Color BACKGROUND_COLOR = new Color(0xffffff);
    public static final Font FONT = new Font("Dialog", Font.PLAIN, 12);
    public static final Color FONT_COLOR = new Color(0x2e3436);
    public static final boolean MARKER_VISIBILITY = true;
    public static final boolean TOOLBAR_VISIBILITY = true;
    public static final boolean TOOLBAR_SMALL_ICONS = false;
    public static final boolean TOOLBAR_SHOW_LABELS = true;
    private static final long serialVersionUID = -1623753103974252354L;
    private double axisTick = AXIS_TICK;
    private double axisDateStick = AXIS_DATE_STICK;
    private double axisPriceStick = AXIS_PRICE_STICK;
    private Color axisColor = AXIS_COLOR;
    private int axisStrokeIndex = AXIS_STROKE_INDEX;
    private boolean axisLogarithmicFlag = AXIS_LOGARITHMIC_FLAG;
    private double barWidth = BAR_WIDTH;
    private Color barColor = BAR_COLOR;
    private int barStrokeIndex = BAR_STROKE_INDEX;
    private boolean barVisibility = BAR_VISIBILITY;
    private Color barDownColor = BAR_DOWN_COLOR;
    private boolean barDownVisibility = BAR_DOWN_VISIBILITY;
    private Color barUpColor = BAR_UP_COLOR;
    private boolean barUpVisibility = BAR_UP_VISIBILITY;
    private Color gridHorizontalColor = GRID_HORIZONTAL_COLOR;
    private int gridHorizontalStrokeIndex = GRID_HORIZONTAL_STROKE_INDEX;
    private boolean gridHorizontalVisibility = GRID_HORIZONTAL_VISIBILITY;
    private Color gridVerticalColor = GRID_VERTICAL_COLOR;
    private int gridVerticalStrokeIndex = GRID_VERTICAL_STROKE_INDEX;
    private boolean gridVerticalVisibility = GRID_VERTICAL_VISIBILITY;
    private Color backgroundColor = BACKGROUND_COLOR;
    private Font font = FONT;
    private Color fontColor = FONT_COLOR;
    private boolean markerVisibility = MARKER_VISIBILITY;
    private String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private boolean toolbarVisibility = TOOLBAR_VISIBILITY;
    private boolean toolbarSmallIcons = TOOLBAR_SMALL_ICONS;
    private boolean toolbarShowLabels = TOOLBAR_SHOW_LABELS;
    private AxisProperties xAxis;
    private AxisProperties yAxis;

    public ChartPropertiesImpl() {
        xAxis = new DateAxisProperties();
        yAxis = new AxisPropertiesImpl();
    }

    public double getAxisTick() {
        return this.axisTick;
    }

    public double getAxisDateStick() {
        return this.axisDateStick;
    }

    public double getAxisPriceStick() {
        return this.axisPriceStick;
    }

    public Color getAxisColor() {
        return this.axisColor;
    }

    public void setAxisColor(Color color) {
        this.axisColor = color;
    }

    public int getAxisStrokeIndex() {
        return this.axisStrokeIndex;
    }

    public void setAxisStrokeIndex(int i) {
        this.axisStrokeIndex = i;
    }

    public Stroke getAxisStroke() {
        return StrokeGenerator.getStroke(axisStrokeIndex);
    }

    public void setAxisStroke(Stroke s) {
        this.axisStrokeIndex = StrokeGenerator.getStrokeIndex(s);
    }

    public boolean getAxisLogarithmicFlag() {
        return axisLogarithmicFlag;
    }

    public void setAxisLogarithmicFlag(boolean b) {
        axisLogarithmicFlag = b;
        fireLogEvent(new LogEvent(this));
    }

    public void setBarWidth(double itemWidth) {
        barWidth = itemWidth;
    }

    public double getBarWidth() {
        return barWidth;
    }

    public Color getBarColor() {
        return this.barColor;
    }

    public void setBarColor(Color color) {
        this.barColor = color;
    }

    public int getBarStrokeIndex() {
        return this.barStrokeIndex;
    }

    public void setBarStrokeIndex(int i) {
        this.barStrokeIndex = i;
    }

    public Stroke getBarStroke() {
        return StrokeGenerator.getStroke(barStrokeIndex);
    }

    public void setBarStroke(Stroke s) {
        this.barStrokeIndex = StrokeGenerator.getStrokeIndex(s);
    }

    public boolean getBarVisibility() {
        return this.barVisibility;
    }

    public void setBarVisibility(boolean b) {
        this.barVisibility = b;
    }

    public Color getBarDownColor() {
        return this.barDownColor;
    }

    public void setBarDownColor(Color color) {
        this.barDownColor = color;
    }

    public boolean getBarDownVisibility() {
        return this.barDownVisibility;
    }

    public void setBarDownVisibility(boolean b) {
        this.barDownVisibility = b;
    }

    public Color getBarUpColor() {
        return this.barUpColor;
    }

    public void setBarUpColor(Color color) {
        this.barUpColor = color;
    }

    public boolean getBarUpVisibility() {
        return this.barUpVisibility;
    }

    public void setBarUpVisibility(boolean b) {
        this.barUpVisibility = b;
    }

    public Color getGridHorizontalColor() {
        return this.gridHorizontalColor;
    }

    public void setGridHorizontalColor(Color color) {
        this.gridHorizontalColor = color;
    }

    public int getGridHorizontalStrokeIndex() {
        return this.gridHorizontalStrokeIndex;
    }

    public void setGridHorizontalStrokeIndex(int i) {
        this.gridHorizontalStrokeIndex = i;
    }

    public Stroke getGridHorizontalStroke() {
        return StrokeGenerator.getStroke(gridHorizontalStrokeIndex);
    }

    public void setGridHorizontalStroke(Stroke s) {
        this.gridHorizontalStrokeIndex = StrokeGenerator.getStrokeIndex(s);
    }

    public boolean getGridHorizontalVisibility() {
        return this.gridHorizontalVisibility;
    }

    public void setGridHorizontalVisibility(boolean b) {
        this.gridHorizontalVisibility = b;
    }

    public Color getGridVerticalColor() {
        return this.gridVerticalColor;
    }

    public void setGridVerticalColor(Color color) {
        this.gridVerticalColor = color;
    }

    public int getGridVerticalStrokeIndex() {
        return this.gridVerticalStrokeIndex;
    }

    public void setGridVerticalStrokeIndex(int i) {
        this.gridVerticalStrokeIndex = i;
    }

    public Stroke getGridVerticalStroke() {
        return StrokeGenerator.getStroke(gridVerticalStrokeIndex);
    }

    public void setGridVerticalStroke(Stroke s) {
        this.gridVerticalStrokeIndex = StrokeGenerator.getStrokeIndex(s);
    }

    public boolean getGridVerticalVisibility() {
        return this.gridVerticalVisibility;
    }

    public void setGridVerticalVisibility(boolean b) {
        this.gridVerticalVisibility = b;
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }

    public Font getFont() {
        return this.font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getFontColor() {
        return this.fontColor;
    }

    public void setFontColor(Color color) {
        this.fontColor = color;
    }

    public void setMarkerVisibility(boolean b) {
        markerVisibility = b;
    }

    public boolean getMarkerVisibility() {
        return markerVisibility;
    }

    public String[] getMonths() {
        return this.months;
    }

    public boolean getToolbarVisibility() {
        return toolbarVisibility;
    }

    public void setToolbarVisibility(boolean b) {
        toolbarVisibility = b;
    }

    public void toggleToolbarVisibility() {
        toolbarVisibility = !toolbarVisibility;
    }

    public boolean getToolbarSmallIcons() {
        return toolbarSmallIcons;
    }

    public void setToolbarSmallIcons(boolean b) {
        toolbarSmallIcons = b;
    }

    public void toggleToolbarSmallIcons() {
        toolbarSmallIcons = !toolbarSmallIcons;
    }

    public boolean getToolbarShowLabels() {
        return toolbarShowLabels;
    }

    public void setToolbarShowLabels(boolean b) {
        toolbarShowLabels = b;
    }

    public void toggleShowLabels() {
        toolbarShowLabels = !toolbarShowLabels;
    }
    private transient EventListenerList logListeners = new EventListenerList();

    public void addLogListener(LogListener l) {
        if (logListeners == null) {
            logListeners = new EventListenerList();
        }
        logListeners.add(LogListener.class, l);
    }

    public void removeLogListener(LogListener l) {
        if (logListeners == null) {
            logListeners = new EventListenerList();
            return;
        }
        logListeners.remove(LogListener.class, l);
    }

    public void fireLogEvent(LogEvent evt) {
        if (logListeners != null) {
            LogListener[] list = logListeners.getListeners(LogListener.class);
            for (LogListener l : list) {
                l.fire(evt);
            }
        }
    }

    @Override
    public void saveToTemplate(Document document, Element element) {
        XMLUtil.addColorProperty(document, element, "axisColor", axisColor);
        XMLUtil.addIntegerProperty(document, element, "axisStrokeIndex", axisStrokeIndex);
        XMLUtil.addDoubleProperty(document, element, "barWidth", barWidth);
        XMLUtil.addColorProperty(document, element, "barColor", barColor);
        XMLUtil.addIntegerProperty(document, element, "barStrokeIndex", barStrokeIndex);
        XMLUtil.addBooleanProperty(document, element, "barVisibility", barVisibility);
        XMLUtil.addColorProperty(document, element, "barDownColor", barDownColor);
        XMLUtil.addBooleanProperty(document, element, "barDownVisibility", barDownVisibility);
        XMLUtil.addColorProperty(document, element, "barUpColor", barUpColor);
        XMLUtil.addBooleanProperty(document, element, "barUpVisibility", barUpVisibility);
        XMLUtil.addColorProperty(document, element, "gridHorizontalColor", gridHorizontalColor);
        XMLUtil.addIntegerProperty(document, element, "gridHorizontalStrokeIndex", gridHorizontalStrokeIndex);
        XMLUtil.addBooleanProperty(document, element, "gridHorizontalVisibility", gridHorizontalVisibility);
        XMLUtil.addColorProperty(document, element, "gridVerticalColor", gridVerticalColor);
        XMLUtil.addIntegerProperty(document, element, "gridVerticalStrokeIndex", gridVerticalStrokeIndex);
        XMLUtil.addBooleanProperty(document, element, "gridVerticalVisibility", gridVerticalVisibility);
        XMLUtil.addColorProperty(document, element, "backgroundColor", backgroundColor);
        XMLUtil.addFontProperty(document, element, "font", font);
        XMLUtil.addColorProperty(document, element, "fontColor", fontColor);
    }

    @Override
    public void loadFromTemplate(Element element) {
        axisColor = XMLUtil.getColorProperty(element, "axisColor");
        axisStrokeIndex = XMLUtil.getIntegerProperty(element, "axisStrokeIndex");
        barWidth = XMLUtil.getDoubleProperty(element, "barWidth");
        barColor = XMLUtil.getColorProperty(element, "barColor");
        barStrokeIndex = XMLUtil.getIntegerProperty(element, "barStrokeIndex");
        barVisibility = XMLUtil.getBooleanProperty(element, "barVisibility");
        barDownColor = XMLUtil.getColorProperty(element, "barDownColor");
        barDownVisibility = XMLUtil.getBooleanProperty(element, "barDownVisibility");
        barUpColor = XMLUtil.getColorProperty(element, "barUpColor");
        barUpVisibility = XMLUtil.getBooleanProperty(element, "barUpVisibility");
        gridHorizontalColor = XMLUtil.getColorProperty(element, "gridHorizontalColor");
        gridHorizontalStrokeIndex = XMLUtil.getIntegerProperty(element, "gridHorizontalStrokeIndex");
        gridHorizontalVisibility = XMLUtil.getBooleanProperty(element, "gridHorizontalVisibility");
        gridVerticalColor = XMLUtil.getColorProperty(element, "gridVerticalColor");
        gridVerticalStrokeIndex = XMLUtil.getIntegerProperty(element, "gridVerticalStrokeIndex");
        gridVerticalVisibility = XMLUtil.getBooleanProperty(element, "gridVerticalVisibility");
        backgroundColor = XMLUtil.getColorProperty(element, "backgroundColor");
        font = XMLUtil.getFontProperty(element, "font");
        fontColor = XMLUtil.getColorProperty(element, "fontColor");
    }

    @Override
    public AxisProperties getXAxis() {
        return xAxis;
    }

    @Override
    public AxisProperties getYAxis() {
        return yAxis;
    }

    @Override
    public Color getColor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setColor(Color color) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
