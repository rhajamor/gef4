/*******************************************************************************
 * Copyright (c) 2009, 2011 Fabian Steeg. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation; see bug 277380
 *******************************************************************************/

package org.eclipse.gef4.graph.internal.dot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef4.graph.Edge;
import org.eclipse.gef4.graph.Graph;
import org.eclipse.gef4.graph.Node;
import org.eclipse.gef4.graph.internal.dot.DotAst.Layout;
import org.eclipse.gef4.graph.internal.dot.DotAst.Style;
import org.eclipse.gef4.graph.internal.dot.parser.dot.AList;
import org.eclipse.gef4.graph.internal.dot.parser.dot.AttrList;
import org.eclipse.gef4.graph.internal.dot.parser.dot.AttrStmt;
import org.eclipse.gef4.graph.internal.dot.parser.dot.Attribute;
import org.eclipse.gef4.graph.internal.dot.parser.dot.AttributeType;
import org.eclipse.gef4.graph.internal.dot.parser.dot.EdgeRhsNode;
import org.eclipse.gef4.graph.internal.dot.parser.dot.EdgeStmtNode;
import org.eclipse.gef4.graph.internal.dot.parser.dot.GraphType;
import org.eclipse.gef4.graph.internal.dot.parser.dot.MainGraph;
import org.eclipse.gef4.graph.internal.dot.parser.dot.NodeId;
import org.eclipse.gef4.graph.internal.dot.parser.dot.NodeStmt;
import org.eclipse.gef4.graph.internal.dot.parser.dot.Stmt;
import org.eclipse.gef4.graph.internal.dot.parser.dot.Subgraph;
import org.eclipse.gef4.graph.internal.dot.parser.dot.util.DotSwitch;
import org.eclipse.gef4.layout.algorithms.TreeLayoutAlgorithm;

