/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.click;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.click.api.model.ClickApp;
import org.netbeans.modules.web.click.api.model.ClickModel;
import org.netbeans.modules.web.click.api.model.Page;
import org.netbeans.modules.web.click.api.model.Pages;
import org.netbeans.modules.web.click.api.model.utils.ClickModelUtilities;
import org.netbeans.modules.web.common.util.WebModuleUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author hantsy
 */
public class ClickResourceFinder {

    //initialize the logger.
    private static final java.util.logging.Logger log = Logger.getLogger(ClickResourceFinder.class.getName());
    //------------------variables-----------------------
    private static Map<String, PathClassPair> pageByPathMap = null;//Collections.<String, String>emptyMap();
    private static Map<String, Set<PathClassPair>> pageByClassMap = null;
    private static Set<String> orphanPageClazzCache = null;

    //------------------public methods------------------
    public static void initialize(Project project) {
        log.finest("Initialize the Click resource template and page class cache.");
        assert project != null;
        if (pageByPathMap == null) {
            pageByPathMap = new HashMap<String, PathClassPair>();
        } else {
            pageByPathMap.clear();
        }

        if (pageByClassMap == null) {
            pageByClassMap = new HashMap<String, Set<PathClassPair>>();
        } else {
            pageByClassMap.clear();
        }

        if(orphanPageClazzCache==null){
            orphanPageClazzCache=new HashSet<String>();
        }else{
            orphanPageClazzCache.clear();
        }


        WebModule wm = WebModuleUtilities.getWebModule(project);
        if (wm == null) {
            return;
        }

        FileObject webRoot = wm.getDocumentBase();
        List<String> templatesList = new ArrayList<String>();

        Enumeration<? extends FileObject> resources = webRoot.getData(true);
        while (resources.hasMoreElements()) {
            FileObject resource = resources.nextElement();
            log.finest("Find data resource " + resource.getName() + "(" + resource.getPath() + ") .");
            try {
                if (!FileUtil.toFile(resource).getCanonicalPath().contains("/WEB-INF") && ("htm".equalsIgnoreCase(resource.getExt()) || "jsp".equalsIgnoreCase(resource.getExt()))) {
                    log.finest("Add " + resource.getName() + " to templates list.");
                    templatesList.add(FileUtil.getRelativePath(webRoot, resource));
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        FileObject clickFO = ClickConfigUtilities.getClickConfigFile(project, ClickConstants.DEFAULT_CLICK_APP_CONFIG_FILE);
        ClickModel clickModel = ClickModelUtilities.getClickModel(clickFO, false);

        ClickApp clickRoot = clickModel.getRootComponent();
        List<Pages> pagesList = clickRoot.getPagesList();

        boolean automap = true;
        Pages pages;
        String pagesPackage;
        for (int i = 0; i < pagesList.size(); i++) {
            pages = pagesList.get(i);
            pagesPackage = pages.getPackage();
            automap = !("false".equals(pages.getAutoMapping()));
            buildManualPageMapping(pages, pagesPackage);
            if (automap) {
                buildAutoPageMapping(project, pages, pagesPackage, templatesList);
            }
        }
        buildClassMap();
    }

    /**
     * Find template path by page class.
     * @param className Page class FQN.
     * @return template file relative path.
     */
    public static String[] findPathByClass(final Project project, final String className) {
        initializeIfNeeded(project);
        List<String> results = new ArrayList<String>();

        //update cache firstly...if needed.
        FileObject classFO = WebModuleUtilities.findJavaSourceByClassFQN(project, className);
        Set<PathClassPair> pagesInCache = pageByClassMap.get(className);

        if (classFO == null) {
            if (pagesInCache != null && !pagesInCache.isEmpty()) {
                //The cache is dirty...update it.
                pageByClassMap.remove(className);
            }
        } else {
            if (pagesInCache != null && !pagesInCache.isEmpty()) {
                String pagePath = null;
                Iterator<PathClassPair> pagesIterator = pagesInCache.iterator();
                PathClassPair page = null;
                while (pagesIterator.hasNext()) {
                    page = pagesIterator.next();
                    pagePath = page.getPath();
                    if (WebModuleUtilities.findWebResourceByPath(project, pagePath) == null) {
                        pageByPathMap.remove(pagePath);
                        pagesInCache.remove(page);
                    }
                }
            } else {
                //TODO add page class to cache...
                WebModule wm = WebModuleUtilities.getWebModule(project);
                FileObject webRoot = wm.getDocumentBase();

                FileObject clickFO = ClickConfigUtilities.getClickConfigFile(project, ClickConstants.DEFAULT_CLICK_APP_CONFIG_FILE);
                ClickModel clickModel = ClickModelUtilities.getClickModel(clickFO, false);
                ClickApp clickModelRoot = clickModel.getRootComponent();

                List<Pages> pagesList = clickModelRoot.getPagesList();
                if (pagesList != null && !pagesList.isEmpty()) {
                    boolean automap = true;
                    Pages pagesCom = null;
                    String pagesPackage = "";
                    for (int i = 0; i < pagesList.size(); i++) {
                        pagesCom = pagesList.get(i);
                        automap = !("false".equals(pagesCom.getAutoMapping()));
                        pagesPackage = pagesCom.getPackage();

                        List<Page> pageList = pagesCom.getPageList();
                        Page pageCom = null;

                        //search page mapping config.
                        for (int j = 0; j < pageList.size(); j++) {
                            pageCom = pageList.get(j);
                            if (className.equals(pageCom.getClassName())) {
                                String pagePath = pageCom.getPath();
                                if (WebModuleUtilities.findWebResourceByPath(project, pagePath) != null) {
                                    PathClassPair pathClazzPair = new PathClassPair(pagePath, className);
                                    pageByPathMap.put(pagePath, pathClazzPair);
                                    addToClassMap(pathClazzPair);
                                } else {
                                    //it is a page class, but does not has a template file.
                                }
                            }
                        }

                        //search automaping.
                        if (automap) {
                            String targetPackageName = className.substring(0, className.lastIndexOf("."));
                            String targetSimpleClazzName = className.substring(className.lastIndexOf(".") + 1);
                            boolean pathClazzPairFound = false;

                            if (targetPackageName.startsWith(pagesPackage)) {

                                String dir = targetPackageName.substring(pagesPackage.length());
                                FileObject targetTemplatesFolder = null;
                                log.finest("Get the dir name @" + dir);
                                if (dir != null && !"".equals(dir)) {
                                    if (dir.contains(".")) {
                                        dir = dir.replaceAll(".", "/");
                                        log.finest("Get the dir name(converted to path) @" + dir);
                                    }
                                    targetTemplatesFolder = webRoot.getFileObject(dir);
                                } else {
                                    //pagesPackage equals targetPackageName
                                    targetTemplatesFolder = webRoot;
                                }


                                if (targetTemplatesFolder != null) {
                                    Enumeration<? extends FileObject> templates = targetTemplatesFolder.getData(false);

                                    FileObject template = null;
                                    String templateName = null;
                                    String guessClazzName = null;
                                    String guessPagePath = "";
                                    PathClassPair autoPageClazzPair = null;


                                    while (templates.hasMoreElements()) {
                                        template = templates.nextElement();

                                        if ("jsp".equals(template.getExt()) || "htm".equals(template.getExt())) {

                                            templateName = template.getName();
                                            log.finest("Guess template@" + templateName);
                                            guessClazzName = computeClassNameByTemplateName(templateName);
                                            log.finest("Guess classname by template@" + guessClazzName);

                                            if (guessClazzName.equals(targetSimpleClazzName)) {

                                                guessPagePath = FileUtil.getRelativePath(webRoot, template);
                                                autoPageClazzPair = new PathClassPair(guessPagePath, guessClazzName);
                                                pageByPathMap.put(guessPagePath, autoPageClazzPair);
                                                addToClassMap(autoPageClazzPair);
                                                pathClazzPairFound = true;
                                            }

                                            if (!pathClazzPairFound && !guessClazzName.endsWith("Page")) {

                                                guessClazzName = guessClazzName + "Page";
                                                log.finest("guess clazz name @" + guessClazzName);
                                                if (guessClazzName.equals(targetSimpleClazzName)) {
                                                    guessPagePath = FileUtil.getRelativePath(webRoot, template);
                                                    autoPageClazzPair = new PathClassPair(guessPagePath, guessClazzName);
                                                    pageByPathMap.put(guessPagePath, autoPageClazzPair);
                                                    addToClassMap(autoPageClazzPair);
                                                    pathClazzPairFound = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }//if (automap)
                    }
                }// if (pagesList != null && !pagesList.isEmpty())
            }
        }

        //fetch results...
        Set<PathClassPair> pathClazzPairList = pageByClassMap.get(className);
        if (pathClazzPairList != null) {
            for (PathClassPair p : pathClazzPairList) {
                results.add(p.getClazz());
            }
        }
        return results.toArray(new String[0]);
    }

    /**
     * Find page class by template path.
     * @param templatePath Page template file path.
     * @return Page class FQN
     */
    public static String findClassByPath(final Project project, final String templatePath) {
        initializeIfNeeded(project);

        //update cache firstly...if needed.
        FileObject pathFO = WebModuleUtilities.findWebResourceByPath(project, templatePath);
        PathClassPair page = pageByPathMap.get(templatePath);

        if (pathFO == null) {
            if (page != null) {
                pageByPathMap.remove(templatePath);
            }
            return null;
        } else {
            if (page != null) {
                String pageClazz = page.getClazz();
                if (WebModuleUtilities.findJavaSourceByClassFQN(project, pageClazz) == null) {
                    pageByClassMap.remove(pageClazz);
                    pageByPathMap.remove(page.getPath());
                }
            } else {
                FileObject clickFO = ClickConfigUtilities.getClickConfigFile(project, ClickConstants.DEFAULT_CLICK_APP_CONFIG_FILE);
                ClickModel clickModel = ClickModelUtilities.getClickModel(clickFO, false);
                ClickApp clickModelRoot = clickModel.getRootComponent();

                List<Pages> pagesList = clickModelRoot.getPagesList();
                if (pagesList != null && !pagesList.isEmpty()) {
                    boolean automap = true;
                    Pages pagesCom = null;
                    String pagesPackage = "";
                    for (int i = 0; i < pagesList.size(); i++) {
                        pagesCom = pagesList.get(i);
                        automap = !("false".equals(pagesCom.getAutoMapping()));
                        pagesPackage = pagesCom.getPackage();

                        List<Page> pageList = pagesCom.getPageList();
                        Page pageCom = null;

                        //search page mapping config.
                        for (int j = 0; j < pageList.size(); j++) {
                            pageCom = pageList.get(j);
                            if (templatePath.equals(pageCom.getPath())) {
                                String pageClazz = pageCom.getClassName();
                                if (WebModuleUtilities.findJavaSourceByClassFQN(project, pageClazz) != null) {
                                    PathClassPair pathClazzPair = new PathClassPair(templatePath, pageClazz);
                                    pageByPathMap.put(templatePath, pathClazzPair);
                                    addToClassMap(pathClazzPair);
                                } else {
                                    //it is page template file, but does not has a page class.
                                }
                            }
                        }

                        //search automaping
                        if (automap) {
                            String targetDirName = "";
                            String targetClazzName = "";

                            if (templatePath.contains("/")) {
                                targetDirName = templatePath.substring(0, templatePath.lastIndexOf("/"));
                                targetClazzName = templatePath.substring(templatePath.lastIndexOf("/") + 1);
                            } else {
                                targetClazzName = templatePath;
                            }

                            String guessClazzName = computeClassNameByTemplateName(targetClazzName);
                            String guessPackageDir = "";
                            if (pagesPackage != null && !"".equals(pagesPackage)) {
                                guessPackageDir = (pagesPackage + ".").replaceAll(".", "/");
                            }
                            if (targetDirName != null && !"".equals(targetDirName)) {
                                guessPackageDir = guessPackageDir + targetDirName;
                            }

                            String guessClazzFilePath = guessPackageDir + "/" + guessClazzName + ".java";

                            Sources sources = ProjectUtils.getSources(project);
                            SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

                            String pageClazz = "";
                            PathClassPair pathClazzPair = null;
                            boolean pathClazzPairFound = false;
                            for (SourceGroup sg : sourceGroups) {
                                if (sg.getRootFolder().getFileObject(guessClazzFilePath) != null) {
                                    pageClazz = guessPackageDir + "/" + guessClazzName;
                                    pageClazz = pageClazz.replaceAll("/", ".");

                                    pathClazzPair = new PathClassPair(templatePath, pageClazz);
                                    pageByPathMap.put(templatePath, pathClazzPair);
                                    addToClassMap(pathClazzPair);
                                    pathClazzPairFound = true;
                                }

                                if (!pathClazzPairFound && !guessClazzName.endsWith("Page")) {
                                    guessClazzFilePath = guessPackageDir + "/" + guessClazzName + "Page.java";

                                    if (sg.getRootFolder().getFileObject(guessClazzFilePath) != null) {
                                        pageClazz = guessPackageDir + "/" + guessClazzName + "Page";
                                        pageClazz = pageClazz.replaceAll("/", ".");

                                        pathClazzPair = new PathClassPair(templatePath, pageClazz);
                                        pageByPathMap.put(templatePath, pathClazzPair);
                                        addToClassMap(pathClazzPair);
                                        pathClazzPairFound = true;
                                    }
                                }
                            }
                        }//if (automap)
                    }
                }
            }
        }

        //fetch result
        PathClassPair targetPathClazzPair = pageByPathMap.get(templatePath);
        if (targetPathClazzPair != null) {
            return targetPathClazzPair.getClazz();
        }

        return null;
    }

    public static boolean isInitialized() {
        return pageByPathMap != null;
    }

    public static void initializeIfNeeded(Project project) {
        if (!isInitialized()) {
            initialize(project);
        }
    }

    //------------------private methods------------------
    private static void buildManualPageMapping(Pages pages, String pagesPackage) {
        log.finest("starting buildManualPageMapping...");

        List<Page> pageList = pages.getPageList();
        if (pageList == null || pageList.isEmpty()) {
            return;
        }

        for (Page page : pageList) {
            pageByPathMap.put(page.getPath(), new PathClassPair(page.getPath(), page.getClassName()));
            log.finest("Add '" + page.getPath() + "' -> '" + page.getClassName() + "' to pageByPathMap");
        }
    }

    private static void buildAutoPageMapping(Project project, Pages pages, String pagesPackage, List<String> templates) {
        log.finest("starting buildAutoPageMapping...");
        for (int i = 0; i
                < templates.size(); i++) {
            String pagePath = templates.get(i);
            if (!pageByPathMap.containsKey(pagePath)) {
                String pageClazz = getPageClass(project, pagePath, pagesPackage);
                if (pageClazz != null) {
                    pageByPathMap.put(pagePath, new PathClassPair(pagePath, pageClazz));
                    log.finest("Add '" + pagePath + "' -> '" + pageClazz + "' to pageByPathMap");
                }
            }
        }
    }

    private static String getPageClass(Project project, String pagePath, String pagesPackage) {
        log.finest(" Find page class package @" + pagesPackage + ", page path @" + pagePath);

        String packageName = pagesPackage + ".";
        String className = "";

        String path = pagePath.substring(0, pagePath.lastIndexOf("."));
        if (path.indexOf("/") != -1) {
            StringTokenizer tokenizer = new StringTokenizer(path, "/");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (tokenizer.hasMoreTokens()) {
                    packageName = packageName + token + ".";
                } else {
                    className = token;
                }
            }
        } else {
            className = path;
        }
        className = computeClassNameByTemplateName(className);

        // className = 'org.apache.click.pages.EditCustomer'
        className = packageName + className;
        String clazzRelativePath = className.replaceAll(".", "/") + ".java";

        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject rootFolder = null;
        FileObject targetFileObject = null;
        for (SourceGroup group : sourceGroups) {
            rootFolder = group.getRootFolder();
            targetFileObject = rootFolder.getFileObject(clazzRelativePath);

            if (targetFileObject != null) {
                return className;
            }
        }

        if (!className.endsWith("Page")) {
            className = className + "Page";
            clazzRelativePath = className.replaceAll(".", "/") + ".java";
            for (SourceGroup group : sourceGroups) {
                rootFolder = group.getRootFolder();
                targetFileObject = rootFolder.getFileObject(clazzRelativePath);
                if (targetFileObject != null) {
                    return className;
                }
            }
        }

        return null;
    }

    private static String computeClassNameByTemplateName(String className) {
        StringTokenizer tokenizer = new StringTokenizer(className, "_-");
        className = "";
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            token = Character.toUpperCase(token.charAt(0)) + token.substring(1);
            className += token;
        }
        return className;
    }

    private static void buildClassMap() {
        log.finest("starting buildClassMap...");
        for (Iterator i = pageByPathMap.values().iterator(); i.hasNext();) {
            PathClassPair page = (PathClassPair) i.next();
            addToClassMap(page);
        }
    }

    private static void addToClassMap(PathClassPair page) {
        log.finest("starting addToClassMap...");
        Set<PathClassPair> value = pageByClassMap.get(page.getClazz());

        if (value == null) {
            value = new HashSet<PathClassPair>();
            value.add(page);
            pageByClassMap.put(page.getClazz(), value);
            log.finest("Add '" + page.getClazz() + "' ->'" + page.getPath() + "@" + page.getClazz() + "'");
        } else {
            log.finest("Add '" + page.getClazz() + "' ->'" + page.getPath() + "@" + page.getClazz() + "'");
            value.add(page);
            log.finest("There are " + value.size() + " mapping to a class, it is problematic at runtime...");
            //TODO Add error maker to Page Class...

        }
    }

    //-----------------inner classes-------------------------
    public static final class PathClassPair {

        String path;
        String clazz;

        public PathClassPair(String path, String clazz) {
            this.path = path;
            this.clazz = clazz;
        }

        public String getClazz() {
            return clazz;
        }

        public String getPath() {
            return path;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PathClassPair other = (PathClassPair) obj;
            if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
                return false;
            }
            if ((this.clazz == null) ? (other.clazz != null) : !this.clazz.equals(other.clazz)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + (this.path != null ? this.path.hashCode() : 0);
            hash = 89 * hash + (this.clazz != null ? this.clazz.hashCode() : 0);
            return hash;
        }
    }
}
