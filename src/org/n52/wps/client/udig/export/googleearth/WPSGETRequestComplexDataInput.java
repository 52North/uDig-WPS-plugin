package org.n52.wps.client.udig.export.googleearth;

import java.net.URLEncoder;

public class WPSGETRequestComplexDataInput implements IWPSGetRequestDataInput{
	private String url;
	private String schema;
	private String key;
	
	
	public WPSGETRequestComplexDataInput(String url, String schema, String key) {
		this.url = url;
		this.key = key;
		this.schema = schema;
	}

	public String toString(){
		String urlEncodedInput = URLEncoder.encode(url);
		return  key+"="+ "@mimeType=text/xml@href=" + urlEncodedInput+"@Schema="+schema;
	}

	private String getSchema(String layerURL) {
		//TODO
		String supportedSchema = "http://schemas.opengis.net/gml/2.1.2/feature.xsd";
		return supportedSchema;
	}
	
	
}
