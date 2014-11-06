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
package org.n52.wps.client.udig.ui;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;
import net.refractions.udig.catalog.ui.workflow.EndConnectionState;
import net.refractions.udig.catalog.ui.workflow.IntermediateState;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizardPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.n52.wps.client.udig.WPSPlugin;
import org.n52.wps.client.udig.WPSServiceExtension;
import org.n52.wps.client.udig.WPSServiceImpl;
import org.n52.wps.client.udig.internal.Messages;
import org.n52.wps.geoserver.WPSDataStoreFactory;
import org.n52.wps.geoserver.WPSDataStoreFactory.Strategy;

/**
 * Data page responsible for aquiring WFS services.
 * <p>
 * Responsibilities:
 * <ul>
 * <li>defaults based on selection - for URL, WFSService, and generic IService (from search)
 * <li>remember history in dialog settings
 * <li>complete list here: <a
 * href="http://udig.refractions.net/confluence/display/DEV/UDIGImportPage+Checklist">Import Page
 * Checklist</a>
 * </ul>
 * </p>
 * <p>
 * This page is used in the Import and Add Layer wizards.
 * </p>
 * 
 * @author Theodor Foerster
 * @since 1.0.0
 */
public class WPSRegistryWizardPage extends WorkflowWizardPage implements ModifyListener, 
UDIGConnectionPage, SelectionListener {
    
    protected Combo urlCombo = null;
    
    private Button[] buttons = null;
    
    private Button advancedTag = null;
    
    private Composite advanced = null;
    
    private Map<String,Serializable> params;
    
   
    
    private IDialogSettings settings;
    private static final String WPS_WIZARD_ID = "WPSWizard"; //$NON-NLS-1$
    private static final String WPS_RECENTLY_USED_ID = "RecentlyUsed"; //$NON-NLS-1$
    
    private String url = ""; //$NON-NLS-1$
    
    private static final int COMBO_HISTORY_LENGTH = 15;
    
   
    
//    WPSConnectionFactory wpsConnFactory = new WPSConnectionFactory();
    public WPSRegistryWizardPage( String name ) {
        super(name);
        settings = WPSPlugin.getDefault().getDialogSettings().getSection(WPS_WIZARD_ID);
        if (settings == null) {
            settings = WPSPlugin.getDefault().getDialogSettings().addNewSection(WPS_WIZARD_ID);
        } 
       
       
        
    }
    
    public WPSRegistryWizardPage () {
        this(""); //$NON-NLS-1$
    }
    
	public String getId() {
		return "org.n52.wps.client.udig.catalog.ui"; //$NON-NLS-1$
	}
	
//	/**
//     * True if this is a *real* wfs, or plays one on tv.
//     * <p>
//     * If this is real WFS, toWFS( Object data ) can be used to check for
//     * explict parameter settings.
//     * </p>
//     * 
//     * @param data
//     * @return boolean
//     */
//    protected boolean isWFS( Object data ){
//       if( data instanceof WPSServiceImpl ){
//           return true;
//       }
//       URL url = wfsConnFactory.toCapabilitiesURL( data );
//       if( url == null ) return false;
//       List<IResolve> list = CatalogPlugin.getDefault().getLocalCatalog().find( url, blm );
//       for( IResolve resolve : list ){
//           if( resolve instanceof WPSServiceImpl) return true;
//       }
//       return false;       
//    }
    
    /** Can be called during createControl */
    protected Map<String,Serializable> defaultParams(){
        IStructuredSelection selection = (IStructuredSelection)PlatformUI
        .getWorkbench() .getActiveWorkbenchWindow().getSelectionService()
        .getSelection();
    return toParams( selection );
    }
    
    /** Retrieve "best" WFS guess of parameters based on provided context */
    protected Map<String,Serializable> toParams( IStructuredSelection context){
        if( context == null ) {
            // lets go with the defaults then
            return Collections.emptyMap();
        }
//        for( Iterator itr = context.iterator(); itr.hasNext(); ) {
//            Map<String,Serializable> params 
//            	= wpsConnFactory.createConnectionParameters( itr.next() );
//            if( !params.isEmpty() ) return params;
//        }
        return Collections.emptyMap();
    }
   
    
    /**
     * TODO summary sentence for createControl ...
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     * @param parent
     */
    public void createControl( Composite arg0 ) {
    	//reset Parameters
    	params = new HashMap<String,Serializable>();
        Composite composite = new Group(arg0,SWT.NULL);
        composite.setLayout(new GridLayout(2, false));

        // add url
        Label label = new Label( composite, SWT.NONE );
        label.setText(Messages.WPSRegistryWizardPage_label_url_text ); 
        label.setToolTipText( Messages.WPSRegistryWizardPage_label_url_tooltip ); 
        label.setLayoutData( new GridData(SWT.END, SWT.DEFAULT, false, false ) );
        
        /*
        url = new Text( composite, SWT.BORDER | SWT.SINGLE );
        url.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );
        url.setText( "http://" );
        url.addModifyListener(this);
        */
        
        String[] temp = settings.getArray(WPS_RECENTLY_USED_ID);
        if (temp == null) {
            temp = new String[0];
           
        }
        List<String> recent = Arrays.asList(temp);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint= 400;
        
        //For Drag 'n Drop as well as for general selections
        // look for a url as part of the selction
        Map<String,Serializable> params = defaultParams(); // based on selection
        URL selectedURL;
        try {
            selectedURL = (URL) WPSDataStoreFactory.URL.lookUp( params );
        } catch (IOException e) {
            selectedURL = null;
        }
        //URL selectedURL = (URL) params.get( WFSDataStoreFactory.URL.key );
        
        urlCombo = new Combo(composite, SWT.BORDER);
        urlCombo.setItems(recent.toArray(new String[recent.size()]));
        urlCombo.setVisibleItemCount( 15 );
        urlCombo.setLayoutData( gridData );
        if (selectedURL != null) {
            urlCombo.setText(selectedURL.toExternalForm());
        } else if (url != null && url.length() != 0) {
            urlCombo.setText(url );
        } else { 
            //urlCombo.setText("http://localhost:8080/wps/WebProcessingService"); //$NON-NLS-1$
        }
        urlCombo.addModifyListener(this);
        
        // add spacer
        label = new Label( composite, SWT.SEPARATOR | SWT.HORIZONTAL );
        label.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false,3,3 ) );
        
   
        label = new Label( composite, SWT.NONE );
        label.setLayoutData( new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false ) );

        advancedTag = new Button( composite, SWT.CHECK );
        advancedTag.setLayoutData( new GridData(SWT.CENTER, SWT.DEFAULT, false, false ) );
        advancedTag.setSelection(false);
        advancedTag.addSelectionListener(this);
        advancedTag.setText("Advanced"); 
        advancedTag.setToolTipText("Advanced"); 

        label = new Label( composite, SWT.NONE );
        label.setLayoutData( new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false ) );
        
        advanced = createAdvancedControl(composite);
        advanced.setLayoutData( new GridData(SWT.CENTER, SWT.DEFAULT, true, true,2,1 ) );
        
        setControl(composite);
        setPageComplete(true);
		
        // add the local wps address containing the ip to the comboBox
        String ip_included_address = "http://"+getIPAddress().getCanonicalHostName()+":8080/wps/WebProcessingService";  
        urlCombo.add(ip_included_address);
        
		urlCombo.addModifyListener(this);
		
    }

	
	@Override
	public void dispose() {
		super.dispose();
        if(red!=null)
            red.dispose();
	}

    public void modifyText( ModifyEvent e ) {
        if(e.widget!=null && e.widget instanceof Text)
            ((Text)e.widget).setForeground(null);
        if(e.widget==urlCombo){
            ((Combo)e.widget).setForeground(null);
            setErrorMessage(null);
            url = urlCombo.getText();
        }
        getContainer().updateButtons();
    }
    
 

 
	private Color red;

	private Button cacheCheckbox;

	private Button cacheDisabledCheckbox;

	private Button bboxCheckbox;

	@Override
	public boolean isPageComplete() {
		
		Map<String, Serializable> params = getParams();
        if( params==null )
            return false;
     
        URL url;
		try {
			url = new URL((String)params.get(WPSDataStoreFactory.URL.key));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			return false;
		}
        String trim = url.getHost().trim();
        if( trim.length()==0 ){
        	return false;
		}
       
        EndConnectionState thisState = ((EndConnectionState)this.getState());
        EndConnectionState newState = new EndConnectionState(thisState.getDescriptor(),true);
      
        newState.setWorkflow(thisState.getWorkflow());
        newState.getWorkflow().setContext(this.getParams());
       
        //newState.setPrevious(oldState);
        //((ConnectionState)this.getState()).setNextState((ConnectionState)getState());
        ((EndConnectionState)this.getState()).setNextState(newState);
        return true;
//        return factory.canProcess(params);
	}
	
	
	
    public List<URL> getURLs() {
    	return null;
    }
    
    public Map<String,Serializable> getParams() {
        
        boolean error = false;
        try {
            URL url = new URL(urlCombo.getText());
            params.put(WPSDataStoreFactory.URL.key, url.toExternalForm());
                       
        } catch (Exception e) {
            if(red == null)
                red = new Color(null,255,0,0);
            urlCombo.setForeground(red);
            error = true;
        }
        /*
        for(Button button : buttons){
        	String strategy = (String) button.getData();
        	params.put(strategy, button.getSelection());
        }
        */
        params.put(WPSDataStoreFactory.ADDMEMBER.key, false);
        return error?null:params;
    }
    /*
     * @see net.refractions.udig.catalog.ui.UDIGImportPage#getResources(org.eclipse.core.runtime.IProgressMonitor)
     */
    public List<IService> getResources( IProgressMonitor monitor ) throws Exception {
        if( !isPageComplete() )
			return null;
        		
        List<IService> list = CatalogPlugin.getDefault().getServiceFactory().acquire( getParams() );
        saveWidgetValues();
        return (List<IService>)list;
	}
    
    /**
     * TODO summary sentence for getDataStoreFactorySpi ...
     * 
     * @see net.refractions.udig.catalog.internal.ui.datastore.DataStoreWizard#getDataStoreFactorySpi()
     * @return
     */
