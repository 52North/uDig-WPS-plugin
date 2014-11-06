/*****************************************************************
Copyright © 2007 52°North Initiative for Geospatial Open Source Software GmbH

 Author: foerster

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
package org.n52.wps.client.udig.export.googleearth;

import net.refractions.udig.catalog.ui.export.ExportResourceSelectionState;

import org.eclipse.jface.viewers.ISelection;

/*
 * @author foerster
 *
 */
public class ExportGoogleEarthSelectionState extends ExportResourceSelectionState {
	private int updateInterval = 20;
	private String KMLlayerName = "";
	private UpdateStrategy updateStrategy;
	
	enum UpdateStrategy {STATIC, DYMANIC, SEMIDYNAMIC};
	
	public ExportGoogleEarthSelectionState() {
		super();
	}
	/**
	 * @param selection
	 */
	public ExportGoogleEarthSelectionState(ISelection selection) {
		super(selection);
	}

	/**
	 * Sets the update interval of the KML networklink
	 * @param i
	 * @return
	 */
	public void setUpdateInterval(int i) {
		updateInterval = i;
	}
	
	public int getUpdateInterval() {
		return updateInterval;
	}
	
	public String getKMLlayerName() {
		return KMLlayerName;
	}
	
	public void setKMLlayerName(String llayerName) {
		KMLlayerName = llayerName;
	}
	
	public UpdateStrategy getUpdateStrategy() {
		return updateStrategy;
	}
	
	public void setUpdateStrategy(UpdateStrategy updateStrategy) {
		this.updateStrategy = updateStrategy;
	}

	
}
