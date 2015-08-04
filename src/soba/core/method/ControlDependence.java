package soba.core.method;

import java.util.Arrays;

import soba.util.IntPairList;
import soba.util.IntPairProc;
import soba.util.IntPairSet;
import soba.util.graph.DepthFirstSearch;
import soba.util.graph.DirectedGraph;
import soba.util.graph.DominanceTree;
import soba.util.graph.IDepthFirstVisitor;
import soba.util.graph.IDirectedGraph;
import soba.util.graph.SingleRootDirectedGraph;

public class ControlDependence {

	
	/**
	 * @param instructionCount is the number of instructions.
	 * @param controlFlowGraph is a directed graph representing control-flow among instructions.
	 * @return a directed graph which represents control dependencies.
	 * The graph should be a "regular" control-flow graph excluding exceptional control-flow paths.
	 * Note: This graph does not contain dependencies from the method entry.
	 */
	public static DirectedGraph getDependence(final int instructionCount, final DirectedGraph controlFlowGraph) {
		IDirectedGraph reverseControlFlow = controlFlowGraph.getReverseGraph();
		SingleRootDirectedGraph rootGraph = new SingleRootDirectedGraph(reverseControlFlow);
		DominanceTree tree = new DominanceTree(rootGraph);

		final IntPairList controlDependenceCandidate = new IntPairList();
		for (int i=0; i<instructionCount; ++i) {
			if (controlFlowGraph.getEdges(i).length > 1) { // is branch
				final int postDom = tree.getDominator(i); // post dominator
				DepthFirstSearch.search(controlFlowGraph, i, new IDepthFirstVisitor() {
					
					private int start; 
					@Override
					public void onStart(int startVertexId) {
						this.start = startVertexId;
					}

					@Override
					public boolean onVisit(int vertexId) {
						if (start != vertexId && vertexId != postDom) {
							controlDependenceCandidate.add(start, vertexId);
						}
						return vertexId != postDom;
					}
					@Override
					public void onVisitAgain(int vertexId) {
					}
					
					@Override
					public void onLeave(int vertexId) {
					}
					
					@Override
					public void onFinished(boolean[] visited) {
					}
				});
			}
		}
		
		// Removing redundant edges: if A->B and B->C, then A->C is redundant.  If A->B and B->A, both A->C and B->C are not redundant.  
		DirectedGraph candidate = new DirectedGraph(instructionCount, controlDependenceCandidate);
		final IntPairSet redundantEdges = new IntPairSet();
		for (int src=0; src<instructionCount; ++src) {
			for (int v: candidate.getEdges(src)) {
				if (controlFlowGraph.getEdges(v).length > 1) { // v is a branch vertex
					int[] edges = candidate.getEdges(v);
					if (Arrays.binarySearch(edges, src) < 0) { // if not src->v and v->src
						for (int d: candidate.getEdges(v)) {  
							redundantEdges.add(src, d);
						}
					}
				}
			}
		}
		final IntPairList controlDependence = new IntPairList();
		controlDependenceCandidate.foreach(new IntPairProc() {
			@Override
			public boolean execute(int elem1, int elem2) {
				if (!redundantEdges.contains(elem1, elem2)) { 
					controlDependence.add(elem1, elem2);
				}
				return true;
			}
		});
		return new DirectedGraph(instructionCount, controlDependence);

	}
}
