/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.spring.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.common.util.WebModuleUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author hantsy
 */
public class SpringConfigUtilities {

    public static final String CONTEXT_LOADER = "org.springframework.web.context.ContextLoaderListener"; // NOI18N
    public static final String TEMPLATE_FOLDER = "SpringFramework/Templates/";
    public static final String CONFIG_FILE = "spring-2.5.xml";
    public static final String LIBRARY_NAME = "spring-framework";

    public static void addSpringSupportToWebModule(WebModule wm) {

        WebApp webApp = WebModuleUtilities.getDDRoot(wm);
        Library springLib = LibraryManager.getDefault().getLibrary(LIBRARY_NAME);
        if (springLib != null) {
            List<Library> libs=new ArrayList<Library>();
            libs.add(springLib);
            WebModuleUtilities.addLibrariesToWebModule(libs, wm);
        }
        WebModuleUtilities.addContextParam(webApp, "contextConfigLocation", "/WEB-INF/applicationContext.xml");
        try {
            WebModuleUtilities.addListener(webApp, CONTEXT_LOADER);
            webApp.write(wm.getDeploymentDescriptor());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        WebModuleUtilities.createFromTemplate(TEMPLATE_FOLDER, CONFIG_FILE, wm.getWebInf(), "applicationContext");

    }
}
