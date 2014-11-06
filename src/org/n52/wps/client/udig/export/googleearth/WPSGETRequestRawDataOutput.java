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

import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;

import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;

public class WPSGETRequestRawDataOutput {
	
	public static final String DEFAULT_KML_NS = "http://www.opengis.net/kml/2.2";
	public static final String DEFAULT_KML_MIME_TYPE = "application/vnd.google-earth.kml+xml";
	
	private String processIdentifier;
	private String mimeType;
	private String encoding;
	private String schema;
	private String wpsURL;
	
	public WPSGETRequestRawDataOutput(String processIdentifier, String wpsURL) {
		this.processIdentifier = processIdentifier;
		this.wpsURL = wpsURL;
		
	}
	
	public WPSGETRequestRawDataOutput(String processIdentifier, String mimeType, String wpsURL) {
		this.processIdentifier = processIdentifier;
		this.mimeType = mimeType;
		this.wpsURL = wpsURL;
	}
	
	public WPSGETRequestRawDataOutput(String processIdentifier, String mimeType, String encoding, String wpsURL) {
		this.processIdentifier = processIdentifier;
		this.mimeType = mimeType;
		this.encoding = encoding;
		this.wpsURL = wpsURL;
	}
	
	public WPSGETRequestRawDataOutput(String processIdentifier, String mimeType, String encoding, String schema, String wpsURL) {
		this.processIdentifier = processIdentifier;
		this.mimeType = mimeType;
		this.encoding = encoding;
		this.schema = schema;
		this.wpsURL = wpsURL;
	}

	public String toString(){
		if(mimeType==null){
			mimeType=DEFAULT_KML_MIME_TYPE;
		}

		return "RawDataOutput="+getOutputIdentifier()+"@mimeType="+mimeType+"@schema="+ DEFAULT_KML_NS;

	}
	
	private String getOutputIdentifier(){
		WPSClientSession session = WPSClientSession.getInstance();
		try {
			session.connect(wpsURL);
			ProcessOutputs processOutputs = session.getProcessDescription(wpsURL, processIdentifier).getProcessOutputs();
			OutputDescriptionType[] outputArray = processOutputs.getOutputArray();
			if(outputArray.length>0){
				return outputArray[0].getIdentifier().getStringValue();
			}
		} catch (WPSClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		return "";
	}
}
