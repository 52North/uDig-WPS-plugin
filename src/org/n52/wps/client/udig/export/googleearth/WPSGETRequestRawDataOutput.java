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
