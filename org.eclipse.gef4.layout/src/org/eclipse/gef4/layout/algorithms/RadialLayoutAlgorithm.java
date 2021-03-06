/*******************************************************************************
 * Copyright (c) 2005-2010 The Chisel Group and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Chisel Group - initial API and implementation
 *               Mateusz Matela 
 *               Ian Bull
 ******************************************************************************/
package org.eclipse.gef4.layout.algorithms;

import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.Rectangle;
import org.eclipse.gef4.layout.LayoutAlgorithm;
import org.eclipse.gef4.layout.interfaces.EntityLayout;
import org.eclipse.gef4.layout.interfaces.LayoutContext;

/**
 * This layout will take the given entities, apply a tree layout to them, and
 * then display the tree in a circular fashion with the roots in the center.
 * 
 * @author Casey Best
 * @auhtor Rob Lintern
 */
public class RadialLayoutAlgorithm implements LayoutAlgorithm {

	private static final double MAX_DEGREES = Math.PI * 2;
	private double startDegree = 0;
	private double endDegree = MAX_DEGREES;

	private LayoutContext context;
	private boolean resize = false;

	private TreeLayoutAlgorithm treeLayout = new TreeLayoutAlgorithm();

	public RadialLayoutAlgorithm() {
	}

	public void applyLayout(boolean clean) {
		if (!clean)
			return;
		treeLayout.internalApplyLayout();
		EntityLayout[] entities = context.getEntities();
		Rectangle bounds = context.getBounds();
		computeRadialPositions(entities, bounds);
		if (resize)
			AlgorithmHelper.maximizeSizes(entities);
		int insets = 4;
		bounds.setX(bounds.getX() + insets);
		bounds.setY(bounds.getY() + insets);
		bounds.setWidth(bounds.getWidth() - 2 * insets);
		bounds.setHeight(bounds.getHeight() - 2 * insets);
		AlgorithmHelper.fitWithinBounds(entities, bounds, resize);
	}

	private void computeRadialPositions(EntityLayout[] entities,
			Rectangle bounds) {
		Rectangle layoutBounds = AlgorithmHelper.getLayoutBounds(entities,
				false);
		layoutBounds.setX(bounds.getX());
		layoutBounds.setWidth(bounds.getWidth());
		for (int i = 0; i < entities.length; i++) {
			Point location = entities[i].getLocation();
			double percenttheta = (location.x - layoutBounds.getX())
					/ layoutBounds.getWidth();
			double distance = (location.y - layoutBounds.getY())
					/ layoutBounds.getHeight();
			double theta = startDegree + Math.abs(endDegree - startDegree)
					* percenttheta;
			location.x = distance * Math.cos(theta);
			location.y = distance * Math.sin(theta);
			entities[i].setLocation(location.x, location.y);
		}
	}

	public void setLayoutContext(LayoutContext context) {
		this.context = context;
		treeLayout.setLayoutContext(context);
	}

	/**
	 * Set the range the radial layout will use when applyLayout is called. Both
	 * values must be in radians.
	 */
	public void setRangeToLayout(double startDegree, double endDegree) {
		this.startDegree = startDegree;
		this.endDegree = endDegree;
	}

	/**
	 * 
	 * @return true if this algorithm is set to resize elements
	 */
	public boolean isResizing() {
		return resize;
	}

	/**
	 * 
	 * @param resizing
	 *            true if this algorithm should resize elements (default is
	 *            false)
	 */
	public void setResizing(boolean resizing) {
		resize = resizing;
		treeLayout.setResizing(resize);
	}
}
