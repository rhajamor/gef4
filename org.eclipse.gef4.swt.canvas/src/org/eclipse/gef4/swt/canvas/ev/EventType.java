/*******************************************************************************
 * Copyright (c) 2013 itemis AG and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 * 
 *******************************************************************************/
package org.eclipse.gef4.swt.canvas.ev;

public class EventType<T extends Event> {

	public static final EventType<Event> ANY = new EventType<Event>();

	private EventType<? super T> superType;
	private String name;

	public EventType() {
	}

	public EventType(EventType<? super T> superType, String name) {
		this.superType = superType;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public EventType<? super T> getSuperType() {
		return superType;
	}

	@Override
	public String toString() {
		return "EventType (" + name + ")";
	}

}