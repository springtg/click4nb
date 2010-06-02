/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cayenne.modeler;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import org.apache.cayenne.modeler.Main;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public final class OpenCayenneModelerAction implements ActionListener {

    private final CayenneDomainDataObject context;

    public OpenCayenneModelerAction(DataObject context) {
        this.context = (CayenneDomainDataObject) context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        new MainWrapper().runModeler(FileUtil.toFile(context.getPrimaryFile()));
    }

    class MainWrapper extends Main {

        @Override
        protected void runModeler(File projectFile) {
            super.runModeler(projectFile);
        }

        @Override
        protected void configureLookAndFeel() {
        }
    }
}
