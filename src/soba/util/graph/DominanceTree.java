package soba.util.graph;

/**
 * This class implements an algorithm described in 
 * Keith D. Cooper, Timothy J. Harvey and Ken Kennedy: A Simple, Fast Dominance Algorithm.
 */
public class DominanceTree {

	private SingleRootDirectedGraph base;
	private IDirectedGraph reverse;
	private int[] reversePostOrder;
	private int[] immediateDominator;

	/**
	 * Creates a new <code>DominanceTree</code> instance from a <code>SingleRootDirectedGraph</code> object.
	 * @param graph is a <code>SingleRootDirectedGraph</code> object.
	 */
	public DominanceTree(SingleRootDirectedGraph graph) {
		this.base = graph;
		this.reverse = graph.getReverseGraph();
		this.reversePostOrder = new int[base.getVertexCount()];
		this.immediateDominator = new int[base.getVertexCount()];
		computeSpanningTree();
		computeDominators();
	}
	
	/**
	 * @param vertexID specifies a vertex.
	 * @return true if the specified vertex is root of the tree.
	 */
	public boolean isRoot(int vertexID) {
		return vertexID == base.getRootId();
	}
	
	/**
	 * @param vertexID specifies a vertex.
	 * @return the dominator vertex ID of the specified vertex.
	 */
	public int getDominator(int vertexID) {
		return immediateDominator[vertexID];
	}

	/**
	 * Visit nodes using depth-first search.
	 * Reverse-post-order traversal.
	 */
	private void computeSpanningTree() {
		
		DepthFirstSearch.search(base, base.getRootId(), new IDepthFirstVisitor() {
			
			private int reversePostOrderIndex = base.getVertexCount() - 1;

			@Override
			public void onStart(int startVertexId) {
			}
			
			@Override
			public boolean onVisit(int vertexId) {
				return true;
			}
			
			@Override
			public void onLeave(int vertexId) {
				reversePostOrder[vertexId] = reversePostOrderIndex;
				reversePostOrderIndex--;
			}
			
			@Override
			public void onFinished(boolean[] visited) {
			}
			
			@Override
			public void onVisitAgain(int vertexId) {
			}
		});
	}
	
	private void computeDominators() {
		
		// sort vertices according to the descending order of reverse post order (a regular post order)
		int[] sortedVertices = new int[base.getVertexCount()];
		for (int i=0; i<base.getVertexCount(); ++i) {
			sortedVertices[reversePostOrder[i]] = i;
		}
		
		// initialize idom
		for (int from=0; from<base.getVertexCount(); ++from) {
			for (int to: base.getEdges(from)) {
				if (reversePostOrder[from] < reversePostOrder[to]) immediateDominator[to] = from;
			}
		}
		
		// iteratively compute dominators
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int v: sortedVertices) {
				// dom(v) == Nearest Common Ancestor of v's predecessors
				int[] pred = reverse.getEdges(v);
				for (int idx=0; idx<pred.length; ++idx) {
					int idom = immediateDominator[v];
					int nca = nearestCommonAncestor(idom, pred[idx]);
					if (idom != nca) {
						changed = true;
						immediateDominator[v] = nca;
					}
				}
			}
		}
	}
	
	/**
	 * Returns the nearest common ancestor of two nodes.
	 */
	public final int nearestCommonAncestor(int v1, int v2) {
		while (v1 != v2) {
			int i1 = reversePostOrder[v1];
			int i2 = reversePostOrder[v2];
			if (i1 > i2) { 
				v1 = immediateDominator[v1];
			} else {
				assert (i1 < i2): "v1!=v2 implies i1!=i2";
				v2 = immediateDominator[v2];
			}
		}
		return v1;
	}

}
