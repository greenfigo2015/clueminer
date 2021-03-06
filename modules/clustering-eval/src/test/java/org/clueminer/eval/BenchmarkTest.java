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
package org.clueminer.eval;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.ArrayDataSet;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.ExternalDataSetPlot;
import com.panayotis.gnuplot.plot.Graph;
import com.panayotis.gnuplot.style.ColorPalette;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.ImageTerminal;
import com.panayotis.iodebug.Debug;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import org.clueminer.clustering.algorithm.KMeans;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringAlgorithm;
import org.clueminer.clustering.api.InternalEvaluator;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dataset.benchmark.DatasetFixture;
import org.clueminer.dataset.impl.SampleDataset;
import org.clueminer.fixtures.CommonFixture;
import org.clueminer.io.arff.ARFFHandler;
import org.clueminer.io.csv.CsvLoader;
import org.clueminer.io.FileHandler;
import org.clueminer.utils.FileUtils;
import org.clueminer.utils.Props;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * To run this file you have to have a gnuplot packgage installed on your system
 *
 * @author Tomas Barton
 */
public class BenchmarkTest {

    private static Collection<? extends InternalEvaluator> evaluators;
    private static String benchmarkFolder;
    private static CommonFixture tf;
    private final Map<String, String> classColors = new HashMap<>();
    private int colorNum = 0;

