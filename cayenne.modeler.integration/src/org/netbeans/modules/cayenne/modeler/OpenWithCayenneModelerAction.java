/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cayenne.modeler;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

public final class OpenWithCayenneModelerAction implements ActionListener {

    private final DataObject context;

    public OpenWithCayenneModelerAction(DataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        if(context !=null){
            final FileObject fo=context.getPrimaryFile();
            new Thread(new CayenneModelerLauncher(fo)).run();
           // new CayenneModelerLauncher().launch(fo);
        }
    }
}
