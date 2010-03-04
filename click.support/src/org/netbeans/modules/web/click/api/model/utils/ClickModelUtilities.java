/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.web.click.api.model.utils;


import org.netbeans.modules.web.click.api.model.ClickModel;
import org.netbeans.modules.web.click.api.model.ClickModelFactory;
import org.netbeans.modules.web.click.api.model.MenuModel;
import org.netbeans.modules.web.click.api.model.MenuModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.Parameters;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.filesystems.FileObject;


/**
 *
 * @author hantsy
 */
public class ClickModelUtilities {

    public static ClickModel getClickModel(FileObject fo, boolean editable){
        Parameters.notNull("ClickModel source file object can not be null", fo);
        ModelSource source=Utilities.getModelSource(fo, editable);

        return ClickModelFactory.getInstance().getModel(source);
    }


    public static MenuModel getMenuModel(FileObject fo, boolean editable){
        Parameters.notNull("MenuModel source file object can not be null", fo);
        ModelSource source=Utilities.getModelSource(fo, editable);

        return MenuModelFactory.getInstance().getModel(source);
    }

}
