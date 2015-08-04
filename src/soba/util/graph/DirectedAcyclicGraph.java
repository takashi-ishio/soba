package soba.util.graph;

import java.util.Arrays;

import soba.util.IntPairProc;
import soba.util.IntPairSet;
import soba.util.IntPairUtil;
import soba.util.IntSetStack;
import soba.util.IntStack;

/**
 * This class represents a directed acyclic graph.
 */
public class DirectedAcyclicGraph implements IDirectedGraph {
	
	private IDirectedGraph base;
	private int[] sccIds;
	private DirectedGraph dag;

	/**
	 * Creates a new <code>DirectedAcyclicGraph</code> instance from a specified base graph.
	 * If the base graph has strongly connected components, 
	 * they are removed by Tarjan's algorithm.
	 * @param base is a directed graph.
	 */
	public DirectedAcyclicGraph(IDirectedGraph base) {
		this.base = base;
		this.sccIds = new int[base.getVertexCount()];
		removeStronglyConnectedComponents();
	}
	
	/**
	 * A copy constructor for getReverseGraph()
	 */
	private DirectedAcyclicGraph(DirectedAcyclicGraph g) {
		this.base = g.base;
		this.sccIds = g.sccIds;
		this.dag = g.dag;
	}
	
    /** {@inheritDoc} */
	@Override
	public int[] getEdges(int memberId) {
		return dag.getEdges(memberId);
	}
	
	/**
	 * @return the number of vertices.
	 * The value is the same as the base graph.
	 */
	@Override
	public int getVertexCount() {
		return dag.getVertexCount();
	}
	
    /** {@inheritDoc} */
	@Override
	public void forEachEdge(IntPairProc proc) {
		dag.forEachEdge(proc);
	}
	
	/**
	 * @param vertexId specifies a vertex.
	 * @return true if the ID specifies a vertex 
	 * included in the DAG.
	 * If the vertex is a part of strongly connected components
	 * and excluded from the DAG, the method returns false. 
	 */
	public boolean isRepresentativeNode(int vertexId) {
		return sccIds[vertexId] == vertexId;
	}
	
	/**
	 * @param vertexId specifies a vertex.
	 * @return a vertex ID which is a representative node 
	 * of the strongly connected components including the specified vertexId.
	 */
	public int getRepresentativeNode(int vertexId) {
		return sccIds[vertexId];
	}
	
	/**
	 * @return a new graph with reversed edges.
	 * An edge from vertex A to vertex B in the original graph is 
	 * translated into an edge from B to A in the new graph.
	 */
	public DirectedAcyclicGraph getReverseGraph() {
		DirectedAcyclicGraph reverse = new DirectedAcyclicGraph(this);
		reverse.dag = this.dag.getReverseGraph();
		return reverse;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DirectedAcyclicGraph) {
			DirectedAcyclicGraph another = (DirectedAcyclicGraph)obj;
			if (this.getVertexCount() == another.getVertexCount()) {
				for (int i=0; i<getVertexCount(); ++i) {
					if (this.sccIds[i] != another.sccIds[i]) return false;
					if (isRepresentativeNode(i)) {
						int[] to1 = this.getEdges(i);
						int[] to2 = another.getEdges(i);
						if (to1.length != to2.length) return false;
						for (int j=0; j<to1.length; ++j) {
							if (to1[j] != to2[j]) return false;
						}
					}
				}
				
				return true;
			} else {
				return false;
			}
			
		} else {
			return false;
		}
	}

	
	private TarjanData data;
	/**
	 * Apply Tarjan's algorithm to detect SCCs.
	 */
	private void removeStronglyConnectedComponents() { 
		data = new TarjanData();
		for (int i=0; i<base.getVertexCount(); ++i) {
			if (data.visitIndex[i] == -1) {
				tarjanDFS(i);
			}
		}
		
		final IntPairSet dagEdges = new IntPairSet();
		base.forEachEdge(new IntPairProc() {
			
			@Override
			public boolean execute(int elem1, int elem2) {
				int from = sccIds[elem1];
				int to = sccIds[elem2];
				if (from != to) dagEdges.add(from, to);
				return true;
			}
		});
		dag = new DirectedGraph(base.getVertexCount(), IntPairUtil.createList(dagEdges));
		data = null;
	}
	
	private void tarjanDFS(int vId) {
		data.visitIndex[vId] = data.currentIndex;
		data.lowlink[vId] = data.currentIndex;
		data.currentIndex++;
		data.stack.push(vId);
		//System.out.println("currentIndex = " + data.currentIndex + ", stack = " + data.stack.size());
		
		// For each edge from vId
		for (int to: base.getEdges(vId)) {
			// If the next vertex is not visited yet, visit it.
			if (data.visitIndex[to] == -1) {
				tarjanDFS(to);
				data.lowlink[vId] = Math.min(data.lowlink[vId], data.lowlink[to]); 
			} else if (data.stack.contains(to)) {
				data.lowlink[vId] = Math.min(data.lowlink[vId], data.visitIndex[to]); 
			}
		}

		// If the minimum ID of reachable vertices is equals to the vertex ID,
		// then the vertex is a "root" of SCC.
		// (If vID is not a "root" and included in a SCC, 
		//  the vID is kept on the stack -- the root of SCC will pop the vID.)
		if (data.lowlink[vId] == data.visitIndex[vId]) {
			int pop;
			do {
				pop = data.stack.pop();
				sccIds[pop] = vId;
			} while (pop != vId); // until pop == vId
		}
	}
	
	private class TarjanData {
		public int currentIndex;
		public int[] visitIndex; // ID for each vertex
		public int[] lowlink;    // the minimum ID of vertices reachable from a vertex
		public IntStack stack;
		
		public TarjanData() {
			currentIndex = 0;
			visitIndex = new int[base.getVertexCount()];
			lowlink = new int[base.getVertexCount()];
			stack = new IntSetStack(base.getVertexCount());
			Arrays.fill(visitIndex, -1);
		}
		
	}

}