    @BeforeClass
    public static void setUpClass() throws Exception {
        evaluators = Lookup.getDefault().lookupAll(InternalEvaluator.class);
        String home = System.getProperty("user.home") + File.separatorChar
                + NbBundle.getMessage(
                        FileUtils.class,
                        "FOLDER_Home");
        benchmarkFolder = home + File.separatorChar + "benchmark";
        File f = new File(benchmarkFolder);
        System.out.println("Writing output to " + f.toString());
        if (!f.exists()) {
            boolean success = (new File(benchmarkFolder)).mkdir();
            if (success) {
                System.out.println("Directory: " + benchmarkFolder + " created");
            }
        }
        tf = new CommonFixture();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private String createFolder(String name) {
        String dir = benchmarkFolder + File.separatorChar + name + File.separatorChar;
        boolean success = (new File(dir)).mkdir();
        if (success) {
            System.out.println("Directory: " + dir + " created");
        }
        return dir;
    }

    /**
     * @TODO rewrite tests, so that gnuplot is not needed
     *
     *
     * @param datasetName
     * @param kmin
     * @param kmax
     * @param results
     * @param kreal
     * @param dir
     * @throws IOException
     */
    private void plotResults(String datasetName, int kmin, int kmax, double[][] results, int kreal, String dir) throws IOException {
        int i = 0;
        for (InternalEvaluator c : evaluators) {
            System.out.println("evaluator " + c.getName());
            JavaPlot p = new JavaPlot();
            JavaPlot.getDebugger().setLevel(Debug.VERBOSE);
            //output format
            ImageTerminal term = new ImageTerminal();
            p.setTerminal(term);
            p.set("term", "png enhanced color font 'Helvetica',9");
            p.setTitle(c.getName() + " on " + datasetName + " dataset");
            p.getAxis("x").setLabel("number of clusters");
            p.getAxis("y").setLabel("score");
            //p.getAxis("x").setBoundaries(-30, 20);
            p.setKey(JavaPlot.Key.OFF);
            p.set("arrow", "from " + kreal + ", graph 0 to " + kreal + ", graph 1 nohead ls 4");
            p.set("xtics", "add ('k=" + kreal + "' " + kreal + ")");
            p.set("size", "1.0,1.0");
            p.set("origin", "0.0,0.0");
            //reformat data for plotting
            double[][] d = new double[kmax - kmin][2];
            for (int n = kmin; n < kmax; n++) {
                d[n - kmin][0] = n;
                d[n - kmin][1] = results[i][n - kmin];
            }
            p.addPlot(d);
            //p.setParameters(null);
            PlotStyle stl = ((AbstractPlot) p.getPlots().get(0)).getPlotStyle();
            stl.setStyle(Style.LINESPOINTS);
            //stl.setLineType(NamedPlotColor.GOLDENROD);
            stl.setPointType(7);
            stl.setPointSize(1);
            stl.setLineWidth(2);
            p.plot();

            BufferedImage image = term.getImage();
            String name = c.getName();
            name = name.toLowerCase().replace(" ", "_");
            ImageIO.write(image, "png", new File(dir + name + ".png"));

            //write GNU plot script to a file
            FileWriter fstream = new FileWriter(dir + name + ".dem");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(p.getCommands());
            out.close();

            i++;
        }
    }

    private String getClassColor(String clazzName) {
        if (classColors.containsKey(clazzName)) {
            return classColors.get(clazzName);
        }
        String color = ColorPalette.getColor(colorNum++);
        classColors.put(clazzName, color);
        return color;
    }

    private double[][] kMeans(Dataset data, int kmin, int kmax, String dir) throws IOException, Exception {
        double[][] results = new double[evaluators.size()][kmax - kmin];
        Props params = new Props();
        for (int n = kmin; n < kmax; n++) {
            long start = System.currentTimeMillis();
            ClusteringAlgorithm km = new KMeans();
            params.putInt("k", n);
            Clustering<Instance, Cluster<Instance>> clusters = km.cluster(data, params);
            long end = System.currentTimeMillis();
            System.out.println("measuring k = " + n + " took " + (end - start) + " ms");
            System.out.println("k = " + n);
            JavaPlot p = new JavaPlot();
            //JavaPlot.getDebugger().setLevel(Debug.VERBOSE);
            //output format
            ImageTerminal term = new ImageTerminal();
            p.setTerminal(term);
            p.set("term", "png enhanced font 'Vera,9' size 1024, 1024");
            //p.setMultiTitle(n + " dataset");
            p.getAxis("x").setLabel("number of clusters");
            p.getAxis("y").setLabel("score");
            //p.getAxis("x").setBoundaries(-30, 20);
            p.setKey(JavaPlot.Key.OFF);
            p.set("size", "1.0,1.0");
            p.set("origin", "0.0,0.0");
            /*
             * p.set("rmargin", "2.5"); p.set("lmargin", "5.0");
             * p.set("tmargin", "2.5"); p.set("bmargin", "3.0");
             */
            //number of plots = n * (n -1 ) /2
            int cols = 2;
            int plotsNumber = data.attributeCount() * (data.attributeCount() - 1) / 2;
            int rows = plotsNumber / cols;
            //p.set("size", "square 0.5, 0.5");
            //we need to generate combinations of all dimensions to 2D (for start)
            int graphCount = 0;
            float xDimension = 1 / (float) cols;
            float yDimension = 1 / (float) rows;
            String dataDir = dir + "data" + File.separatorChar;
            (new File(dataDir)).mkdir();
            float x, y, w, h;
            boolean eachClassDifferentColor = false;
            for (int m = 0; m < data.attributeCount(); m++) {
                for (int l = 0; l < m; l++) {
                    Graph gr = new Graph();
                    x = ((graphCount % cols) * xDimension);
                    y = ((graphCount / cols) * yDimension);
                    w = xDimension;
                    h = yDimension;
                    gr.setMetrics(x, y, w, h);
                    //going through all clusters
                    int clust = 0;
                    for (Cluster cluster : clusters) {
                        //  System.out.println(inst);
                        //     p.set("arrow", "from " + kreal + ", graph 0 to " + kreal + ", graph 1 nohead ls 4");
                        //     p.set("xtics", "add ('k=" + kreal + "' " + kreal + ")");
                        //reformat data for plotting
                        double[][] d = new double[cluster.size()][2];
                        String[] labels = new String[cluster.size()];
                        for (int i = 0; i < cluster.size(); i++) {
                            d[i][0] = cluster.instance(i).value(m);
                            d[i][1] = cluster.instance(i).value(l);
                            labels[i] = (String) cluster.instance(i).classValue();
                        }
                        ArrayDataSet dat = new ArrayDataSet(d);
                        dat.setLabels(labels);

                        String filename = dataDir + data.getName() + "-" + n + "-" + m + "-" + l + "c" + clust + ".data";
                        dat.save(filename);
                        if (eachClassDifferentColor) {
                            for (Object o : cluster.getClasses()) {
                                ExternalDataSetPlot subPlot = new ExternalDataSetPlot("< awk '{if($3 == \\\"" + o.toString() + "\\\") print}' " + filename);
                                subPlot.set("w p pt " + (clust + 1) + " lc rgb \"" + getClassColor(o.toString()) + "\""); //
                                gr.addPlot(subPlot);
                            }
                        } else {
                            ExternalDataSetPlot subPlot = new ExternalDataSetPlot(filename);
                            subPlot.set("w p pt " + (clust + 1) + " lc rgb \"" + ColorPalette.getColor(clust) + "\"");
                            gr.addPlot(subPlot);
                        }

                        //save values to a file
                        //subPlot.setTitle("iris " + m + ":" + l);
                        //subPlot.setSmooth(Smooth.UNIQUE);
                        gr.getAxis("x").setLabel(data.getAttribute(m).getName());
                        gr.getAxis("y").setLabel(data.getAttribute(l).getName());

                        //PlotStyle stl = ((AbstractPlot) p.getPlots().instance(0)).getPlotStyle();
                        //stl.setStyle(Style.DOTS);
                        //stl.setLineType(NamedPlotColor.GOLDENROD); stl.setPointType(2);
                        //stl.setPointSize(2);
                        clust++;
                    }
                    p.addGraph(gr);
                    graphCount++;
                }
            }

            /*
             * AutoGraphLayout lo = new AutoGraphLayout(); lo.setColumns(2);
             * p.getPage().setLayout(lo);
             */
            p.plot();
            String name = String.valueOf(n);
            name = name.toLowerCase().replace(" ", "_");

            //write GNU plot script to a file
            FileWriter fstream = new FileWriter(dataDir + name + ".dem");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(p.getCommands());
            out.close();

            BufferedImage image = term.getImage();
            ImageIO.write(image, "png", new File(dir + name + "-kmeans" + ".png"));

            double score;
            int j = 0;
            for (InternalEvaluator c : evaluators) {
                score = c.score(clusters);
                results[j++][n - kmin] = score;
            }
            System.out.println("===========");
        }
        return results;
    }

    private void runExperiment(String datasetName, Dataset data, int kmin, int kmax, int kreal) throws IOException, Exception {
        String dir = createFolder(datasetName);
        long start = System.currentTimeMillis();
        double[][] results = kMeans(data, kmin, kmax, dir);
        long end = System.currentTimeMillis();
        System.out.println("measuring " + datasetName + " took " + (end - start) + " ms");
        plotResults(datasetName, kmin, kmax, results, kreal, dir);
    }

    @Test
    public void testPlotting() throws IOException, Exception {
        String dir = createFolder("iris");
        int kmin = 2;
        //max k we test
        int kmax = 10;
        assertTrue(kmin == 2);
        //double[][] results = kMeans(DatasetFixture.iris(), kmin, kmax, dir);
    }

    //@Test
    public void testIris() throws IOException, Exception {
        String datasetName = "iris";
        Dataset data = new SampleDataset();
        ARFFHandler arff = new ARFFHandler();
        arff.load(tf.irisArff(), data, 4);
        assertTrue(150 == data.size());
        int kmin = 2;
        //max k we test
        int kmax = 15;
        int kreal = 3;
        runExperiment(datasetName, data, kmin, kmax, kreal);
    }

    //@Test
    public void testWellSeparated() throws IOException, Exception {
        String datasetName = "well-separated";
        Dataset data = new SampleDataset();
        CsvLoader csv = new CsvLoader();
        csv.setClassIndex(2);
        csv.setDataset(data);
        csv.load(tf.wellSeparatedCsv(), data);
        assertTrue(1777 == data.size());
        int kmin = 2;
        //max k we test
        int kmax = 6;
        int kreal = 5;
        System.out.println("starting experiment");
        runExperiment(datasetName, data, kmin, kmax, kreal);
    }

    //@Test
    public void testWine() throws IOException, Exception {
        String datasetName = "wine";
        // 1st attribute is class identifier (1-3)
        Dataset data = new SampleDataset();
        FileHandler.loadDataset(tf.irisData(), data, 1, ",");
        int kmin = 2;
        //max k we test
        int kmax = 15;
        int kreal = 3;
        runExperiment(datasetName, data, kmin, kmax, kreal);
    }

    //  @Test
    public void testYeast() throws IOException, Exception {
        String datasetName = "yeast";
        int kmin = 2;
        //max k we test
        int kmax = 15;
        int kreal = 10;
        runExperiment(datasetName, DatasetFixture.yeast(), kmin, kmax, kreal);
    }
}
