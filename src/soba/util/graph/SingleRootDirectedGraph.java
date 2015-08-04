package soba.util.graph;

import soba.util.IntPairList;
import soba.util.IntPairProc;

/**
 * A directed graph that has a single root node.
 */
public class SingleRootDirectedGraph implements IDirectedGraph {

	private IDirectedGraph base;
	private int[] edgesFromRoot;
	
	/**
	 * Creates a new <code>SingleRootDirectedGraph</code> instance from a base graph.
	 * The instance is a directed graph with a single root.
	 * The root is connected to vertices which have no incoming edges in the original graph.
	 * @param base is a directed graph.
	 */
	public SingleRootDirectedGraph(IDirectedGraph base) {
		this.base = base;
		
		// To find vertices without incoming edges, first cycles must be removed from the graph.
		DirectedAcyclicGraph dag = new DirectedAcyclicGraph(base);
		final boolean[] hasIncomingEdge = new boolean[base.getVertexCount()];
		dag.forEachEdge(new IntPairProc() {
			@Override
			public boolean execute(int from, int to) {
				hasIncomingEdge[to] = true;
				return true;
			}
		});
		
		// Find vertices which have incoming edges
		int count = 0;
		for (int i=0; i<hasIncomingEdge.length; ++i) {
			if (!hasIncomingEdge[i] && dag.isRepresentativeNode(i)) count++;
		}
		
		// Connect the root vertex to the vertices that have no incoming edges
		edgesFromRoot =  new int[count];
		int edgeIndex = 0;
		for (int i=0; i<base.getVertexCount(); ++i) {
			if (!hasIncomingEdge[i] && dag.isRepresentativeNode(i)) {
				edgesFromRoot[edgeIndex] = i;
				edgeIndex++;
			}
		}
	}
	
	/**
	 * @return the root vertex ID.
	 */
	public int getRootId() {
		return base.getVertexCount();
	}
	
	@Override
	public int getVertexCount() {
		return base.getVertexCount() + 1;
	}
	
	@Override
	public int[] getEdges(int memberId) {
		if (memberId < base.getVertexCount()) {
			return base.getEdges(memberId);
		} else {
			return edgesFromRoot;
		}
	}
	
	@Override
	public void forEachEdge(IntPairProc proc) {
		// base vertices
		for (int from=0; from<base.getVertexCount(); ++from) {
			for (int to: base.getEdges(from)) {
				if (!proc.execute(from, to)) return;
			}
		}
		// the root vertex
		for (int to: edgesFromRoot) {
			if (!proc.execute(getRootId(), to)) return;
		}
	}
	
	/**
	 * @return a new graph with reversed edges.
	 * An edge from vertex A to vertex B in the original graph is 
	 * translated into an edge from B to A in the new graph.
	 */
	public DirectedGraph getReverseGraph() {
		final IntPairList reverseEdges = new IntPairList();
		base.forEachEdge(new IntPairProc() {
			@Override
			public boolean execute(int elem1, int elem2) {
				reverseEdges.add(elem2, elem1);
				return true;
			}
		});
		for (int id: getEdges(getRootId())) {
			reverseEdges.add(id, getRootId());
		}
		return new DirectedGraph(getVertexCount(), reverseEdges);
	}

	
}
