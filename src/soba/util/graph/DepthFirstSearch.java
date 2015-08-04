package soba.util.graph;

import java.util.Stack;

/**
 * An implementation of Depth-First Search 
 * for IDirectedGraph object.
 */
public class DepthFirstSearch {

	/**
	 * Executes DFS from the specified vertex.
	 * @param startVertexId
	 * @param visit will receive a call back from this method.
	 */
	public static void search(final IDirectedGraph graph, int startVertexId, IDepthFirstVisitor visit) {
		Stack<DfsProgress> stack = new Stack<DfsProgress>();
		boolean[] visited = new boolean[graph.getVertexCount()];
		visit.onStart(startVertexId);

		DfsProgress progress = new DfsProgress(graph, startVertexId);
		stack.push(progress);
		
		while (!stack.isEmpty()) {
			// The top of the stack represents the current location and state.
			DfsProgress node = stack.peek();
			
			boolean isFirstVisit = !visited[node.getVertex()];
			visited[node.getVertex()] = true;
			
			if (isFirstVisit) { 
				boolean continueVisit = visit.onVisit(node.getVertex());
				if (!continueVisit) {
					stack.pop(); // go back to the previous vertex
					visit.onLeave(node.getVertex());
					continue; 
				}
			}
			
			// Find a next node to visit
			int nextNode = -1;
			while (node.hasNext()) {
				int nextNodeCandidate = node.next();
				if (!visited[nextNodeCandidate]) {
					nextNode = nextNodeCandidate;
					break;
				} else {
					visit.onVisitAgain(nextNodeCandidate);
				}
			} 
			if (nextNode != -1) { 
				// If found, set the next visit 
				stack.push(new DfsProgress(graph, nextNode));
			} else {
				// If not found, go back to the previous node from the current node. 
				stack.pop();
				visit.onLeave(node.getVertex());
			}
		}
		
		visit.onFinished(visited);
	}
	
	/**
	 * This object manages visited edges from a vertex. 
	 */
	private static class DfsProgress {

		private IDirectedGraph graph;
		private int vertex;
		private int edgeIndex;
		
		public DfsProgress(IDirectedGraph graph, int vertexId) {
			this.graph = graph;
			this.vertex = vertexId;
			this.edgeIndex = 0;
		}
		public int getVertex() {
			return vertex;
		}
		public boolean hasNext() {
			int[] edgeList = graph.getEdges(vertex);
			return (edgeList != null) && (edgeIndex < edgeList.length);
		}
		public int next() {
			if (graph.getEdges(vertex) != null) {
				int next = graph.getEdges(vertex)[edgeIndex];
				edgeIndex++;
				return next;
			} else {
				return -1;
			}
		}
		
	}
	

}
