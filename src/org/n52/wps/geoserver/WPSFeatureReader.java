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
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.kml.bindings.FeatureTypeBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;




public class WPSFeatureReader implements FeatureReader {

	FeatureCollection fc;
	FeatureIterator iter;
    SimpleFeatureType featureType;
	
	WPSFeatureReader(SimpleFeatureType featureType, FeatureCollection fc) {
		iter = fc.features();
		this.featureType = featureType;
		this.fc = fc;
		
	}
	
	public Envelope getBounds() {
		return fc.getBounds();
	}

	public SimpleFeature next() throws IOException, IllegalAttributeException, NoSuchElementException {
		return (SimpleFeature) iter.next();
	}

	public boolean hasNext() throws IOException {
		return iter.hasNext();
	}

	public void close() throws IOException {
		iter.close();
	}
	
	public SimpleFeatureType getFeatureType() {
		return featureType;
	}

}
