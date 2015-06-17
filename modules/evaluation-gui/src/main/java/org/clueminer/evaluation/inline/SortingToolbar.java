/*
 * Copyright (C) 2011-2015 clueminer.org
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
package org.clueminer.evaluation.inline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.clueminer.clustering.api.ClusterEvaluation;
import org.clueminer.clustering.api.factory.EvaluationFactory;
import org.clueminer.evaluation.gui.EvaluatorComboBox;
import static org.clueminer.evaluation.inline.InlinePanel.NONE;
import org.clueminer.export.sorting.SortingExporter;
import org.openide.util.ImageUtilities;

/**
 *
 * @author deric
 */
public class SortingToolbar extends JToolBar {

    private JComboBox comboEvaluatorX;
    private JComboBox comboEvaluatorY;
    private JComboBox comboEvaluatorZ;
    private JButton export;
    private final ScorePlot plot;

    public SortingToolbar(ScorePlot plot) {
        super(SwingConstants.HORIZONTAL);
        this.plot = plot;
        initComponents();
    }

    private void initComponents() {
        this.setFloatable(false);
        this.setRollover(true);

        comboEvaluatorX = new JComboBox();
        comboEvaluatorX.setModel(new EvaluatorComboBox(EvaluationFactory.getInstance().getProvidersArray()));
        comboEvaluatorX.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboEvaluatorXActionPerformed(evt);
            }
        });
        add(comboEvaluatorX);

        comboEvaluatorY = new JComboBox();
        comboEvaluatorY.setModel(new EvaluatorComboBox(EvaluationFactory.getInstance().getProvidersArray()));
        comboEvaluatorY.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboEvaluatorYActionPerformed(evt);
            }
        });
        add(comboEvaluatorY);

        //MO-criterion
        comboEvaluatorZ = new JComboBox();
        List<String> providers = EvaluationFactory.getInstance().getProviders();
        providers.add(0, NONE);
        comboEvaluatorZ.setModel(new EvaluatorComboBox(providers.toArray(new String[0])));
        comboEvaluatorZ.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboEvaluatorZActionPerformed(evt);
            }
        });
        add(comboEvaluatorZ);

        export = new JButton(ImageUtilities.loadImageIcon("org/clueminer/evaluation/gui/save16.png", false));
        export.setToolTipText("Export current results");
        add(export);

        export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                SortingExporter exp = new SortingExporter();
                exp.setDataset(plot.getDataset());
                //  exp.setResults(plot.getResults());
                exp.setClusterings(plot.getClusterings());
                exp.showDialog();
            }
        });
    }

    private void comboEvaluatorXActionPerformed(java.awt.event.ActionEvent evt) {
        String item = (String) comboEvaluatorX.getSelectedItem();
        if (item != null) {
            plot.setEvaluatorX(EvaluationFactory.getInstance().getProvider(item));
        }
    }

    private void comboEvaluatorYActionPerformed(java.awt.event.ActionEvent evt) {
        String item = (String) comboEvaluatorY.getSelectedItem();
        if (item != null) {
            plot.setEvaluatorY(EvaluationFactory.getInstance().getProvider(item));
        }
    }

    private void comboEvaluatorZActionPerformed(ActionEvent evt) {
        String item = (String) comboEvaluatorZ.getSelectedItem();
        if (item != null && !item.equals(NONE)) {
            plot.setEvaluatorZ(EvaluationFactory.getInstance().getProvider(item));
        }
    }

    public void setEvaluatorX(ClusterEvaluation ex) {
        comboEvaluatorX.setSelectedItem(ex.getName());
        plot.setEvaluatorX(ex);
    }

    public void setEvaluatorY(ClusterEvaluation ey) {
        comboEvaluatorY.setSelectedItem(ey.getName());
        plot.setEvaluatorY(ey);
    }
}