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
package org.n52.wps.geoserver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;

import org.apache.log4j.Logger;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.client.udig.SpatialQueryAlgorithmsHelper;
import org.n52.wps.client.udig.SpatialQueryAlgorithmsHelper.SpatialQueryAlgorithms;
import org.n52.wps.client.udig.WPSProcess;
import org.n52.wps.geoserver.WPSDataStoreFactory.Strategy;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * @author Theodor Foerster
 *
 */
public class WPSDataStore extends AbstractDataStore {

	private String url;
	private WPSClientSession session;
	private Map<String, WPSProcess> processes;
	private Strategy strategy;
	private List<WPSProcess> cache;


	private Logger LOGGER = Logger.getLogger(WPSDataStore.class);

	public WPSDataStore(String url, WPSClientSession session, Strategy strategy)
			throws IllegalArgumentException {
		this.session = session;
		this.url = url;
		this.processes = new HashMap<String, WPSProcess>();
		this.strategy = strategy;
		cache = new ArrayList<WPSProcess>();
	}

	/**
	 * returns all available complex outputs of a wps process
	 */
	public String[] getTypeNames() throws IOException {
		ArrayList<String> typeNames = new ArrayList<String>();
		Iterator<WPSProcess> it = processes.values().iterator();
		while (it.hasNext()) {
			WPSProcess process = it.next();
			ProcessOutputs processOutputs = session.getProcessDescription(url,
					process.getProcessID()).getProcessOutputs();
			OutputDescriptionType[] processOutputDescriptions = processOutputs
					.getOutputArray();

			for (int i = 0; i < processOutputDescriptions.length; i++) {
				if (processOutputDescriptions[i].getComplexOutput() != null) {
					typeNames.add(process.getUniqueIdentifier()
							+ "@"
							+ processOutputDescriptions[i].getIdentifier()
									.getStringValue());
				} else if (processOutputDescriptions[i].getLiteralOutput() != null) {
					typeNames.add(process.getUniqueIdentifier()
							+ "@"
							+ processOutputDescriptions[i].getIdentifier()
									.getStringValue());
				}
			}

		}
		String[] result = new String[typeNames.size()];
		for (int i = 0; i < typeNames.size(); i++) {
			result[i] = (String) typeNames.get(i);
		}
		return result;
	}

	public String[] getTypeNames(String processID) throws IOException {
		ArrayList<String> typeNames = new ArrayList<String>();

		WPSProcess process = (WPSProcess) processes.get(processID);
		ProcessOutputs processOutputs = session.getProcessDescription(url,
				process.getProcessID()).getProcessOutputs();
		OutputDescriptionType[] processOutputDescriptions = processOutputs
				.getOutputArray();

		for (int i = 0; i < processOutputDescriptions.length; i++) {
			if (processOutputDescriptions[i].getComplexOutput() != null) {
				typeNames.add(process.getUniqueIdentifier()
						+ "@"
						+ processOutputDescriptions[i].getIdentifier()
								.getStringValue());
			}else if (processOutputDescriptions[i].getLiteralOutput() != null) {
				typeNames.add(process.getUniqueIdentifier()
						+ "@"
						+ processOutputDescriptions[i].getIdentifier()
								.getStringValue());
			}
		}

		String[] result = new String[typeNames.size()];
		for (int i = 0; i < typeNames.size(); i++) {
			result[i] = (String) typeNames.get(i);
		}
		return result;

	}

	/*
	 * public ProcessDescriptionType getProcessDescription(String processID)
	 * throws IOException {
	 * 
	 * ProcessDescriptionType[] processes =
	 * getDesc().getProcessDescriptions().getProcessDescriptionArray(); for(int
	 * i = 0; i<processes.length; i++){ if(processID ==
	 * processes[i].getIdentifier().getStringValue()){ return processes[i]; } }
	 * 
	 * return null; }
	 */

