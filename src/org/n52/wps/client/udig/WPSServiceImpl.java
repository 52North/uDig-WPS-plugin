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
package org.n52.wps.client.udig;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.opengis.wps.x100.CapabilitiesDocument;
import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveChangeEvent;
import net.refractions.udig.catalog.IResolveDelta;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.internal.CatalogImpl;
import net.refractions.udig.catalog.internal.ResolveChangeEvent;
import net.refractions.udig.catalog.internal.ResolveDelta;
import net.refractions.udig.ui.ErrorManager;
import net.refractions.udig.ui.UDIGDisplaySafeLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.xml.wfs.WFSSchema;
import org.n52.wps.geoserver.WPSDataStore;
import org.n52.wps.geoserver.WPSDataStoreFactory;

/**
 * Handle for a WFS service.
 * 
 * @author David Zwiers, Refractions Research
 * @since 0.6
 */
public class WPSServiceImpl extends IService {

    private URL url = null;
    private Map<String,Serializable> params = null;
    protected Lock rLock=new UDIGDisplaySafeLock();
    
    private volatile List<WPSGeoResourceImpl> members = null;
    private Throwable msg = null;
    private volatile WPSDataStore ds = null;
    private static final Lock dsLock = new UDIGDisplaySafeLock();

    
    public WPSServiceImpl(URL url, Map<String,Serializable> params){
        this.url = url;
        this.params = params;
        
    }
    
