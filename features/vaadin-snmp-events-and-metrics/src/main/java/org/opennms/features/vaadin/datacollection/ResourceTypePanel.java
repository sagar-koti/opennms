/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS ResourceType, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS ResourceType, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS ResourceType, Inc.
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
package org.opennms.features.vaadin.datacollection;

import java.util.List;

import org.opennms.features.vaadin.api.Logger;
import org.opennms.features.vaadin.config.EditorToolbar;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.ResourceType;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class ResourceTypePanel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class ResourceTypePanel extends Panel {

    /** The isNew flag. True, if the resource type is new. */
    private boolean isNew;

    /** The resource type table. */
    private final ResourceTypeTable resourceTypeTable;

    /**
     * Instantiates a new resource type panel.
     *
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param source the OpenNMS Data Collection ResourceType object
     * @param logger the logger object
     */
    public ResourceTypePanel(final DataCollectionConfigDao dataCollectionConfigDao, final DatacollectionGroup source, final Logger logger) {

        if (dataCollectionConfigDao == null)
            throw new RuntimeException("dataCollectionConfigDao cannot be null.");

        if (source == null)
            throw new RuntimeException("source cannot be null.");

        addStyleName("light");

        resourceTypeTable = new ResourceTypeTable(source.getResourceTypeCollection());

        final ResourceTypeForm resourceTypeForm = new ResourceTypeForm();
        resourceTypeForm.setVisible(false);

        final EditorToolbar bottomToolbar = new EditorToolbar() {
            @Override
            public void save() {
                ResourceType resourceType = resourceTypeForm.getResourceType();
                logger.info("Resource Type " + resourceType.getName() + " has been " + (isNew ? "created." : "updated."));
                try {
                    resourceTypeForm.getFieldGroup().commit();
                    resourceTypeForm.setReadOnly(true);
                    resourceTypeTable.refreshRowCache();
                } catch (CommitException e) {
                    String msg = "Can't save the changes: " + e.getMessage();
                    logger.error(msg);
                    Notification.show(msg, Notification.Type.ERROR_MESSAGE);
                }
            }
            @Override
            public void delete() {
                Object resourceTypeId = resourceTypeTable.getValue();
                if (resourceTypeId != null) {
                    ResourceType resourceType = resourceTypeTable.getResourceType(resourceTypeId);
                    logger.info("SNMP ResourceType " + resourceType.getName() + " has been removed.");
                    resourceTypeTable.select(null);
                    resourceTypeTable.removeItem(resourceTypeId);
                    resourceTypeTable.refreshRowCache();
                }
            }
            @Override
            public void edit() {
                resourceTypeForm.setReadOnly(false);
            }
            @Override
            public void cancel() {
                resourceTypeForm.getFieldGroup().discard();
                resourceTypeForm.setReadOnly(true);
            }
        };
        bottomToolbar.setVisible(false);

        resourceTypeTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                Object resourceTypeId = resourceTypeTable.getValue();
                if (resourceTypeId != null) {
                    resourceTypeForm.setResourceType(resourceTypeTable.getResourceType(resourceTypeId));
                }
                resourceTypeForm.setReadOnly(true);
                resourceTypeForm.setVisible(resourceTypeId != null);
                bottomToolbar.setReadOnly(true);
                bottomToolbar.setVisible(resourceTypeId != null);
            }
        });   

        final Button add = new Button("Add Resource Type", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                resourceTypeTable.addResourceType(resourceTypeForm.createBasicResourceType());
                resourceTypeForm.setReadOnly(false);
                bottomToolbar.setReadOnly(false);
                setIsNew(true);
            }
        });

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(resourceTypeTable);
        mainLayout.addComponent(add);
        mainLayout.addComponent(resourceTypeForm);
        mainLayout.addComponent(bottomToolbar);
        mainLayout.setComponentAlignment(add, Alignment.MIDDLE_RIGHT);
        setContent(mainLayout);
    }

    /**
     * Sets the value of the ifNew flag.
     *
     * @param isNew true, if the resourceType is new.
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Gets the resource types.
     *
     * @return the resource types
     */
    public List<ResourceType> getResourceTypes() {
        return resourceTypeTable.getResourceTypes();
    }

}
