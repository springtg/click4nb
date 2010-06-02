/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cayenne.modeler;

import java.io.IOException;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.text.DataEditorSupport;

public class CayenneDomainDataObject extends MultiDataObject {

    public CayenneDomainDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
       // cookies.add(new Opener());
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    public void editorInitialized(CayenneModelerTopComponent ed) {
        Opener op = getLookup().lookup(Opener.class);
        op.editor = ed;
    }

    class Opener implements OpenCookie {

        public CayenneModelerTopComponent editor;

        @Override
        public void open() {
            if (editor == null) {
                editor = new CayenneModelerTopComponent(CayenneDomainDataObject.this);
            }
            editor.open();
            editor.requestActive();
        }
    }
}
