/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.click;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author hantsy
 */
public class ClickFrameworkProvider extends WebFrameworkProvider {

    public ClickFrameworkProvider() {
        super(NbBundle.getMessage(ClickFrameworkProvider.class, "CLICK_NAME"),
                NbBundle.getMessage(ClickFrameworkProvider.class, "CLICK_DESCRIPTION"));
    }

    @Override
    public boolean isInWebModule(WebModule wm) {
        return ClickConfigUtilities.isClickProject(wm);
    }

    @Override
    public File[] getConfigurationFiles(WebModule wm) {
       Set<File> foSet= new HashSet<File>();
        FileObject clickApp= wm.getWebInf().getFileObject(ClickConstants.DEFAULT_CLICK_APP_CONFIG_FILE);
        if(clickApp!=null){
            foSet.add(FileUtil.toFile(clickApp));
        }

        FileObject menuFO= wm.getWebInf().getFileObject(ClickConstants.DEFAULT_MENU_CONFIG_FILE);
        if(menuFO!=null){
            foSet.add(FileUtil.toFile(menuFO));
        }

        return foSet.toArray(new File[foSet.size()]);
    }

    @Override
    public WebModuleExtender createWebModuleExtender(WebModule wm, ExtenderController controller) {
        boolean defaultValue = (wm == null || !isInWebModule(wm));
        return new ClickWebModuleExtender(wm, controller, defaultValue);
    }
}
