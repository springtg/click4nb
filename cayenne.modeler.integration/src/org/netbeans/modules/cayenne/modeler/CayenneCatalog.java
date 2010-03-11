/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cayenne.modeler;

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
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author hantsy
 */
public class CayenneCatalog implements CatalogReader, CatalogDescriptor, EntityResolver {

    private static final String CAYENNE_MODELMAP_XSD = "modelMap.xsd";
    private static final String CAYENNE_MODELMAP_XSD_URI = "http://cayenne.apache.org/schema/3.0/modelMap";
    private static final String CAYENNE_MODELMAP_XSD_LOCAL_URI = "nbres://org/netbeans/modules/cayenne/modeler/resource/modelMap.xsd";
    private static final String CAYENNE_MODELMAP_XSD_ID = "SCHEMA:" + CAYENNE_MODELMAP_XSD_URI;

    ;

    public Iterator getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.add(CAYENNE_MODELMAP_XSD_ID);

        return list.listIterator();
    }

    public void refresh() {
    }

    public String getSystemID(String publicId) {
        if (publicId.equals(CAYENNE_MODELMAP_XSD_ID)) {
            return CAYENNE_MODELMAP_XSD_LOCAL_URI;
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
        return ImageUtilities.loadImage("/org/netbeans/modules/cayenne/resource/cayenne.png");
    }

    public String getDisplayName() {
        return "Cayenne Catalog";
    }

    public String getShortDescription() {
        return "XML Catalog of Apache Cayenne ModelMap Files";
    }

    public void addPropertyChangeListener(PropertyChangeListener arg0) {
    }

    public void removePropertyChangeListener(PropertyChangeListener arg0) {
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (CAYENNE_MODELMAP_XSD_URI.equals(systemId)) {
            return new org.xml.sax.InputSource(CAYENNE_MODELMAP_XSD_LOCAL_URI);
        }
        if (systemId != null && systemId.endsWith(CAYENNE_MODELMAP_XSD)) {
            return new org.xml.sax.InputSource(CAYENNE_MODELMAP_XSD_LOCAL_URI);
        }

        return null;
    }
}