	public WPSClientSession getSession() {
		return session;
	}

	/**
	 * Returns a FeatureType, which has the name of the process result plus
	 * additional processresult properties.
	 */
	public SimpleFeatureType getSchema(String typeName) throws IOException {
		OutputDescriptionType processOutput = null;
		String[] typeNameContents = typeName.split("@");
		ProcessDescriptionType processDescription = processes.get(
				typeNameContents[0]).getProcessDescription();

		OutputDescriptionType[] outputDescriptions = processDescription
				.getProcessOutputs().getOutputArray();
		for (int i = 0; i < outputDescriptions.length; i++) {
			// there are comlexoutputs only(see WPSDataStore.getFeatureTypes()
			if (outputDescriptions[i].getIdentifier().getStringValue()
					.startsWith(typeNameContents[1])) {
				processOutput = outputDescriptions[i];
				break;
			}
		}
		if (processOutput == null || processOutput.getComplexOutput() == null) {
					
				WPSProcess p = getProcessForQuery(new DefaultQuery(
						typeNameContents[0]));

				Object o = p.getLiteralDataResult();

				if(o instanceof LiteralBooleanBinding){
					
					Boolean result = ((LiteralBooleanBinding)o).getPayload();
								
					String processID = p.getProcessID().substring(p.getProcessID().lastIndexOf('.') + 1);
					
					String[] messages = null;
					
					SpatialQueryAlgorithmsHelper spqaHelper = SpatialQueryAlgorithmsHelper.getInstance();
					
					if(processID.equals(SpatialQueryAlgorithms.TouchesAlgorithm.toString())){
						messages = spqaHelper.getAlgorithmsToMessagesMap().get(SpatialQueryAlgorithms.TouchesAlgorithm.toString());
					}else if(processID.equals(SpatialQueryAlgorithms.OverlapsAlgorithm.toString())){
						messages = spqaHelper.getAlgorithmsToMessagesMap().get(SpatialQueryAlgorithms.OverlapsAlgorithm.toString());
					}else if(processID.equals(SpatialQueryAlgorithms.CrossesAlgorithm.toString())){
						messages = spqaHelper.getAlgorithmsToMessagesMap().get(SpatialQueryAlgorithms.CrossesAlgorithm.toString());
					}else if(processID.equals(SpatialQueryAlgorithms.DisjointAlgorithm.toString())){
						messages = spqaHelper.getAlgorithmsToMessagesMap().get(SpatialQueryAlgorithms.DisjointAlgorithm.toString());
					}else if(processID.equals(SpatialQueryAlgorithms.ContainsAlgorithm.toString())){
						messages = spqaHelper.getAlgorithmsToMessagesMap().get(SpatialQueryAlgorithms.ContainsAlgorithm.toString());
					}else if(processID.equals(SpatialQueryAlgorithms.DistanceAlgorithm.toString())){
						messages = spqaHelper.getAlgorithmsToMessagesMap().get(SpatialQueryAlgorithms.DistanceAlgorithm.toString());
					}else if(processID.equals(SpatialQueryAlgorithms.WithinAlgorithm.toString())){
						messages = spqaHelper.getAlgorithmsToMessagesMap().get(SpatialQueryAlgorithms.WithinAlgorithm.toString());
					}else if(processID.equals(SpatialQueryAlgorithms.IntersectsAlgorithm.toString())){
						messages = spqaHelper.getAlgorithmsToMessagesMap().get(SpatialQueryAlgorithms.IntersectsAlgorithm.toString());
					}else if(processID.equals(SpatialQueryAlgorithms.EqualsAlgorithm.toString())){
						messages = spqaHelper.getAlgorithmsToMessagesMap().get(SpatialQueryAlgorithms.EqualsAlgorithm.toString());
					}
					
					if(messages != null){
										
					if (result) {
							JOptionPane.showMessageDialog(null, messages[0]);
						} else {
							JOptionPane.showMessageDialog(null, messages[1]);
						}
					}
				}else if(o instanceof LiteralDoubleBinding){
					
					Double d = ((LiteralDoubleBinding)o).getPayload();
					
					SpatialQueryAlgorithmsHelper spqaHelper = SpatialQueryAlgorithmsHelper.getInstance();
					
					String processID = p.getProcessID().substring(p.getProcessID().lastIndexOf('.') + 1);
										
					if(processID.equals(SpatialQueryAlgorithms.DistanceAlgorithm.toString())){
					
						String message = spqaHelper.getAlgorithmsToMessagesMap().get(SpatialQueryAlgorithms.DistanceAlgorithm.toString())[0].replace(SpatialQueryAlgorithmsHelper.DISTANCE_PLACEHOLDER, String.valueOf(d));
					
						JOptionPane.showMessageDialog(null, message);
					}
					
				}			
			return null;
		}
		FeatureReader featureReader = this.getFeatureReader(new DefaultQuery(
				typeNameContents[0]), null);
		return (SimpleFeatureType) featureReader.getFeatureType();
	}

