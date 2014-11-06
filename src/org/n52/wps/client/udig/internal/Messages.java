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
package org.n52.wps.client.udig.internal;

import org.eclipse.osgi.util.NLS;

/**
 * @author Theodor Foerster
 *
 */
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
