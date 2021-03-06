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
package org.clueminer.importer.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.clueminer.gui.BusyUtils;
import org.clueminer.importer.FileImporterFactory;
import org.clueminer.importer.ImportController;
import org.clueminer.importer.Issue;
import org.clueminer.io.importer.api.AttributeDraft;
import org.clueminer.io.importer.api.Container;
import org.clueminer.io.importer.api.Report;
import org.clueminer.processor.spi.Processor;
import org.clueminer.processor.spi.ProcessorFactory;
import org.clueminer.processor.spi.ProcessorUI;
import org.clueminer.spi.AnalysisListener;
import org.clueminer.spi.FileImporter;
import org.clueminer.spi.ImportListener;
import org.clueminer.spi.Importer;
import org.clueminer.spi.ImporterUI;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author deric
 */
public class ReportPanel extends javax.swing.JPanel implements AnalysisListener, ImportListener {

    private static final long serialVersionUID = 1692175812146977202L;
    //Preferences
    private final static String SHOW_ISSUES = "ReportPanel_Show_Issues";
    private final static String SHOW_REPORT = "ReportPanel_Show_Report";
    private final static int ISSUES_LIMIT = 5000;
    private ThreadGroup fillingThreads;
    //Icons
    private ImageIcon infoIcon;
    private ImageIcon warningIcon;
    private ImageIcon severeIcon;
    private ImageIcon criticalIcon;
    //Container
    private Container container;
    //UI
    private final ButtonGroup processorGroup = new ButtonGroup();
    private Outline issuesOutline;
    protected LinkedHashMap<String, FileImporter> providers;
    private final ImportController controller;
    private FileImporter fileImporter;
    private ImporterUI importerUI;
    private GridBagConstraints gbc;
    private ColumnsPreview colPreviewPane;
    private DataTableModel dataTableModel;
    private static final Logger LOG = LoggerFactory.getLogger(ReportPanel.class);
    private FileObject currentFile;
    private static final RequestProcessor RP = new RequestProcessor("Preloading file");
    private CountDownLatch initLatch = new CountDownLatch(1);

