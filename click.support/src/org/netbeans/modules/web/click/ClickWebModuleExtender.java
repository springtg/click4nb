/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.click;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.web.AuthConstraint;
import org.netbeans.modules.j2ee.dd.api.web.FormLoginConfig;
import org.netbeans.modules.j2ee.dd.api.web.LoginConfig;
import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.spring.util.SpringConfigUtilities;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.netbeans.modules.web.common.util.WebModuleUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

/**
 *
 * @author hantsy
 */
class ClickWebModuleExtender extends WebModuleExtender {

    WebModule wm;
    ExtenderController controller;
    boolean defaultValue;
    ClickConfigurationPanel component;

    public ClickWebModuleExtender(WebModule wm, ExtenderController controller, boolean defaultValue) {
        this.wm = wm;
        this.controller = controller;
        this.defaultValue = defaultValue;
        getComponent();
    }

    @Override
    public void addChangeListener(ChangeListener arg0) {
    }

    @Override
    public void removeChangeListener(ChangeListener arg0) {
    }

    @Override
    public JComponent getComponent() {
        if (component == null) {
            component = new ClickConfigurationPanel(defaultValue);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void update() {
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Set<FileObject> extend(WebModule wm) {

        Set<FileObject> filesToOpen = new HashSet<FileObject>();
        FileSystem fs;

        if (null != component.getSelectedLibrary()) {
            //final LibraryManager libManager = LibraryManager.getDefault();
            List<Library> libs = new ArrayList<Library>();
            //Library clickLib = libManager.getLibrary(ClickConstants.LIBRARY_CLICK);
            //Library clickMockLib = libManager.getLibrary(ClickConstants.LIBRARY_CLICK_MOCK);
            //libs.add(clickLib);
            libs.add(component.getSelectedLibrary());
            WebModuleUtilities.addLibrariesToWebModule(libs, wm);
        }
        try {
            fs = wm.getWebInf().getFileSystem();
            fs.runAtomicAction(new ClickFrameworkEnabler(wm, filesToOpen));
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return filesToOpen;
    }

    private class ClickFrameworkEnabler implements AtomicAction {

        WebModule wm;
        Set<FileObject> filesToOpen;

        public ClickFrameworkEnabler(WebModule wm, Set<FileObject> filesToOpen) {
            this.wm = wm;
            this.filesToOpen = filesToOpen;
        }

        public void run() throws IOException {
            WebApp webApp = WebModuleUtilities.getDDRoot(wm);

            String defaultPagesPkg = component.getPagesPackage();
            String basePkgName = ClickConstants.DEFAULT_PACKAGE_NAME;
            if (defaultPagesPkg != null && defaultPagesPkg.trim().length() > 0) {
                basePkgName = defaultPagesPkg;
            }

            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("package", basePkgName);
            replacements.put("mode", component.getMode());

            FileObject webInf = wm.getWebInf();

//            WebModuleUtilities.createFromTemplate(ClickConstants.BASE_TEMPLATES_DIR,
//                    ClickConstants.DEFAULT_CLICK_APP_CONFIG_FILE,
//                    webInf,
//                    ClickConstants.DEFAULT_CLICK_APP_CONFIG_FILE,
//                    replacements);

            FileObject basePkg = FileUtil.createFolder(WebModuleUtilities.getJavaSourcesRoot(wm), basePkgName.replaceAll("\\.", "/"));

            //FileObject metainfDir = docBase.createFolder("META-INF");

            String exampleTemplateDir = ClickConstants.BASE_TEMPLATES_DIR;

            //admin-admin-1.htm               LoginPage.java.template
            //admin-Admin1Page.java.template  logout.htm
            //admin-admin-2.htm               LogoutPage.java.template
            //admin-Admin2Page.java.template  menu.xml
            //assets-bannar.png               metainf-context.xml
            //assets-home.png                 not-authorized.htm
            //assets-login.png                NotNotAuthorizedPage.java.template
            //assets-style.css                redirect.html
            //BasePage.java.template          user-home.htm
            //BorderPage.java.template        user-HomePage.java.template
            //border-template.htm             user-user-1.htm
            //click-error.htm                 user-User1Page.java.template
            //click-ErrorPage.java.template   user-user-2.htm
            //click.xml.tempalte                       user-User2Page.java.template
            //login.htm                       web.xml



            String servletClass = ClickConstants.CLICK_SERVELT_CLASS;

            if (component.supportSpring()) {
                servletClass = ClickConstants.SPRING_CLICK_SERVELT_CLASS;
                addSpringSupport(wm);
            }

            WebModuleUtilities.addServlet(webApp, ClickConstants.CLICK_SERVLET_NAME, servletClass, "*.htm", "0");



            //click.xml.template
            WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                    "click.xml",
                    webInf,
                    "click",
                    replacements);

            if (component.requireCreateExample()) {
                FileObject adminPkg = FileUtil.createFolder(basePkg, "admin");
                FileObject userPkg = FileUtil.createFolder(basePkg, "user");
                FileObject clickPkg = FileUtil.createFolder(basePkg, "click");

                // CREATE FOLDERS FOR FILES



                FileObject docBase = wm.getDocumentBase();
                FileObject clickDir = docBase.createFolder("click");
                FileObject assetsDir = docBase.createFolder("assets");
                FileObject adminDir = docBase.createFolder("admin");
                FileObject userDir = docBase.createFolder("user");
                //LoginPage.java.template
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "LoginPage.java",
                        basePkg,
                        "LoginPage",
                        replacements);

                //LogoutPage.java.template
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "LogoutPage.java",
                        basePkg,
                        "LogoutPage",
                        replacements);

                //BasePage.java.template
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "BasePage.java",
                        basePkg,
                        "BasePage",
                        replacements);

                //BorderPage.java
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "BorderPage.java",
                        basePkg,
                        "BorderPage.java",
                        replacements);

                //NotNotAuthorizedPage.java.template
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "NotAuthorizedPage.java",
                        basePkg,
                        "NotAuthorizedPage",
                        replacements);

                //admin-Admin1Page.java.template
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "admin-Admin1Page.java",
                        adminPkg,
                        "Admin1Page.java",
                        replacements);

