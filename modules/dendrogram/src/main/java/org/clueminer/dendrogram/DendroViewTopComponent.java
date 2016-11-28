/*
 * Copyright (C) 2011-2016 clueminer.org
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
package org.clueminer.dendrogram;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.clustering.api.HierarchicalResult;
import org.clueminer.clustering.api.dendrogram.DendroViewer;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dgram.DgViewer;
import org.clueminer.project.api.ProjectController;
import org.clueminer.utils.Props;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * Top component which displays dendrograms
 *
 * @param <E>
 * @param <C>
 */
@ConvertAsProperties(
        dtd = "-//org.clueminer.dendrogram//DendroView//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "DendroViewTopComponent",
        iconBase = "org/clueminer/clustering/resources/clustering16.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "rightSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "org.clueminer.dendrogram.DendroViewTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DendroViewAction",
        preferredID = "DendroViewTopComponent"
)
@Messages({
    "CTL_DendroViewAction=DendroView",
    "CTL_DendroViewTopComponent=DendroView Window",
    "HINT_DendroViewTopComponent=Displays dendrogram"
})
public final class DendroViewTopComponent<E extends Instance, C extends Cluster<E>>
        extends TopComponent implements LookupListener, ClusteringListener<E, C> {

    private static final long serialVersionUID = -1479282981915282578L;
    private Lookup.Result<Clustering> result = null;
    private DendroViewer frame;
    private DendroToolbar toolbar;
    private final InstanceContent content = new InstanceContent();
    private static final Logger logger = Logger.getLogger(DendroViewTopComponent.class.getName());

    public DendroViewTopComponent() {
        initComponents();
        setName(Bundle.CTL_DendroViewTopComponent());
        setToolTipText(Bundle.HINT_DendroViewTopComponent());
        associateLookup(new AbstractLookup(content));
        initialize();
    }

    private void initialize() {
        frame = new DgViewer();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(0, 0, 0, 0);
        add((Component) frame, c);
        frame.addClusteringListener(this);
        toolbar = new DendroToolbar(frame);
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        add(toolbar, c);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        result = Utilities.actionsGlobalContext().lookupResult(Clustering.class);
        result.addLookupListener(this);
        resultChanged(new LookupEvent(result));
    }

    @Override
    public void componentClosed() {
        if (result != null) {
            result.removeLookupListener(this);
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends Clustering> allClusterings = result.allInstances();
        if (allClusterings != null && allClusterings.size() > 0) {

            Iterator<? extends Clustering> it = allClusterings.iterator();

            if (it.hasNext()) {
                Clustering clust = it.next();
                if (clust != null) {
                    frame.setClustering(clust);
                }
            }

        }
    }

    @Override
    public void clusteringChanged(Clustering<E, C> clust) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        //add result to lookup
        pc.getCurrentProject().add(Lookups.singleton(clust));
        logger.log(Level.INFO, "adding to lookup clustring {0} - {1}", new Object[]{clust.size(), clust.getName()});
        content.set(Collections.singleton(clust), null);
    }

    @Override
    public void resultUpdate(HierarchicalResult hclust) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        //add result to lookup
        if (hclust != null) {
            pc.getCurrentProject().add(Lookups.singleton(hclust));
            //System.out.println("adding clustering result to lookup");
            Clustering c = hclust.getClustering();
            logger.log(Level.INFO, "hclust update: {0}", c.size());
            content.set(Collections.singleton(c), null);
        }

    }

    @Override
    public void clusteringStarted(Dataset<E> dataset, Props params) {
        //nothing to do
    }
}