	public boolean isLiteralOutput(String typeName) throws IOException {
		OutputDescriptionType processOutput = null;
		String[] typeNameContents = typeName.split("@");
		ProcessDescriptionType processDescription = processes.get(
				typeNameContents[0]).getProcessDescription();

		OutputDescriptionType[] outputDescriptions = processDescription
				.getProcessOutputs().getOutputArray();
		for (int i = 0; i < outputDescriptions.length; i++) {
			// there are comlexoutputs only(see WPSDataStore.getFeatureTypes()
			if (outputDescriptions[i].getIdentifier().getStringValue()
					.startsWith(typeNameContents[1])) {
				processOutput = outputDescriptions[i];
				break;
			}
		}
			if (processOutput == null
					|| processOutput.getComplexOutput() == null) {
				return true;
			}
		
		return false;
	}

	protected FeatureReader getFeatureReader(String type) throws IOException {
		return getFeatureReader(type, new DefaultQuery(type));
	}

	public FeatureReader getFeatureReader(Query query, Transaction transaction)
			throws IOException {
		WPSProcess currentProcess = getProcessForQuery(query);
		GTVectorDataBinding data = null;
		data = (GTVectorDataBinding) currentProcess.getResult();
		
		FeatureCollection fc = data.getPayload();
		SimpleFeatureType featureType = (SimpleFeatureType) currentProcess.getFeatureType();
		return new WPSFeatureReader(featureType, fc);
	}

	private WPSProcess isProcessInCache(WPSProcess currentProcess) {
		for (WPSProcess process : cache) {
			if (process.equals(currentProcess)) {
				// if(request.valueEquals(currentRequest)){
				return process;
			}
		}
		return null;
	}

	private WPSProcess getProcessForQuery(Query query) throws IOException {
		Object foundWPS =  processes.get(query.getTypeName());
		if(foundWPS ==null){
			for(WPSProcess process :processes.values()){
				String localpart = process.getFeatureType().getName().getLocalPart();
				if(localpart.equals(query.getTypeName())){
					foundWPS = process;
					break;
				}
			}
		}
		return (WPSProcess) foundWPS;
	}

	public void setFile(InputStream io, String fileName) throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		byte[] buf = new byte[256];
		int read = 0;
		while ((read = io.read(buf)) > 0) {
			fos.write(buf, 0, read);
		}
		fos.flush();
		fos.close();
	}

	public WPSProcess addProcess(Map<String, Serializable> params) {
		WPSProcess process = new WPSProcess(params);
		processes.put(process.getUniqueIdentifier(), process);
		return process;
	}

	public WPSProcess getProcess(String id) {
		int delimiterPos = id.indexOf("@");
		String processID = id.substring(0, delimiterPos);
		return (WPSProcess) processes.get(processID);
	}
}
