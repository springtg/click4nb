/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.click.actions;

import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.click.ClickResourceFinder;
import org.netbeans.modules.web.click.api.ClickComponentQuery;
import org.netbeans.modules.web.click.api.ClickFileType;
import org.netbeans.modules.web.click.editor.ClickEditorUtilities;
import org.netbeans.modules.web.common.util.WebModuleUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author hantsy
 */
public class OpenComponentThread implements Runnable {

    Logger log = Logger.getLogger(OpenComponentThread.class.getName());
    FileObject activeFileObject;
    ClickFileType typeToFind;

    public void findAndOpenFile(FileObject file, ClickFileType type) {
        this.activeFileObject = file;
        this.typeToFind = type;
        RequestProcessor.getDefault().post(this);
    }

    @Override
    public void run() {
        FileObject[] targetFO = ClickComponentQuery.findComponent(activeFileObject, typeToFind);

        if (targetFO == null || targetFO.length == 0) {
            String key = NbBundle.getMessage(OpenComponentThread.class, "MSG_FileNotFound");
            NotifyDescriptor.Message d =
                    new NotifyDescriptor.Message(key, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        } else if (targetFO.length == 1) {
            openFile(targetFO[0]);
        } else {
            log.finest("find more than one file... and popup a window to select.");
            log.finest("target file size@" + targetFO.length);

            String classFQN = WebModuleUtilities.getClassFQNByJavaSourceFileObject(activeFileObject);
            Project project = FileOwnerQuery.getOwner(activeFileObject);
            String[] files = ClickResourceFinder.findPathByClass(project, classFQN);
            TemplateSelectionPanel panel = new TemplateSelectionPanel(files);
            DialogDescriptor d = new DialogDescriptor(panel, "Select a tempalte file", true, null);
            FileObject fileToOpen = null;
            String selectedResource = null;
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                selectedResource = files[panel.getSelectionIndex()];
                fileToOpen = WebModuleUtilities.findWebResourceByPath(project, selectedResource);
                if (fileToOpen != null) {
                    openFile(fileToOpen);
                }
            }
        }
    }

    private void openFile(FileObject fo) {
        ClickEditorUtilities.openInEditor(fo);
    }
}
