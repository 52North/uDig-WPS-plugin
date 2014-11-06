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
package org.n52.wps.client.udig;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.SupportedComplexDataType;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.commands.DeleteLayerCommand;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.n52.wps.geoserver.WPSDataStore;
import org.n52.wps.geoserver.WPSFeatureReader;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Access a feature type in a wfs.
 * 
 * @author David Zwiers, Refractions Research, Theodor Foerster
 * @since 0.6
 */
public class WPSGeoResourceImpl extends IGeoResource {
    private WPSServiceImpl parent;
    private String typeName = null;//result name
    private WPSProcess process;
    private URL identifier;
    private Map<String, Serializable> params;
    private WPSGeoResourceImpl(){/*not for use*/}
    /**
     * Construct <code>WPSGeoResourceImpl</code>.
     *
     * @param parent
     * @param typename
     * @param params 
     */
    public WPSGeoResourceImpl(WPSServiceImpl parent, WPSProcess process){
        super();
        this.process = process;
        
    	this.parent = parent; 
        try {
            identifier= new URL(process.getUniqueIdentifier()); //$NON-NLS-1$
        } catch (MalformedURLException e) {
            identifier= parent.getIdentifier();
        }
        this.typeName = process.getUniqueIdentifier() + "@" + process.getProcessDescription().getProcessOutputs().getOutputArray(0).getIdentifier().getStringValue();
    }
    
    public URL getIdentifier() {
        return identifier;
    }

    /*
     * @see net.refractions.udig.catalog.IGeoResource#getStatus()
     */
    public Status getStatus() {
        return parent.getStatus();
    }

    public WPSProcess getProcess() {
    	return process;
    }
    /*
     * @see net.refractions.udig.catalog.IGeoResource#getStatusMessage()
     */
    public Throwable getMessage() {
        return parent.getMessage();
    }
    
