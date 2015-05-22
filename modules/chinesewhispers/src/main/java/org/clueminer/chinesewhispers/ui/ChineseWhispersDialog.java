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
package org.clueminer.chinesewhispers.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.clueminer.chinesewhispers.ChineseWhispers;
import org.clueminer.clustering.api.AgglParams;
import org.clueminer.clustering.api.ClusteringAlgorithm;
import org.clueminer.clustering.gui.ClusteringDialog;
import org.clueminer.utils.Props;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author deric
 */
@ServiceProvider(service = ClusteringDialog.class)
public class ChineseWhispersDialog extends JPanel implements ClusteringDialog {

    private JTextField tfIterations;
    private JSlider sliderIter;

    public ChineseWhispersDialog() {
        initComponents();
    }

    @Override
    public String getName() {
        return "Chinese Whispers dialog";
    }

    @Override
    public Props getParams() {
        Props params = new Props();
        params.putBoolean(AgglParams.CLUSTER_COLUMNS, false);
        params.putInt(ChineseWhispers.MAX_ITERATIONS, sliderIter.getValue());
        return params;
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public boolean isUIfor(ClusteringAlgorithm algorithm) {
        return algorithm instanceof ChineseWhispers;
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0.1;
        c.weighty = 1.0;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        //iterations
        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("Iterations:"), c);
        sliderIter = new JSlider(10, 2000);
        sliderIter.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                tfIterations.setText(String.valueOf(sliderIter.getValue()));
            }
        });
        c.gridx = 1;
        add(sliderIter, c);

        c.gridx = 2;
        tfIterations = new JTextField("100", 4);
        add(tfIterations, c);
        tfIterations.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                updateIterSlider();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                updateIterSlider();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                updateIterSlider();
            }
        });
        sliderIter.setValue(100);
    }

    private void updateIterSlider() {
        try {
            int val = Integer.valueOf(tfIterations.getText());
            sliderIter.setValue(val);
        } catch (NumberFormatException ex) {
            // wrong input so we do not set the slider but also do not want to raise an exception
        }
    }

}
