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
