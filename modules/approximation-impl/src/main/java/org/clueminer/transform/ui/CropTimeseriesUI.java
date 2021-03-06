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
package org.clueminer.transform.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.clueminer.dataset.api.ContinuousInstance;
import org.clueminer.dataset.api.Timeseries;
import org.clueminer.flow.api.FlowPanel;
import org.clueminer.project.api.Project;
import org.clueminer.project.api.ProjectController;
import org.clueminer.types.TimePoint;
import org.clueminer.utils.Props;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author deric
 */
public class CropTimeseriesUI extends JPanel implements FlowPanel {

    private TimeSelectionPlot plot;
    private JTextField tfStart;
    private JTextField tfEnd;
    private JButton btnScan;
    private double start;
    private double end;
    private static final Logger LOG = LoggerFactory.getLogger(CropTimeseriesUI.class);

    public CropTimeseriesUI() {
        initComponents();
    }

    @Override
    public Props getParams() {
        Props params = new Props();
        params.putDouble(CropTimeseries.CROP_START, start);
        params.putDouble(CropTimeseries.CROP_END, end);
        return params;
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    private void initComponents() {
        setMinimumSize(new Dimension(800, 600));
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0.05;
        c.weighty = 0.05;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Select data area:"), c);
        c.gridy = 1;
        add(formFieldsPanel(), c);
        c.gridy = 2;
        c.weightx = 0.95;
        c.weighty = 0.95;
        c.fill = GridBagConstraints.BOTH;
        plot = new TimeSelectionPlot(this);

        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                scanForData();
            }

            public void componentResized(ComponentEvent e) {
                plot.sizeUpdated(getSize());
            }
        });
        add(plot, c);
    }

    private void scanForData() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc != null) {
            LOG.info("current project {}", pc.getCurrentProject().getName());
            Project proj = pc.getCurrentProject();
            if (proj != null) {
                Collection<? extends Timeseries> allData = proj.getLookup().lookupAll(Timeseries.class);
                LOG.debug("found {} timeseries", allData.size());
                //TODO: handle multiple datasets
                for (Timeseries t : allData) {
                    setDataset(t);
                }
                revalidate();
                validate();
                repaint();
            }
        }
    }

    private JPanel formFieldsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        tfStart = new JTextField(10);
        panel.add(new JLabel("Start:"));
        panel.add(tfStart);
        panel.add(new JLabel("End:"));
        tfEnd = new JTextField(10);
        panel.add(tfEnd);
        btnScan = new JButton("Scan for data");
        btnScan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scanForData();
            }
        });
        panel.add(btnScan);

        return panel;
    }

    public void setDataset(Timeseries<? extends ContinuousInstance> dataset) {
        if (dataset != null) {
            plot.setDataset(dataset);

            TimePoint[] tp = dataset.getTimePoints();
            setStart(tp[0].getPosition());
            setEnd(tp[tp.length - 1].getPosition());
        }
    }

    public void setStart(double start) {
        this.start = start;
        tfStart.setText(String.format("%.2f", start));
    }

    public void setEnd(double end) {
        this.end = end;
        tfEnd.setText(String.format("%.2f", end));
    }

}
