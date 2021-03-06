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
package org.clueminer.chameleon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import org.clueminer.chameleon.similarity.CLS;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.dendrogram.DendroNode;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.graph.api.Graph;
import org.clueminer.graph.api.Node;
import org.clueminer.hclust.DClusterLeaf;
import org.clueminer.kdtree.KDTree;
import org.clueminer.kdtree.KeyDuplicateException;
import org.clueminer.kdtree.KeySizeException;
import org.clueminer.partitioning.api.Bisection;
import org.clueminer.partitioning.api.Merger;
import org.clueminer.utils.PairValue;
import org.clueminer.utils.Props;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Faster merger that does not compute full similarity matrix. Only k nearest clusters
 * are considered as candidates for merging.
 *
 * @author deric
 * @param <E>
 */
@ServiceProvider(service = Merger.class)
public class KnnMerger<E extends Instance> extends PairMerger<E> implements Merger<E> {

    public static final String NAME = "k-NN merger";
    protected KDTree<GraphCluster<E>> kdTree;
    private static final Logger LOG = LoggerFactory.getLogger(KnnMerger.class);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ArrayList<E> initialize(ArrayList<ArrayList<Node<E>>> clusterList, Graph<E> graph, Bisection bisection, Props params, ArrayList<E> noise) {
        this.graph = graph;
        this.bisection = bisection;
        blacklist = new HashSet<>();
        clusters = createClusters(clusterList, bisection, params);
        assignNodesToCluters(clusters);
        computeExternalProperties(clusters);
        if (noise == null) {
            noise = new ArrayList<>();
        }
        prefilter(clusters, noise, params);
        LOG.debug("creating tree with {} clusters and {} noisy points ", clusters.size(), noise.size());
        nodes = initiateTree(clusters, noise);
        return noise;
    }

