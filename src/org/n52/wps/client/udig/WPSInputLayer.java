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
package org.n52.wps.client.udig;

import java.io.IOException;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.type.FeatureType;

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
