package soba.util.graph;

public interface IDepthFirstVisitor {

	/**
	 * This method is called in order to notify the beginning of a visiting process.
	 * @param startVertexId
	 */
	public void onStart(int startVertexId);
	
	/**
	 * @param vertexId identifies a visited vertex.
	 * @return true if you want to continue visiting process beyond the vertex.
	 */
	public boolean onVisit(int vertexId);
	
	/**
	 * This method is called when a vertex is visited again.
	 * You may use this method to detect two or more paths for the vertex.
	 * @param vertexId
	 */
	public void onVisitAgain(int vertexId);
	
	/**
	 * @param vertexId
	 */
	public void onLeave(int vertexId);
	
	/**
	 * This method is called when the visit process is finished.
	 * @param visited
	 */
	public void onFinished(boolean[] visited);

}
