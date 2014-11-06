/*
* Copyright (C) 2007 - 2010 52°North Initiative for Geospatial Open Source Software GmbH
*
* This program is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License version 2 as published
* by the Free Software Foundation.
*
* If the program is linked with libraries which are licensed under one of
* the following licenses, the combination of the program with the linked
* library is not considered a "derivative work" of the program:
*
* - Apache License, version 2.0
* - Apache Software License, version 1.0
* - GNU Lesser General Public License, version 3
* - Mozilla Public License, versions 1.0, 1.1 and 2.0
* - Common Development and Distribution License (CDDL), version 1.0
*
* Therefore the distribution of the program linked with libraries licensed
* under the aforementioned licenses, is permitted by the copyright holders
* if the distribution is compliant with both the GNU General Public
* License version 2 and the aforementioned licenses.
*
* This program is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
* Public License for more details.
*/
package org.n52.wps.client.udig.export.googleearth;

import net.refractions.udig.catalog.ui.export.ExportResourceSelectionPage;
import net.refractions.udig.project.ILayer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.n52.wps.client.udig.UdigHelper;
import org.n52.wps.client.udig.export.googleearth.ExportGoogleEarthSelectionState.UpdateStrategy;

public class ExportGoogleEarthSelectionPage extends ExportResourceSelectionPage {

	Text processNameText = null;
	Spinner updateIntervalSpinner = null;
	Combo updateStrategyCombo = null;
	
	private static String STRATEGY_STATIC = "Static";
	private static String STRATEGY_DYNAMIC = "Dynamic";
	private static String STRATEGY_SEMI_DYNAMIC = "Semi-dynamic";
	/**
	 * @param pageName
	 * @param title
	 * @param banner
	 */
	public ExportGoogleEarthSelectionPage(String pageName, String title, ImageDescriptor banner) {
		super(pageName, title, banner);
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		super.createControl(parent);
		Composite control = (Composite)this.getControl();

		//PROCESS NAME
		Composite processComp = new Composite(control, SWT.NONE);
		processComp.setLayout(new GridLayout(2,false));
		processComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label processLabel = new Label(processComp, SWT.NONE);
		processLabel.setText("Name of the KML Layer");
		processNameText = new Text(processComp, SWT.BORDER);
		String layerID = this.getState().getLayers().get(0).getResource().getIdentifier().toString();
		String layerName = this.getLayerNameByID(layerID);
		ExportGoogleEarthSelectionState state = (ExportGoogleEarthSelectionState)this.getState();
		state.setKMLlayerName(layerName);
		processNameText.setText(layerName);
        processNameText.addListener(SWT.Modify, new Listener(){

			public void handleEvent(Event event) {
				((ExportGoogleEarthSelectionState)getState()).setKMLlayerName(processNameText.getText());
			}
        	
        });
	    // General update strategy
        Composite updateStrategyComp = new Composite(control, SWT.NONE);
        updateStrategyComp.setLayout(new GridLayout(2,false));
        updateStrategyComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label updateStrategyLabel = new Label(updateStrategyComp, SWT.NONE);
        updateStrategyLabel.setText("Retrieve strategy");
        updateStrategyCombo = new Combo(updateStrategyComp, SWT.BORDER);
        updateStrategyCombo.add(STRATEGY_STATIC, 0);
        updateStrategyCombo.add(STRATEGY_DYNAMIC, 1);
//        updateStrategyCombo.add(STRATEGY_SEMI_DYNAMIC, 1);
        updateStrategyCombo.select(0);
        ((ExportGoogleEarthSelectionState)getState()).setUpdateStrategy(UpdateStrategy.STATIC);
        updateStrategyCombo.addListener(SWT.Modify, new Listener() {
        	
        	public void handleEvent(Event e) {
        		int selectionPos = updateStrategyCombo.getSelectionIndex();
        		if(selectionPos == 0) {
        			((ExportGoogleEarthSelectionState)getState()).setUpdateStrategy(UpdateStrategy.STATIC);
        			updateIntervalSpinner.setEnabled(false);
        		}
        		if(selectionPos == 1) {
        			((ExportGoogleEarthSelectionState)getState()).setUpdateStrategy(UpdateStrategy.DYMANIC);
        			updateIntervalSpinner.setEnabled(true);
        		}
//        		if(selectionPos == 2) {
//        			((ExportGoogleEarthSelectionState)getState()).setUpdateStrategy(UpdateStrategy.SEMIDYNAMIC);
//        			updateIntervalSpinner.setEnabled(true);
//        		}
        	}
        	
        });
        
	    //KML update INTERVAL SIZE
		Composite intervalComp = new Composite(control, SWT.NONE);
		intervalComp.setLayout(new GridLayout(2,false));
		intervalComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label intervalLabel = new Label(intervalComp, SWT.NONE);
		intervalLabel.setText("update interval (in seconds)");
		updateIntervalSpinner = new Spinner(intervalComp, SWT.BORDER);
		updateIntervalSpinner.setSelection(20);
		updateIntervalSpinner.setEnabled(false);
        updateIntervalSpinner.addListener(SWT.Modify, new Listener(){

			public void handleEvent(Event event) {
				((ExportGoogleEarthSelectionState)getState()).setUpdateInterval(updateIntervalSpinner.getSelection());
			}
        	
        });
		
		updateIntervalSpinner.pack();

	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		// TODO Auto-generated method stub
		super.checkStateChanged(event);
	}

	@Override
	public void syncWithUI() {
		// TODO Auto-generated method stub
		super.syncWithUI();
		ExportGoogleEarthSelectionState state = (ExportGoogleEarthSelectionState)this.getState();
		if(processNameText != null) {
			state.setKMLlayerName(processNameText.getText());
		}
		if(updateIntervalSpinner != null) {
			state.setUpdateInterval(updateIntervalSpinner.getSelection());
		}
		
	}

	private String getLayerNameByID(String layerID) {
		for(ILayer layer :UdigHelper.getAllLayers()) {
			if(layer.getID().toString().equals(layerID)) {
				return layer.getName();
			}
		}
		return null;
	}
	
}
