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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.Filter;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class UdigHelper {

	
	public static FeatureCollection getSelectedFeatures(String layerName) throws IOException {
		ILayer layer = getLayerForName(layerName);
		Query query = new DefaultQuery(layer.getSchema().getTypeName(), layer.getFilter());
        
        FeatureSource featureSource = layer.getResource(FeatureSource.class, null);
        return featureSource.getFeatures(query);
	}
	
	public static FeatureCollection getSelectedFeaturesByValue(String layerName) throws IOException, IllegalAttributeException {
		ILayer layer = getLayerForName(layerName);
		Query query = new DefaultQuery(layer.getSchema().getTypeName(), layer.getFilter());
        
        FeatureSource featureSource = layer.getResource(FeatureSource.class, null);
        featureSource.getFeatures(query);
        FeatureCollection featureCollection = featureSource.getFeatures(query);
        
        return cloneFeatureCollection(featureCollection);
	}
	
	public static FeatureCollection getFeaturesOfLayer(String layerName) throws IOException {
		ILayer layer = getLayerForName(layerName);
		Query query = new DefaultQuery(layer.getSchema().getTypeName(), Filter.NONE);
        
        FeatureSource featureSource = layer.getResource(FeatureSource.class, null);
        return featureSource.getFeatures(query);
	}
	
	public static FeatureCollection cloneFeatureCollection(FeatureCollection collection) throws IllegalAttributeException{
		FeatureCollection clonedFeatureCollection  = new TempFeatureCollection(collection.getID(), (SimpleFeatureType) collection.getSchema());
		//clonedFeatureCollection.setDefaultGeometry(collection.getDefaultGeometry());
        Iterator iter = collection.iterator();
        while(iter.hasNext()) {
        	SimpleFeature defaultFeature = (SimpleFeature) iter.next();
        	SimpleFeature clonedFeature = SimpleFeatureBuilder.copy(defaultFeature);
;
        	clonedFeatureCollection.add(clonedFeature);
      
       }
        return clonedFeatureCollection;
	}
	
	
	//TODO has to be id
	public static ILayer getLayerForName(String name){
		List<ILayer> layers = getAllLayers();
		Iterator<ILayer> it = layers.iterator();
		while(it.hasNext()){
			ILayer layer = it.next();
			if(layer.getName().equals(name)){
				return layer;
			}
		}
		return null;
	}
	
	public static List<ILayer> getAllLayers() {
		List<ILayer> layerList = new ArrayList<ILayer>();
		List elements = ApplicationGIS.getActiveProject().getElements();
		Iterator elementIterator = elements.iterator();
		while(elementIterator.hasNext())
		{
			Object element = elementIterator.next();
			if(element instanceof IMap)
			{
				IMap map = (IMap) element;
				layerList.addAll(map.getMapLayers());
				}
		}
		return layerList;
		
	}
	
	public static CoordinateReferenceSystem getCRSofMap() {
		return ApplicationGIS.getActiveMap().getViewportModel().getCRS();
	}
	
	public static boolean hasSelectedFeatures(String layerID) throws IOException {
		return (!UdigHelper.getSelectedFeatures(layerID).isEmpty());
		
	}
	
	public static class TempFeatureCollection extends DefaultFeatureCollection {
	    public TempFeatureCollection(String id, SimpleFeatureType childType) {
	      super(id, childType);
	    }
	  }
}
