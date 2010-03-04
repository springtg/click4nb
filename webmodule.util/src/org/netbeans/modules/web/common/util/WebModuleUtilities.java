/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.common.util;

import java.io.IOException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.CreateCapability;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.ErrorPage;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * Utilities for web framework support development.
 * Some of these methods are copied from existed web framework utilities.
 * 
 * @author hantsy
 */
public class WebModuleUtilities {

    private static final Logger LOGGER = Logger.getLogger(WebModuleUtilities.class.getName());

    public static FileObject createFromTemplate(String templatePath, String templateName, FileObject targetFolder, String fileName, Map<String, ?> params) {

        //FileObject templateFO = FileUtil.getConfigFile("SpringFramework/Templates/" + templateName);
        FileObject templateFO = FileUtil.getConfigFile(templatePath + templateName);
        DataObject templateDO = null;
        try {
            templateDO = DataObject.find(templateFO);
            return templateDO.createFromTemplate(DataFolder.findFolder(targetFolder), fileName, params).getPrimaryFile();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static FileObject copyResource(String templateFoler, String resourceName, FileObject targetFolder, String targetName) {
        FileObject templateFO = FileUtil.getConfigFile(templateFoler + resourceName);
        try {
            return FileUtil.copyFile(templateFO, targetFolder, targetName);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static FileObject createFromTemplate(String templatePath, String templateName, FileObject targetFolder, String fileName) {
        //FileObject templateFO = FileUtil.getConfigFile("SpringFramework/Templates/" + templateName);
        FileObject templateFO = FileUtil.getConfigFile(templatePath + templateName);
        DataObject templateDO = null;
        try {
            templateDO = DataObject.find(templateFO);
            return templateDO.createFromTemplate(DataFolder.findFolder(targetFolder), fileName).getPrimaryFile();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static Project getProject(WebModule webModule) {
        // List<FileObject> files = getStrutsFiles(webModule);
        // MODIFY WEB.XML
        FileObject dd = webModule.getDeploymentDescriptor();
        Project project = FileOwnerQuery.getOwner(dd);
        return project;
    }

    public static WebModule getWebModule(Project project) {
        WebModuleProvider provider = project.getLookup().lookup(WebModuleProvider.class);
        if (provider == null) {
            return null;
        }
        WebModule webModule = provider.findWebModule(project.getProjectDirectory());
        if (webModule == null) {
            return null;
        }
        return webModule;
    }

    public static WebApp getDDRoot(WebModule webModule) {
        FileObject dd = webModule.getDeploymentDescriptor();
        try {
            return DDProvider.getDefault().getDDRoot(dd);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static FileObject getJavaSourcesRoot(WebModule webModule) {
        return getJavaSourcesRoot(getProject(webModule));
    }

    public static FileObject getResourcesRoot(WebModule webModule) {
        return getResourcesRoot(getProject(webModule));
    }

    public static FileObject getJavaSourcesRoot(Project project) {
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] javaSrcs =
                srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (javaSrcs != null && javaSrcs.length > 0) {
            return javaSrcs[0].getRootFolder();
        } else {
            throw new RuntimeException("No java sources in given project.");
        }
    }

    public static FileObject findJavaSourceByClassFQN(final Project project, final String classFQN) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String clazzPath = classFQN.replaceAll(".", "/") + ".java";
        FileObject rootFolder = null;
        FileObject targetFO = null;
        for (SourceGroup group : sourceGroups) {
            rootFolder = group.getRootFolder();
            targetFO = rootFolder.getFileObject(clazzPath);

            if (targetFO != null) {
                break;
            }
        }

        return targetFO;
    }

    public static String getClassFQNByJavaSourceFileObject(final FileObject javaSourceFO) {
        if (!"text/x-java".equals(javaSourceFO.getMIMEType())) {
            return null;
        }
        JavaSource js = JavaSource.forFileObject(javaSourceFO);
        final List<String> result = new ArrayList<String>();
        try {
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    List<? extends TypeElement> types = cc.getTopLevelElements();
                    //assert types.size() == 1;
                    for (TypeElement e : types) {
                        if (javaSourceFO.getName().equals(e.getSimpleName().toString())) {
                            result.add(e.getQualifiedName().toString());
                            break;
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public static FileObject findWebResourceByPath(Project project, String path) {
        WebModule wm = getWebModule(project);
        if (wm == null) {
            return null;
        }
        return wm.getDocumentBase().getFileObject(path);
    }

    public static String getPathByWebResourceFileObject(Project project, FileObject webResourceFO) {
        WebModule wm = getWebModule(project);
        if (wm == null) {
            return null;
        }

        FileObject webRoot = wm.getDocumentBase();
        if (webRoot == null) {
            return null;
        }

        if(FileUtil.isParentOf(webRoot, webResourceFO)){
            return FileUtil.getRelativePath(webRoot, webResourceFO);
        }

        return null;
    }

    public static FileObject getResourcesRoot(Project project) {
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] javaSrcs =
                srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (javaSrcs != null && javaSrcs.length > 0) {
            return javaSrcs[0].getRootFolder();
        } else {
            return getJavaSourcesRoot(project);
        }
    }

    public static boolean addLibrariesToWebModule(List<Library> libraries, WebModule webModule) {
        Project project = getProject(webModule);
        if (project == null) {
            return false;
        }
        boolean addLibraryResult = false;
        FileObject javaSource = null;
        try {
            javaSource = getJavaSourcesRoot(project);
            addLibraryResult = ProjectClassPathModifier.addLibraries(libraries.toArray(new Library[libraries.size()]), javaSource, ClassPath.COMPILE);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Libraries required for the Spring MVC project not added", e); // NOI18N
        } catch (UnsupportedOperationException uoe) {
            LOGGER.log(Level.WARNING, "This project does not support adding these types of libraries to the classpath", uoe); // NOI18N
        }
        return addLibraryResult;
    }

    public static Listener addListener(WebApp webApp, String classname) {
        Listener listener = (Listener) createBean(webApp, "Listener"); // NOI18N
        listener.setListenerClass(classname);
        webApp.addListener(listener);
        return listener;
    }

    public static Servlet addServlet(WebApp webApp, String name, String classname, String pattern, String loadOnStartup) {
        Servlet servlet = (Servlet) createBean(webApp, "Servlet"); // NOI18N
        servlet.setServletName(name);
        servlet.setServletClass(classname);
        if (loadOnStartup != null) {
            servlet.setLoadOnStartup(new BigInteger(loadOnStartup));
        }
        webApp.addServlet(servlet);
        if (pattern != null) {
            addServletMapping(webApp, name, pattern);
        }
        return servlet;
    }

    public static ServletMapping addServletMapping(WebApp webApp, String name, String pattern) {
        ServletMapping mapping = (ServletMapping) createBean(webApp, "ServletMapping"); // NOI18N
        mapping.setServletName(name);
        mapping.setUrlPattern(pattern);
        webApp.addServletMapping(mapping);
        return mapping;
    }

    public static Filter addFilter(WebApp webApp, String name, String classname, Map<String, String> initParams, String mappingValue, FilterMappingType mappingType, FilterMappingDispatherType[] types) {
        Filter filter = (Filter) createBean(webApp, "Filter");
        filter.setFilterName(name);
        filter.setFilterClass(classname);
        if (null != initParams) {
            for (String paramName : initParams.keySet()) {
                InitParam ip = (InitParam) createBean(webApp, "InitParam");
                ip.setParamName(paramName);
                ip.setParamValue(initParams.get(paramName));

                filter.addInitParam(ip);
            }
        }
        webApp.addFilter(filter);
        if (mappingValue != null) {
            addFilterMapping(webApp, name, mappingValue, mappingType, types);
        }
        return filter;
    }

    public static Filter addFilter(WebApp webApp, String name, String classname, Map<String, String> initParams, String mappingValue) {
        Filter filter = addFilter(webApp, name, classname, initParams, mappingValue, FilterMappingType.PATTERN, null);
        return filter;
    }

    public static FilterMapping addFilterMappingToServlet(WebApp webApp, String filterName, String servletName) {
        FilterMapping mapping = (FilterMapping) createBean(webApp, "FilterMapping"); // NOI18N
        mapping.setFilterName(filterName);
        mapping.setServletName(servletName);
        webApp.addFilterMapping(mapping);
        return mapping;

    }

    public static FilterMapping addFilterMappingToUrlPattern(WebApp webApp, String filterName, String urlPattern, FilterMappingDispatherType[] types) {
        FilterMapping mapping = (FilterMapping) createBean(webApp, "FilterMapping"); // NOI18N
        mapping.setFilterName(filterName);
        mapping.setUrlPattern(urlPattern);

        if (types != null && types.length > 0) {
            int i = 0;
            for (FilterMappingDispatherType type : types) {
                try {
                    mapping.setDispatcher(i++, type.toString());
                } catch (VersionNotSupportedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        webApp.addFilterMapping(mapping);
        return mapping;
    }

    public static FilterMapping addFilterMapping(WebApp webApp, String name, String mappingValue, FilterMappingType mappingType, FilterMappingDispatherType[] types) {
        FilterMapping mapping = (FilterMapping) createBean(webApp, "FilterMapping"); // NOI18N
        mapping.setFilterName(name);
        if (FilterMappingType.SERVLET == mappingType) {
            mapping.setServletName(mappingValue);
        } else if (FilterMappingType.PATTERN == mappingType) {
            mapping.setUrlPattern(mappingValue);
        }
        if (types != null && types.length > 0) {
            int i = 0;
            for (FilterMappingDispatherType type : types) {
                try {
                    mapping.setDispatcher(i++, type.toString());
                } catch (VersionNotSupportedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        webApp.addFilterMapping(mapping);
        return mapping;
    }

    public static FilterMapping addFilterMapping(WebApp webApp, String name, String mappingValue) {
        FilterMapping mapping = addFilterMapping(webApp, name, mappingValue, FilterMappingType.PATTERN, null);
        return mapping;
    }

    public static ErrorPage addErrorPage(WebApp webApp, String errorCode, String location) {
        ErrorPage errPage = (ErrorPage) createBean(webApp, "ErrorPage");
        errPage.setErrorCode(Integer.parseInt(errorCode));
        errPage.setLocation(location);
        webApp.addErrorPage(errPage);
        return errPage;
    }

    public static InitParam addContextParam(WebApp webApp, String name, String value) {
        InitParam initParam = (InitParam) createBean(webApp, "InitParam"); // NOI18N
        initParam.setParamName(name);
        initParam.setParamValue(value);
        webApp.addContextParam(initParam);
        return initParam;
    }

    public static CommonDDBean createBean(CreateCapability creator, String beanName) {
        CommonDDBean bean = null;
        try {
            bean = creator.createBean(beanName);
        } catch (ClassNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
        return bean;
    }

    public static enum FilterMappingType {

        SERVLET, PATTERN;
    }

    public static enum FilterMappingDispatherType {

        REQUEST("SERVLET"),
        FORWARD("FORWARD"),
        REDIRECT("REDIRECT"),
        INCLUDE("INCLUDE");
        String type;

        FilterMappingDispatherType(String type) {
            this.type = type;

        }

        @Override
        public String toString() {
            return this.type;
        }
    }
}
