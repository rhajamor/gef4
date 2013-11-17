/*******************************************************************************
 * Copyright (c) 2009, 2010 Fabian Steeg. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation; see bug 277380
 *******************************************************************************/
package org.eclipse.gef4.graph.tests.dot;

import java.io.File;

import org.eclipse.gef4.graph.Edge;
import org.eclipse.gef4.graph.Graph;
import org.eclipse.gef4.graph.Node;
import org.eclipse.gef4.graph.internal.dot.ZestStyle;
import org.eclipse.gef4.graph.internal.dot.export.DotExport;
import org.eclipse.gef4.layout.algorithms.TreeLayoutAlgorithm;

/**
 * Zest graph sample input for the Zest-To-Dot transformation. Contains
 * everything that is currently supported by the transformation: node and edge
 * labels, edge styles.
 * <p/>
 * Uses the actual Zest Graph class and populates an instance of that, instead
 * of subclassing the Zest Graph and exporting the subclass (as in the samples
 * used for testing, which are based on Graphs generated using the
 * org.eclipse.gef4.zest.dot.import bundle).
 */
public final class DotExportSample {
	public static void main(final String[] args) {
		/* Set up a directed Zest graph with a single connection: */
		Graph graph = new Graph();
		graph.withAttribute(Graph.Attr.EDGE_STYLE.toString(),
				ZestStyle.CONNECTIONS_DIRECTED).withEdges(
				new Edge(new Node().withAttribute(Graph.Attr.LABEL.toString(),
						"Node 1"), new Node().withAttribute(
						Graph.Attr.LABEL.toString(), "Node 2")).withAttribute(
						Graph.Attr.LABEL.toString(), "A dotted edge")
						.withAttribute(Graph.Attr.EDGE_STYLE.toString(),
								ZestStyle.LINE_DOT));
		/* Export the Zest graph to a DOT string or a DOT file: */
		DotExport dotExport = new DotExport(graph);
		System.out.println(dotExport.toDotString());
		dotExport.toDotFile(new File("src-gen/DirectSample.dot")); //$NON-NLS-1$
		/* Show the Zest graph: */
		graph.withAttribute(Graph.Attr.LAYOUT.toString(),
				new TreeLayoutAlgorithm());
	}

	private DotExportSample() { /* enforce non-instantiability */
	}
}
