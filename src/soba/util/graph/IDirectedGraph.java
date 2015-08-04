package soba.util.graph;

import soba.util.IntPairProc;

public interface IDirectedGraph {

	/**
	 * @return the number of vertices.
	 */
	public int getVertexCount();
	
	/**
	 * @param memberId specifies a vertex.
	 * @return an array of vertex IDs connected from the specified vertex.
	 */
	public int[] getEdges(int memberId);
	
	/**
	 * Executes a procedure for each edge.
	 */
	public void forEachEdge(IntPairProc proc);

}
