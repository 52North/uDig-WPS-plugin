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

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.n52.wps.commons.WPSConfig;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * @author Theodor Foerster
 */
public class WPSPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static WPSPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	public static final String ID = "org.n52.wps.client.udig"; //$NON-NLS-1$
	
	/**
	 * The constructor.
	 */
	public WPSPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 * 
	 * includes initialization of wps config.
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
     //*   ClassLoader current = getClass().getClassLoader();
    //*    try{
   //*         Thread.currentThread().setContextClassLoader(WPSDataStore.class.getClassLoader());
//            setLoggerLevel("org.geotools.data.wfs", "net.refractions.udig.catalog.wfs/debug/wfs"); //$NON-NLS-1$ //$NON-NLS-2$
//            setLoggerLevel("org.geotools.xml","net.refractions.udig.catalog.wfs/debug/wfs");         //$NON-NLS-1$ //$NON-NLS-2$
//            setLoggerLevel("org.geotools.xml.sax", "net.refractions.udig.catalog.wfs/debug/wfs");         //$NON-NLS-1$ //$NON-NLS-2$
   //*         if( isDebugging("net.refractions.udig.catalog.wfs/debug/gml") ) //$NON-NLS-1$
		//*             GMLSchema.setLogLevel(Level.FINE);
		//*         else
		//*             GMLSchema.setLogLevel(Level.SEVERE);
            WPSConfig.forceInitialization(this.getClass().getResourceAsStream("/config/wps_config.xml"));
        
          //*    }finally{
          //*          Thread.currentThread().setContextClassLoader(current);                      
          //*     }
	}

    private void setLoggerLevel( String loggerID, String traceID ) {
        Logger logger = Logger.getLogger(loggerID);
        if( isDebugging(traceID) ) 
            logger.setLevel(Level.FINE);
        else
            logger.setLevel(Level.SEVERE);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);
    }

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		resourceBundle = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static WPSPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = WPSPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("org.n52.wps.client.udig.WfsPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}
    /**
     * Logs the Throwable in the plugin's log.
     * <p>
     * This will be a user visable ERROR iff:
     * <ul>
     * <li>t is an Exception we are assuming it is human readable or if a message is provided
     * </ul>
     * </p>
     * @param message 
     * @param t 
     */
    public static void log( String message, Throwable t ) {
        int status = t instanceof Exception || message != null ? IStatus.ERROR : IStatus.WARNING;
        getDefault().getLog().log(new Status(status, ID, IStatus.OK, message, t));
    }
    /**
     * Messages that only engage if getDefault().isDebugging()
     * <p>
     * It is much prefered to do this:<pre><code>
     * private static final String RENDERING = "net.refractions.udig.project/render/trace";
     * if( ProjectUIPlugin.getDefault().isDebugging() && "true".equalsIgnoreCase( RENDERING ) ){
     *      System.out.println( "your message here" );
     * }
     * </code></pre>
     * </p>
     * @param message 
     * @param e 
     */
    public static void trace( String message, Throwable e) {
        if( getDefault().isDebugging() ) {
            if( message != null ) System.out.println( message );
            if( e != null ) e.printStackTrace();
        }
    }
    /**
     * Performs the Platform.getDebugOption true check on the provided trace
     * <p>
     * Note: ProjectUIPlugin.getDefault().isDebugging() must also be on.
     * <ul>
     * <li>Trace.RENDER - trace rendering progress
     * </ul>
     * </p> 
     * @param trace currently only RENDER is defined
     * @return true if -debug is on for this plugin 
     */
    public static boolean isDebugging( final String trace ){
        return getDefault().isDebugging() &&
            "true".equalsIgnoreCase(Platform.getDebugOption(trace)); //$NON-NLS-1$    
    }    
}
