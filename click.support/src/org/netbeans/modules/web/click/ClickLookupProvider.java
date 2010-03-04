/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.click;

import org.netbeans.api.project.Project;
import org.netbeans.modules.web.click.api.ClickProjectQuery;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author hantsy
 */
@LookupProvider.Registration(projectTypes =
@LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-project", position = 300))
public class ClickLookupProvider implements LookupProvider {

    public Lookup createAdditionalLookup(Lookup baseContext) {
        Project proj = baseContext.getDefault().lookup(Project.class);
        if (proj != null && ClickProjectQuery.isClick(proj)) {
            return Lookups.fixed(new Object[]{new ClickProjectOpenHookImpl(proj)});
        } else {
            return Lookup.EMPTY;
        }
    }

    private static final class ClickProjectOpenHookImpl extends ProjectOpenedHook {

        private static final RequestProcessor PROJ_OPEN_HOOK_RESYNCHRONIZER = new RequestProcessor("Eclipse.Resynchronizer"); // NOI18N
        private static RequestProcessor.Task currentTask;

        public ClickProjectOpenHookImpl() {
        }
        Project project;

        public ClickProjectOpenHookImpl(Project project) {
            this.project = project;
        }

        @Override
        protected void projectOpened() {
            if (currentTask == null) {
                currentTask = PROJ_OPEN_HOOK_RESYNCHRONIZER.create(new Runnable() {

                    public void run() {
                        ClickResourceFinder.initialize(project);
                    }
                });
            }
            // coalesce events from multiple project being opened.
            currentTask.schedule(10000);
        }

        @Override
        protected void projectClosed() {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
