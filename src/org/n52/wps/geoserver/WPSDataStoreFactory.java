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
package org.n52.wps.geoserver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.n52.wps.client.WPSClientSession;

public class WPSDataStoreFactory implements DataStoreFactorySpi {
  
	public static final Param URL = new Param("WPSDataStoreFactory:GET_CAPABILITIES_URL",
            String.class,
            "Represents the entry URL to the WPS.",
            true);
	
	public static final Param PROCESS_ID = new Param("WPSDataStoreFactory:PROCESS_ID",
            String.class,
            "Represents a Process id.",
            true);
	
	public static final String INPUTVALUES = "WPSDataStoreFactory:INPUTVALUES";
			
	public static final Param REQUEST_STRATEGY = new Param("WPSDataStoreFactory:REQUEST_STRATEGY",
		 Strategy.class , "", true);
	
	public static final Param CACHE_REQUEST_STRATEGY = new Param("WPSDataStoreFactory:CACHE_REQUEST_STRATEGY",
			 Boolean.class , "", true);
		
	public static final Param SELECTED_FEATURE_REQUEST_STRATEGY = new Param("WPSDataStoreFactory:SELECTED_FEATURE_REQUEST_STRATEGY",
			 Boolean.class , "", true);
		
		
	public enum Strategy {STATICBBOXPROCESS, DYNAMICBBOXPROCESS};
	
	public static final Param ADDMEMBER = new Param("WPSDataStoreFactory:ADDMEMBER",
			 Boolean.class , "", true);
	
	private WPSClientSession session;
	private static WPSDataStoreFactory instance;
	
	private  WPSDataStoreFactory(){
		super();
		session = WPSClientSession.getInstance();
	}
	 
	
	public static WPSDataStoreFactory getInstance(){
		if(instance==null){
			instance = new WPSDataStoreFactory();
		}
		return instance;
	}
    
    //private Map<String, WPSDataStore> instanceCache = new HashMap<String, WPSDataStore>();
    
	public DataStore createDataStore(Map params) throws IOException {
		/*String url = (String)URL.lookUp(params);
		
		if(instanceCache.containsKey(url)) {
			return instanceCache.get(url);
		}*/
		return createNewDataStore(params);
	}

	public DataStore createNewDataStore(Map params) throws IOException {
		Object urlObject = params.get(WPSDataStoreFactory.URL.key);
		String url = null;
		if(urlObject instanceof String){
			url = (String) urlObject;
		}
		if(urlObject instanceof URL){
			url = ((URL) urlObject).toString();
		}
		url = url.replace("?Service=WPS", "");
		//Strategy strategy = (Strategy) REQUEST_STRATEGY.lookUp(params);
		Strategy strategy = Strategy.STATICBBOXPROCESS;
		WPSDataStore ds = new WPSDataStore(url, session,strategy);
		ds.addProcess(params);
		//instanceCache.put(url, ds);
		return ds;
	}

	public String getDisplayName() {
		return "Web Processing Service";
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return "Represents a connection to a WPS";
	}

	public Param[] getParametersInfo() {
		// TODO Auto-generated method stub
		return new Param[]{URL};
	}

	public boolean canProcess(Map params) {
		if(!params.containsKey(URL.key)) {
			return false;
		}
		String[] processes = null;
		try {
			 URL base = null;
				try {
					Object urlObject = params.get(URL.key);
					if(urlObject instanceof String){
						base = new URL((String)params.get(URL.key));
					}
					if(urlObject instanceof URL){
						base = (URL) params.get(URL.key);
					}
					
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					base = null;
				}
			
			processes = session.getProcessNames(base.toString().replace("Service=WPS",""));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		if(processes == null || processes.length == 0){
			return false;
		}
		return true;
	}

	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return true;
	}

	public Map getImplementationHints() {
		// TODO Auto-generated method stub
		return null;
	}
/*
	public static URL createGetCapabilitiesRequest(URL base) {
		if(base.toExternalForm().contains("Request=")) {
			return base;
		}else  {
			try {
				return new URL(base+"?Request=GetCapabilities&Service=WPS");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
			
		return null;
	}
	*/


	public WPSClientSession getSession() {
		return session;
	}
}
