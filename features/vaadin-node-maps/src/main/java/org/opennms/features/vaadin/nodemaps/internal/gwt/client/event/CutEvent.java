/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import com.google.gwt.event.dom.client.DomEvent;

/**
 * Represents a native change event.
 */
public class CutEvent extends DomEvent<CutHandler> {

  /**
   * Event type for cut events. Represents the meta-data associated with this
   * event.
   */
  private static final Type<CutHandler> TYPE = new Type<CutHandler>("cut", new CutEvent());

  /**
   * Gets the event type associated with change events.
   * 
   * @return the handler type
   */
  public static Type<CutHandler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
   * to fire change events.
   */
  protected CutEvent() {
  }

  @Override
  public final Type<CutHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final CutHandler handler) {
    handler.onCut(this);
  }

}
