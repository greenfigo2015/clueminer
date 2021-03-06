package org.clueminer.chart;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.clueminer.chart.api.Annotation;
import org.clueminer.chart.api.ChartConfig;
import org.clueminer.dialogs.AnnotationProperties;
import org.clueminer.chart.api.Range;
import org.clueminer.chart.factory.AnnotationFactory;
import org.clueminer.events.DatasetEvent;
import org.clueminer.events.DatasetListener;
import org.clueminer.timeseries.chart.NormalizationEvent;
import org.clueminer.timeseries.chart.NormalizationListener;

/**
 *
 * @author Tomas Barton
 */
public class AnnotationPanel extends JPanel
        implements MouseListener, MouseMotionListener, KeyListener, Serializable, DatasetListener, NormalizationListener {

    public static final int NONE = 0;
    public static final int NEWANNOTATION = 1;
    public static final int RESIZE = 2;
    public static final int MOVE = 3;
    public static final int NORMALIZED = 4;
    private static final long serialVersionUID = 125387474053489733L;
    private int state;
    private ChartConfig chartFrame;
    private List<AnnotationImpl> annotations;
    private AnnotationImpl current = null;

    public AnnotationPanel(ChartConfig frame) {
        state = NONE;
        chartFrame = frame;
        annotations = new ArrayList<AnnotationImpl>();
        setOpaque(false);

        addMouseListener((MouseListener) this);
        addMouseMotionListener((MouseMotionListener) this);
        addKeyListener((KeyListener) this);
    }

    public ChartConfig getChartFrame() {
        return chartFrame;
    }

    public int getState() {
        return state;
    }

    public void setState(int i) {
        state = i;
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        setDoubleBuffered(true);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        g2.setPaintMode();

        g2.setPaint(chartFrame.getChartProperties().getXAxis().getColor());

        for (AnnotationImpl a : annotations) {
            a.paint(g2);
        }
    }

    public Range getRange() {
        return chartFrame.getRange();
    }

    public void setAnnotationsList(List<Annotation> list) {
        AnnotationImpl impl;
        for (Annotation a : list) {
            impl = (AnnotationImpl) a;
            impl.setChartConfig(chartFrame);
            impl.setAnnotationPanel(this);
            annotations.add(impl);
        }
        repaint();
    }

    public List<AnnotationImpl> getAnnotationsList() {
        return annotations;
    }

    public AnnotationImpl[] getAnnotations() {
        return annotations.toArray(new AnnotationImpl[annotations.size()]);
    }

    public void addAnnotation(AnnotationImpl a) {
        annotations.add(a);
    }

    public boolean hasCurrent() {
        return current != null;
    }

    public boolean isCurrentNull() {
        return current == null;
    }

    public AnnotationImpl getCurrent() {
        return current;
    }

    public void setCurrent(AnnotationImpl a) {
        current = a;
    }

    public void deselectAll() {
        for (AnnotationImpl annotation : annotations) {
            annotation.setSelected(false);
        }
        current = null;
    }

    public void removeAllAnnotations() {
        try {
            current = null;
            annotations.clear();
            validate();
            repaint();
        } catch (Exception ex) {
            ChartFrame.LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void removeAnnotation() {
        if (!isCurrentNull() && getCurrent().isSelected()) {
            current.setSelected(false);
            annotations.remove(getCurrent());
            current = null;
            repaint();
        }
    }

    private boolean isAnnotation(int x, int y) {
        for (AnnotationImpl a : annotations) {
            boolean b = a.pointIntersects(x, y);
            if (b) {
                current = a;
                current.setActive(true);
                current.setSelected(true);
                return b;
            }
        }
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        requestFocus();

        if (e.isConsumed()) {
            return;
        }

        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
            if (getCursor().equals(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR))) {
                getParent().requestFocus();
            }

            if (AnnotationFactory.getInstance().hasDefault()) {
                setState(NEWANNOTATION);
            }

            switch (getState()) {
                case NONE:
                    chartFrame.deselectAll();
                    if (!isAnnotation(e.getX(), e.getY())) {
                        if (!getCursor().equals(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR))) {
                            if (chartFrame.hasData()) {
                                if (chartFrame.getChartProperties().getMarkerVisibility()) {
                                    Rectangle rect = getBounds();
                                    rect.grow(-2, -2);

                                    int i = chartFrame.getChartData().getIndex(e.getPoint(), rect);
                                    if (i != -1) {
                                        chartFrame.getSplitPanel().setIndex(i);
                                        chartFrame.getSplitPanel().labelText();
                                        chartFrame.getSplitPanel().repaint();
                                    }
                                } else {
                                    chartFrame.getSplitPanel().setIndex(-1);
                                }
                            }
                        }
                    } else {
                        if (!isCurrentNull()) {
                            getCurrent().mousePressed(e);
                        }
                    }
                    break;
                case RESIZE:
                    if (!isCurrentNull()) {
                        getCurrent().mousePressed(e);
                    }
                    break;
                case MOVE:
                    if (!isCurrentNull()) {
                        getCurrent().mousePressed(e);
                    }
                    break;
                case NEWANNOTATION:
                    chartFrame.deselectAll();
                    AnnotationImpl a = (AnnotationImpl) AnnotationFactory.getInstance().getDefault();

                    a.setAnnotationPanel(this);
                    setCurrent(a);
                    if (!isCurrentNull()) {
                        getCurrent().mousePressed(e);
                    } else {
                        setState(NONE);
                        mousePressed(e);
                    }
                    break;
                case NORMALIZED:
                    if (!isCurrentNull()) {
                        getCurrent().mousePressed(e);
                    }
                    break;
            }
        }
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
            if (!isCurrentNull()) {
                AnnotationProperties dialog = new AnnotationProperties(new JFrame(), true);
                dialog.initializeForm(getCurrent());
                dialog.setLocationRelativeTo((Component) chartFrame);
                dialog.setVisible(true);
            }
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            switch (getState()) {
                case NONE:
                    chartFrame.getMenu().show(this, e.getX(), e.getY());
                    break;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isConsumed()) {
            return;
        }

        if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
            if (!isCurrentNull()) {
                getCurrent().mouseReleased(e);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (e.isConsumed()) {
            e.consume();
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.isConsumed()) {
            return;
        }
        if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
            if (!isCurrentNull()) {
                getCurrent().mouseDragged(e);
            } else {
                switch (getState()) {
                    case NONE:
                        if (chartFrame.getChartProperties().getMarkerVisibility()) {
                            Rectangle rect = getBounds();
                            rect.grow(-2, -2);

                            int i = chartFrame.getChartData().getIndex(e.getPoint(), rect);
                            if (i != -1) {
                                chartFrame.getSplitPanel().setIndex(i);
                                chartFrame.getSplitPanel().labelText();
                                chartFrame.getSplitPanel().repaint();
                            }
                        } else {
                            chartFrame.getSplitPanel().setIndex(-1);
                        }
                        break;
                }

            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isConsumed()) {
            return;
        }

        requestFocus();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_MINUS:
                chartFrame.zoomOut();
                break;
            case KeyEvent.VK_SUBTRACT:
                chartFrame.zoomOut();
                break;
            case KeyEvent.VK_ADD:
                chartFrame.zoomIn();
                break;
        }
        switch (e.getModifiers()) {
            case KeyEvent.SHIFT_MASK:
                if (e.getKeyCode() == KeyEvent.VK_EQUALS) {
                    chartFrame.zoomIn();
                }
                break;
        }

        if (hasCurrent() && getCurrent().isSelected()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DELETE:
                    removeAnnotation();
                    break;
                case KeyEvent.VK_UP:
                    getCurrent().moveUp();
                    break;
                case KeyEvent.VK_DOWN:
                    getCurrent().moveDown();
                    break;
                case KeyEvent.VK_LEFT:
                    getCurrent().moveLeft();
                    break;
                case KeyEvent.VK_RIGHT:
                    getCurrent().moveRight();
                    break;
            }
        } else {
            if (chartFrame.getChartProperties().getMarkerVisibility()) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        chartFrame.getSplitPanel().moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        chartFrame.getSplitPanel().moveRight();
                        break;
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void datasetChanged(DatasetEvent evt) {
    }

    @Override
    public void datasetOpened(DatasetEvent evt) {
    }

    @Override
    public void datasetClosed(DatasetEvent evt) {
    }

    @Override
    public void markerMoved(NormalizationEvent evt) {
    }

    @Override
    public void normalizationCompleted() {
        System.out.println("normalized state setted");
        setState(AnnotationPanel.NORMALIZED);

    }

    @Override
    public void datasetCropped(DatasetEvent evt) {
    }
}
