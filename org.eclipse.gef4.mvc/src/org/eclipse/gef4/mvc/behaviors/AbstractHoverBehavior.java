/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.gef4.mvc.behaviors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import org.eclipse.gef4.geometry.planar.IGeometry;
import org.eclipse.gef4.mvc.IProvider;
import org.eclipse.gef4.mvc.models.IHoverModel;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.mvc.parts.IHandlePart;

/**
 * The AbstractSelectionFeedbackPolicy is responsible for creating and removing
 * selection feedback.
 * 
 * @author anyssen
 * 
 * @param <V>
 */
public abstract class AbstractHoverBehavior<V> extends
		AbstractBehavior<V> implements PropertyChangeListener {

	private IProvider<IGeometry> feedbackGeometryProvider = new IProvider<IGeometry>() {
		@Override
		public IGeometry get() {
			return getFeedbackGeometry();
		}
	};
	
	private List<IHandlePart<V>> handles;
	
	@Override
	public void activate() {
		super.activate();
		getHost().getRoot().getViewer().getHoverModel()
				.addPropertyChangeListener(this);
	}

	@Override
	public void deactivate() {
		getHost().getRoot().getViewer().getHoverModel()
				.removePropertyChangeListener(this);
		super.deactivate();
	}
	
	/**
	 * Returns an {@link IGeometry} for which visual selection feedback will be
	 * provided.
	 * 
	 * @return an {@link IGeometry} determining feedback positions
	 */
	protected abstract IGeometry getFeedbackGeometry();

	@SuppressWarnings({ "unchecked" })
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getPropertyName().equals(IHoverModel.HOVER_PROPERTY)) {
			IContentPart<V> oldHovered = (IContentPart<V>) event.getOldValue();
			IContentPart<V> newHovered = (IContentPart<V>) event.getNewValue();

			if (oldHovered == getHost()) {
				if (handles != null && !handles.isEmpty()) {
					BehaviorUtils.removeHandles(getHost().getRoot(), Collections.singletonList((IContentPart<V>)getHost()), handles);
					handles.clear();
				}
				hideFeedback();
			} else if (newHovered == getHost()) {
				showFeedback();
				handles = createHandles();
				BehaviorUtils.addHandles(getHost().getRoot(), Collections.singletonList((IContentPart<V>)getHost()), handles);
			}
		}
	}
	
	public IProvider<IGeometry> getFeedbackGeometryProvider() {
		return feedbackGeometryProvider;
	}

	protected abstract void showFeedback();

	protected abstract void hideFeedback();

	protected List<IHandlePart<V>> createHandles() {
		return Collections.emptyList();
	}

}
