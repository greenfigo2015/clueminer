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
package org.clueminer.clustering.benchmark.nsga;

import com.beust.jcommander.JCommander;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.clueminer.clustering.api.ClusterEvaluation;
import org.clueminer.clustering.api.InternalEvaluator;
import org.clueminer.clustering.api.factory.InternalEvaluatorFactory;
import org.clueminer.clustering.benchmark.Bench;
import static org.clueminer.clustering.benchmark.Bench.ensureFolder;
import static org.clueminer.clustering.benchmark.Bench.printUsage;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;

/**
 *
 * @author Tomas Barton
 */
public class NsgaScore extends Bench {

    public static final String name = "nsga-scores";

    protected static NsgaParams parseArguments(String[] args) {
        NsgaParams params = new NsgaParams();
        JCommander cmd = new JCommander(params);
        printUsage(args, cmd, params);
        return params;
    }

    @Override
    public void main(String[] args) {
        NsgaParams params = parseArguments(args);
        if (params.test) {
            loadIris();
        } else {
            loadDatasets();
        }
        System.out.println("loaded dataset");
        int i = 0;
        for (Map.Entry<String, Map.Entry<Dataset<? extends Instance>, Integer>> e : availableDatasets.entrySet()) {
            System.out.println((i++) + ":" + e.getKey());
        }

        benchmarkFolder = params.home + '/' + "benchmark" + '/' + name;
        ensureFolder(benchmarkFolder);
        System.out.println("writing results to: " + benchmarkFolder);

        System.out.println("=== starting " + name);
        List<InternalEvaluator> eval = InternalEvaluatorFactory.getInstance().getAll();
        ClusterEvaluation[] scores = eval.toArray(new ClusterEvaluation[eval.size()]);
        System.out.println("scores size: " + scores.length);
        NsgaExp exp = new NsgaExp(params, benchmarkFolder, scores, availableDatasets);
        ExecutorService execService = Executors.newFixedThreadPool(1);
        execService.submit(exp);
        execService.shutdown();
    }
}
