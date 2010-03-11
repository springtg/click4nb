/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cayenne.modeler;

import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author hantsy
 */
public class CayenneConfigUtilities {

    public static void copyLibraries(Project project){
        Sources sources=ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups=sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject sourceRoot=null;
        if(sourceGroups!=null && sourceGroups.length>0){
            sourceRoot=sourceGroups[0].getRootFolder();
        }
        try {
            ProjectClassPathModifier.addLibraries(new Library[]{LibraryManager.getDefault().getLibrary("cayenne-library")}, sourceRoot, ClassPath.COMPILE);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (UnsupportedOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
