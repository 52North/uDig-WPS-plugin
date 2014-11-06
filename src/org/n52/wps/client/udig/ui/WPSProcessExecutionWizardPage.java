/***************************************************************
Copyright © 2007 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Theodor Foerster, ITC

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.client.udig.ui;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.wfs.WFSServiceImpl;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;
import net.refractions.udig.catalog.ui.workflow.EndConnectionState;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizardPage;
import net.refractions.udig.project.ILayer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.geotools.feature.IllegalAttributeException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.client.udig.UdigHelper;
import org.n52.wps.client.udig.WPSGeoResourceImpl;
import org.n52.wps.client.udig.WPSInputLayer;
import org.n52.wps.client.udig.WPSServiceImpl;
import org.n52.wps.client.udig.WPSInputLayer.ProcessMode;
import org.n52.wps.geoserver.WPSDataStore;
import org.n52.wps.geoserver.WPSDataStoreFactory;

public class WPSProcessExecutionWizardPage extends WorkflowWizardPage implements
		UDIGConnectionPage {
	private WPSDataStore wpsDataStore;
	private HashMap<String, Object> inputValues;
	private boolean isFaulty = false;

	protected WPSProcessExecutionWizardPage(String pageName) {
		super(pageName);
		inputValues = new HashMap<String, Object>();
		super.setTitle("Web Processing Service");
		super
				.setDescription("Set the Input values for the previously selected process");

	}

	public Map<String, Serializable> getParams() {
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(WPSDataStoreFactory.INPUTVALUES, inputValues);
		params.put(WPSDataStoreFactory.ADDMEMBER.key, true);
		return params;
	}

	public void createControl(Composite parent) {
		Composite composite = new Group(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));

		Map<String, Serializable> selectedProcessIDList = (Map<String, Serializable>) ((EndConnectionState) getState()).getWorkflow().getContext();
				
		String selectedProcessID = (String) selectedProcessIDList
				.get(WPSDataStoreFactory.PROCESS_ID.key);
		ProcessDescriptionType descriptionType;
		WPSClientSession session = WPSDataStoreFactory.getInstance()
				.getSession();
		String wpsURL = (String) selectedProcessIDList.get(WPSDataStoreFactory.URL.key);
		try {
			descriptionType = session.getProcessDescription(wpsURL,
					selectedProcessID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		DataInputs dataInputs = descriptionType.getDataInputs();
		InputDescriptionType[] inputDescriptions = dataInputs.getInputArray();

		for (int i = 0; i < inputDescriptions.length; i++) {
			final InputDescriptionType currentDescriptionType = inputDescriptions[i];
			Label label = new Label(composite, SWT.CENTER);
			label.setText(currentDescriptionType.getTitle().getStringValue());

			// Analyse complexData
			if (currentDescriptionType.getComplexData() != null) {
				final Combo layerCombo = new Combo(composite, SWT.CENTER);
				// Create LayerCombo
				layerCombo.addSelectionListener(new SelectionListener() {

					public void widgetDefaultSelected(SelectionEvent e) {

					}

					public void widgetSelected(SelectionEvent e) {
						checkLayersValidity(layerCombo, currentDescriptionType);
						getContainer().updateButtons();
					}

					private Serializable checkSelectedFeatures(String item) {
						return null;
					}

				});
				List<ILayer> layerList = UdigHelper.getAllLayers();
				Iterator<ILayer> layerIterator = layerList.iterator();
				// Populate layerCombo
				while (layerIterator.hasNext()) {
					ILayer layer = layerIterator.next();
					if (layer != null && layer.getName() != null) {
						layerCombo.add(layer.getName());
					}
				}
				if (layerCombo.getItemCount() > 0) {
					layerCombo.select(0);
					checkLayersValidity(layerCombo, currentDescriptionType);
				}
			}
			// analyse literalData
			else if (currentDescriptionType.getLiteralData() != null) {
				if (currentDescriptionType.getLiteralData().getDataType()
						.getReference().equals("xs:boolean")) {
					final Combo combo = new Combo(composite, SWT.CENTER);
					combo.add("true");
					combo.add("false");
					combo.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent e) {
							Object newInputValue = new String(combo.getText());
							inputValues.put(currentDescriptionType
									.getIdentifier().getStringValue(),
									newInputValue);
							getWizard().getWorkflow().setContext(mergeParams((Map<String, Serializable>) getWizard().getWorkflow().getContext(), getParams()));

						}

						public void widgetDefaultSelected(SelectionEvent e) {
							inputValues.put(currentDescriptionType
									.getIdentifier().getStringValue(),
									Boolean.TRUE);
							getWizard().getWorkflow().setContext(mergeParams((Map<String, Serializable>) getWizard().getWorkflow().getContext(), getParams()));
						}
					});
					// standard is true
					inputValues.put(currentDescriptionType.getIdentifier()
							.getStringValue(), Boolean.TRUE);
					combo.select(0);
					getWizard().getWorkflow().setContext(mergeParams((Map<String, Serializable>) getWizard().getWorkflow().getContext(), getParams()));
				} else {
					final Text valueField = new Text(composite, SWT.CENTER);
					valueField.addModifyListener(new ModifyListener() {

						public void modifyText(ModifyEvent e) {
							Object newInputValue = new String(valueField
									.getText());
							inputValues.put(currentDescriptionType
									.getIdentifier().getStringValue(),
									newInputValue);
							getWizard().getWorkflow().setContext(mergeParams((Map<String, Serializable>) getWizard().getWorkflow().getContext(), getParams()));
						}
						
					});
				}

			}
		}
		getWizard().getWorkflow().setContext(mergeParams((Map<String, Serializable>) getWizard().getWorkflow().getContext(), getParams()));
		setControl(composite);

	}

	public boolean isPageComplete() {
		if (isFaulty) {
			if ((Boolean) ((HashMap<String, Object>) ((EndConnectionState) getState()).getWorkflow().getContext()).get(
					WPSDataStoreFactory.SELECTED_FEATURE_REQUEST_STRATEGY.key)) {
				this.setErrorMessage("Selected Layer has no selected Features");
				return false;
			} else {
				// send only selected features
				// look if any features are selected
				this.setErrorMessage("Something went wrong with the layer");
				return false;
			}
		}
		this.setErrorMessage(null);
		EndConnectionState state = (EndConnectionState) getState();
		state.getWorkflow().setContext((mergeParams((Map<String, Serializable>) state.getWorkflow().getContext(), this.getParams())));
		return true;
	}

	private Map<String, Serializable> mergeParams(
			Map<String, Serializable> params, Map<String, Serializable> params2) {
		params.putAll(params2);
		return params;
	}

	private boolean isWFSReferenceLayer(String item) {
		List<ILayer> layerList = UdigHelper.getAllLayers();
		Iterator<ILayer> layerIterator = layerList.iterator();
		while (layerIterator.hasNext()) {
			ILayer layer = layerIterator.next();
			IGeoResource georesource = layer.getGeoResource();

			try {
				IService service = georesource.service(null);
				if (layer.getName().equals(item)
						&& service instanceof WFSServiceImpl) {
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private boolean isWPSLayer(String item) {
		List<ILayer> layerList = UdigHelper.getAllLayers();
		Iterator<ILayer> layerIterator = layerList.iterator();
		while (layerIterator.hasNext()) {
			ILayer layer = layerIterator.next();
			IGeoResource georesource = layer.getGeoResource();

			try {
				IService service = georesource.service(null);
				if (layer.getName() != null) {
					if (layer.getName().equals(item)
							&& service instanceof WPSServiceImpl) {
						return true;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private String lookUpReference(String item) {
		List<ILayer> layerList = UdigHelper.getAllLayers();
		Iterator<ILayer> layerIterator = layerList.iterator();
		while (layerIterator.hasNext()) {
			ILayer layer = layerIterator.next();
			IGeoResource georesource = layer.getGeoResource();
			isFaulty = true;
			try {
				IService service = georesource.service(null);
				if (service instanceof WFSServiceImpl
						&& layer.getName().equals(item)) {
					isFaulty = false;
					WFSServiceImpl wfs = (WFSServiceImpl) service;
					String url = wfs.getIdentifier().toExternalForm();
					return Pattern.compile("REQUEST=GetCapabilities",
							Pattern.CASE_INSENSITIVE).matcher(url)
							.replaceFirst(
									"REQUEST=GetFeature&typename="
											+ layer.getID().getRef());
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			 * LayerResource layerResource = (LayerResource)
			 * layer.getGeoResource(); IGeoResourceInfo info =
			 * layerResource.getInfo(null); if (info instanceof WFSGeoResource)
			 * {
			 * 
			 * 
			 * } layerResource.getIdentifier().get;
			 */

		}

		return null;

	}

	/**
	 * This checks if the selected layer has selected Features and adds layers
	 * if they are valid
	 * 
	 * @param layerCombo
	 * @param currentDescriptionType
	 */
	private void checkLayersValidity(Combo layerCombo,
			InputDescriptionType currentDescriptionType) {
		isFaulty = false;
		String parameterID = currentDescriptionType.getIdentifier()
				.getStringValue();
		String layerName = layerCombo.getItem(layerCombo.getSelectionIndex());
		if ((Boolean) ((HashMap<String, Object>) ((EndConnectionState) getState()).getWorkflow().getContext()).get(
				WPSDataStoreFactory.SELECTED_FEATURE_REQUEST_STRATEGY.key)) {
			try {
				if (!UdigHelper.hasSelectedFeatures(layerName)) {
					isFaulty = true;
				} else {
					isFaulty = false;
				}
			} catch (IOException e1) {
				isFaulty = true;
				e1.printStackTrace();
			}

			try {

				WPSInputLayer newInputValue = new WPSInputLayer(layerName,
						WPSInputLayer.ProcessMode.SELECTED_FEATURES);
				inputValues.put(parameterID, newInputValue);
				getWizard().getWorkflow().setContext(mergeParams((Map<String, Serializable>) getWizard().getWorkflow().getContext(), getParams()));
			} catch (IOException e1) {
				isFaulty = true;
				e1.printStackTrace();
			} catch (IllegalAttributeException e2) {
				isFaulty = true;
				e2.printStackTrace();
			}
		}// endif selectedFeatureRequestStrategy
		else {
			Object newInputValue = null;
			try {
				if (isWPSLayer(layerName)) {
					newInputValue = ((WPSGeoResourceImpl) UdigHelper
							.getLayerForName(layerName).getGeoResource()
							.resolve(WPSGeoResourceImpl.class, null))
							.getProcess();
				} else if (isWFSReferenceLayer(layerName)) {
					newInputValue = new WPSInputLayer(layerCombo
							.getItem(layerCombo.getSelectionIndex()),
							lookUpReference(layerCombo.getItem(layerCombo
									.getSelectionIndex())),
							ProcessMode.FEATURES_AS_REFERENCE);
				} else {
					newInputValue = new WPSInputLayer(layerName,
							ProcessMode.ALL_FEATURES);
					isFaulty = false;
				}
				inputValues.put(parameterID, newInputValue);
				getWizard().getWorkflow().setContext(mergeParams((Map<String, Serializable>) getWizard().getWorkflow().getContext(), getParams()));
			} catch (IOException e1) {
				isFaulty = true;
				e1.printStackTrace();
			} catch (IllegalAttributeException e2) {
				isFaulty = true;
				e2.printStackTrace();
			}
		}
	}

	private boolean isFileFeatureLayer() {
		List<ILayer> layerList = UdigHelper.getAllLayers();
		Iterator<ILayer> layerIterator = layerList.iterator();
		while (layerIterator.hasNext()) {
			ILayer layer = layerIterator.next();
			IGeoResource georesource = layer.getGeoResource();

			try {
				IService service = georesource.service(null);
				/*
				 * if(layer.getName().equals(item) && service instanceof
				 * WFSServiceImpl) { return true; }
				 */
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public Collection<URL> getResourceIDs() {
		return new ArrayList<URL>();
	}

	@Override
	public Collection<IService> getServices() {
		Collection<IService> list = new ArrayList<IService>();
		try {
			WPSServiceImpl wps = new WPSServiceImpl(new URL("http://localhost:8080/wps/WebProcessingService"), getParams());
			list.add(wps);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     return list;
	}
}
