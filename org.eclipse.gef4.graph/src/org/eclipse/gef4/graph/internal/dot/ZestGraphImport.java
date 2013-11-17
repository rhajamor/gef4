/*******************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation; see bug 277380
 *******************************************************************************/
package org.eclipse.gef4.graph.internal.dot;

import org.eclipse.gef4.graph.Edge;
import org.eclipse.gef4.graph.Graph;
import org.eclipse.gef4.graph.Node;

/**
 * Imports the content of a Zest graph generated from DOT into an existing Zest
 * graph.
 * 
 * @author Fabian Steeg (fsteeg)
 */
final class ZestGraphImport {
	private Graph graphFromDot;

	/**
	 * @param sourceGraph
	 *            The Zest source graph to import into another graph. Note that
	 *            this will only support a subset of the graph attributes, as it
	 *            is used for import of Zest graphs created from DOT input.
	 */
	ZestGraphImport(Graph sourceGraph) {
		this.graphFromDot = sourceGraph;
	}

	/**
	 * @param targetGraph
	 *            The graph to add content to
	 */
	void into(Graph targetGraph) {
		Graph sourceGraph = graphFromDot;
		targetGraph.withAttribute(Graph.Attr.NODE_STYLE.toString(),
				sourceGraph.getAttribute(Graph.Attr.NODE_STYLE.toString()));
		targetGraph.withAttribute(Graph.Attr.EDGE_STYLE.toString(),
				sourceGraph.getAttribute(Graph.Attr.EDGE_STYLE.toString()));
		targetGraph.withAttribute(Graph.Attr.LAYOUT.toString(),
				sourceGraph.getAttribute(Graph.Attr.LAYOUT.toString()));
		for (Object edge : sourceGraph.getEdges()) {
			copy((Edge) edge, targetGraph);
		}
		for (Object node : sourceGraph.getNodes()) {
			copy((Node) node, targetGraph);
		}
	}

	private Edge copy(Edge edge, Graph targetGraph) {
		Node source = copy(edge.getSource(), targetGraph);
		Node target = copy(edge.getTarget(), targetGraph);
		Edge copy = new Edge(source, target)
				.withAttribute(Graph.Attr.STYLE.toString(),
						edge.getAttribute(Graph.Attr.STYLE.toString()))
				.withAttribute(Graph.Attr.LABEL.toString(),
						edge.getAttribute(Graph.Attr.LABEL.toString()))
				.withAttribute(Graph.Attr.DATA.toString(),
						edge.getAttribute(Graph.Attr.DATA.toString()))
				.withAttribute(Graph.Attr.EDGE_STYLE.toString(),
						edge.getAttribute(Graph.Attr.EDGE_STYLE.toString()));
		targetGraph.withEdges(copy);
		return copy;
	}

	private Node copy(Node node, Graph targetGraph) {
		Node find = find(node, targetGraph);
		if (find == null) {
			Node copy = new Node()
					.withAttribute(Graph.Attr.LABEL.toString(),
							node.getAttribute(Graph.Attr.LABEL.toString()))
					.withAttribute(Graph.Attr.STYLE.toString(),
							node.getAttribute(Graph.Attr.STYLE.toString()))
					.withAttribute(Graph.Attr.IMAGE.toString(),
							node.getAttribute(Graph.Attr.IMAGE.toString()))
					.withAttribute(Graph.Attr.DATA.toString(),
							node.getAttribute(Graph.Attr.DATA.toString()));
			targetGraph.withNodes(copy);
			return copy;
		}
		return find; // target already contains the node to copy over
	}

	private Node find(Node node, Graph graph) {
		for (Object o : graph.getNodes()) {
			Node n = (Node) o;
			Object nodeData = node.getAttribute(Graph.Attr.DATA.toString());
			if (nodeData != null
					&& nodeData.equals(n.getAttribute(Graph.Attr.DATA
							.toString()))) {
				return n;
			}
		}
		return null;
	}
}
