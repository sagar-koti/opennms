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


public class ApplicationInitializedEvent extends OpenNMSEvent<ApplicationInitializedEventHandler> {
    public static Type<ApplicationInitializedEventHandler> TYPE = new Type<ApplicationInitializedEventHandler>();

    public ApplicationInitializedEvent() {
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ApplicationInitializedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    public void dispatch(final ApplicationInitializedEventHandler handler) {
        handler.onApplicationInitialized(this);
    }

    @Override
    public String toDebugString() {
        return "event: ApplicationInitializedEvent:";
    }
}
