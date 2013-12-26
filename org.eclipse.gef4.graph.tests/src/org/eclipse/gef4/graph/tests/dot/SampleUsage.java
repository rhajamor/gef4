/*******************************************************************************
 * Copyright (c) 2009, 2010 Fabian Steeg. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation; see bug 277380
 *******************************************************************************/
package org.eclipse.gef4.graph.tests.dot;

import org.eclipse.gef4.graph.Graph;
import org.junit.Test;

/**
 * API sample usage.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public class SampleUsage {
	@Test
	public void sampleUsage() {
		/*
		 * A graph can be created from DOT, given as a String:
		 */
		Graph graph = new Graph("digraph{1->2}");
		/* The Dot graph can be modified using DOT snippets: */
		graph.withDot("2->3").withDot("2->4");
		/* The snippets can contain DOT node and edge attributes: */
		graph.withDot("node[label=zested]; edge[style=dashed]; 3->5; 4->6");
		/* Export to a DOT string: */
		String dot = graph.toDot();
		/* See some of the results: */
		System.out.println(graph);
		System.out.println(dot);
	}
}