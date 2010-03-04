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
public interface  Page  extends ClickComponent{
    //Attributes
    public static final String PROP_PATH="path";
    public static final String PROP_CLASSNAME="classname";

    //Elements
    public static final String PROP_HEADER="header";

    String getPath();
    void setPath(String path);

    String getClassName();
    void setClassName(String cls);

    List<Header> getHeaderList();
    void addHeader(Header header);
    void removeHeader(Header header);
}
