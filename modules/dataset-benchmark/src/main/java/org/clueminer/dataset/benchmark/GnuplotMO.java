/*
 * Copyright (C) 2015 clueminer.org
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
package org.clueminer.dataset.benchmark;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import org.clueminer.clustering.api.ClusterEvaluation;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.evolution.api.Evolution;
import org.clueminer.evolution.api.EvolutionMO;
import org.clueminer.oo.api.OpListener;
import org.clueminer.oo.api.OpSolution;
import org.clueminer.utils.DatasetWriter;
import org.openide.util.Exceptions;

/**
 * Class for generating Gnuplot scripts. It collects results from multiple
 * evolution runs.
 *
 * @author deric
 */
public class GnuplotMO extends GnuplotHelper implements OpListener {

    private EvolutionMO evolution;
    private LinkedList<String> plots;

    public GnuplotMO() {
        plots = new LinkedList<>();
    }

    @Override
    public void started(Evolution evolution) {
        this.evolution = (EvolutionMO) evolution;
    }

    private String createName(EvolutionMO evo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < evo.getNumObjectives(); i++) {
            if (i > 0) {
                sb.append("-");
            }
            sb.append(((ClusterEvaluation) evo.getObjectives().get(i)).getName());
        }
        return safeName(sb.toString());
    }

    private String gptDir() {
        return getCurrentDir() + File.separatorChar + "gpt";
    }

    private String dataDir() {
        return getCurrentDir() + File.separatorChar + "data";
    }

    /**
     * Result from single evolution run
     *
     * @param result
     */
    @Override
    public void finalResult(List<OpSolution> result) {
        String expName = createName(evolution);
        mkdir(dataDir());
        mkdir(gptDir());
        String dataFile = writeData(expName, dataDir(), result);

        writeGnuplot(gptDir(), expName, gnuplotParetoFront(dataFile, evolution.getObjective(0), evolution.getObjective(1)));
        plots.add(expName);

        writeBashScripts();
    }

    public void writeBashScripts() {

        try {
            bashPlotScript(plots.toArray(new String[plots.size()]), getCurrentDir(), "set term pdf font 'Times-New-Roman,8'", "pdf");
            bashPlotScript(plots.toArray(new String[plots.size()]), getCurrentDir(), "set terminal pngcairo size 800,600 enhanced font 'Verdana,10'", "png");

        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            System.err.println("failed to write gnuplot scripts");
            Exceptions.printStackTrace(ex);
        }
    }

    private String writeData(String ident, String dataDir, List<OpSolution> result) {
        PrintWriter writer = null;
        String dataFile = ident + ".csv";
        try {
            writer = new PrintWriter(dataDir + File.separatorChar + dataFile, "UTF-8");
            CSVWriter csv = new CSVWriter(writer, ',');
            toCsv(csv, result);
            writer.close();

        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return dataFile;
    }

    public void toCsv(DatasetWriter writer, List<OpSolution> result) {
        int offset = 3;
        String[] header = new String[evolution.getNumObjectives() + offset];
        header[0] = "k";
        header[1] = "fingerprint";
        header[2] = evolution.getExternal().getName();
        List<ClusterEvaluation> objectives = evolution.getObjectives();
        for (int i = 0; i < evolution.getNumObjectives(); i++) {
            header[i + offset] = objectives.get(i).getName();

        }
        writer.writeNext(header);
        Clustering clust;
        String[] line = new String[header.length];
        for (OpSolution solution : result) {
            clust = solution.getClustering();
            line[0] = String.valueOf(clust.size());
            line[1] = clust.fingerprint();
            line[2] = String.valueOf(clust.getEvaluationTable().getScore(evolution.getExternal()));
            for (int i = 0; i < objectives.size(); i++) {
                line[i + offset] = String.valueOf(solution.getObjective(i));
            }

            writer.writeNext(line);
        }
    }

    private String getTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(evolution.getAlgorithm().getName());
        sb.append(" generations: ").append(evolution.getGenerations());
        sb.append(" population: ").append(evolution.getPopulationSize());
        sb.append(" crossover: ").append(evolution.getCrossoverProbability());
        sb.append(" mutation: ").append(evolution.getMutationProbability());
        return sb.toString();
    }

    /**
     *
     * @param dataFile
     * @param c1
     * @param c2
     * @return
     */
    private String gnuplotParetoFront(String dataFile, ClusterEvaluation c1, ClusterEvaluation c2) {
        String res = "set title '" + getTitle() + "'\n"
                + "set grid \n"
                + "set size 1.0, 1.0\n"
                + "set key outside bottom horizontal box\n"
                + "set datafile separator \",\"\n"
                + "set datafile missing \"NaN\"\n"
                + "set ylabel '" + c1.getName() + "'\n"
                + "set xlabel \"" + c2.getName() + "\"\n"
                + "plot '" + "data" + File.separatorChar + dataFile
                + "' u 4:5 title 'pareto front' with points pointtype 7 pointsize 0.7";

        return res;
    }

}
