/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.click.refactorings;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Handles package rename.
 *
 * @author Erno Mononen
 */
public class ClickXmlPackageRename extends BaseRename {

    /**
     * The folder or package being renamed.
     */
    private final FileObject pkg;
    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = java.util.logging.Logger.getLogger("org.netbeans.modules.web.click.refactorings.ClickXmlPackageRename");
        org.netbeans.modules.web.click.refactorings.ClickXmlPackageRename.initLoggerHandlers();
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
    private final RenameRefactoring rename;

    public ClickXmlPackageRename(FileObject clickFO, FileObject pkg, RenameRefactoring rename) {

        super(clickFO);
        this.pkg = pkg;
        this.rename = rename;
        LOGGER.log(Level.FINEST, "clickFO@" + clickFO + ", pkg @" + pkg + ", rename @" + rename);

    }

    protected List<RenameItem> getRenameItems() {
        List<RenameItem> result = new ArrayList<RenameItem>();
        List<FileObject> fos = new ArrayList<FileObject>();
        RefactoringUtil.collectChildren(pkg, fos);
        for (FileObject each : fos) {
            // #142870 -- skip package-info, it is not needed in web.xml refactoring
            if (RefactoringUtil.isPackageInfo(each)) {
                continue;
            }
            String oldFqn = RefactoringUtil.getQualifiedName(each);
            String fqn = RefactoringUtil.getQualifiedName(each);
            // #153294 - additional check before refactoring starts
            if (RefactoringUtil.isValidPackageName(fqn)) {
                String newFqn = RefactoringUtil.constructNewName(each, rename);
                result.add(new RenameItem(newFqn, oldFqn));
            } else {
                result.add(new RenameItem(null, null, new Problem(true,
                        NbBundle.getMessage(ClickXmlPackageRename.class,
                        "TXT_ErrInvalidPackageName", fqn))));
            }

        }
        return result;
    }

    List<RenameItem> getPackageRenameItems() {
        List<RenameItem> result = new ArrayList<RenameItem>();
        ClassPath classPath = ClassPath.getClassPath(pkg, ClassPath.SOURCE);
        String oldPkgName = "";

        if (classPath != null) {
            oldPkgName = classPath.getResourceName(pkg, '.', false);
        }

        if (RefactoringUtil.isPackage(rename)) {
            result.add(new RenameItem(rename.getNewName(), oldPkgName));
            LOGGER.log(Level.FINEST, "it is package rename, oldpkgName@" + oldPkgName + ", new pkgname@" + rename.getNewName());
            return result;
        }

        LOGGER.log(Level.FINEST, "it is folder rename");

        List<FileObject> fos = new ArrayList<FileObject>();
        RefactoringUtil.collectFolders(pkg, fos);

        String newName = rename.getNewName();
        String prefix = "";
        if (null != newName && !"".equals(newName)) {
            prefix = classPath.getResourceName(pkg.getParent(), '.', false) + "." + newName;
            result.add(new RenameItem(prefix, oldPkgName));
        }

        for (FileObject each : fos) {

            if (RefactoringUtil.isValidPackageName(newName)) {
                String newPackageName = prefix + "." + FileUtil.getRelativePath(pkg, each).replaceAll("/", "\\.");
                String oldPackageName = classPath.getResourceName(each, '.', false);
                LOGGER.log(Level.FINEST, "folder rename, oldPackageName@" + oldPackageName + ", newPackageName@" + newPackageName);
                result.add(new RenameItem(newPackageName, oldPackageName));
            } else {
                result.add(new RenameItem(null, null, new Problem(true,
                        NbBundle.getMessage(ClickXmlPackageRename.class,
                        "TXT_ErrInvalidPackageName", newName))));
            }
        }
        return result;
    }

    protected AbstractRefactoring getRefactoring() {
        return rename;
    }
}
