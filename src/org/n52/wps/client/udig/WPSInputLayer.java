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
package org.n52.wps.client.udig;

import java.io.IOException;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.type.FeatureType;

/**
 * @author Theodor Foerster
 *
 */
public class WPSInputLayer {

	private FeatureCollection features;
	private String layerName;
	private String url;
	private ProcessMode processMode;
	
	// this indicates,how the wps layer should be processed. 
	public enum ProcessMode {SELECTED_FEATURES, FEATURES_AS_REFERENCE, ALL_FEATURES}; 
	/**
	 * 
	 * @param layerName name of the inputLayer
	 * @param selectedFeaturesOnly
	 * @throws IOException
	 * @throws IllegalAttributeException
	 */
	public WPSInputLayer(String layerName, ProcessMode pm) throws IOException, IllegalAttributeException {
		this.layerName = layerName;
		this.processMode = pm;
		if(processMode.compareTo(ProcessMode.SELECTED_FEATURES) == 0){
			this.features = UdigHelper.getFeaturesOfLayer(layerName);
		}
		else {
			this.features =  UdigHelper.getFeaturesOfLayer(layerName);
		}
	}

	/**
	 * @param layerName name of the inputLayer
	 * @param string url reference of the inputLayer
	 * @param selectedFeaturesOnly
	 */
	public WPSInputLayer(String layerName, String url, ProcessMode pm) throws IOException, IllegalAttributeException{
		this(layerName, pm);
		this.url = url;
	}

	public ProcessMode getProcessMode() {
		return processMode;
	}
	
	public FeatureCollection getFeatures() {
		return features;
	}
	
	public FeatureType getFeatureType() throws IOException {
		return features.getSchema();
	}
	
	public String getURL() {
		return url;
	}
	
	public boolean equals(Object o) {
		WPSInputLayer v = (WPSInputLayer) o;
		if(!super.equals(v)) {
			return false;
		}
		if(url != null) {
			if(v.getURL() == null) {
				return false;
			}
			if(!url.equals(v.getURL())) {
				return false;
			}
		}
		
		return true;
	}
}
