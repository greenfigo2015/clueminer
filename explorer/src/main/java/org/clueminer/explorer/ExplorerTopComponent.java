package org.clueminer.explorer;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clueminer.clustering.algorithm.KMeans;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.evolution.Evolution;
import org.clueminer.clustering.api.evolution.EvolutionListener;
import org.clueminer.clustering.api.evolution.Individual;
import org.clueminer.clustering.api.evolution.Pair;
import org.clueminer.clustering.api.factory.InternalEvaluatorFactory;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.eval.AICScore;
import org.clueminer.evolution.EvolutionFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.IconView;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "//org.clueminer.explorer//Explorer//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "ExplorerTopComponent",
        iconBase = "org/clueminer/explorer/evolution16.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.clueminer.explorer.ExplorerTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ExplorerAction",
        preferredID = "ExplorerTopComponent")
@Messages({
    "CTL_ExplorerAction=Explorer",
    "CTL_ExplorerTopComponent=Explorer Window",
    "HINT_ExplorerTopComponent=This is a Explorer window"
})
public final class ExplorerTopComponent extends CloneableTopComponent implements ExplorerManager.Provider, LookupListener, TaskListener, EvolutionListener, ToolbarListener {

    private static final long serialVersionUID = 5542932858488609860L;
    private final transient ExplorerManager mgr = new ExplorerManager();
    private Lookup.Result<Clustering> result = null;
    private AbstractNode root;
    private Dataset<? extends Instance> dataset;
    private static final RequestProcessor RP = new RequestProcessor("Evolution");
    private RequestProcessor.Task task;
    private static final Logger logger = Logger.getLogger(ExplorerTopComponent.class.getName());
    private ExplorerToolbar toolbar;
    private javax.swing.JScrollPane explorerPane;
    private IconView iconView;
    private ClustComparator comparator;

    public ExplorerTopComponent() {
        initComponents();
        setName(Bundle.CTL_ExplorerTopComponent());
        setToolTipText(Bundle.HINT_ExplorerTopComponent());
        init();

        associateLookup(ExplorerUtils.createLookup(mgr, getActionMap()));

        //maybe we want IconView
        //explorerPane.setViewportView(new BeanTreeView());
        //explorerPane.setViewportView(new IconView());
//root = new AbstractNode(new ClusteringChildren());
        //root.setDisplayName("Clustering Evolution");
        //explorerManager.setRootContext(root);
//        mgr.setRootContext(new AbstractNode(Children.create(factory, true)));
    }

    private void init() {
        iconView = new IconView();
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
        add(iconView, c);

        toolbar = new ExplorerToolbar();
        toolbar.setListener(this);
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        c.anchor = java.awt.GridBagConstraints.NORTHWEST;
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
        //result.addLookupListener(this);
        //resultChanged(new LookupEvent(result));

        //ClustGlobal children = new ClustGlobal(result);
        comparator = new ClustComparator(new AICScore());
        ClustSorted children = new ClustSorted(result);
        children.setComparator(comparator);
        //mgr.getRootContext().
        root = new AbstractNode(children);

        root.setDisplayName("root node");
        mgr.setRootContext(root);
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
    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends Clustering> allClusterings = result.allInstances();
        ClusteringNode node;
        for (Clustering c : allClusterings) {
            //System.out.println("clustring size" + c.size());
            //System.out.println(c.toString());
            //root = new ClusteringNode(c);
            node = new ClusteringNode(c);
            //
        }
        //mgr.setRootContext(root);
    }

    public void setDataset(Dataset<? extends Instance> dataset) {
        this.dataset = dataset;
    }

    @Override
    public void taskFinished(Task task) {
        logger.log(Level.INFO, "evolution finished");
        toolbar.evolutionFinished();
    }

    @Override
    public void bestInGeneration(int generationNum, Individual best, double avgFitness, double external) {
        logger.log(Level.INFO, "best in generation, fitness: {0}", avgFitness);
    }

    @Override
    public void finalResult(Evolution evolution, int g, Individual best, Pair<Long, Long> time, Pair<Double, Double> bestFitness, Pair<Double, Double> avgFitness, double external) {
        logger.log(Level.INFO, "final result of the evolution, generation: {0} best fitness: {1}", new Object[]{g, bestFitness});
    }

    @Override
    public void evolutionAlgorithmChanged(ActionEvent evt) {
        //
    }

    @Override
    public void startEvolution(ActionEvent evt, String evolution) {
        if (dataset != null) {
            //start evolution
            EvolutionFactory ef = EvolutionFactory.getInstance();
            Evolution alg = ef.getProvider(evolution);
            if (alg != null) {
                toolbar.evolutionStarted();
                alg.setDataset(dataset);
                alg.setGenerations(toolbar.getGenerations());
                alg.setAlgorithm(new KMeans(3, 100));

                InternalEvaluatorFactory fact = InternalEvaluatorFactory.getInstance();
                alg.setEvaluator(fact.getDefault());
                alg.addEvolutionListener(this);
                final ProgressHandle ph = ProgressHandleFactory.createHandle("Evolution");
                alg.setProgressHandle(ph);

                //childern node will get all clustering results
                //ClusteringChildren children = new ClusteringChildren(alg);
                comparator = new ClustComparator(new AICScore());
                ClustSorted children = new ClustSorted(alg);
                children.setComparator(comparator);

                root = new AbstractNode(children);
                root.setDisplayName("root node");
                mgr.setRootContext(root);
                logger.log(Level.INFO, "starting evolution...");
                task = RP.create(alg);
                task.addTaskListener(this);
                task.schedule(0);
            }
        }
    }

}
