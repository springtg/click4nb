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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.web.click.api.model.ClickModel;
import org.openide.filesystems.FileObject;

/**
 * Handles moving of a class.
 *
 * @author Erno Mononen
 */
public class ClickXmlMove extends BaseRename{
    
    private final MoveRefactoring move;
    private final List<String> classes;
    
    public ClickXmlMove(FileObject clickFO, MoveRefactoring move) {
        super(clickFO);
        this.move = move;
        this.classes = RefactoringUtil.getRefactoredClasses(move);
    }
    
    protected AbstractRefactoring getRefactoring() {
        return move;
    }
    
    protected List<RenameItem> getRenameItems() {
        String pkg = RefactoringUtil.getPackageName(move.getTarget().lookup(URL.class));
        List<RenameItem> result = new ArrayList<RenameItem>();
        for (String clazz : classes) {
            String newName = pkg + "." + RefactoringUtil.unqualify(clazz);
            result.add(new RenameItem(newName, clazz));
        }
        return result;
    }

    @Override
    List<RenameItem> getPackageRenameItems() {
        return Collections.<RenameItem>emptyList();
    }
}
