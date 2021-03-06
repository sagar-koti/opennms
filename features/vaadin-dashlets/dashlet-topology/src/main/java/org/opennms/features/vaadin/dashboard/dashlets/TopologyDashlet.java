/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.model.*;

/**
 * This class implements a {@link Dashlet} for displaying the topology map application.
 *
 * @author Christian Pape
 */
public class TopologyDashlet extends AbstractDashlet {

    private DashletComponent m_dashletComponent;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     */
    public TopologyDashlet(String name, DashletSpec dashletSpec) {
        super(name, dashletSpec);
    }

    @Override
    public DashletComponent getWallboardComponent() {
        if (m_dashletComponent == null) {
            m_dashletComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setSizeFull();
                }

                @Override
                public void refresh() {
                    m_verticalLayout.removeAllComponents();

                    String focusNodes = "";
                    String szl = "";
                    String provider = "";

                    if (getDashletSpec().getParameters().containsKey("focusNodes")) {
                        focusNodes = getDashletSpec().getParameters().get("focusNodes");
                    }

                    if (getDashletSpec().getParameters().containsKey("szl")) {
                        szl = getDashletSpec().getParameters().get("szl");
                    }

                    if (getDashletSpec().getParameters().containsKey("provider")) {
                        provider = getDashletSpec().getParameters().get("provider");
                    }

                    String query = "";

                    if (!"".equals(focusNodes)) {
                        query += "focusNodes=" + focusNodes + "&";
                    }

                    if (!"".equals(szl)) {
                        query += "szl=" + szl + "&";
                    }

                    if (!"".equals(provider)) {
                        query += "provider=" + provider + "&";
                    }
                    /**
                     * creating browser frame to display node-maps
                     */
                    BrowserFrame browserFrame = new BrowserFrame(null, new ExternalResource("/opennms/topology?" + query));
                    browserFrame.setSizeFull();
                    m_verticalLayout.addComponent(browserFrame);
                }

                @Override
                public Component getComponent() {
                    return m_verticalLayout;
                }
            };
        }

        return m_dashletComponent;
    }

    @Override
    public DashletComponent getDashboardComponent() {
        return getWallboardComponent();
    }
}
