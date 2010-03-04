/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.web.click.api.model;

import java.util.List;

/**
 *
 * @author hantsy
 */
public interface  ServiceComponent extends ClickComponent{
    //Attributs
    public static final String  PROP_CLASSNAME="classname";

    //Elements
    public static final String PROP_PROPERTY="property";
    
    String getClassName();
    void setClassName(String classname);

    List<Property> getPropertyList();
    void addProperty(Property property);
    void removeProperty(Property pro);
}
