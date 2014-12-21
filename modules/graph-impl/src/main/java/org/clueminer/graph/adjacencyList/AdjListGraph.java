package org.clueminer.graph.adjacencyList;

import java.util.Collection;
import java.util.HashMap;
import org.clueminer.graph.api.Edge;
import org.clueminer.graph.api.EdgeIterable;
import org.clueminer.graph.api.Graph;
import org.clueminer.graph.api.GraphFactory;
import org.clueminer.graph.api.Node;
import org.clueminer.graph.api.NodeIterable;

/**
 *
 * @author Hamster
 */
public class AdjListGraph implements Graph {

	HashMap<Long, AdjListNode> nodes;
	HashMap<Long, AdjListEdge> edges;

	@Override
	public boolean addEdge(Edge edge) {
		if(edges.containsKey(edge.getId()))
			return false;
		if(!nodes.containsKey(edge.getSource().getId()) || !nodes.containsKey(edge.getTarget().getId()))
			throw new IllegalArgumentException("Source or target node does not exist");
		edges.put(edge.getId(), (AdjListEdge) edge);
		AdjListNode source = (AdjListNode) edge.getSource();
		AdjListNode target = (AdjListNode) edge.getTarget();
		source.addEdgeOut(edge);
		target.addEdgeIn(edge);
		return true;
	}

	@Override
	public boolean addNode(Node node) {
		if(nodes.containsKey(node.getId()))
			return false;
		nodes.put(node.getId(), (AdjListNode) node);
		return true;
	}

	@Override
	public boolean addAllEdges(Collection<? extends Edge> edges) {
		boolean added = false;
		for(Edge edge : edges)
			if(addEdge(edge))
				added = true;
		return added;
	}

	@Override
	public boolean addAllNodes(Collection<? extends Node> nodes) {
		boolean added = false;
		for(Node node : nodes)
			if(addNode(node))
				added = true;
		return added;
	}

	@Override
	public boolean removeEdge(Edge edge) {
		return edges.remove(edge.getId()) != null;
	}

	@Override
	public boolean removeNode(Node node) {
		return nodes.remove(node.getId()) != null;
	}

	@Override
	public boolean removeAllEdges(Collection<? extends Edge> edges) {
		boolean removed = false;
		for(Edge edge : edges)
			if(removeEdge(edge))
				removed = true;
		return removed;
	}

	@Override
	public boolean removeAllNodes(Collection<? extends Node> nodes) {
		boolean removed = false;
		for(Node node : nodes)
			if(removeNode(node))
				removed = true;
		return removed;
	}

	@Override
	public boolean contains(Node node) {
		return nodes.containsKey(node.getId());
	}

	@Override
	public boolean contains(Edge edge) {
		return edges.containsKey(edge.getId());
	}

	@Override
	public Node getNode(long id) {
		return nodes.get(id);
	}

	@Override
	public Edge getEdge(long id) {
		return edges.get(id);
	}

	@Override
	public Edge getEdge(Node node1, Node node2) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Edge getEdge(Node node1, Node node2, int type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public NodeIterable getNodes() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public EdgeIterable getEdges() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public EdgeIterable getSelfLoops() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public NodeIterable getNeighbors(Node node) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public NodeIterable getNeighbors(Node node, int type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public EdgeIterable getEdges(Node node) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public EdgeIterable getEdges(Node node, int type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int getNodeCount() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int getEdgeCount() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int getEdgeCount(int type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Node getOpposite(Node node, Edge edge) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int getDegree(Node node) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isSelfLoop(Edge edge) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isDirected(Edge edge) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isAdjacent(Node node1, Node node2) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isAdjacent(Node node1, Node node2, int type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isIncident(Edge edge1, Edge edge2) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isIncident(Node node, Edge edge) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void clearEdges(Node node) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void clearEdges(Node node, int type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void clearEdges() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isDirected() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isUndirected() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isMixed() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public GraphFactory getFactory() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean addEdgesFromNeigborArray(int[][] nearests, int k) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int getIndex(Node node) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
