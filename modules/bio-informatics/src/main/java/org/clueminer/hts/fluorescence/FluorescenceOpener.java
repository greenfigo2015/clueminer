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
package org.clueminer.hts.fluorescence;

import eu.medsea.mimeutil.MimeUtil2;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.dendrogram.DendroViewTopComponent;
import org.clueminer.hts.api.HtsInstance;
import org.clueminer.hts.api.HtsPlate;
import org.clueminer.openfile.OpenFileImpl;
import org.clueminer.project.api.Project;
import org.clueminer.project.api.Workspace;
import org.clueminer.project.impl.ProjectControllerImpl;
import org.clueminer.project.impl.ProjectImpl;
import org.clueminer.project.impl.ProjectInformationImpl;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tomas Barton
 */
@org.openide.util.lookup.ServiceProvider(service = org.clueminer.openfile.OpenFileImpl.class, position = 60)
public class FluorescenceOpener implements OpenFileImpl, TaskListener {

    private MimeUtil2 mimeUtil = new MimeUtil2();
    private static final RequestProcessor RP = new RequestProcessor("non-interruptible tasks", 1, false);
    private static Project project;
    private FluorescenceImporter importer;
    private static final Logger LOG = LoggerFactory.getLogger(FluorescenceOpener.class);

    public FluorescenceOpener() {
        //MIME type detection
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
    }

    protected Collection detectMIME(File file) {
        Collection mimeTypes = null;
        try {
            byte[] data;
            InputStream in = new FileInputStream(file);
            data = new byte[1024];
            in.read(data, 0, 1024);
            in.close();
            mimeTypes = mimeUtil.getMimeTypes(data);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return mimeTypes;
    }

    @Override
    public boolean open(FileObject fileObject) {
        File f = FileUtil.toFile(fileObject);
        return openFile(f);
    }

    protected boolean openFile(File f) {
        Collection mimeTypes = detectMIME(f);
        if (mimeTypes.contains("text/x-tex")) {
            String line;
            BufferedReader br;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                line = br.readLine();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }

            if ("% Fluorescence version 1.0".equals(line)) {
                importer = new FluorescenceImporter(f);
                openFluorescenceFile(importer);
                return true;
            }
        }

        return false;
    }

    protected void openFluorescenceFile(FluorescenceImporter importer) {
        ProgressHandle ph = ProgressHandle.createHandle("Opening file " + importer.getFile().getName());
        importer.setProgressHandle(ph);
        //Project instance
        project = new ProjectImpl();
        project.getLookup().lookup(ProjectInformationImpl.class).setFile(importer.getFile());
        final RequestProcessor.Task task = RP.create(importer);
        task.addTaskListener(this);
        task.schedule(0);
    }

    @Override
    public void taskFinished(Task task) {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                System.out.println("opening task finished");
                ProjectControllerImpl pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
                project.add(importer.getDataset());
                DendroViewTopComponent<HtsInstance, Cluster<HtsInstance>> tc = new DendroViewTopComponent();
                HtsPlate<HtsInstance> plate = importer.getDataset();

                /* Normalization norm = new QuadruplicateNormalization();
                 * HtsPlate<HtsInstance> normalized = (HtsPlate<HtsInstance>) plate.duplicate();
                 * norm.normalize(plate, normalized);
                 *
                 * /* saveDataset(plate, "import", false);
                 * saveDataset(normalized, "norm", true);
                 */
                //tc.setDataset(plate);
                //tc.setDataset(plate);
                //tc.setProject(project);
                tc.setDisplayName(plate.getName());
                tc.open();
                tc.requestActive();

                pc.openProject(project);
                Workspace workspace = pc.getCurrentWorkspace();
                if (workspace != null) {
                    System.out.println("workspace: " + workspace.toString());
                    System.out.println("adding plate to lookup");
                    workspace.add(importer.getDataset());  //add plate to project's lookup
                } else {
                    System.out.println("workspace is null!!!!");
                }

                //     DataPreprocessing preprocess = new DataPreprocessing(plate, tc);
                //     preprocess.start();
            }
        });
    }

    public void saveDataset(HtsPlate<HtsInstance> plate, String ident, boolean normalized) {
        String filename = System.getProperty("user.home") /*
                 * FileUtils.LocalFolder()
                 */ + "/" + "david-" + plate.getName() + "-" + ident + ".csv";

        String separator = ",";
        String eol = "\n";
        double value;

        try {
            System.out.println("writing to " + filename);
            //header
            /* writer.append(separator);
             * for (String s : paramNames) {
             * writer.append(s).append(separator);
             * }
             * writer.append(eol); */
            //content
            try (FileWriter writer = new FileWriter(filename)) {
                //header
                /* writer.append(separator);
                 * for (String s : paramNames) {
                 * writer.append(s).append(separator);
                 * }
                 * writer.append(eol); */
                //content
                HtsInstance current;
                String sampleName;
                LOG.info("export size {}", plate.size());

                if (normalized) {
                    for (int i = 0; i < plate.size(); i++) {
                        current = plate.instance(i);
                        if (current.getColumn() < 46) {
                            sampleName = current.getName();
                            writer.append(sampleName).append(separator);

                            for (int j = 0; j < plate.attributeCount(); j++) {
                                value = plate.get(i, j);
                                writer.append(String.valueOf(value)).append(separator);
                            }
                            writer.append(eol);
                        }
                    }
                } else {
                    for (int i = 0; i < plate.size(); i++) {
                        current = plate.instance(i);
                        sampleName = current.getName();
                        writer.append(sampleName).append(separator);
                        for (int j = 0; j < plate.attributeCount(); j++) {
                            value = plate.get(i, j);
                            writer.append(String.valueOf(value)).append(separator);
                        }
                        writer.append(eol);
                    }

                }

                writer.flush();
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }
}
