/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.click.api.model;

/**
 *
 * @author hantsy
 */
public interface Excludes extends ClickComponent {
    //Attributs
    public static final String PROP_PATTERN = "pattern";

    String getPattern();
    void setPattern(String pattern);
}