//    protected DataStoreFactorySpi getDataStoreFactorySpi() {
//        return factory;
//    }
    
    /**
     * Saves the widget values
     */
    private void saveWidgetValues() {
        // Update history
        if (settings != null) {
            String[] recentWPSs = settings.getArray(WPS_RECENTLY_USED_ID);
            if (recentWPSs == null) recentWPSs = new String[0];
            recentWPSs = addToHistory(recentWPSs, urlCombo.getText());
            settings.put(WPS_RECENTLY_USED_ID, recentWPSs);
        }
    }
    
    /**
     * Adds an entry to a history, while taking care of duplicate history items
     * and excessively long histories.  The assumption is made that all histories
     * should be of length <code>ConfigurationWizardMainPage.COMBO_HISTORY_LENGTH</code>.
     *
     * @param history the current history
     * @param newEntry the entry to add to the history
     * @return the history with the new entry appended
     * Stolen from org.eclipse.team.internal.ccvs.ui.wizards.ConfigurationWizardMainPage
     */
    private String[] addToHistory(String[] history, String newEntry) {
        ArrayList<String> l = new ArrayList<String>(Arrays.asList(history));
        addToHistory(l, newEntry);
        String[] r = new String[l.size()];
        l.toArray(r);
        return r;
    }
    
    /**
     * Adds an entry to a history, while taking care of duplicate history items
     * and excessively long histories.  The assumption is made that all histories
     * should be of length <code>ConfigurationWizardMainPage.COMBO_HISTORY_LENGTH</code>.
     *
     * @param history the current history
     * @param newEntry the entry to add to the history
     * Stolen from org.eclipse.team.internal.ccvs.ui.wizards.ConfigurationWizardMainPage
     */
    private void addToHistory(List<String> history, String newEntry) {
        history.remove(newEntry);
        history.add(0,newEntry);
    
        // since only one new item was added, we can be over the limit
        // by at most one item
        if (history.size() > COMBO_HISTORY_LENGTH)
            history.remove(COMBO_HISTORY_LENGTH);
    }

	@Override
	public IWizardPage getNextPage() {
		WorkflowWizardPage nextPage =  new WPSProcessSelectionWizardPage("Second Page");
		nextPage.setWizard(getWizard());
		nextPage.setState(getState().next());
		nextPage.setPreviousPage(this);
		return nextPage;
	}  
	
	 public void setWizard(IWizard newWizard){
		 super.setWizard(newWizard);
	 
	 }

    private Composite createAdvancedControl( Composite arg0 ) {
        advanced = new Group(arg0,SWT.BORDER);
        advanced.setLayout(new GridLayout(1, false));

        buttons = new Button[4];
      
            
        buttons[0] = new Button( advanced, SWT.CHECK );
        buttons[0].setLayoutData( new GridData(SWT.LEFT, SWT.DEFAULT, false, false ) );
        buttons[0].setSelection(true);
        buttons[0].setText("cache the processed dataset");
        buttons[0].setData(WPSDataStoreFactory.CACHE_REQUEST_STRATEGY.key);
        buttons[0].addSelectionListener(new ButtonListener(buttons[0]));
        //update the context, because this button is checked by default
       	params.put(WPSDataStoreFactory.CACHE_REQUEST_STRATEGY.key, true);
        
        
        buttons[1] = new Button( advanced, SWT.CHECK );
        buttons[1].setLayoutData( new GridData(SWT.LEFT, SWT.DEFAULT, false, false ) );
        buttons[1].setSelection(false);
        buttons[1].setText("process selected Features");
        buttons[1].setData(WPSDataStoreFactory.SELECTED_FEATURE_REQUEST_STRATEGY.key);
        buttons[1].addSelectionListener(new ButtonListener(buttons[1]));
        //update the context, because this button is not checked by default
       	params.put(WPSDataStoreFactory.SELECTED_FEATURE_REQUEST_STRATEGY.key, false);
        
        
        buttons[2] = new Button( advanced, SWT.RADIO );
        buttons[2].setLayoutData( new GridData(SWT.LEFT, SWT.DEFAULT, false, false ) );
        buttons[2].setSelection(true);
        buttons[2].setText("process by bbox");
        buttons[2].setData(Strategy.STATICBBOXPROCESS.name());
        buttons[2].setEnabled(false);
        
        buttons[3] = new Button( advanced, SWT.RADIO );
        buttons[3].setLayoutData( new GridData(SWT.LEFT, SWT.DEFAULT, false, false ) );
        buttons[3].setSelection(true);
        buttons[3].setText("process by bbox");
        buttons[2].setData(Strategy.DYNAMICBBOXPROCESS.name());
        buttons[3].setEnabled(false);

        
        advanced.setVisible(false);
        
        return advanced;
    }

	public void widgetDefaultSelected(SelectionEvent e) {
		if( getWizard().canFinish() ){
            getWizard().performFinish();
        }
		
	}

	public void widgetSelected(SelectionEvent e) {
	    Button b = (Button)e.widget;
	    if(b.equals(advancedTag)){
    		advanced.setVisible(advancedTag.getSelection());
        }
        
        getWizard().getContainer().updateButtons();
		
	}
	
	private class ButtonListener implements SelectionListener {
		Button button;
		
		ButtonListener(Button button){
			this.button = button;
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void widgetSelected(SelectionEvent e) {
			String strategy = (String) button.getData();
        	params.put(strategy, button.getSelection());
        	getWizard().getWorkflow().setContext(mergeParams((Map<String, Serializable>) getWizard().getWorkflow().getContext(), getParams()));
			
		}
		
	}
	
	
	public InetAddress getIPAddress()
	{
		InetAddress addr = null;
			
		try 
		{
		    addr = InetAddress.getLocalHost();		        
		} 
		catch (UnknownHostException e){  }
		    
		return addr;
	}

	@Override
	public Collection<URL> getResourceIDs() {
		return new ArrayList<URL>();
	}

	@Override
	public Collection<IService> getServices() {
		Collection<IService> list = new ArrayList<IService>();
		try {
			
			WPSServiceImpl wps = new WPSServiceImpl(new URL("http://localhost:8080/wps/WebProcessingService"), mergeParams((Map<String, Serializable>) getWizard().getWorkflow().getContext(), getParams()));
			list.add(wps);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     return list;
	}

	private Map<String, Serializable> mergeParams(
			Map<String, Serializable> params, Map<String, Serializable> params2) {
		params.putAll(params2);
		return params;
	} 

    
}