                //admin-Admin2Page.java.template
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "admin-Admin2Page.java",
                        adminPkg,
                        "Admin2Page",
                        replacements);

                //click-ErrorPage.java
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "click-ErrorPage.java",
                        clickPkg,
                        "ErrorPage",
                        replacements);


                //user-HomePage.java
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "user-HomePage.java",
                        userPkg,
                        "HomePage",
                        replacements);

                //user-User1Page.java
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "user-User1Page.java",
                        userPkg,
                        "User1Page",
                        replacements);

                //user-User2Page.java
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "user-User2Page.java",
                        userPkg,
                        "User2Page",
                        replacements);

                //Create none templates files
                //menu.xml
                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
                        "menu.xml",
                        webInf,
                        "menu");

                //metainf-context.xml
//                WebModuleUtilities.createFromTemplate(exampleTemplateDir,
//                        "metainf-context.xml",
//                        metainfDir,
//                        "context.xml");

                //login.htm
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "login.htm",
                        docBase,
                        "login");

                //logout.htm
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "logout.htm",
                        docBase,
                        "logout");

                //border-template.htm
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "border-template.htm",
                        docBase,
                        "border-template");

                //not-authorized.htm
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "not-authorized.htm",
                        docBase,
                        "not-authorized");

                //macro.vm
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "macro.vm",
                        docBase,
                        "macro");

                //redirect.html
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "redirect.html",
                        docBase,
                        "redirect");

                //admin-admin-1.htm
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "admin-admin-1.htm",
                        adminDir,
                        "admin-1");

                //admin-admin-2.htm
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "admin-admin-2.htm",
                        adminDir,
                        "admin-2");

                //user-home.htm
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "user-home.htm",
                        userDir,
                        "home");

                //user-user-1.htm
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "user-user-1.htm",
                        userDir,
                        "user-1");

                //user-user-2.htm
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "user-user-2.htm",
                        userDir,
                        "user-2");

                //click-error.htm
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "click-error.htm",
                        clickDir,
                        "error");

                //assets-style.css
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "assets-style.css",
                        assetsDir,
                        "style");

                //assets-login.png
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "assets-login.png",
                        assetsDir,
                        "login");

                //assets-home.png
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "assets-home.png",
                        assetsDir,
                        "home");

                //assets-bannar.png
                WebModuleUtilities.copyResource(exampleTemplateDir,
                        "assets-bannar.png",
                        assetsDir,
                        "bannar");

                WebModuleUtilities.addErrorPage(webApp,
                        "403",
                        "/not-authorized.htm");

                WelcomeFileList welcomeFileList = webApp.getSingleWelcomeFileList();
                if (welcomeFileList == null) {
                    try {
                        welcomeFileList = (WelcomeFileList) webApp.createBean("WelcomeFileList"); //NOI18N
                    } catch (ClassNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    webApp.setWelcomeFileList(welcomeFileList);
                }
                welcomeFileList.addWelcomeFile("redirect.html");
                welcomeFileList.setWelcomeFile(0, "redirect.html");
                SecurityConstraint constraint = null;
                try {
                    constraint = (SecurityConstraint) webApp.createBean("SecurityConstraint");

                    WebResourceCollection webResourceCollection = (WebResourceCollection) constraint.createBean("WebResourceCollection");
                    webResourceCollection.setWebResourceName("admin");
                    webResourceCollection.setUrlPattern(new String[]{"/admin/*"});
                    constraint.addWebResourceCollection(webResourceCollection);

                    AuthConstraint authConstraint = (AuthConstraint) constraint.createBean("AuthConstraint");
                    authConstraint.setRoleName(new String[]{"admin"});
                    constraint.setAuthConstraint(authConstraint);
                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
                webApp.addSecurityConstraint(constraint);

                try {
                    constraint = (SecurityConstraint) webApp.createBean("SecurityConstraint");

                    WebResourceCollection webResourceCollection = (WebResourceCollection) constraint.createBean("WebResourceCollection");
                    webResourceCollection.setWebResourceName("user");
                    webResourceCollection.setUrlPattern(new String[]{"/user/*"});
                    constraint.addWebResourceCollection(webResourceCollection);

                    AuthConstraint authConstraint = (AuthConstraint) constraint.createBean("AuthConstraint");
                    authConstraint.setRoleName(new String[]{"admin", "user"});
                    constraint.setAuthConstraint(authConstraint);
                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
                webApp.addSecurityConstraint(constraint);

                LoginConfig loginConfig = null;
                try {
                    loginConfig = (LoginConfig) webApp.createBean("LoginConfig");
                    loginConfig.setAuthMethod("FORM");
                    loginConfig.setRealmName("Secure Zone");

                    FormLoginConfig formLoginConfig = (FormLoginConfig) loginConfig.createBean("FormLoginConfig");
                    formLoginConfig.setFormLoginPage("/login.htm");
                    formLoginConfig.setFormErrorPage("/login.htm?auth-error=true");

                    loginConfig.setFormLoginConfig(formLoginConfig);
                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
                webApp.setLoginConfig(loginConfig);

                SecurityRole securityRole = null;
                try {
                    securityRole = (SecurityRole) webApp.createBean("SecurityRole");
                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
                securityRole.setRoleName("admin");
                securityRole.setDescription("admin role - for convenience using existing Tomcat roles");
                webApp.addSecurityRole(securityRole);

                try {
                    securityRole = (SecurityRole) webApp.createBean("SecurityRole");
                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
                securityRole.setRoleName("user");
                securityRole.setDescription("user role - for convenience using existing Tomcat roles");
                webApp.addSecurityRole(securityRole);

            }


            webApp.write(wm.getDeploymentDescriptor());

        }

        public void writePerformanceFilterToWebApp(WebApp webApp) {
            String filterName = "PerformanceFilter";
            Map<String, String> initParams = new HashMap<String, String>();
            initParams.put("cachable-paths", "/assets/*");
            WebModuleUtilities.addFilter(webApp,
                    filterName,
                    "org.apache.click.extras.filter.PerformanceFilter",
                    initParams, ClickConstants.CLICK_SERVLET_NAME, WebModuleUtilities.FilterMappingType.SERVLET, null);
            WebModuleUtilities.addFilterMapping(webApp, filterName, ".css");
            WebModuleUtilities.addFilterMapping(webApp, filterName, ".js");
            WebModuleUtilities.addFilterMapping(webApp, filterName, ".png");
            WebModuleUtilities.addFilterMapping(webApp, filterName, ".jpg");

        }

        public void writeCompressionFilterToWebApp(WebApp webApp) {
            WebModuleUtilities.addFilter(webApp,
                    "CompressionFilter",
                    "org.apache.click.extras.filter.CompressionFilter",
                    null, ClickConstants.CLICK_SERVLET_NAME, WebModuleUtilities.FilterMappingType.SERVLET, null);
        }

        private void addSpringSupport(WebModule wm) {
            SpringConfigUtilities.addSpringSupportToWebModule(wm);
        }
    }
}
