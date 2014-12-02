/*
 * Copyright (C) 2007 - 2010 52°North Initiative for Geospatial Open Source
 * Software GmbH
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 * 
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 * 
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
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
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.LiteralDataType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ResponseDocumentType;

import org.apache.xmlbeans.XmlException;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.ExecuteResponseAnalyser;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.client.udig.WPSInputLayer.ProcessMode;
import org.n52.wps.geoserver.WPSDataStoreFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

/**
 * @author Theodor Foerster
 *
 */
public class WPSProcess {
	private String host;
	private String processID;
	private HashMap<String, Object> inputValues;
	private boolean cacheIt;
	private Object result;
	private ProcessDescriptionType processDesc;
	private String resultReference;
	private String identifier;
	private SimpleFeatureType featureType;

	
	
	public WPSProcess(Object urlObject, String processID,Object inputValues, Object cacheIt) {
		String url = null;
		if(urlObject instanceof String){
			url = (String) urlObject;
		}
		if(urlObject instanceof URL){
			url = ((URL) urlObject).toString();
		}
		url = url.replace("?Service=WPS", "");
		this.host = url;
		this.processID = processID;
		if(inputValues==null){
			this.inputValues =  new HashMap<String, Object>();
		}else{
			if(inputValues instanceof String){
				String[] inputs = ((String)inputValues).split(",");
				for(String inputTuple : inputs){
					String[] inputElements = inputTuple.split("=");
					this.inputValues =  new HashMap<String, Object>();
					this.inputValues.put(inputElements[0], inputElements[1]);
				}
			}else{
				this.inputValues = (HashMap<String, Object>) inputValues;
			}
		}
		if(cacheIt==null){
			this.cacheIt = false;
		}else{
		this.cacheIt = (Boolean)cacheIt;
		}
		try {
			processDesc = WPSClientSession.getInstance().getProcessDescription(this.getHost(), this.getProcessID());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		identifier = this.getHost()+"#"+processID+this.hashCode();
	}
	
	
	/**
	 * @param params
	 */
	public WPSProcess(Map<String, Serializable> params) {
		this(params.get(WPSDataStoreFactory.URL.key),
		(String)params.get(WPSDataStoreFactory.PROCESS_ID.key), 
		params.get(WPSDataStoreFactory.INPUTVALUES),
		params.get(WPSDataStoreFactory.CACHE_REQUEST_STRATEGY.key));
	}

	public String getUniqueIdentifier() {
		return identifier;
	}

	public String getHost() {
		return host;
	}
	
	public HashMap<String, Object> getInputValues() {
		return inputValues;
	}
	
	public Object getWPSInputValueForName(String key){
		return inputValues.get(key);
	}
	
	public String getProcessID() {
		return processID;
	}


	public boolean isCacheIt() {
		return cacheIt;
	}
	
	/**
	 * Compares if a process is equal to another one by value.
	 * @return
	 */
	public boolean equals(WPSProcess process) {
		if(!this.getHost().equals(process.getHost())) {
			return false;
		}
		if(!this.getProcessID().equals(process.getProcessID())) {
			return false;
		}
		if(!this.getInputValues().equals(process.getInputValues())) {
			return false;
		}
		return true;	
	}
	
	public Object getResult() throws IOException {
		if(result == null) {
			WPSClientSession session = WPSClientSession.getInstance();
			ExecuteRequestBuilder builder = this.getPOSTRequest();
			try {
				//if processDesc has only one processOutput, then we retrieve rawdata!
				if(processDesc.getProcessOutputs().getOutputArray().length==1) {
					OutputDescriptionType output = processDesc.getProcessOutputs().getOutputArray(0);
					String schema = output.getComplexOutput().getDefault().getFormat().getSchema();
					String mimeType = output.getComplexOutput().getDefault().getFormat().getMimeType();
					String encoding = output.getComplexOutput().getDefault().getFormat().getEncoding();
				
					
					builder.setRawData(schema,encoding, mimeType);
					ExecuteDocument execute = builder.getExecute();
					InputStream is = (InputStream)session.execute(this.getHost(), execute,true);
					ExecuteResponseAnalyser analyser = new ExecuteResponseAnalyser(is, execute, processDesc);
					result = analyser.getComplexDataByIndex(0);
				}
			}
			catch(WPSClientException e) {
				throw new IOException("Executing request produced errors: " + e.getServerException().xmlText());
			}
		}
		
		
		return result;
	}
	
	public Object getLiteralDataResult() throws IOException {
		
		if(result == null) {
			WPSClientSession session = WPSClientSession.getInstance();
			ExecuteRequestBuilder builder = this.getPOSTRequest();
			try {
				//if processDesc has only one processOutput, then we retrieve rawdata!
				if(processDesc.getProcessOutputs().getOutputArray().length==1) {

					ExecuteDocument execute = builder.getExecute();
					ResponseDocumentType output = execute.getExecute().addNewResponseForm().addNewResponseDocument();
					output.addNewOutput().setIdentifier(processDesc.getProcessOutputs().getOutputArray(0).getIdentifier());
			
					InputStream is = (InputStream)session.execute(this.getHost(), execute,true);
					
					//TODO:
//					ExecuteResponseAnalyser analyser = new ExecuteResponseAnalyser(is, execute, processDesc);
//					result = analyser.getLiteralData();
										
					ExecuteResponseDocument erDoc = ExecuteResponseDocument.Factory.parse(is);
					
					LiteralDataType literalData = erDoc.getExecuteResponse().getProcessOutputs().getOutputArray(0).getData().getLiteralData();		
					
					String datatype = literalData.getDataType();
					
					if(datatype.contains("boolean")){
						String booleanAsString = literalData.getStringValue();
						
						Boolean b = new Boolean(booleanAsString);
						
						return new LiteralBooleanBinding(b);
					}else if(datatype.contains("double")){
						
						String doubleAsString = literalData.getStringValue();
						
						Double d = new Double(doubleAsString);
						
						return new LiteralDoubleBinding(d);				
					}
					
				}
			}
			catch(WPSClientException e) {
				throw new IOException("Executing request produced errors: " + e.getServerException().xmlText());
			} catch (XmlException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public String getResultReference() throws IOException {
		if(resultReference == null) {
			ExecuteRequestBuilder builder = getPOSTRequest();
			builder.setStoreSupport(processDesc.getProcessOutputs().getOutputArray(0).getIdentifier().getStringValue());
			ExecuteDocument execute = builder.getExecute();
			WPSClientSession session = WPSClientSession.getInstance();
			try {
				Object result = session.execute(this.getHost(), execute);
				ExecuteResponseAnalyser analyser = new ExecuteResponseAnalyser((ExecuteResponseDocument)result, processDesc);
				resultReference = analyser.getComplexReferenceByIndex(0);
			}
			catch(WPSClientException e) {
				throw new IOException("Executing request produced errors: " + e.getServerException().xmlText());
			}	
		}
		return resultReference;
	}
	
	
	public String getResultReference(String schema, String mimeType, String encoding) throws IOException {
		ExecuteRequestBuilder builder = getPOSTRequest();
		builder.setStoreSupport(processDesc.getProcessOutputs().getOutputArray(0).getIdentifier().getStringValue());
		ExecuteDocument execute =builder.getExecute();
		execute.getExecute().getResponseForm().getResponseDocument().getOutputArray(0).setSchema(schema);
		execute.getExecute().getResponseForm().getResponseDocument().getOutputArray(0).setMimeType(mimeType);
		execute.getExecute().getResponseForm().getResponseDocument().getOutputArray(0).setEncoding(encoding);
		WPSClientSession session = WPSClientSession.getInstance();
		try {
			Object result = session.execute(this.getHost(), execute);
			ExecuteResponseAnalyser analyser = new ExecuteResponseAnalyser((ExecuteResponseDocument)result, processDesc);
			resultReference = analyser.getComplexReferenceByIndex(0);
		}
		catch(WPSClientException e) {
			throw new IOException("Executing request produced errors: " + e.getServerException().xmlText());
		}
		return resultReference;
	}
	
	public ExecuteRequestBuilder getPOSTRequest() {
		
		if (processDesc == null) {
			throw new IllegalArgumentException("Could not find appropriate processDescription for: " + this.getProcessID());
		}
		ExecuteRequestBuilder executeBuilder = new ExecuteRequestBuilder(processDesc);
		for(InputDescriptionType input : processDesc.getDataInputs().getInputArray()) {
			String inputName = input.getIdentifier().getStringValue();
			//String propertyValue = filterVisitor.getFilterValue(inputName);
			Object inputValue = this.getWPSInputValueForName(inputName);
			if (input.getLiteralData() != null) {
				executeBuilder.addLiteralData(inputName, (String)inputValue);
			}
			else if (input.getComplexData() != null) {
				//TODO check if supported
				String schema = input.getComplexData().getDefault().getFormat().getSchema();
				String mimeType = input.getComplexData().getDefault().getFormat().getMimeType();
				String encoding = input.getComplexData().getDefault().getFormat().getEncoding();
				if(inputValue instanceof WPSInputLayer) {
					WPSInputLayer layer = (WPSInputLayer) inputValue;
					if(layer.getProcessMode().compareTo(ProcessMode.FEATURES_AS_REFERENCE)!=0){
				//	by value - send plain features
						FeatureCollection features = ((WPSInputLayer)inputValue).getFeatures();
						IData data = new GTVectorDataBinding(features);
						executeBuilder.addComplexData(inputName, data,schema, mimeType, encoding);
					}else{
						//ComplexValue Reference
						executeBuilder.addComplexDataReference(inputName, layer.getURL(), schema, encoding, mimeType);
					}
				}
				else if(inputValue instanceof WPSProcess) {
					WPSProcess process = (WPSProcess) inputValue;
					try {
					GTVectorDataBinding features = (GTVectorDataBinding)process.getResult();
					executeBuilder.addComplexData(inputName, features, schema, encoding, mimeType);
					}
					catch (IOException e) {
						throw new IllegalArgumentException("There went something wrong retrieving the process result as input for execute request", e);
					}
					
				}
				
			}
			else if(inputValue == null && input.getMinOccurs().intValue() > 0) {
				throw new IllegalArgumentException("Property not set, but mandatory: " + inputName);
			}
		}
		
		return executeBuilder;
	}


	public ProcessDescriptionType getProcessDescription() {
		return processDesc;
	}
	
	public FeatureType getFeatureType() throws IOException {
		if(featureType != null) {
			return featureType;
		}
		FeatureType sourceFT = null;
		for(Object value : this.getInputValues().values()) {
			if(value instanceof WPSInputLayer) {
				WPSInputLayer layer = (WPSInputLayer) value;
				sourceFT = layer.getFeatureType();
				break;
			}
			else if(value instanceof WPSProcess) {
				WPSProcess process = (WPSProcess) value;
				sourceFT = process.getFeatureType();
				break;
			}
		}
		
			
		FeatureCollection results = ((GTVectorDataBinding)this.getResult()).getPayload();
		FeatureType ftOld = results.getSchema();
		Feature tempFeature = results.features().next();
		return tempFeature.getType();
	/*	try {
			SimpleFeatureTypeBuilder ftb = SimpleFeatureTypeBuilder.newInstance(identifier);
			if(sourceFT.getName() != null) {
				if(sourceFT.getName().getNamespaceURI() != null) {
				ftb.setNamespaceURI(sourceFT.getName().getNamespaceURI() );
			}
			CoordinateReferenceSystem crs = null;
			if(tempFeature.getDefaultGeometryProperty().getUserData() != null) {
				crs = (CoordinateReferenceSystem) tempFeature.getDefaultGeometryProperty().getUserData().get(CoordinateReferenceSystem.class);

			}
			else if (crs == null && sourceFT != null){
				crs = sourceFT.getCoordinateReferenceSystem();
			}
			AttributeType[] attributeTypes = new AttributeType[ftOld.getAttributeCount()];
			AttributeType[] oldAttributeTypes = ftOld.getAttributeTypes();
			for(int i = 0; i < oldAttributeTypes.length; i++) {
				//Handling GeometricAttributeType
				if(oldAttributeTypes[i] instanceof GeometricAttributeType) {
				attributeTypes[i] = AttributeTypeFactory.newAttributeType(	oldAttributeTypes[i].getName(), 
																	oldAttributeTypes[i].getType(), 
																	false,  
																	oldAttributeTypes[i].getRestriction(),
																	null,
																	crs);
				}
				//Handling other attributeType
				else{
					attributeTypes[i]= oldAttributeTypes[i];
				}
			}
			ftb.addTypes(attributeTypes);
			featureType = ftb.getFeatureType();
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (SchemaException e) {
			e.printStackTrace();
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		}
		return featureType;
		*/
	}
	
	/** Convenience method, returns the schema for a specific complexData input of the process's execute request
	 * 
	 * @return
	 */
	public String getSchemaInput(String parameterID) {
		for(InputType input : this.getPOSTRequest().getExecute().getExecute().getDataInputs().getInputArray()) {
			if(input.getIdentifier().getStringValue().equals(parameterID)) {
				if(input.isSetReference()) {
					return input.getReference().getSchema();
				}
				else if(input.isSetData()) {
					return input.getData().getComplexData().getSchema();
				}
			}
		}
		return null;
	}
	
	/** Convenience method, returns the schema for a specific complexData output of the process's execute request
	 * 
	 * @return
	 */
	public String getSchemaOutput(String parameterID) {
		if(this.getPOSTRequest().getExecute().getExecute().getResponseForm() != null) {
			for(OutputDefinitionType output : this.getPOSTRequest().getExecute().getExecute().getResponseForm().getResponseDocument().getOutputArray())
			if(output.getIdentifier().getStringValue().equals(parameterID)) {
				return output.getSchema();
			}
		}
		for(OutputDescriptionType outputDesc : processDesc.getProcessOutputs().getOutputArray()) {
			if(outputDesc.getIdentifier().getStringValue().equals(parameterID)) {
				return outputDesc.getComplexOutput().getDefault().getFormat().getSchema();
			}
		}
		return null;
	}
	
	
}
