package soba.util.graph;

import java.util.Arrays;

import soba.util.IntPairList;
import soba.util.IntPairProc;

import gnu.trove.set.hash.TIntHashSet;

/**
 * An instance of this class is a directed graph.
 * Each vertex is represented as an integer
 * between 0 and vertexCount-1. 
 */
public class DirectedGraph implements IDirectedGraph {

	private int vertexCount;
	private int edgeCount;
	private IntPairList edges;
	private int[][] forward;
	
	private static final int[] EMPTY_ARRAY = new int[0];
	            
	/**
	 * Creates a new <code>DirectedGraph</code> instance.
	 * Duplicated edges are ignored.
	 * @param vertexCount is the number of vertices.
	 * @param edges will be modified by DirectedGraph.
	 */
	public DirectedGraph(int vertexCount, IntPairList edges) {
		this.vertexCount = vertexCount;
		this.edges = edges;
		this.edgeCount = edges.size();
		this.edges.sort();
		this.forward = constructEdgeArray(edges);
	}
	
	/**
	 * @param edges are pairs of vertex IDs. 
	 * @return array representing edges.
	 * Duplicated edges are excluded in the resultant array.
	 */
	private int[][] constructEdgeArray(IntPairList edges) {
		// forwardTemp[V] means a list of edges from vertex V.
		TIntHashSet[] forwardTemp = new TIntHashSet[vertexCount];
		for (int i=0; i<edges.size(); ++i) {
			int from = edges.getFirstValue(i);
			int to = edges.getSecondValue(i);

			if (forwardTemp[from] == null) {
				forwardTemp[from] = new TIntHashSet(2);
			}
			forwardTemp[from].add(to);
		}
		// Translate a set object to an array.
		int[][] forward = new int[vertexCount][];
		for (int i=0; i<vertexCount; ++i) {
			if (forwardTemp[i] != null) {
				int[] array = forwardTemp[i].toArray();
				Arrays.sort(array);
				forward[i] = array;
			} else {
				forward[i] = EMPTY_ARRAY;
			}
		}
		return forward;
	}

	/**
	 * @return the number of edges.
	 */
	public int getEdgeCount() {
		return edgeCount;
	}

    /** {@inheritDoc} */
	@Override
	public int getVertexCount() {
		return vertexCount;
	}
	
    /** {@inheritDoc} */
	@Override
	public void forEachEdge(IntPairProc proc) {
		edges.foreach(proc);
	}
	
    /** {@inheritDoc} */
	@Override
	public int[] getEdges(int memberId) {
		return this.forward[memberId];
	}

	/**
	 * @return a new graph with reversed edges.
	 * An edge from vertex A to vertex B in the original graph is 
	 * translated into an edge from B to A in the new graph.
	 */
	public DirectedGraph getReverseGraph() {
		return GraphUtil.getReverseGraph(this);
	}
	
	/**
	 * @return a new graph whose vertices are connected by undirected edges.
	 * In other words, a directed edge from vertex A to vertex B is 
	 * translated into two edges A to B and B to A. 
	 */
	public DirectedGraph getUndirectedGraph() {
		return GraphUtil.getUndirectedGraph(this);
	}

}
