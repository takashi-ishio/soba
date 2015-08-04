package soba.util.graph;

import soba.util.IntPairList;
import soba.util.IntPairProc;

public class GraphUtil {

	/**
	 * @return a new graph with reversed edges.
	 * An edge from vertex A to vertex B in the original graph is 
	 * translated into an edge from B to A in the new graph.
	 */
	public static DirectedGraph getReverseGraph(IDirectedGraph g) {
		final IntPairList reverseEdges = new IntPairList();
		g.forEachEdge(new IntPairProc() {
			@Override
			public boolean execute(int elem1, int elem2) {
				reverseEdges.add(elem2, elem1);
				return true;
			}
		});
		return new DirectedGraph(g.getVertexCount(), reverseEdges);
	}
	
	/**
	 * @return a new graph whose vertices are connected by undirected edges.
	 * In other words, a directed edge from vertex A to vertex B is 
	 * translated into two edges A to B and B to A. 
	 */
	public static DirectedGraph getUndirectedGraph(IDirectedGraph g) {
		final IntPairList undirectedEdges = new IntPairList();
		g.forEachEdge(new IntPairProc() {
			@Override
			public boolean execute(int elem1, int elem2) {
				undirectedEdges.add(elem1, elem2);
				undirectedEdges.add(elem2, elem1);
				return true;
			}
		});
		return new DirectedGraph(g.getVertexCount(), undirectedEdges);
	}

}
