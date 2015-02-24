package org.clueminer.properties;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.clueminer.properties//Properties//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "PropertiesTopComponent",
        iconBase = "org/clueminer/properties/properties16.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = true)
@ActionID(category = "Window", id = "org.clueminer.properties.PropertiesTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_PropertiesAction",
        preferredID = "PropertiesTopComponent"
)
@Messages({
    "CTL_PropertiesAction=Properties",
    "CTL_PropertiesTopComponent=Properties",
    "HINT_PropertiesTopComponent=Information about clustering"
})
public final class PropertiesTopComponent extends TopComponent implements LookupListener {

    private Lookup.Result<AbstractNode> result = null;
    private static final Logger logger = Logger.getLogger(PropertiesTopComponent.class.getName());
    private final PropPanel panel;
    private final JScrollPane scrollPane = null;

    public PropertiesTopComponent() {
        initComponents();
        setName(Bundle.CTL_PropertiesTopComponent());
        setToolTipText(Bundle.HINT_PropertiesTopComponent());
        panel = new PropPanel();
        /*     scrollPane = new JScrollPane();
         scrollPane.setLayout(new GridBagLayout());
        scrollPane.getViewport().add(panel);*/
        add(panel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
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
        result = Utilities.actionsGlobalContext().lookupResult(AbstractNode.class);
        result.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
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
        if (result != null) {
            Collection<? extends AbstractNode> res = result.allInstances();
            panel.setNodes(res);
            if (res.size() == 1) {
                setName(res.iterator().next().getName() + " - " + Bundle.CTL_PropertiesTopComponent());
            }
        }
    }
}
