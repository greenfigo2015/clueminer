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
package org.clueminer.export.newick;

import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.clueminer.clustering.api.dendrogram.DendrogramMapping;
import org.clueminer.clustering.gui.ClusteringExportGui;
import org.clueminer.export.impl.ClusteringExporter;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Barton
 */
@ServiceProvider(service = ClusteringExportGui.class)
public class NewickExporter extends ClusteringExporter implements ClusteringExportGui {

    public static final String title = "Export to Newick";
    public static final String ext = ".nwk";
    private NewickOptions options;

    public NewickExporter() {
    }

    @Override
    public JPanel getOptions() {
        if (options == null) {
            options = new NewickOptions();
        }
        return options;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void updatePreferences(Preferences p) {
        options.updatePreferences(p);
    }

    @Override
    public FileFilter getFileFilter() {
        if (fileFilter == null) {
            fileFilter = new FileFilter() {

                @Override
                public boolean accept(File file) {
                    String filename = file.getName();
                    return file.isDirectory() || filename.endsWith(ext);
                }

                @Override
                public String getDescription() {
                    return "Newick (*.nwk)";
                }
            };
        }
        return fileFilter;
    }

    @Override
    public String getExtension() {
        return ext;
    }

    @Override
    public Runnable getRunner(File file, DendrogramMapping mapping, Preferences pref, ProgressHandle ph) {
        return new NewickExportRunner(file, mapping, pref, ph);
    }

    @Override
    public boolean hasData() {
        return mapping != null || clustering != null;
    }

}
