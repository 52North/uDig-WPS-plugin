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

 ***************************************************************/package org.n52.wps.client.udig.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.n52.wps.client.udig.internal.messages"; //$NON-NLS-1$
	public static String WPSGoogleExport_ExportLayer_WizardTitle;
	public static String WPSRegistryWizardPage_label_timeout_tooltip;
	public static String WPSRegistryWizardPage_label_timeout_text;
	public static String WPSRegistryWizardPage_label_buffer_tooltip;
	public static String WPSRegistryWizardPage_label_buffer_text;
	public static String WPSRegistryWizardPage_label_post_tooltip;
	public static String WPSRegistryWizardPage_label_get_tooltip;
	public static String WPSRegistryWizardPage_advanced_tooltip;
	public static String WPSRegistryWizardPage_advanced_text;
	public static String WPSRegistryWizardPage_label_password_tooltip;
	public static String WPSRegistryWizardPage_label_password_text;
	public static String WPSRegistryWizardPage_label_username_tooltip;
	public static String WPSRegistryWizardPage_label_username_text;
	public static String WPSRegistryWizardPage_label_url_tooltip;
	public static String WPSRegistryWizardPage_label_url_text;
	public static String UDIGWPSDataStoreFactory_error_usernameAndPassword;
	public static String WPSServiceImpl_broken;
	public static String WPSServiceImpl_connecting_to;
	public static String WPSServiceImpl_could_not_connect;;
	public static String WPSWizardPage_title;
	public static String WPSGoogleExport_ExportLayer_PageTitle;
	public static String WPSGoogleExport_ExportLayer_Message0;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
