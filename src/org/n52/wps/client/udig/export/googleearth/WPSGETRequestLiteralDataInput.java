package org.n52.wps.client.udig.export.googleearth;

import java.net.URLEncoder;

public class WPSGETRequestLiteralDataInput implements IWPSGetRequestDataInput{
	private Object value;
	private String key;
	
	public WPSGETRequestLiteralDataInput(String key, Object value) {
		this.value = value;
		this.key = key;
	}

	
	public String toString(){
		String literalValue = (String) value;
		String urlEncodedLiteralValue = URLEncoder.encode(literalValue);
		return key + "="+ urlEncodedLiteralValue;
	}
}
