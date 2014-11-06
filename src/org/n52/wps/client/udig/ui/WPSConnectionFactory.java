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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.UDIGConnectionFactory;

import org.n52.wps.client.udig.WPSGeoResourceImpl;
import org.n52.wps.client.udig.WPSServiceExtension;
import org.n52.wps.client.udig.WPSServiceImpl;
import org.n52.wps.geoserver.WPSDataStore;
import org.n52.wps.geoserver.WPSDataStoreFactory;

public class WPSConnectionFactory extends UDIGConnectionFactory {

	public boolean canProcess( Object data ) {        
        if( data instanceof IResolve ){
            IResolve resolve = (IResolve) data;
            return resolve.canResolve( WPSDataStore.class );
        }
        return toCapabilitiesURL(data) != null;
    }

	public Map<String, Serializable> createConnectionParameters(Object data) {
		if( data==null )
            return null;
        if( data instanceof WPSServiceImpl ){
            WPSServiceImpl wps = (WPSServiceImpl) data;
            return wps.getConnectionParams();
        }
        URL url = toCapabilitiesURL( data );
        if( url == null ){
            // so we are not sure it is a wms url
            // lets guess
            url = CatalogPlugin.locateURL(data);
        }
        if( url != null ) {
            // well we have a url - lets try it!            
            List<IResolve> list = CatalogPlugin.getDefault().getLocalCatalog().find( url, null );
            for( IResolve resolve : list ){
                if( resolve instanceof WPSServiceImpl) {
                    // got a hit!
                    WPSServiceImpl wfs = (WPSServiceImpl) resolve;
                    return wfs.getConnectionParams();
                }
                else if (resolve instanceof WPSGeoResourceImpl ){
                    WPSGeoResourceImpl layer = (WPSGeoResourceImpl) resolve;
                    WPSServiceImpl wfs;
                    try {
                        wfs = (WPSServiceImpl) layer.parent( null );
                        return wfs.getConnectionParams();
                    } catch (IOException e) {
                        checkedURL( layer.getIdentifier() );
                    }                    
                }
            }
            return createParams( url );            
        }    
        if(data instanceof Map){
        	return (Map<String, Serializable>) data;
        }
        return Collections.emptyMap();
	}

	public URL createConnectionURL(Object context) {
		if(context instanceof Map){
			try {
				return new URL((String) ((Map<String, Serializable>) context).get("WPSDataStoreFactory:GET_CAPABILITIES_URL")+"?Service=WPS");
			} catch (MalformedURLException e) {
				return null;
			}
        	
        }else{
        	return null;
        }
	}

	  /**
     * Convert "data" to a wfs capabilities url
     * <p>
     * Candidates for conversion are:
     * <ul>
     * <li>URL - from browser DnD
     * <li>URL#layer - from browser DnD
     * <li>WFSService - from catalog DnD
     * <li>IService - from search DnD
     * </ul>
     * </p>
     * <p>
     * No external processing should be required here, it is enough to guess and let
     * the ServiceFactory try a real connect.
     * </p>
     * @param data IService, URL, or something else
     * @return URL considered a possibility for a WFS Capabilities, or null
     */
    URL toCapabilitiesURL( Object data ) {
        if( data instanceof IResolve ){
            return toCapabilitiesURL( (IResolve) data );
        }
        else if( data instanceof URL ){
            return toCapabilitiesURL( (URL) data );
        }
        else if( CatalogPlugin.locateURL(data) != null ){
            return toCapabilitiesURL( CatalogPlugin.locateURL(data) );
        }
        else {
            return null; // no idea what this should be
        }
    }
    protected URL toCapabilitiesURL( IResolve resolve ){
        if( resolve instanceof IService ){
            return toCapabilitiesURL( (IService) resolve );
        }
        return toCapabilitiesURL( resolve.getIdentifier() );        
    }
    protected URL toCapabilitiesURL( IService resolve ){
        if( resolve instanceof WPSServiceImpl ){
            return toCapabilitiesURL( (WPSServiceImpl) resolve );
        }
        return toCapabilitiesURL( resolve.getIdentifier() );        
    }
    protected URL toCapabilitiesURL( WPSServiceImpl wfs ){
        return wfs.getIdentifier();                
    }
    protected URL toCapabilitiesURL( URL url ){
        if (url == null) return null;

        String path = url.getPath();
        String query = url.getQuery();
        String protocol = (url.getProtocol() != null ) ? url.getProtocol().toLowerCase() 
        		: null;
        
        if( !"http".equals(protocol) && !"https".equals(protocol)){ //$NON-NLS-1$ //$NON-NLS-2$ 
            return null;

        }
        if (query != null && query.indexOf("service=wps") != -1) { //$NON-NLS-1$
            return checkedURL( url );
        }
        if (url.toExternalForm().indexOf("WPS") != -1) { //$NON-NLS-1$
            return checkedURL( url );
        }
        return null;
    }
    /** Check that any trailing #layer is removed from the url */
    private static final URL checkedURL( URL url ){
        String check = url.toExternalForm();
        int hash = check.indexOf('#');
        if ( hash == -1 ){
            return url;            
        }
        try {
            return new URL( check.substring(0, hash ));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    /** 'Create' params given the provided url, no magic occurs */
    protected Map<String,Serializable> createParams( URL url ){
        WPSServiceExtension factory = new WPSServiceExtension();
        Map<String,Serializable> params = factory.createParams( url );
        if( params != null) return params;
        
        Map<String,Serializable> params2 = new HashMap<String,Serializable>();
        params2.put(WPSDataStoreFactory.URL.key,url);
        return params2;
    }
}
