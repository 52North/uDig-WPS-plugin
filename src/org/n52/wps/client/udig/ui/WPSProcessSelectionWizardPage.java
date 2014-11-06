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
package org.n52.wps.client.udig.ui;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.ResolveLabelProviderSimple;
import net.refractions.udig.catalog.ui.ResolveTitlesDecorator;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;
import net.refractions.udig.catalog.ui.workflow.EndConnectionState;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizardPage;

import org.apache.xmlbeans.XmlOptions;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.client.udig.WPSServiceImpl;
import org.n52.wps.geoserver.WPSDataStoreFactory;



public class WPSProcessSelectionWizardPage extends WorkflowWizardPage implements UDIGConnectionPage {
	//private WPSDataStore wpsDataStore;
	private TextViewer textViewer;
	
	 private ListViewer processIdViewer;

	    /** url from workbench selection * */
	private ResolveTitlesDecorator titleDecorator;
	    /**
	     * Indicates whether selected services should be collapse when input is changed
	     */
	 private boolean collapseCheckedInput=false;
	
	    
	public WPSProcessSelectionWizardPage(String pageName) {
		super(pageName);
		super.setTitle("Web Processing Service");
		super.setDescription("Select a Process");
		
		
	}

	public Map<String, Serializable> getParams() {
		HashMap<String, Serializable> params = new HashMap<String, Serializable>();
		IStructuredSelection selection = (IStructuredSelection) processIdViewer.getSelection();
		if(selection.isEmpty())
		{
			return params;
		}
		String selectedProcess = (String) selection.getFirstElement();
		params.put(WPSDataStoreFactory.PROCESS_ID.key, selectedProcess);
		
		return params;
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout(2,true));
		processIdViewer = new ListViewer(composite, SWT.MULTI);
		textViewer = new TextViewer(composite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		textViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	
		final WPSClientSession session = WPSDataStoreFactory.getInstance().getSession();
		final String wpsURL = (String) ((HashMap)((EndConnectionState)getState()).getWorkflow().getContext()).get(WPSDataStoreFactory.URL.key);
		processIdViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        processIdViewer.addPostSelectionChangedListener(new ISelectionChangedListener(){
        
        
            public void selectionChanged( SelectionChangedEvent event ) {
            	String selection = (String) getParams().get(WPSDataStoreFactory.PROCESS_ID.key);
                if(selection==null){
                	return;
                }
            	ProcessDescriptionType description;
               
                try {
					description = session.getProcessDescription(wpsURL, selection);
					XmlOptions opts = new XmlOptions();
					
					textViewer.setInput(new Document(description.toString()));
					if(isPageComplete()){
						
						getWizard().getContainer().updateButtons();
						
						EndConnectionState thisState = (EndConnectionState) getState();
						thisState.getWorkflow().setContext(mergeParams((Map<String, Serializable>) thisState.getWorkflow().getContext(), getParams()));
						 //there is a next state
						
						
						
						
						EndConnectionState newState = new EndConnectionState(thisState.getDescriptor(),true);
					    newState.setPrevious(thisState);
					    newState.setNextState(null);
					    newState.setWorkflow(thisState.getWorkflow());
					    newState.getWorkflow().setContext(((EndConnectionState)getState()).getWorkflow().getContext());
					    thisState.setNextState(newState);
						
						getContainer().updateButtons();
						//((ConnectionState)getState()).setNextState((ConnectionState) state);
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
            }

        });
        
        processIdViewer.setContentProvider(new ProcessContentProvider());

        titleDecorator = new ResolveTitlesDecorator(new ResolveLabelProviderSimple(), true);
        LabelProvider labelProvider = new DecoratingLabelProvider(titleDecorator.getSource(),
                titleDecorator);

        processIdViewer.setLabelProvider(labelProvider);
       
        // use the state to initialize ui
       
        
        
        try {
        	
        	ProcessDescriptionType[] processes = session.getAllProcessDescriptions(wpsURL);
        	ArrayList<String> processNames = new ArrayList<String>();
    		for(int i = 0; i<processes.length; i++){
    			//only process with vector data input/raster not allowed
    			boolean rasterPresent = false;
    			InputDescriptionType[] inputs = processes[i].getDataInputs().getInputArray();
    			for(InputDescriptionType input : inputs){
    				if(input.isSetComplexData()){
    					String defaultMimeType = input.getComplexData().getDefault().getFormat().getMimeType();
    					if(defaultMimeType.contains("tif")){
    						rasterPresent = true;
    					}
    					if(input.getComplexData().getSupported()!=null){
	    					ComplexDataDescriptionType[] formats = input.getComplexData().getSupported().getFormatArray();
	    					for(ComplexDataDescriptionType format : formats){
	    						String mimeType = format.getMimeType();
	        					if(mimeType == null || mimeType.contains("tif")){
	        						rasterPresent = true;
	        					}
	    					}
    					}
    			}
    			//also look at the outputs
    			OutputDescriptionType[] outputs = processes[i].getProcessOutputs().getOutputArray();
        		for(OutputDescriptionType output : outputs){
        				if(output.isSetComplexOutput()){
        					String defaultMimeType = output.getComplexOutput().getDefault().getFormat().getMimeType();
        					if(defaultMimeType.contains("tif")){
        						rasterPresent = true;
        					}
        					if(output.getComplexOutput().getSupported()!=null){
    	    					ComplexDataDescriptionType[] formats = output.getComplexOutput().getSupported().getFormatArray();
    	    					for(ComplexDataDescriptionType format : formats){
    	    						String mimeType = format.getMimeType();
    	        					if(mimeType == null || mimeType.contains("tif")){
    	        						rasterPresent = true;
    	        					}
    	    					}
        					}
        				}
        		}	
    		}
    			if(!rasterPresent){
    				processNames.add(processes[i].getIdentifier().getStringValue());
    			}
    		}
    		
        	String[] processNamesArray = new String[processNames.size()];
        	for(int k=0; k<processNames.size();k++){
        		processNamesArray[k] = processNames.get(k);
        	}
        	
			processIdViewer.setInput(processNamesArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//the description
		
		
		//textViewer.setDocument(new Document());
		/*textViewer.setEditable(false);
		 String string = "This is plain text\n" 
	         + "This is bold text\n" 
	         + "This is red text";
	      Document document = new Document(string);
	      textViewer.setDocument(document);
	     */
        setControl(composite);

	       
		
	}
	
	
	public boolean isPageComplete() {
		Map<String, Serializable> params = getParams();
		if(params==null || params.isEmpty()){
            return false;
		}
		
				
		return true;
	}
	
	
	
	
	 
	 
	private Map<String, Serializable> mergeParams(Map<String, Serializable> params, Map<String, Serializable> params2) {
		  params.putAll(params2);
		  return params;
	}

	@Override
		public IWizardPage getNextPage() {
			WorkflowWizardPage nextPage = new WPSProcessExecutionWizardPage("Third Page");
			nextPage.setWizard(getWizard());
			EndConnectionState state = (EndConnectionState) getState();
			state.getWorkflow().setContext(mergeParams((Map<String, Serializable>) state.getWorkflow().getContext(), this.getParams()));
			 //there is a next state
			//state.setNextState(null);
			//nextPage.setState(state);
			EndConnectionState thisState = ((EndConnectionState)this.getState());
			EndConnectionState newState = new EndConnectionState(thisState.getDescriptor(),true);
			newState.setWorkflow(thisState.getWorkflow());
		    newState.getWorkflow().setContext(thisState.getWorkflow().getContext());
		    newState.setPrevious(thisState);
		    newState.setNextState(null);
		    newState.setWorkflow(thisState.getWorkflow());
		    thisState.setNextState(newState);
		    nextPage.setState(newState);
		    nextPage.setPreviousPage(this);
			return nextPage;
			
			

			
			
		}

	@Override
	public Collection<URL> getResourceIDs() {
		return new ArrayList<URL>();
	}

	@Override
	public Collection<IService> getServices() {
		Collection<IService> list = new ArrayList<IService>();
		try {
			WPSServiceImpl wps = new WPSServiceImpl(new URL("http://localhost:8080/wps/WebProcessingService"), getParams());
			list.add(wps);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     return list;
	}

	 
	
	
}