    /**
     * Creates new form ReportPanel
     */
    public ReportPanel() {
        fillingThreads = new ThreadGroup("Report Panel Issues");
        controller = Lookup.getDefault().lookup(ImportController.class);
        //controller = new ImportControllerImpl();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                issuesOutline = new org.netbeans.swing.outline.Outline();
                initComponents();
                tab1ScrollPane.setViewportView(issuesOutline);
                initIcons();
                initPreview();
                initImporters();
                initProcessorsUI();
                initLatch.countDown();
            }
        });

    }

    public void initIcons() {
        infoIcon = new javax.swing.ImageIcon(getClass().getResource("/org/clueminer/importer/gui/info.png"));
        warningIcon = new javax.swing.ImageIcon(getClass().getResource("/org/clueminer/importer/gui/warning.gif"));
        severeIcon = new javax.swing.ImageIcon(getClass().getResource("/org/clueminer/importer/gui/severe.png"));
        criticalIcon = new javax.swing.ImageIcon(getClass().getResource("/org/clueminer/importer/gui/critical.png"));
    }

    public void setData(Container container) {
        this.container = container;
        if (container.getReport() != null) {
            Report report = container.getReport();
            //currentReader = container.getLoader().getLineReader();
            report.pruneReport(ISSUES_LIMIT);
            try {
                initLatch.await();
                fillIssues(report);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            fillReport(report);
        }

        fillStats(container);
        fillParameters(container);
    }

    private void fillIssues(Report report) {
        final List<Issue> issues = report.getIssues();
        if (issues.isEmpty()) {
            JLabel label = new JLabel(NbBundle.getMessage(getClass(), "ReportPanel.noIssues"));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            tab1ScrollPane.setViewportView(label);
        } else {
            //Busy label
            final BusyUtils.BusyLabel busyLabel = BusyUtils.createCenteredBusyLabel(tab1ScrollPane, "Retrieving issues...", issuesOutline);

            //Thread
            Thread thread = new Thread(fillingThreads, new Runnable() {
                @Override
                public void run() {
                    busyLabel.setBusy(true);
                    final TreeModel treeMdl = new IssueTreeModel(issues);
                    final OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, new IssueRowModel(), true);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            issuesOutline.setRootVisible(false);
                            issuesOutline.setRenderDataProvider((RenderDataProvider) new IssueRenderer());
                            issuesOutline.setModel(mdl);
                            busyLabel.setBusy(false);
                        }
                    });
                }
            }, "Report Panel Issues Outline");
            if (NbPreferences.forModule(ReportPanel.class).getBoolean(SHOW_ISSUES, true)) {
                thread.start();
            }
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public String[] getImporterProviders() {
        return FileImporterFactory.getInstance().getProvidersArray();
    }

    public String[] initProcessorProviders() {
        return ProcessorFactory.getInstance().getProvidersArray();
    }

    private void initImporters() {
        dataTableModel = new DataTableModel();
        dataTableModel.setTable(dataTable);
        dataTable.setModel(dataTableModel);
        importerPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 20, 0, 20);

        cbImporter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //importer selected
                comboImporterChanged();
            }
        });
    }

    private void initPreview() {
        columnsPreview.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 20, 0, 20);

        colPreviewPane = new ColumnsPreview();
        columnsPreview.add(colPreviewPane);
        columnsPreview.validate();
        columnsPreview.revalidate();
        columnsPreview.repaint();

    }

    private void comboImporterChanged() {
        if (cbImporter.getSelectedItem() != null) {
            FileImporter fi = FileImporterFactory.getInstance().getProvider((String) cbImporter.getSelectedItem());
            if (fi != null) {
                if (!fileImporter.getName().equals(cbImporter.getSelectedItem().toString())) {
                    fileImporterChanged(fi);
                }
            }
        }
    }

    public void fileImporterChanged(FileImporter importer) {
        if (importerUI != null) {
            importerUI.unsetup(false);
            importerUI.removeListener(colPreviewPane);
            importerUI.removeListener(this);
            importerPanel.removeAll();
        }
        if (fileImporter != null) {
            fileImporter.removeListener(dataTableModel);
            fileImporter.removeListener(this);
        }
        fileImporter = importer;
        fileImporter.addAnalysisListener(dataTableModel);
        fileImporter.addAnalysisListener(this);
        LOG.info("new file importer: {}", fileImporter.getName());
        cbImporter.setSelectedItem(fileImporter.getName());
        if (controller != null) {
            importerUI = controller.getUI(importer);
            if (importerUI != null) {
                JPanel panel = importerUI.getPanel();
                importerUI.setup(importer);
                importerUI.addListener(colPreviewPane);
                importerUI.addListener(this);
                importerUI.fireImporterChanged();

                importerPanel.add(panel, gbc);
                importerPanel.validate();
                importerPanel.revalidate();
                importerPanel.repaint();
            } else {
                LOG.warn("importer UI missing");
            }
        } else {
            LOG.error("no controller found");
        }

        dataTableModel.setContainer(container);

        dataTableModel.fireTableDataChanged();
        repaint();
        columnsPreview.validate();
        columnsPreview.revalidate();
        columnsPreview.repaint();
    }

    private void fillReport(final Report report) {
        Thread thread = new Thread(fillingThreads, new Runnable() {
            @Override
            public void run() {
                final String str = report.getText();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        reportEditor.setText(str);
                    }
                });
            }
        }, "Report Panel Issues Report");
        if (NbPreferences.forModule(ReportPanel.class).getBoolean(SHOW_REPORT, true)) {
            thread.start();
        }
        try {
            thread.join();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void fillParameters(final Container container) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //get number of lines etc.

            }
        });
    }

    private void fillStats(final Container container) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Source
                String source = container.getSource();
                if (source != null) {
                    String[] label = source.split("\\.");
                    if (label.length > 2 && label[label.length - 2].matches("\\d+")) { //case of temp file
                        source = source.replaceFirst("." + label[label.length - 2], "");
                    }
                }

                sourceLabel.setText(source);
                lbNumLines.setText(String.valueOf(container.getInstanceCount()));
                lbAttr.setText(String.valueOf(container.getAttributeCount()));
            }
        });
    }

    private void initProcessorsUI() {
        Processor p = getProcessor();
        //Enabled
        ProcessorUI pui = getProcessorUI(p);
        if (pui != null) {
            processorPanel.add(pui.getPanel());
        }
    }

    public void destroy() {
        if (fileImporter != null) {
            fileImporter.removeListener(dataTableModel);
            fileImporter.removeListener(this);
        }
        currentFile = null;
        colPreviewPane.dataLoaded();
    }

    public Processor getProcessor() {
        ProcessorFactory pf = ProcessorFactory.getInstance();
        Object selected = cbDataType.getSelectedItem();
        if (selected != null) {
            return pf.getProvider((String) selected);
        }
        return null;
    }

    private ProcessorUI getProcessorUI(Processor processor) {
        for (ProcessorUI pui : Lookup.getDefault().lookupAll(ProcessorUI.class)) {
            if (pui.isUIFoProcessor(processor)) {
                return pui;
            }
        }
        return null;
    }

    public void setCurrentFile(FileObject currentFile) {
        this.currentFile = currentFile;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        tab1ScrollPane = new javax.swing.JScrollPane();
        tab2ScrollPane = new javax.swing.JScrollPane();
        reportEditor = new javax.swing.JEditorPane();
        lbSource = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        statsPanel = new javax.swing.JPanel();
        lbLines = new javax.swing.JLabel();
        lbNumLines = new javax.swing.JLabel();
        lbAttributes = new javax.swing.JLabel();
        lbAttr = new javax.swing.JLabel();
        processorPanel = new javax.swing.JPanel();
        lbImport = new javax.swing.JLabel();
        cbImporter = new JComboBox(getImporterProviders());
        jScrollPane1 = new javax.swing.JScrollPane();
        columnsPreview = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cbDataType = new JComboBox(initProcessorProviders());
        importerPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dataTable = new javax.swing.JTable();

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.tab1ScrollPane.TabConstraints.tabTitle"), tab1ScrollPane); // NOI18N

        tab2ScrollPane.setViewportView(reportEditor);

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.tab2ScrollPane.TabConstraints.tabTitle"), tab2ScrollPane); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbSource, org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.lbSource.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(sourceLabel, org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.sourceLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbLines, org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.lbLines.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbNumLines, org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.lbNumLines.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbAttributes, org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.lbAttributes.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbAttr, org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.lbAttr.text")); // NOI18N

        javax.swing.GroupLayout statsPanelLayout = new javax.swing.GroupLayout(statsPanel);
        statsPanel.setLayout(statsPanelLayout);
        statsPanelLayout.setHorizontalGroup(
            statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(statsPanelLayout.createSequentialGroup()
                        .addComponent(lbAttributes)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbAttr)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(statsPanelLayout.createSequentialGroup()
                        .addComponent(lbLines)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lbNumLines)))
                .addContainerGap())
        );
        statsPanelLayout.setVerticalGroup(
            statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbLines)
                    .addComponent(lbNumLines))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbAttributes)
                    .addComponent(lbAttr))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout processorPanelLayout = new javax.swing.GroupLayout(processorPanel);
        processorPanel.setLayout(processorPanelLayout);
        processorPanelLayout.setHorizontalGroup(
            processorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 347, Short.MAX_VALUE)
        );
        processorPanelLayout.setVerticalGroup(
            processorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(lbImport, org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.lbImport.text")); // NOI18N

        javax.swing.GroupLayout columnsPreviewLayout = new javax.swing.GroupLayout(columnsPreview);
        columnsPreview.setLayout(columnsPreviewLayout);
        columnsPreviewLayout.setHorizontalGroup(
            columnsPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1135, Short.MAX_VALUE)
        );
        columnsPreviewLayout.setVerticalGroup(
            columnsPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 388, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(columnsPreview);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.jLabel1.text")); // NOI18N

        cbDataType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbDataTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout importerPanelLayout = new javax.swing.GroupLayout(importerPanel);
        importerPanel.setLayout(importerPanelLayout);
        importerPanelLayout.setHorizontalGroup(
            importerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 323, Short.MAX_VALUE)
        );
        importerPanelLayout.setVerticalGroup(
            importerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 84, Short.MAX_VALUE)
        );

        dataTable.setModel(new org.clueminer.importer.gui.DataTableModel());
        jScrollPane2.setViewportView(dataTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lbSource)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sourceLabel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lbImport)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbImporter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(144, 144, 144)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbDataType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(statsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(265, 265, 265)
                                .addComponent(processorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(importerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbSource)
                    .addComponent(sourceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbImport)
                    .addComponent(cbImporter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(cbDataType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(processorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(importerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbDataTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbDataTypeActionPerformed
        if (container != null) {
            //loader.setDataType((String) cbDataType.getSelectedItem());
            initProcessorsUI();
        }

    }//GEN-LAST:event_cbDataTypeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbDataType;
    private javax.swing.JComboBox cbImporter;
    private javax.swing.JPanel columnsPreview;
    private javax.swing.JTable dataTable;
    private javax.swing.JPanel importerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lbAttr;
    private javax.swing.JLabel lbAttributes;
    private javax.swing.JLabel lbImport;
    private javax.swing.JLabel lbLines;
    private javax.swing.JLabel lbNumLines;
    private javax.swing.JLabel lbSource;
    private javax.swing.JPanel processorPanel;
    private javax.swing.JEditorPane reportEditor;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JScrollPane tab1ScrollPane;
    private javax.swing.JScrollPane tab2ScrollPane;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void analysisFinished(Container box) {
        if (box != null) {
            setData(box);
        }
    }

    @Override
    public void importerChanged(final Importer importer, final ImporterUI importerUI) {
        LOG.info("current importer: {}", importer.getName());
        Container cont = importer.getContainer();

        //import is executed asynchronously, we might not have container immediately
        if (cont == null) {
            LOG.error("container is null!");
            if (cont == null) {
                LOG.error("reimport");
                Callable<Container> c = new Callable<Container>() {
                    @Override
                    public Container call() throws Exception {
                        FileImporter fi = (FileImporter) importer;
                        Container cont = controller.importFile(currentFile, currentFile.getInputStream(), fi, true);
                        LOG.info("container for {}", cont.getFile().getName());
                        setData(cont);
                        LOG.info("finished loading data with {}", importer.getName());

                        return cont;
                    }

                }; //triggerReimport((FileImporter) importer, null);
                Future<Container> fc = RP.submit(c);
                try {
                    cont = fc.get();
                } catch (InterruptedException | ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
                LOG.info("container instances {}", cont.getInstanceCount());
                this.container = cont;
            }
            return;
        }

        this.validate();
        this.revalidate();
        this.repaint();
    }

    @Override
    public void dataLoaded() {
        LOG.info("data loaded");
        if (fileImporter != null) {
            fileImporter.removeListener(dataTableModel);
            fileImporter.removeListener(this);
        }
        currentFile = null;
    }

    @Override
    public void attributeChanged(AttributeDraft attr, Object property) {
        if (colPreviewPane != null) {
            //update GUI
            LOG.info("attr {} changed prop ={}", attr.getName(), property);
            colPreviewPane.attributeChanged(attr);
        }
    }

    private class IssueTreeModel implements TreeModel {

        private final List<Issue> issues;

        public IssueTreeModel(List<Issue> issues) {
            this.issues = issues;
        }

        @Override
        public Object getRoot() {
            return "root";
        }

        @Override
        public Object getChild(Object parent, int index) {
            return issues.get(index);
        }

        @Override
        public int getChildCount(Object parent) {
            return issues.size();
        }

        @Override
        public boolean isLeaf(Object node) {
            return node instanceof Issue;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return issues.indexOf(child);
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
        }
    }

    private class IssueRowModel implements RowModel {

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueFor(Object node, int column) {
            if (node instanceof Issue) {
                Issue issue = (Issue) node;
                return issue.getLevel().toString();
            }
            return "";
        }

        @Override
        public Class getColumnClass(int column) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(Object node, int column) {
            return false;
        }

        @Override
        public void setValueFor(Object node, int column, Object value) {
        }

        @Override
        public String getColumnName(int column) {
            return NbBundle.getMessage(ReportPanel.class, "ReportPanel.issueTable.issues");
        }
    }

    private class IssueRenderer implements RenderDataProvider {

        @Override
        public String getDisplayName(Object o) {
            Issue issue = (Issue) o;
            return issue.getMessage();
        }

        @Override
        public boolean isHtmlDisplayName(Object o) {
            return false;
        }

        @Override
        public Color getBackground(Object o) {
            return null;
        }

        @Override
        public Color getForeground(Object o) {
            return null;
        }

        @Override
        public String getTooltipText(Object o) {
            return "";
        }

        @Override
        public Icon getIcon(Object o) {
            Issue issue = (Issue) o;
            switch (issue.getLevel()) {
                case INFO:
                    return infoIcon;
                case WARNING:
                    return warningIcon;
                case SEVERE:
                    return severeIcon;
                case CRITICAL:
                    return criticalIcon;
            }
            return null;
        }
    }

}
