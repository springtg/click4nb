/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.click;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.ImageUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author hantsy
 */
public class ClickCatalog implements CatalogReader, CatalogDescriptor, EntityResolver {

    //click dtd 2.2
    public static final String CLICK_2_2_DTD = "click-2.2.dtd";
    public static final String CLICK_2_2_DTD_PUBLIC_ID = "-//Apache Software Foundation//DTD Click Configuration 2.2//EN";
    public static final String CLICK_2_2_DTD_LOCAL_URI = "nbres:/org/netbeans/modules/web/click/resources/click-2.2.dtd";
    // CLICK DTD
    public static final String CLICK_2_1_DTD = "click_2_1.dtd";
    //In 2.2rc, the name of click 2.1 dtd file was refined.
    public static final String CLICK_2_1_DTD_ALTERNATIVE = "click-2.1.dtd";
    public static final String CLICK_2_1_DTD_PUBLIC_ID = "-//Apache Software Foundation//DTD Click Configuration 2.1//EN";
    public static final String CLICK_2_1_DTD_LOCAL_URI = "nbres:/org/netbeans/modules/web/click/resources/click-2.1.dtd";
    // CLICK MENU 2.2 DTD
    public static final String MENU_2_2_DTD = "menu-2.2.dtd";
    public static final String MENU_2_2_DTD_PUBLIC_ID = "-//Apache Software Foundation//DTD Click Menu 2.2//EN";
    public static final String MENU_2_2_DTD_LOCAL_URI = "nbres:/org/netbeans/modules/web/click/resources/menu-2.2.dtd";
    // CLICK MENU DTD
    public static final String MENU_2_1_DTD = "menu.dtd";
    public static final String MENU_2_1_DTD_PUBLIC_ID = "-//Apache Software Foundation//DTD Click Menu 2.1//EN";
    public static final String MENU_2_1_DTD_LOCAL_URI = "nbres:/org/netbeans/modules/web/click/resources/menu.dtd";

    public Iterator getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.add(CLICK_2_1_DTD_PUBLIC_ID);
        list.add(CLICK_2_2_DTD_PUBLIC_ID);
        list.add(MENU_2_1_DTD_PUBLIC_ID);
        list.add(MENU_2_2_DTD_PUBLIC_ID);
        return list.listIterator();
    }

    public void refresh() {
    }

    public String getSystemID(String publicId) {
        if (CLICK_2_1_DTD_PUBLIC_ID.equals(publicId)) {
            return CLICK_2_1_DTD_LOCAL_URI;
        } else if (CLICK_2_2_DTD_PUBLIC_ID.equals(publicId)) {
            return CLICK_2_2_DTD_LOCAL_URI;
        } else if (MENU_2_2_DTD_PUBLIC_ID.equals(publicId)) {
            return MENU_2_2_DTD_LOCAL_URI;
        } else if (MENU_2_1_DTD_PUBLIC_ID.equals(publicId)) {
            return MENU_2_1_DTD_LOCAL_URI;
        }
        return null;
    }

    public String resolveURI(String arg0) {
        return null;
    }

    public String resolvePublic(String arg0) {
        return null;
    }

    public void addCatalogListener(CatalogListener arg0) {
    }

    public void removeCatalogListener(CatalogListener arg0) {
    }

    public Image getIcon(int arg0) {
        return ImageUtilities.loadImage("/org/netbeans/modules/web/click/resources/click-icon.png");
    }

    public String getDisplayName() {
        return "Click Catalog";
    }

    public String getShortDescription() {
        return "XML Catalog of Click Configuration and Menu Component";
    }

    public void addPropertyChangeListener(PropertyChangeListener arg0) {
    }

    public void removePropertyChangeListener(PropertyChangeListener arg0) {
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (CLICK_2_1_DTD_PUBLIC_ID.equals(publicId)) {
            return new InputSource(CLICK_2_1_DTD_LOCAL_URI);
        }
        if (systemId != null && (systemId.endsWith(CLICK_2_1_DTD) || systemId.endsWith(CLICK_2_1_DTD_ALTERNATIVE))) {
            return new org.xml.sax.InputSource(CLICK_2_1_DTD_LOCAL_URI);
        }

        if (CLICK_2_2_DTD_PUBLIC_ID.equals(publicId)) {
            return new InputSource(CLICK_2_2_DTD_LOCAL_URI);
        }

        if (systemId != null && (systemId.endsWith(CLICK_2_2_DTD))) {
            return new InputSource(CLICK_2_2_DTD_LOCAL_URI);
        }

        if (MENU_2_2_DTD_PUBLIC_ID.equals(publicId)) {
            return new InputSource(MENU_2_2_DTD_LOCAL_URI);
        }

        if (systemId != null && systemId.endsWith(MENU_2_2_DTD)) {
            return new org.xml.sax.InputSource(MENU_2_2_DTD_LOCAL_URI);
        }

        if (MENU_2_1_DTD_PUBLIC_ID.equals(publicId)) {
            return new InputSource(MENU_2_1_DTD_LOCAL_URI);
        }

        if (systemId != null && systemId.endsWith(MENU_2_1_DTD)) {
            return new org.xml.sax.InputSource(MENU_2_1_DTD_LOCAL_URI);
        }

        return null;
    }

    public ClickVersion extractClickVersion(Document doc) {
        DocumentType docType = doc.getDoctype();
        ClickVersion clickVersion = ClickVersion.CLICK_2_2;
        if (docType != null) {
            if (CLICK_2_2_DTD_PUBLIC_ID.equals(docType.getPublicId())) {
                clickVersion = ClickVersion.CLICK_2_2;
            } else if (CLICK_2_1_DTD_PUBLIC_ID.equals(docType.getPublicId())) {
                clickVersion = ClickVersion.CLICK_2_1;
            } else {
                clickVersion = ClickVersion.UNKNOWN;
            }
        } else {
            clickVersion = ClickVersion.UNKNOWN;
        }
        return clickVersion;
    }

    public MenuVersion extractMenuVersion(Document doc) {
        DocumentType docType = doc.getDoctype();
        MenuVersion menuVersion = MenuVersion.MENU_2_2;
        if (docType != null) {
            if (MENU_2_2_DTD_PUBLIC_ID.equals(docType.getPublicId())) {
                menuVersion = MenuVersion.MENU_2_2;
            } else {
                menuVersion = MenuVersion.UNKNOWN;
            }
        } else {
            menuVersion = MenuVersion.UNKNOWN;
        }

        return menuVersion;
    }
}
