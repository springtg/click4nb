/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cayenne.modeler;

import org.apache.cayenne.modeler.Main;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 *
 * @author hantsy
 */
public class CayenneModelerLauncher extends Main implements Runnable {

    private FileObject domainFile;

    CayenneModelerLauncher(FileObject fo) {
        this.domainFile = fo;
    }
//    public void launch(FileObject fo) {
//        this.domainFile=fo;
//        RequestProcessor.getDefault().post(this);
//    }

    @Override
    protected void configureLookAndFeel() {
    }

    public void run() {
        runModeler(FileUtil.toFile(domainFile));
    }
}
