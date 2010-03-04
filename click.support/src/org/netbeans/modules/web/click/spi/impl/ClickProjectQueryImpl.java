
package org.netbeans.modules.web.click.spi.impl;

import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.click.ClickConfigUtilities;
import org.netbeans.modules.web.click.spi.ClickProjectQueryImplementation;
import org.netbeans.modules.web.common.util.WebModuleUtilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.web.click.spi.ClickProjectQueryImplementation.class)
public class ClickProjectQueryImpl implements ClickProjectQueryImplementation {

    @Override
    public boolean isClick(Project project) {
        WebModule wm = WebModuleUtilities.getWebModule(project);
        return ClickConfigUtilities.isClickProject(wm);
    }
}
