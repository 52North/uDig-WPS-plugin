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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.n52.wps.client.udig.WPSInputLayer;
import org.n52.wps.client.udig.WPSProcess;

public class WPSGETRequestDataInputs{
	private WPSProcess process;
	
	
	
	
	public WPSGETRequestDataInputs(WPSProcess process){
		this.process = process;
	}
	
	public String toString(){
		String tempResult = null;
		String wpsGetInputValues = "DataInputs=";
		HashMap<String, Object> inputValues = process.getInputValues();
		for(Map.Entry<String, Object> entry: inputValues.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			//add a ; seperator after the previous inputvalue
			IWPSGetRequestDataInput wpsGETRequestDataInput = null;
			//complexValue
			if(value instanceof WPSInputLayer){
				WPSInputLayer layer = (WPSInputLayer) value;
				wpsGETRequestDataInput = new WPSGETRequestComplexDataInput(layer.getURL(), process.getSchemaInput(key), key);
			}
			//literal value
			else if(value instanceof String){
				wpsGETRequestDataInput = new WPSGETRequestLiteralDataInput(key, value);
			}
			else if(value instanceof WPSProcess) {
				WPSProcess process = (WPSProcess) value;
				try {
					String resultURL = process.getResultReference();
					wpsGETRequestDataInput = new WPSGETRequestComplexDataInput(resultURL, process.getSchemaOutput(key), key);
				} catch (IOException e) {
					throw new IllegalArgumentException("There went something wrong creating the reference for the Execute request parameter: " +key,e);
				}
			}
			if(wpsGETRequestDataInput == null){
					throw new RuntimeException("Input Value incompatible");
			}
			if(tempResult != null){
				tempResult = ";";
			}
			else {
				tempResult = "";
			}
			wpsGetInputValues = wpsGetInputValues + tempResult+wpsGETRequestDataInput.toString();
		}
		
		
		
		return wpsGetInputValues;
	}
	

	
	
	
	
}
