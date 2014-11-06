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

/**
 * @author Theodor Foerster
 *
 */
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
