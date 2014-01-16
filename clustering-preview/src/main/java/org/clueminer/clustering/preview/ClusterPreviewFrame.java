package org.clueminer.clustering.preview;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.Serializable;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.dataset.api.ContinuousInstance;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dataset.plugin.TimeseriesDataset;
import org.clueminer.utils.Dump;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author Tomas Barton
 */
public class ClusterPreviewFrame extends JPanel implements Serializable, AdjustmentListener, ChangeListener, TaskListener {

    private static final long serialVersionUID = -8719504995316248781L;
    private JScrollPane scroller;
    private PreviewFrameSet previewSet;
    private JSlider chartSizeSlider;
    private JToolBar toolbar;
    private JButton btnChooseMeta;
    private final int minChartHeight = 150;
    private final int maxChartHeight = 650;
    private MetaLoaderDialog loader = new MetaLoaderDialog(this);

    public ClusterPreviewFrame() {
        initComponents();
    }

    private void initComponents() {
        //setLayout(new GridBagLayout());
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        previewSet = new PreviewFrameSet(this);

        chartSizeSlider = new JSlider(SwingConstants.HORIZONTAL);
        chartSizeSlider.setMinimum(minChartHeight);
        chartSizeSlider.setMaximum(maxChartHeight);
        chartSizeSlider.addChangeListener(this);
        chartSizeSlider.setMaximumSize(new Dimension(250, 20));
        chartSizeSlider.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        btnChooseMeta = new JButton("choose meta-data...");
        btnChooseMeta.addActionListener(loader);

        toolbar = new JToolBar(SwingConstants.HORIZONTAL);
        JLabel label = new JLabel(java.util.ResourceBundle.getBundle("org/clueminer/clustering/preview/Bundle").getString("CHART HEIGHT:"));
        toolbar.add(label);
        toolbar.add(chartSizeSlider);
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbar.add(btnChooseMeta);

        scroller = new JScrollPane(previewSet);
        scroller.getViewport().setDoubleBuffered(true);
        scroller.setVisible(true);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.getVerticalScrollBar().addAdjustmentListener(this);

        add(toolbar);
        scroller.getViewport().revalidate();
        add(scroller);
    }

    public PreviewFrameSet getViewer() {
        return previewSet;
    }

    @Override
    public void repaint() {
        if (scroller != null) {
            scroller.getViewport().revalidate();
            super.repaint();
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        //System.out.println("clust preview adjusted");
        //scroller.getViewport().revalidate();
    }

    public void setClustering(Clustering<Cluster> clustering) {
        previewSet.setClustering(clustering);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            previewSet.setChartHeight(chartSizeSlider.getValue());
        }
    }

    public int getChartSize() {
        return chartSizeSlider.getValue();
    }

    public void setChartSize(int size) {
        if (size > 0) {
            chartSizeSlider.setValue(size);
            previewSet.setChartHeight(size);
        }
    }

    /**
     * Loading meta-data finished
     *
     * @param task
     */
    @Override
    public void taskFinished(Task task) {
        System.out.println("meta data loading finished");
        Dataset<? extends Instance>[] result = loader.getDatasets();
        HashMap<Integer, Instance> metaMap = new HashMap<Integer, Instance>(3000);
        int id;
        System.out.println("result " + result);
        if (result != null) {
            for (Dataset<? extends Instance> d : result) {
                if (d != null) {
                    Dump.array(((TimeseriesDataset<ContinuousInstance>) d).getTimePointsArray(), "timepoints ");
                    System.out.println("data po" + ((TimeseriesDataset<ContinuousInstance>) d).getTimePoints().toString());
                    for (Instance inst : d) {
                        //id = Integer.valueOf(inst.getId());
                        id = Integer.valueOf((String) inst.classValue());
                        metaMap.put(id, (Instance) inst);
                    }
                } else {
                    System.out.println("dataset d null!!!");
                }

            }
        }
        previewSet.setMetaMap(metaMap);
    }

}
