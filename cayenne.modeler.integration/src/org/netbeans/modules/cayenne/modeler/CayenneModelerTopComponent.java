/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cayenne.modeler;

import java.awt.BorderLayout;
import org.apache.cayenne.modeler.Application;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.ImageUtilities;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Top component which displays something.
 */
public final class CayenneModelerTopComponent extends TopComponent {

    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/cayenne/modeler/resource/cayenne-16.png";
    private static final String PREFERRED_ID = "CayenneModelerTopComponent";

    public CayenneModelerTopComponent(CayenneDomainDataObject cayenneDO) {
        initComponents();
        init(cayenneDO);
        setName(NbBundle.getMessage(CayenneModelerTopComponent.class, "CTL_CayenneModelerTopComponent"));
        setToolTipText(NbBundle.getMessage(CayenneModelerTopComponent.class, "HINT_CayenneModelerTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

    }

    public CayenneModelerTopComponent() {
    }

    private void init(CayenneDomainDataObject cayenneDO) {
        associateLookup(cayenneDO.getLookup());
        Application app = new Application(FileUtil.toFile(cayenneDO.getPrimaryFile()));
        app.startup();
        add(app.getFrame(), BorderLayout.CENTER);
        cayenneDO.editorInitialized(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }


    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}