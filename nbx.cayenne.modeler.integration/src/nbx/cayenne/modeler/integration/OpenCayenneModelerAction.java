/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nbx.cayenne.modeler.integration;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

public final class OpenCayenneModelerAction implements ActionListener {

    private final DataObject context;

    public OpenCayenneModelerAction(DataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        // TODO use context
        if (context==null) {
            //open cayenne modeler directly.
        }

        FileObject fo=context.getPrimaryFile();


    }
}
