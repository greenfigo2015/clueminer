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
package org.clueminer.chinesewhispers;

import java.util.HashMap;
import java.util.Random;
import org.clueminer.clustering.api.Algorithm;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringAlgorithm;
import org.clueminer.clustering.api.Configurator;
import org.clueminer.clustering.api.config.annotation.Param;
import org.clueminer.clustering.api.factory.Clusterings;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.distance.api.DistanceFactory;
import org.clueminer.graph.adjacencyList.AdjListFactory;
import org.clueminer.graph.adjacencyList.AdjListGraph;
import org.clueminer.graph.api.GraphConvertor;
import org.clueminer.graph.api.GraphConvertorFactory;
import org.clueminer.graph.api.Node;
import org.clueminer.utils.Props;
import org.openide.util.lookup.ServiceProvider;

/**
 * Chinese Whispers is a clustering method used in network science named after a
 * famous whispering game. Clustering methods are basically used to identify
 * communities of nodes or links in a given network. This algorithm was designed
 * by Chris Biemann and Sven Teresinak in 2005. The name comes from the fact
 * that the process can be modeled as a separation of communities where the
 * nodes send the same type of information to each other.
 *
 * @cite Chris Biemann,"Chinese Whispers- an Efficient Graph Clustering
 * Algorithm and its Applications to Natural Language Processing Problems" 2006
 *
 * @author Hamster
 * @param <E>
 * @param <C>
 */
@ServiceProvider(service = ClusteringAlgorithm.class)
public class ChineseWhispers<E extends Instance, C extends Cluster<E>> extends Algorithm<E, C> {

    private AdjListGraph graph;

    public static final String MAX_ITERATIONS = "max_iterations";

    @Param(name = ChineseWhispers.MAX_ITERATIONS, description = "Maximum number of iterations")
    private int maxIterations;

    public static final String EDGE_THRESHOLD = "edge_threshold";
    @Param(name = ChineseWhispers.EDGE_THRESHOLD, description = "Minimum distance for edge initialization")
    protected double edgeThreshold;

    public static final String GRAPH_CONV = "graph_conv";
    //it's a performance parameter
    /* @Param(name = ChineseWhispers.GRAPH_CONV,
     * factory = "org.clueminer.graph.api.GraphConvertorFactory",
     * type = org.clueminer.clustering.params.ParamType.STRING) */
    private GraphConvertor graphCon;

    private static final String CLS = "cw_cls";

    @Override
    public String getName() {
        return "ChineseWhispers";
    }

    @Override
    public Clustering<E, C> cluster(Dataset<E> dataset, Props props) {
        if (colorGenerator != null) {
            colorGenerator.reset();
        }
        graph = new AdjListGraph();
        String dist = props.get("distance", "Euclidean");
        this.distanceFunction = DistanceFactory.getInstance().getProvider(dist);
        props.put("algorithm", getName());
        int iter = (int) (2 * Math.sqrt(dataset.size()));
        maxIterations = props.getInt(MAX_ITERATIONS, iter);

        Long[] mapping = AdjListFactory.getInstance().createNodesFromInput(dataset, graph);
        String initializer = props.get(GRAPH_CONV, "k-NNG");
        graphCon = GraphConvertorFactory.getInstance().getProvider(initializer);
        graphCon.setDistanceMeasure(distanceFunction);
        graphCon.createEdges(graph, dataset, mapping, props);

        //assign node to a random class (each node forms a cluster)
        for (Node node : graph.getNodes()) {
            node.setAttribute(CLS, node.getId());
        }

        boolean changes = true;
        Random random = new Random();
        int i = 0;
        while (changes && i < maxIterations) {
            changes = false;
            for (Node node : graph.getNodes()) {
                HashMap<Long, Integer> classes = new HashMap<>(graph.getNodeCount());
                long maxClass = (long) node.getAttribute(CLS);
                Integer maxCount = 1;
                for (Node neighbor : graph.getNeighbors(node)) {
                    long classValue = (long) neighbor.getAttribute(CLS);
                    Integer count = classes.get(classValue);
                    count = (count == null) ? 1 : count++;
                    classes.put(classValue, count);
                    if (count > maxCount
                            || (count.equals(maxCount) && random.nextBoolean())) {
                        maxCount = count;
                        maxClass = classValue;
                    }
                }
                if (maxClass != (long) node.getAttribute(CLS)) {
                    changes = true;
                    node.setAttribute(CLS, maxClass);
                }
            }
            i++;
        }

        //graph.print();
        Clustering<E, C> result = (Clustering<E, C>) Clusterings.newList();
        result.lookupAdd(dataset);
        Cluster<E> cluster;
        HashMap<Long, Integer> id2clust = new HashMap<>();
        for (Node<E> node : graph.getNodes()) {
            long classValue = (long) node.getAttribute(CLS);
            if (!id2clust.containsKey(classValue)) {
                cluster = result.createCluster();
                id2clust.put(classValue, cluster.getClusterId());
                if (colorGenerator != null) {
                    cluster.setColor(colorGenerator.next());
                }
            } else {
                cluster = result.get(id2clust.get(classValue));
            }
            cluster.add(node.getInstance());
        }
        result.setParams(props);
        return result;
    }

    @Override
    public Configurator<E> getConfigurator() {
        return CWConfig.getInstance();
    }

    @Override
    public boolean isDeterministic() {
        return false;
    }
}