    @Override
    public void prefilter(Clustering<E, GraphCluster<E>> clusters, ArrayList<E> noise, Props pref) {
        //build kd-tree for fast search
        kdTree = new KDTree<>(clusters.get(0).attributeCount());
        for (GraphCluster<E> a : clusters) {
            try {
                kdTree.insert(a.getCentroid(), a);
            } catch (KeySizeException | KeyDuplicateException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        //renumberClusters(clusters, noise);
    }

    /**
     * Using only k would cause problems on some datasets, e.g. 3-spiral
     *
     * @param pref
     * @return
     */
    private int getK(Props pref) {
        return 2 * pref.getInt(Chameleon.K, 15);
    }

    /**
     * Initialize cluster pairs based on its nearest-neighbors. We don't compute
     * full similarity matrix.
     *
     * @param numClusters
     * @param pref
     * @return
     */
    @Override
    protected int buildQueue(int numClusters, Props pref) {
        int k = getK(pref);
        int capacity = k * numClusters;
        pq = initQueue(capacity);
        double sim;
        //number of nearest clusters that we evaluate

        //AbstractSimilarity as = (AbstractSimilarity) evaluation;
        //GraphPropertyStore gps = as.getGraphPropertyStore(clusters.get(0));
        int maxClusterId = clusters.size();
        CLS<E> closeness = new CLS<>();

        E centroid;
        for (GraphCluster<E> a : clusters) {
            try {
                //find nearest neighbors
                centroid = a.getCentroid();
                if (centroid == null) {
                    throw new RuntimeException("no centroid of cluster " + a.toString());
                }
                List<GraphCluster<E>> nn = kdTree.nearest(centroid, k);
                //for each NN compute their similarities
                for (GraphCluster<E> b : nn) {
                    if (a.getClusterId() != b.getClusterId()) {
                        sim = evaluation.score(a, b, pref);
                        if (sim > 0) {
                            pq.add(new PairValue<>(a, b, sim));
                        } else {
                            sim = closeness.score(a, b, pref);
                            if (sim > 0) {
                                //System.out.println("CLS (" + a.getClusterId() + "," + b.getClusterId() + ") = " + sim);
                                pq.add(new PairValue<>(a, b, sim));
                            }
                            //System.out.println("excluding pair " + a + ", " + b);
                        }
                        //gps.dump();
                    }
                }
            } catch (KeySizeException | IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return maxClusterId;
    }

    @Override
    public void finalize(Clustering<E, GraphCluster<E>> clusters, PriorityQueue<PairValue<GraphCluster<E>>> pq, Dataset<E> dataset) {
        int i, j;
        PairValue<GraphCluster<E>> curr;
        Cluster<E> noise = clusters.getNoise();
        LOG.debug("merging queue size: {}", pq.size());
        LOG.debug("original clusters {} nodes {}", clusters.size(), nodes.length);

        if (nodes[nodes.length - 1] == null) {
            LOG.debug("no noisy tree node, adding {}", noise.size());
            List<E> n = new ArrayList<>(noise.size());
            nodes[nodes.length - 1] = new DClusterLeaf(noise.size() + 10, n);
            nodes[nodes.length - 1].setHeight(0.0);
            nodes[nodes.length - 1].setLevel(0);
        }
        List<E> treeNoise = ((DClusterLeaf) nodes[nodes.length - 1]).getInstances();
        //int k = 0;
        //int m = 0;
        //int numClusters = clusters.size();

        if (!pq.isEmpty()) {
            LOG.warn("not all clusters were merged. consider increasing k value");

            while (!pq.isEmpty()) {
                curr = pq.poll();
                i = curr.A.getClusterId();
                j = curr.B.getClusterId();
                if (!blacklist.contains(i) && !blacklist.contains(j)) {
                    blacklist.add(i);
                    blacklist.add(j);
                    if (i == j) {
                        throw new RuntimeException("Cannot merge two same clusters");
                    }
                    addToNoise(noise, treeNoise, curr.A);
                    addToNoise(noise, treeNoise, curr.B);
                    //k += 2;
                }
                //m++;
            }
        }
        LOG.info("node size: {}, clusters: {}", nodes.length, clusters.size());
        if (nodes.length > clusters.size()) {
            LOG.info("shrink from {} -> {}", nodes.length, clusters.size());
            DendroNode[] shrinkNodes = new DendroNode[clusters.size()];
            System.arraycopy(nodes, 0, shrinkNodes, 0, clusters.size());
            shrinkNodes[shrinkNodes.length - 1] = nodes[nodes.length - 1];
            nodes = shrinkNodes;
        }

        if (noise.isEmpty()) {
            LOG.info("noise empty, removin' treenode");
            nodes[nodes.length - 1] = null;
        } else {
            LOG.info("noise size: {}", noise.size());
        }

        LOG.info("root: {}", nodes[nodes.length - 2]);
        LOG.info("cluster size: {}", clusters.size());
    }

    private void addToNoise(Cluster<E> noise, List<E> treeNoise, GraphCluster<E> cluster) {
        for (E inst : cluster) {
            noise.add(inst);
            treeNoise.add(inst);
        }
    }

    /**
     * Merges two most similar clusters
     *
     * @param curr
     * @param pref
     * @param newClusterId
     */
    @Override
    protected int singleMerge(PairValue<GraphCluster<E>> curr, Props pref, int newClusterId) {
        int i = curr.A.getClusterId();
        int j = curr.B.getClusterId();
        while (!pq.isEmpty() && (blacklist.contains(i) || blacklist.contains(j))) {
            curr = pq.poll();
            i = curr.A.getClusterId();
            j = curr.B.getClusterId();
        }
        merge(i, j, curr, pref, newClusterId);
        return 1;
    }

    private void merge(int i, int j, PairValue<GraphCluster<E>> curr, Props pref, int newClusterId) {
        blacklist.add(i);
        blacklist.add(j);
        if (i == j) {
            throw new RuntimeException("Cannot merge two same clusters");
        }
        //System.out.println("merging: " + curr.getValue() + " A: " + curr.A.getClusterId() + " B: " + curr.B.getClusterId() + " -> " + newClusterId);
        //clonning won't be necessary if we don't wanna recompute RCL for clusters that were merged
        //LinkedList<Node> clusterNodes = (LinkedList<Node>) curr.A.getNodes().clone();
        //WARNING: we copy nodes from previous clusters (we save memory, but
        //it's not a good idea to work with merged clusters)
        ArrayList<Node<E>> clusterNodes = curr.A.getNodes();
        clusterNodes.addAll(curr.B.getNodes());
        merged(curr);
        GraphCluster<E> newCluster = new GraphCluster(clusterNodes, graph, newClusterId, bisection, pref);
        clusters.add(newCluster);
        evaluation.clusterCreated(curr, newCluster, pref);
        addIntoTree(curr, pref);
        updateExternalProperties(newCluster, curr.A, curr.B);
        addIntoQueue(newCluster, pref);
    }

}
