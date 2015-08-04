package soba.core.vta;

public interface ITopologicalVisitor {
	
	/**
	 * @param vertexId identifies a visited vertex.
	 * @return true if you want to continue visiting 
	 * process beyond the vertex.
	 */
	public boolean onVisit(int vertexId);

	public void onFinished();
}
