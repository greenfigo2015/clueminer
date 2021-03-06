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
package org.clueminer.graph.fast;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.clueminer.graph.api.Node;

/**
 *
 * @author deric
 */
public class GraphGenerator {

    public static EdgeImpl[] generateEdgeList(int edgeCount) {
        return generateEdgeList(edgeCount, 0, true, true);
    }

    public static EdgeImpl[] generateSmallEdgeList() {
        return generateEdgeList(100, 0, true, true);
    }

    public static EdgeImpl[] generateEdgeList(int edgeCount, int type, boolean directed, boolean allowSelfLoops) {
        int nodeCount = Math.max((int) Math.ceil(Math.sqrt(edgeCount * 2)), (int) (edgeCount / 10.0));
        return generateEdgeList(generateNodeStore(nodeCount), edgeCount, type, directed, allowSelfLoops);
    }

    public static NodeStore generateNodeStore(int nodeCount) {
        final NodeStore nodeStore = new NodeStore();
        Node n;
        GraphFactoryImpl factory = GraphFactoryImpl.getInstance();
        for (int i = 0; i < nodeCount; i++) {
            n = factory.newNode(String.valueOf(i));
            nodeStore.add(n);
        }
        return nodeStore;
    }

    public static EdgeImpl[] generateEdgeList(NodeStore nodeStore, int edgeCount, int type, boolean directed, boolean allowSelfLoops) {
        int nodeCount = nodeStore.size();
        final List<EdgeImpl> edgeList = new ArrayList<>();
        LongSet idSet = new LongOpenHashSet();
        Random r = new Random(124);

        IntSet leafs = new IntOpenHashSet();
        if (nodeCount > 10) {
            for (int i = 0; i < Math.min(10, (int) (nodeCount * .05)); i++) {
                int id = r.nextInt(nodeCount);
                if (leafs.contains(id)) {
                    i--;
                } else {
                    leafs.add(id);
                }
            }
        }

        long cnt = 0;
        while (idSet.size() < edgeCount) {
            int sourceId = r.nextInt(nodeCount);
            int targetId = r.nextInt(nodeCount);
            Node source = nodeStore.get(sourceId);
            Node target = nodeStore.get(targetId);
            EdgeImpl edge = new EdgeImpl(cnt++, source, target, 1.0, directed);
            if (!leafs.contains(sourceId) && !leafs.contains(targetId) && (allowSelfLoops || (!allowSelfLoops && source != target)) && !idSet.contains(edge.getLongId())) {
                edgeList.add(edge);
                idSet.add(edge.getLongId());
            }
        }

        return edgeList.toArray(new EdgeImpl[0]);
    }

    public static NodeImpl[] generateNodeList(int nodeCount) {
        NodeImpl[] nodes = new NodeImpl[nodeCount];
        long cnt = 0;
        for (int i = 0; i < nodeCount; i++) {
            NodeImpl node = new NodeImpl(cnt++, null);
            nodes[i] = node;
        }
        return nodes;
    }

    public static NodeImpl[] generateSmallNodeList() {
        return generateNodeList(100);
    }

    public static NodeImpl[] generateLargeNodeList() {
        return generateNodeList(FastGraphConfig.NODESTORE_BLOCK_SIZE * 3 + (int) (FastGraphConfig.NODESTORE_BLOCK_SIZE / 3.0));
    }

    public static FastGraph generateSmallGraphStore() {
        int edgeCount = 100;
        FastGraph graphStore = new FastGraph();
        graphStore.setAllowParallelEdges(true);
        NodeImpl[] nodes = generateNodeList(Math.max((int) Math.ceil(Math.sqrt(edgeCount * 2)), (int) (edgeCount / 10.0)));
        graphStore.addAllNodes(Arrays.asList(nodes));
        EdgeImpl[] edges = generateEdgeList(graphStore.nodeStore, edgeCount, 0, true, true);
        graphStore.addAllEdges(Arrays.asList(edges));
        return graphStore;
    }

    public static EdgeImpl generateSingleEdge() {
        return generateEdgeList(1, 0, true, false)[0];
    }

    public static EdgeImpl[] generateLargeEdgeList() {
        return generateEdgeList(FastGraphConfig.EDGESTORE_BLOCK_SIZE * 3 + (int) (FastGraphConfig.EDGESTORE_BLOCK_SIZE / 3.0), 0, true, true);
    }
}
