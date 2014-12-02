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
package org.n52.wps.client.udig.export.googleearth;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.opengis.ows.x11.DCPDocument.DCP;
import net.opengis.ows.x11.OperationDocument.Operation;
import net.opengis.wps.x100.CapabilitiesDocument;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.ui.export.CatalogExport;
import net.refractions.udig.catalog.ui.export.CatalogExportWizard;
import net.refractions.udig.catalog.ui.export.Data;
import net.refractions.udig.catalog.ui.workflow.BasicWorkflowWizardPageFactory;
import net.refractions.udig.catalog.ui.workflow.State;
import net.refractions.udig.catalog.ui.workflow.Workflow;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizard;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizardPageProvider;
import net.refractions.udig.project.ui.internal.Images;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.client.udig.WPSGeoResourceImpl;
import org.n52.wps.client.udig.WPSProcess;
import org.n52.wps.client.udig.export.googleearth.ExportGoogleEarthSelectionState.UpdateStrategy;
import org.n52.wps.client.udig.internal.Messages;

/**
 * Exports layers and maps as part of the export functionality.
 * 
 * @author Jesse
 */
public class GoogleEarthExporter extends CatalogExportWizard implements
        IExportWizard {


    private static final MyCatalogExport CONFIGURATION = new MyCatalogExport();
    
    public GoogleEarthExporter() {
        super(CONFIGURATION.createWorkflow(), CONFIGURATION.createPageMapping());
        setWindowTitle( Messages.WPSGoogleExport_ExportLayer_WizardTitle );
    }
    
    
    final static class MyCatalogExport extends CatalogExport {
            
        

        MyCatalogExport(){
            // I'm just updating this code.  Apparently this class is here to disable initializing 
            //  not sure why... Oh I think it is because they don't want the 
            // dialog created.  They just want to be able to obtain the default workflow and 
            // page mapping.
            super(false);
        }
        
       
        protected WorkflowWizard createWorkflowWizard(Workflow workflow,
                Map<Class<? extends State>, WorkflowWizardPageProvider> map) {
            return super.createWorkflowWizard(workflow, map);
        }
        
        protected Workflow createWorkflow() {
        	ExportGoogleEarthSelectionState layerState = new ExportGoogleEarthSelectionState();
            Workflow workflow = new Workflow();
            workflow.setStates(new State[]{layerState});
            return workflow;
        }
        
        @Override
        protected Map<Class<? extends State>, WorkflowWizardPageProvider> createPageMapping() {
            
            Map<Class<? extends State>, WorkflowWizardPageProvider> map = super.createPageMapping();
            
            String title = Messages.WPSGoogleExport_ExportLayer_PageTitle;
            ImageDescriptor banner = Images.getDescriptor("wizban/exportselection_wiz.gif");
            
            ExportGoogleEarthSelectionPage page = new ExportGoogleEarthSelectionPage( "Select Layers", title, banner ); //$NON-NLS-1$
            page.setMessage( Messages.WPSGoogleExport_ExportLayer_Message0);
            map.put(ExportGoogleEarthSelectionState.class, new BasicWorkflowWizardPageFactory(page)); 
            return map;
            
        }
    }
    
    @Override
	public boolean performFinish(IProgressMonitor monitor) {
      ExportGoogleEarthSelectionPage p = (ExportGoogleEarthSelectionPage) this.getStartingPage();
      ExportGoogleEarthSelectionState state = (ExportGoogleEarthSelectionState) p.getState();
      List<Data> elements = state.getLayers();
      String kmlLayerName = state.getKMLlayerName();
      int updateInterval = state.getUpdateInterval();
      for(Data data : elements){
    	  IGeoResource georesource;
		try {
			georesource = data.getResource().resolve(WPSGeoResourceImpl.class, null);
		
    	  if(georesource != null && georesource instanceof WPSGeoResourceImpl) {
	    	  WPSGeoResourceImpl wpsGeoresource = (WPSGeoResourceImpl) georesource;
	    	  WPSProcess process = wpsGeoresource.getProcess();
	      	  String getRequest = createWPSGetRequest(process, state.getUpdateStrategy());
	      	  
	      	  String networkLink = createNetworkLink(getRequest, kmlLayerName, updateInterval, state.getUpdateStrategy());
	      	  createFile(p.getState().getExportDir(), networkLink);
    	  }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
			
      }
        return true;
	}
    
    private void createFile(String exportDir, String networkLink) {
    	File f = new File(exportDir);
    	String newFileName ="";
    	if(!f.isDirectory() && !exportDir.endsWith(".kml") && !exportDir.endsWith(".KML") && !exportDir.endsWith(".kmz") && !exportDir.endsWith(".KMZ")){
    		newFileName = exportDir+".kml";
    	}
    	if(!f.isDirectory() && (exportDir.endsWith(".kml") || exportDir.endsWith(".KML") || exportDir.endsWith(".kmz") || exportDir.endsWith(".KMZ"))){
    		newFileName = exportDir;
    		
    	}
    	if(f.isDirectory()){
    		newFileName = exportDir+"wpsLayer"+networkLink.hashCode()+".kml";
    	}
    	File f1 = new File(newFileName);
    	try {
			FileWriter fstream = new FileWriter(f1);
	        BufferedWriter out = new BufferedWriter(fstream);
	        out.write(networkLink);
	        
	        out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		
	}

	private String createNetworkLink(String getRequest, String layerName, int updateInterval, UpdateStrategy strategy) {
    	String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
    	"<kml xmlns=\"http://earth.google.com/kml/2.2\">"+
    	  "<Folder>"+
    	    "<name>"+ layerName +"</name>"+
    	    "<visibility>0</visibility>"+
    	    "<open>0</open>"+
    	    "<description>WPS Layer</description>"+
    	    "<NetworkLink>"+
    	      "<name>WPS Layer</name>"+
    	      "<visibility>0</visibility>"+
    	      "<open>0</open>"+
    	      "<description>WPS Layer</description>"+
    	      "<refreshVisibility>0</refreshVisibility>"+
    	      "<flyToView>0</flyToView>"+
    	      "<Link>"+
    	        "<href>"+getRequest+"</href>";
    	        if(!strategy.equals(UpdateStrategy.STATIC)) {
    	        result = result + "<refreshMode>onInterval</refreshMode>"+
    	        "<refreshInterval>"+ updateInterval + "</refreshInterval>";
    	        }
    	      result = result + "</Link>"+
    	    "</NetworkLink>"+
    	  "</Folder>"+
    	"</kml>";
    	
    	return result;
	}

	private String createWPSGetRequest(WPSProcess process, UpdateStrategy us) throws IOException{
    	if(us.equals(UpdateStrategy.STATIC)) {
    		return process.getResultReference("http://www.opengis.net/kml/2.2", "application/vnd.google-earth.kml+xml", "UTF-8");
    	}
    	if(us.equals(UpdateStrategy.DYMANIC)) {
	    	WPSGETRequestDataInputs dataInputs = new WPSGETRequestDataInputs(process);
	    	WPSGETRequestRawDataOutput rawDataOutput = new WPSGETRequestRawDataOutput(process.getProcessID(),process.getHost());
	    	CapabilitiesDocument caps = WPSClientSession.getInstance().getWPSCaps(process.getHost());
	    	String executeURL = null;
	    	for(Operation op :caps.getCapabilities().getOperationsMetadata().getOperationArray()) {
	    		if(op.getName().equals("Execute")) {
	    			for(DCP dcp :op.getDCPArray()) {
	    				if(dcp.getHTTP().getGetArray().length != 0) {
	    					executeURL = dcp.getHTTP().getGetArray(0).getHref();
	    				}
	    			}
	    		}
	    	}
	    	if(executeURL == null) {
	    		executeURL = process.getHost();
	    	}
	    	if(!executeURL.endsWith("?")) {
				executeURL = executeURL + "?";
			}
	    	String result = executeURL + "request=execute&service=WPS&version=1.0.0&Identifier="+process.getProcessID()+"&"+dataInputs.toString()+"&"+rawDataOutput.toString();
	    	
	    	result = result.replace("&", "&amp;");
	    	result = result.replace("+", "%2B");
	    	return result;
    	}
    	else {
    		return null;
    	}
    }

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IWizardContainer getContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getDefaultPageImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDialogSettings getDialogSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWizardPage getPage(String pageName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPageCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IWizardPage[] getPages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWizardPage getStartingPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RGB getTitleBarColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWindowTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isHelpAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean needsPreviousAndNextButtons() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean needsProgressMonitor() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean performCancel() {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void setContainer(IWizardContainer wizardContainer) {
		// TODO Auto-generated method stub
		
	}
}