/**
 * Create a Zest graph instance from a DOT string by interpreting the AST of the
 * parsed DOT.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public final class GraphCreatorInterpreter extends DotSwitch<Object> {

	private Map<String, Node.Builder> nodes;
	private Graph.Builder graph;
	private String globalEdgeStyle;
	private String globalEdgeLabel;
	private String globalNodeLabel;
	private String currentEdgeStyleValue;
	private String currentEdgeLabelValue;
	private String currentEdgeSourceNodeId;
	private boolean gotSource;

	public Graph create(DotAst dotAst) {
		return create(dotAst, new Graph.Builder());
	}

	public Graph create(DotAst dotAst, Graph.Builder graph) {
		if (dotAst.errors().size() > 0) {
			throw new IllegalArgumentException(String.format(
					DotMessages.GraphCreatorInterpreter_0 + ": %s", dotAst //$NON-NLS-1$
							.errors().toString()));
		}
		this.graph = graph;
		nodes = new HashMap<String, Node.Builder>();
		TreeIterator<Object> contents = EcoreUtil.getAllProperContents(
				dotAst.resource(), false);
		while (contents.hasNext()) {
			doSwitch((EObject) contents.next());
		}
		return graph.build();
	}

	@Override
	public Object caseMainGraph(MainGraph object) {
		createGraph(object);
		return super.caseMainGraph(object);
	}

	@Override
	public Object caseAttribute(Attribute object) {
		/*
		 * Convenience for common 'rankdir=LR' attribute: use
		 * TreeLayoutAlgorithm.LEFT_RIGHT if nothing else is specified
		 */
		if (object.getName().equals("rankdir") //$NON-NLS-1$
				&& object.getValue().equals("LR")) { //$NON-NLS-1$
			TreeLayoutAlgorithm algorithm = new TreeLayoutAlgorithm(
					TreeLayoutAlgorithm.LEFT_RIGHT);
			graph.attr(Graph.Attr.LAYOUT.toString(), algorithm);
		} else if (graph != null && object.getName().equals("label")) { //$NON-NLS-1$
			graph.attr(Graph.Attr.LABEL.toString(), object.getValue());
		}
		return super.caseAttribute(object);
	}

	@Override
	public Object caseAttrStmt(AttrStmt object) {
		createAttributes(object);
		return super.caseAttrStmt(object);
	}

	@Override
	public Object caseNodeStmt(NodeStmt object) {
		createNode(object);
		return super.caseNodeStmt(object);
	}

	@Override
	public Object caseEdgeStmtNode(EdgeStmtNode object) {
		currentEdgeLabelValue = getAttributeValue(object, "label"); //$NON-NLS-1$
		currentEdgeStyleValue = getAttributeValue(object, "style"); //$NON-NLS-1$
		return super.caseEdgeStmtNode(object);
	}

	@Override
	public Object caseNodeId(NodeId object) {
		if (!gotSource) {
			gotSource = true;
		} else {
			String targetNodeId = escaped(object.getName());
			if (currentEdgeSourceNodeId != null && targetNodeId != null) {
				addConnectionTo(targetNodeId);
			}
			gotSource = false;
		}
		/* Current node is potential source in case next is caseEdgeRhsNode: */
		currentEdgeSourceNodeId = escaped(object.getName());
		return super.caseNodeId(object);
	}

	private void addConnectionTo(String targetNodeId) {
		Edge.Builder graphConnection = new Edge.Builder(
				node(currentEdgeSourceNodeId), node(targetNodeId));
		/* Set the optional label, if set in the DOT input: */
		if (currentEdgeLabelValue != null) {
			graphConnection.attr(Graph.Attr.LABEL.toString(),
					currentEdgeLabelValue);
		} else if (globalEdgeLabel != null) {
			graphConnection.attr(Graph.Attr.LABEL.toString(), globalEdgeLabel);
		}
		/* Set the optional style, if set in the DOT input and supported: */
		if (supported(currentEdgeStyleValue, Style.values())) {
			Style v = Enum.valueOf(Style.class,
					currentEdgeStyleValue.toUpperCase());
			graphConnection.attr(Graph.Attr.EDGE_STYLE.toString(), v.style);
		} else if (supported(globalEdgeStyle, Style.values())) {
			Style v = Enum.valueOf(Style.class, globalEdgeStyle.toUpperCase());
			graphConnection.attr(Graph.Attr.EDGE_STYLE.toString(), v.style);
		}
		graph.edges(graphConnection.build());
	}

	private boolean supported(String value, Enum<?>[] vals) {
		if (value == null)
			return false;
		for (Enum<?> v : vals)
			if (v.name().equalsIgnoreCase(value))
				return true;
		return false;
	}

	@Override
	public Object caseEdgeRhsNode(EdgeRhsNode object) {
		// Set the flag for the node_id case handled above
		gotSource = true;
		return super.caseEdgeRhsNode(object);
	}

	@Override
	public Object caseSubgraph(Subgraph object) {
		return super.caseSubgraph(object);
	}

	// private implementation of the cases above

	private void createGraph(MainGraph object) {
		graph.attr(Graph.Attr.LAYOUT.toString(),
				DotImport.DEFAULT_LAYOUT_ALGORITHM);
		GraphType graphType = object.getType();
		graph.attr(Graph.Attr.GRAPH_TYPE.toString(),
				graphType == GraphType.DIGRAPH ? ZestStyle.GRAPH_DIRECTED
						: ZestStyle.GRAPH_UNDIRECTED);
	}

	private void createAttributes(final AttrStmt attrStmt) {
		AttributeType type = attrStmt.getType();
		switch (type) {
		case EDGE: {
			globalEdgeStyle = getAttributeValue(attrStmt, "style"); //$NON-NLS-1$
			globalEdgeLabel = getAttributeValue(attrStmt, "label"); //$NON-NLS-1$
			break;
		}
		case NODE: {
			globalNodeLabel = getAttributeValue(attrStmt, "label"); //$NON-NLS-1$
			break;
		}
		case GRAPH: {
			for (AList a : attrStmt.getAttributes().get(0).getA_list()) {
				graph.attr(a.getName(), a.getValue());
			}
			String graphLayout = getAttributeValue(attrStmt, "layout"); //$NON-NLS-1$
			if (graphLayout != null) {
				Layout layout = Enum.valueOf(Layout.class,
						graphLayout.toUpperCase());
				graph.attr(Graph.Attr.LAYOUT.toString(), layout.algorithm);
			}
			break;
		}
		}
	}

	private void createNode(final NodeStmt nodeStatement) {
		String nodeId = escaped(nodeStatement.getName());
		Node.Builder node = new Node.Builder();
		if (nodes.containsKey(nodeId)) {
			node = nodes.get(nodeId);
		} else {
			node = node.attr(Graph.Attr.LABEL.toString(), nodeId).attr(
					Graph.Attr.ID.toString(), nodeId);
			graph = graph.nodes(node.build());
		}
		String value = getAttributeValue(nodeStatement, "label"); //$NON-NLS-1$
		if (value != null) {
			node = node.attr(Graph.Attr.LABEL.toString(), value);
		} else if (globalNodeLabel != null) {
			node = node.attr(Graph.Attr.LABEL.toString(), globalNodeLabel);
		}
		nodes.put(nodeId, node);
	}

	private Node node(String id) {
		if (!nodes.containsKey(id)) { // undeclared node, as in "graph{1->2}"
			Node.Builder node = new Node.Builder().attr(
					Graph.Attr.LABEL.toString(),
					globalNodeLabel != null ? globalNodeLabel : id).attr(
					Graph.Attr.ID.toString(), id);
			nodes.put(id, node);
			graph = graph.nodes(node.build());
		}
		return nodes.get(id).build();
	}

	/**
	 * @param eStatementObject
	 *            The statement object, e.g. the object corresponding to
	 *            "node[label="hi"]"
	 * @param attributeName
	 *            The name of the attribute to get the value for, e.g. "label"
	 * @return The value of the given attribute, e.g. "hi"
	 */
	private String getAttributeValue(final Stmt eStatementObject,
			final String attributeName) {
		Iterator<EObject> nodeContents = eStatementObject.eContents()
				.iterator();
		while (nodeContents.hasNext()) {
			EObject nodeContentElement = nodeContents.next();
			if (nodeContentElement instanceof AttrList) {
				Iterator<EObject> attributeContents = nodeContentElement
						.eContents().iterator();
				while (attributeContents.hasNext()) {
					EObject next = attributeContents.next();
					if (next instanceof AList) {
						AList attributeElement = (AList) next;
						if (attributeElement.getName().equals(attributeName)) {
							return escaped(attributeElement.getValue());
						}
					}
				}
			}
		}
		return null;
	}

	private String escaped(String id) {
		return id
		/* In DOT, an ID can be quoted... */
		.replaceAll("^\"|\"$", "") //$NON-NLS-1$//$NON-NLS-2$
				/*
				 * ...and may contain escaped quotes, see footnote on
				 * http://www.graphviz.org/doc/info/lang.html
				 */
				.replaceAll("\\\\\"", "\""); //$NON-NLS-1$//$NON-NLS-2$
	}
}