    /*
     * Required adaptions:
     * <ul>
     * <li>IServiceInfo.class
     * <li>List.class <IGeoResource>
     * </ul>
     * 
     * @see net.refractions.udig.catalog.IService#resolve(java.lang.Class, org.eclipse.core.runtime.IProgressMonitor)
     */
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if(adaptee == null)
            return null;
        if(adaptee.isAssignableFrom(WPSDataStore.class)){
            return adaptee.cast( getDS(monitor));
        }
        return super.resolve(adaptee, monitor);
    }
    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public <T> boolean canResolve( Class<T> adaptee ) {
        if(adaptee == null)
            return false;
        return adaptee.isAssignableFrom(WPSDataStore.class)||
                super.canResolve(adaptee);
    }
    
    public void dispose( IProgressMonitor monitor ) {
        if( members==null)
            return;

        int steps = (int) ((double) 99 / (double) members.size());
        for( IResolve resolve : members ) {
            try {
                SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, steps);
                resolve.dispose(subProgressMonitor);
                subProgressMonitor.done();
            } catch (Throwable e) {
                ErrorManager.get().displayException(e,
                        "Error disposing members of service: " + getIdentifier(), CatalogPlugin.ID); //$NON-NLS-1$
            }
        }
    }
  
    
    
    public void addMember(Map<String, Serializable> params) throws IOException {
		//add members only if we have all parameters
    	if((Boolean) params.get(WPSDataStoreFactory.ADDMEMBER.key)){
	    	//TODO check if member already exists 
	    	setParams(params);
			ds = getDS(null);// load ds
			WPSProcess process = ds.addProcess(params);
	         //members = new LinkedList<WPSGeoResourceImpl>();
	         String[] typenames = ds.getTypeNames(process.getUniqueIdentifier());
	         for(int i=0;i<typenames.length;i++){
	             try{
	                 members.add(new WPSGeoResourceImpl(this, process));
	             }catch (Exception e) {
	            	 e.printStackTrace();
	                 WPSPlugin.log("", e); //$NON-NLS-1$
	             }
	         }
		}
		
	}
   
    
    /*
     * @see net.refractions.udig.catalog.IService#getInfo(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IServiceInfo getInfo( IProgressMonitor monitor ) throws IOException {
        getDS(monitor); // load ds
        if(info == null && ds!=null){
            rLock.lock();
            try{
                if(info == null){
                    info = new IServiceWPSInfo(ds);
                    IResolveDelta delta = new ResolveDelta( this, IResolveDelta.Kind.CHANGED );
                    ((CatalogImpl)CatalogPlugin.getDefault().getLocalCatalog()).fire( new ResolveChangeEvent( this, IResolveChangeEvent.Type.POST_CHANGE, delta )  );
                }
            }finally{
                rLock.unlock();
            }
        }
        return info;
    }
    private volatile IServiceInfo info = null;
    /*
     * @see net.refractions.udig.catalog.IService#getConnectionParams()
     */
    public Map<String,Serializable> getConnectionParams() {
        return params;
    }
   
    WPSDataStore getDS(IProgressMonitor monitor) throws IOException{
        if(ds == null){
            if (monitor == null) monitor = new NullProgressMonitor();
            monitor.beginTask("Connecting to WPS", 3);
            dsLock.lock();
            monitor.worked(1);
            try{
                if(ds == null){
                	WPSDataStoreFactory dsf = WPSDataStoreFactory.getInstance();
                    if(dsf.canProcess(params)){
                        monitor.worked(1);                        
                        try {
                            ds = (WPSDataStore) dsf.createDataStore(params);
                            monitor.worked(1);                            
                        } catch (IOException e) {
                            msg = e;
                            throw e;
                        }
                    }
                }
            }finally{
                dsLock.unlock();                
                monitor.done();
            }
            IResolveDelta delta = new ResolveDelta( this, IResolveDelta.Kind.CHANGED );
            ((CatalogImpl)CatalogPlugin.getDefault().getLocalCatalog()).fire( new ResolveChangeEvent( this, IResolveChangeEvent.Type.POST_CHANGE, delta )  );
        }
        return ds;
    }
    /*
     * @see net.refractions.udig.catalog.IResolve#getStatus()
     */
    public Status getStatus() {
        return msg != null? Status.BROKEN : ds == null? Status.NOTCONNECTED : Status.CONNECTED;
    }
    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return msg;
    }
    /*
     * @see net.refractions.udig.catalog.IResolve#getIdentifier()
     */
    public URL getIdentifier() {
        return url;
    }

    private class IServiceWPSInfo extends IServiceInfo {

        private CapabilitiesDocument caps = null;
        IServiceWPSInfo( WPSDataStore resource ){
         
            caps = resource.getSession().getWPSCaps(url.toExternalForm());
       
        }
        
        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getAbstract()
         */
        public String getAbstract() {
        	return caps.getCapabilities().getServiceIdentification().getAbstractArray(0).getStringValue();
        	//return caps==null?null:caps.get.getService()==null?null:caps.getService().get_abstract();
        }
        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getIcon()
         */
        public Icon getIcon() {
            //return CatalogUIPlugin.getDefault().getImages().getImageDescriptor( ISharedImages.WFS_OBJ );
            return new ImageIcon( WPSPlugin.ID, "icons/obj16/wfs_obj.16"); //$NON-NLS-1$
        }
        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getKeywords()
         */
        public Set<String> getKeywords() {
        	return new HashSet();
            //return caps==null?null:caps.getService()==null?null:((Service) caps.getService()).getKeywordList();
        }
        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getSchema()
         */
        public URI getSchema() {
        	//TODO
            return WFSSchema.NAMESPACE;
        }
        public String getDescription() {
            return getIdentifier().toString();
        }

        public URI getSource() {
            try {
				return getIdentifier().toURI();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
        }

        public String getTitle() {
        	if(caps==null){
        		return "WPS";
        	}
            return caps.getCapabilities().getServiceIdentification().getTitleArray(0).getStringValue();
        }
    }

	private void setParams(Map<String, Serializable> params) {
		this.params = params;
	}

	@Override
	public List<? extends IGeoResource> resources(IProgressMonitor monitor)
			throws IOException {

    	if(members == null){
            rLock.lock();
            try{
            	if(members == null){
            		members = new LinkedList<WPSGeoResourceImpl>();
                    if(!params.containsKey("WPSDataStoreFactory:PROCESS_ID")){
                    	return members;
                    }
                    ds = getDS(monitor);// load ds
                    if(ds == null){
                    	return members;
                    }
                    members = new LinkedList<WPSGeoResourceImpl>();
                    String[] typenames = ds.getTypeNames();
                    for(int i=0;i<typenames.length;i++){
                        try{
                            members.add(new WPSGeoResourceImpl(this, ds.getProcess(typenames[i])));
                        }catch (Exception e) {
                            WPSPlugin.log("", e); //$NON-NLS-1$
                        }
                    }
            	}
            }finally{
                rLock.unlock();
            }
    	}
    	if(members.size() >0 && ((Boolean) params.get(WPSDataStoreFactory.ADDMEMBER.key))){
    		List<WPSGeoResourceImpl> tempList = new LinkedList<WPSGeoResourceImpl>();;
    		tempList.add(members.get(members.size()-1));
    		return tempList;
    		
    	}else{
    		return members;
    	}
	}

	@Override
	protected IServiceInfo createInfo(IProgressMonitor monitor)
			throws IOException {
		return getInfo(monitor);
	}

	
	
}