package soba.core.vta;

import soba.util.graph.DirectedAcyclicGraph;

public class TopologicalOrderSearch {

	/**
	 * Visit vertices in their topological order.
	 * @param graph
	 * @param visit
	 */
	public static void searchFromRoot(final DirectedAcyclicGraph graph, ITopologicalVisitor visit) {
		// To find root vertices, count the number of incoming edges for each vertex.
		int[] incoming = new int[graph.getVertexCount()];
		for (int i=0; i<graph.getVertexCount(); ++i) {
			for (int to: graph.getEdges(i)) {
				incoming[to] += 1;
			}
		}
		
		// Push root vertices into the queue.
		int[] queue = new int[graph.getVertexCount()];
		int queueEndIndex = 0;
		for (int i=0; i<graph.getVertexCount(); ++i) {
			if (incoming[i] == 0 && graph.isRepresentativeNode(i)) {
				queue[queueEndIndex] = i;
				queueEndIndex++;
			}
		}
		
		// Main Loop
		int queueIndex = 0;
		while (queueIndex < queueEndIndex) {
			int v = queue[queueIndex];
			queueIndex++;
			boolean continueVisit = visit.onVisit(v);
			if (continueVisit) {
				for (int to: graph.getEdges(v)) {
					incoming[to] -= 1;
					if (incoming[to] == 0) {
						queue[queueEndIndex] = to;
						queueEndIndex++;
					}
				}
			}
		}
		visit.onFinished();
	}
}
