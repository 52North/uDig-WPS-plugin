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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;

import org.n52.wps.geoserver.WPSDataStoreFactory;

/**
 * Provides ...TODO summary sentence
 * <p>
 * TODO Description
 * </p>
 * @author David Zwiers, Refractions Research
 * @since 0.6
 */
public class WPSServiceExtension implements ServiceExtension {

    
    
   
    
    /**
     * TODO summary sentence for createService ...
     * 
     * @param id
     * @param params
     * @return x
     */
    public IService createService(URL id, Map<String,Serializable> params ) {
        if(params == null || !params.containsKey(WPSDataStoreFactory.URL.key))
            return null;
        if(id == null){
            URL base = null;
			try {
				Object urlObject = params.get(WPSDataStoreFactory.URL.key);
				if(urlObject instanceof String){
					base = new URL((String)params.get(WPSDataStoreFactory.URL.key));
				}
				if(urlObject instanceof URL){
					base = (URL) params.get(WPSDataStoreFactory.URL.key);
				}
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				base = null;
			}
            //base = base == null?null:WPSDataStoreFactory.createGetCapabilitiesRequest(base);
			
			//look if WPS has been already registered in the catalog
			//if catalog has been regsitered, return this registered service, but add the new process to it's members
			WPSServiceImpl service = (WPSServiceImpl) searchLocalCatalog(base, params);
			if(service == null){
				return new WPSServiceImpl(base,params);
			}else{
				//the service exists, but a new process or the same process with different input values should be added
				try {
					service.addMember(params);
					return service;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
            
        }
        return new WPSServiceImpl(id,params);
    }

    /**
     * TODO summary sentence for createParams ...
     * 
     * @see net.refractions.udig.catalog.ServiceExtension#createParams(java.net.URL)
     * @param url
     * @return x
     */
    public Map<String,Serializable> createParams( URL url ) {
        if (!isWPS(url)) {
            return null;
        }
        
        // wfs check
        Map<String,Serializable> params = new HashMap<String,Serializable>();
        params.put(WPSDataStoreFactory.URL.key,url);
        //params.put(WPSDataStoreFactory.BUFFER_SIZE.key, 100);
        
        // don't check ... it blocks
        // (XXX: but we are using that to figure out if the service will work?)
        return params;
    }
    
    /** A couple quick checks on the url */ 
    private static final boolean isWPS( URL url ){
        String PATH = url.getPath();
        String QUERY = url.getQuery();
        String PROTOCOL = url.getProtocol();
        
        if( PROTOCOL.indexOf("http") == -1 ){ //$NON-NLS-1$ 
            return false;
        }
        if( QUERY != null && QUERY.toUpperCase().indexOf( "SERVICE=" ) != -1){ //$NON-NLS-1$
            // we have a service! it better be wfs            
            return QUERY.toUpperCase().indexOf( "SERVICE=WPS") != -1; //$NON-NLS-1$
        }
        return true; // try it anyway
    }
    
    private IService searchLocalCatalog( URL url, Map<String, Serializable> params) {
        //TODO add look up for matching params
    	ICatalog localCatalog=CatalogPlugin.getDefault().getLocalCatalog();
        List<IResolve> results = localCatalog.find(url, null);
         for( Iterator<IResolve> iter = results.iterator(); iter.hasNext(); ) {
            IResolve resolve = iter.next();
            if( resolve instanceof IService )
               return ((IService) resolve);
        }
      return null;
    }

}