    /*
     * Required adaptions:
     * <ul>
     * <li>IGeoResourceInfo.class
     * <li>IService.class
     * </ul>
     * @see net.refractions.udig.catalog.IResolve#resolve(java.lang.Class, org.eclipse.core.runtime.IProgressMonitor)
     */
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if(adaptee == null)
            return null;
//        if(adaptee.isAssignableFrom(IService.class))
//            return adaptee.cast( parent );        
        if(adaptee.isAssignableFrom(WPSDataStore.class))
            return parent.resolve( adaptee, monitor );
        if (adaptee.isAssignableFrom(IGeoResource.class))
            return adaptee.cast( this );
        if(adaptee.isAssignableFrom(IGeoResourceInfo.class))
            return adaptee.cast( getInfo(monitor));
        if(adaptee.isAssignableFrom(FeatureStore.class)){
        	FeatureSource fs = parent.getDS(monitor).getFeatureSource(typeName);
            if(fs instanceof FeatureStore)
                return adaptee.cast( fs);
        if(adaptee.isAssignableFrom(FeatureSource.class))
            return adaptee.cast( parent.getDS(monitor).getFeatureSource(typeName));
        }
        return super.resolve(adaptee, monitor);
    }
    public IService service( IProgressMonitor monitor ) throws IOException {
        return parent;
    }
    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public <T> boolean canResolve( Class<T> adaptee ) {
        if(adaptee == null)
            return false;
        return (adaptee.isAssignableFrom(IGeoResourceInfo.class) || 
                adaptee.isAssignableFrom(FeatureStore.class) || 
                adaptee.isAssignableFrom(FeatureSource.class) || 
                adaptee.isAssignableFrom(WPSDataStore.class) ||
                adaptee.isAssignableFrom(IService.class))||
                super.canResolve(adaptee);
    }
    private volatile IGeoResourceInfo info;
    public IGeoResourceInfo getInfo(IProgressMonitor monitor) throws IOException{
        if(info == null && getStatus()!=Status.BROKEN){
            parent.rLock.lock();
            try{
                if(info == null){
                    info = new GeoResourceWPSInfo();
                }
            } catch (URISyntaxException e) {
				e.printStackTrace();
			}finally{					
                parent.rLock.unlock();
                
                if (parent.getDS(monitor).isLiteralOutput(typeName)) {

					// make sure the created layer, which produces a
					// NullPointerException with uDig, is removed
					// in fact any layers that can not be rendered will be
					// removed
					// it is done at this point because the layer is already
					// created, when this method is called
					// and so the layer can be removed
					Iterator<ILayer> layers = ApplicationGIS.getActiveMap()
							.getMapLayers().iterator();

					while (layers.hasNext()) {
						ILayer layer = (ILayer) layers.next();
						if (layer.getStatus() == 2) {
							try {
								ApplicationGIS.getActiveMap().sendCommandASync(
										new DeleteLayerCommand((Layer) layer));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

					}
				} 
            }
        }        
        return info;
    }

	public class GeoResourceWPSInfo extends IGeoResourceInfo {

        CoordinateReferenceSystem crs = null;
        
        
        
        GeoResourceWPSInfo() throws IOException, URISyntaxException{
        	ProcessDescriptionType processDescription = parent.getDS(null).getSession().getProcessDescription(process.getHost(), process.getProcessID());
        	OutputDescriptionType[] outputDescriptions = processDescription.getProcessOutputs().getOutputArray();;
        	OutputDescriptionType outputDescriptionType = null;
        	 String [] processIDcontents = typeName.split("@");
             
        	for (int i = 0; i < outputDescriptions.length; i++) {
        		//there are comlexoutputs only(see WPSDataStore.getFeatureTypes()
				if(outputDescriptions[i].getIdentifier().getStringValue().equals( processIDcontents[1])){
					SupportedComplexDataType complexOutput = outputDescriptions[i].getComplexOutput();
		            this.description = outputDescriptions[i].getAbstract().getStringValue();
		            this.title = outputDescriptions[i].getTitle().getStringValue();
		            this.name = outputDescriptions[i].getIdentifier().getStringValue();
		            if(complexOutput.getSupported()!=null && complexOutput.getSupported().getFormatArray().length>0){
		            	//TODO what todo with multiple supported schemas?
		            	this.schema = new URI(complexOutput.getSupported().getFormatArray(0).getSchema());
		            }else{
		            	this.schema = new URI(complexOutput.getDefault().getFormat().getSchema());
		            }
				}
			}
        	
        	
        }
        public ReferencedEnvelope getBounds(){
        	if (this.bounds != null) {
        		return bounds;
        	}
//    		ATTENTION: the featureReader is retrieved per process not per output! So always the same featureReader for multiple outputs
    		WPSFeatureReader fr = null;
			try {
				fr = (WPSFeatureReader)parent.getDS(null).getFeatureReader(new DefaultQuery(process.getUniqueIdentifier()), null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				WPSPlugin.log("Error occured retrieving BBOX", e);
			}
			if(fr == null) {
				return null;
			}
    		Envelope extent = fr.getBounds();
    		if(crs != null) {
    			this.bounds  = new ReferencedEnvelope(extent,crs);
    		}
    		else{
    			this.bounds = new ReferencedEnvelope(extent, DefaultGeographicCRS.WGS84);
    		}
    		return bounds;
        }
            
            
        	/*FeatureType ft = parent.getDS(null).getSchema(typename);

            List<FeatureSetDescription> featureSetDescriptionList = parent.getDS(null).getCapabilities().getFeatureTypes();
            FeatureSetDescription featureSetDescription = null;
            if(featureSetDescriptionList!=null){
                Iterator<FeatureSetDescription> iterator = featureSetDescriptionList.iterator();
                while(iterator.hasNext()){
                    FeatureSetDescription currentFeatuerSetDescription = iterator.next();
                    if(currentFeatuerSetDescription!=null && typename.equals(currentFeatuerSetDescription.getName()))
                    	featureSetDescription = currentFeatuerSetDescription;
                    	break;
                }
            }
            
            if( featureSetDescription==null ){
                bounds = new ReferencedEnvelope(-180,180,-90,90,DefaultGeographicCRS.WGS84);
            }else{
                bounds = new ReferencedEnvelope(featureSetDescription.getLatLongBoundingBox(),DefaultGeographicCRS.WGS84);
                description = featureSetDescription.getAbstract();
                title = featureSetDescription.getTitle();
            }

            GeometryAttributeType defaultGeom=ft.getDefaultGeometry();
            if( defaultGeom==null ){
               crs=null; 
            }else{
                crs = defaultGeom.getCoordinateSystem();
            }
            
            name = typename;
            schema = ft.getNamespace();
              
            keywords = new String[]{
                "wfs", //$NON-NLS-1$
                typename,
                ft.getNamespace().toString()
            };
            */
        
        
        /*
         * @see net.refractions.udig.catalog.IGeoResourceInfo#getCRS()
         */
        @Override
        public CoordinateReferenceSystem getCRS() {
            if(crs != null) {
                return crs;
            }
          
            return super.getCRS();
        }
        
        public Map<String, Serializable> getParams(){
        	return params;
        }
        
        
        
    }

	@Override
	protected IGeoResourceInfo createInfo(IProgressMonitor monitor)
			throws IOException {
		// TODO Auto-generated method stub
		return getInfo(monitor);
	}
    
   

}