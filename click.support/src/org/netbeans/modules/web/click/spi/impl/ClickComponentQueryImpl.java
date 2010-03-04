/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.click.spi.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.click.ClickResourceFinder;
import org.netbeans.modules.web.click.api.ClickFileType;
import org.netbeans.modules.web.click.editor.ClickEditorUtilities;
import org.netbeans.modules.web.click.spi.ClickComponentQueryImplementation;
import org.netbeans.modules.web.common.util.WebModuleUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hantsy
 */
@ServiceProvider(service = org.netbeans.modules.web.click.spi.ClickComponentQueryImplementation.class)
public class ClickComponentQueryImpl implements ClickComponentQueryImplementation {

    public ClickComponentQueryImpl() {
    }
    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = java.util.logging.Logger.getLogger("org.netbeans.modules.web.click.spi.impl.ClickComponentQueryImpl");
        org.netbeans.modules.web.click.spi.impl.ClickComponentQueryImpl.initLoggerHandlers();
    }

    private static final void initLoggerHandlers() {
        java.util.logging.Handler[] handlers = LOGGER.getHandlers();
        boolean hasConsoleHandler = false;
        for (java.util.logging.Handler handler : handlers) {
            if (handler instanceof java.util.logging.ConsoleHandler) {
                hasConsoleHandler = true;
            }
        }
        if (!hasConsoleHandler) {
            LOGGER.addHandler(new java.util.logging.ConsoleHandler());
        }
        LOGGER.setLevel(java.util.logging.Level.FINEST);
    }

    @Override
    public FileObject[] find(FileObject activeFileObject, ClickFileType clickFileType) {
        Parameters.notNull("ClickComponentQueryImpl:activeFileObject can be null", activeFileObject);
        Parameters.notNull("ClickComponentQueryImpl:clickFileType can be null", clickFileType);

        Project project = FileOwnerQuery.getOwner(activeFileObject);

        String fileNameExt = activeFileObject.getNameExt();
        LOGGER.log(Level.FINEST, "activatedFile@" + fileNameExt + ",target clickFileType @" + clickFileType);

        switch (clickFileType) {
            case CLASS:
                if (fileNameExt.endsWith(".java")) {
                    return new FileObject[]{activeFileObject};
                } else if (fileNameExt.endsWith(".htm") || fileNameExt.endsWith(".jsp")) {
                    return new FileObject[]{findClassByPage(project, activeFileObject)};
                }
//                else if (fileNameExt.endsWith(".properties")) {
//                    return findClassByProperites(project, activeFileObject);
//                }
                break;
            case TEMPLATE:
                if (fileNameExt.endsWith(".htm") || fileNameExt.endsWith(".jsp")) {
                     return new FileObject[]{activeFileObject};
                } else if (fileNameExt.endsWith(".java")) {
                     return findPageByClass(project, activeFileObject).toArray(new FileObject[0]);
                }
//                else if (fileNameExt.endsWith(".properties")) {
//                    return findPageByProperites(project, activeFileObject);
//                }
                break;
//            case PROPETIES:
//
//                if (fileNameExt.endsWith(".properties")) {
//                    return activeFileObject;
//                } else if (fileNameExt.endsWith(".java")) {
//                    return findPropertiesByClass(project, activeFileObject);
//                } else if (fileNameExt.endsWith(".htm") || fileNameExt.endsWith(".jsp")) {
//                    return findPropertiesByPage(project, activeFileObject);
//                }
//                break;
            default:
                break;
        }
        return null;
    }

    //private methods
    private FileObject findClassByPage(final Project project, FileObject pageFileObject) {

        String pagePath = WebModuleUtilities.getPathByWebResourceFileObject(project, pageFileObject);
        if (pagePath == null) {
            return null;
        }

        String pageClazz = ClickResourceFinder.findClassByPath(project, pagePath);
        if (pageClazz == null) {
            return null;
        }

        return WebModuleUtilities.findJavaSourceByClassFQN(project, pageClazz);
    }

    public List<FileObject> findPageByClass(Project project, FileObject classFileObject) {
        String className = WebModuleUtilities.getClassFQNByJavaSourceFileObject(classFileObject);
        if (className == null) {
            return Collections.<FileObject>emptyList();
        }

        String[] paths = ClickResourceFinder.findPathByClass(project, className);
        List<FileObject> pathFOs = new ArrayList<FileObject>();
        for (String p : paths) {
            pathFOs.add(WebModuleUtilities.findWebResourceByPath(project, p));
        }

        return pathFOs;
    }

    private FileObject findPageByPath(FileObject webRoot, String path) {
        return ClickEditorUtilities.findPageByPath(webRoot, path);
    }

    private FileObject findPropertiesByClass(Project project, FileObject classFileObject) {

        String classFQN = WebModuleUtilities.getClassFQNByJavaSourceFileObject(classFileObject);
        if (classFQN == null) {
            return null;
        }

        String proFilePath = classFQN.replaceAll(".", "/") + ".properties";
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] resourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        FileObject targetFO = null;
        for (SourceGroup sg : resourceGroups) {
            targetFO = sg.getRootFolder().getFileObject(proFilePath);
            if (targetFO != null) {
                return targetFO;
            }
        }

        if (targetFO == null) {
            resourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (SourceGroup sg : resourceGroups) {
                targetFO = sg.getRootFolder().getFileObject(proFilePath);
                if (targetFO != null) {
                    break;
                }
            }
        }

        return targetFO;
    }

//    private FileObject findClassByProperites(Project project, FileObject propertiesFileObject) {
//        FileObject resouceRoot = WebModuleUtilities.getResourcesRoot(project);
//
//
//        if (!FileUtil.isParentOf(resouceRoot, propertiesFileObject)) {
//            return null;
//
//
//        }
//
//        String relativePath = FileUtil.getRelativePath(resouceRoot, propertiesFileObject);
//        FileObject srcRoot = WebModuleUtilities.getJavaSourcesRoot(project);
//
//
//        return srcRoot.getFileObject(relativePath.substring(0, relativePath.lastIndexOf(".")) + ".java");
//
//
//    }
//
//    private FileObject findPageByProperites(Project project, FileObject activeFileObject) {
//        FileObject classFO = findClassByProperites(project, activeFileObject);
//
//
//        if (classFO != null) {
//            return findPageByClass(project, classFO);
//
//
//        }
//        return null;
//
//
//    }

    private FileObject findPropertiesByPage(Project project, FileObject activeFileObject) {
        FileObject classFO = findClassByPage(project, activeFileObject);
        if (classFO != null) {
            return findPropertiesByClass(project, classFO);
        }
        return null;
    }
}
