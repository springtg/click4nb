/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.web.click;

/**
 *
 * @author hantsy
 */
public enum  MenuVersion {
    UNKNOWN("2.1"), MENU_2_2("2.2");

    private String version;
    private MenuVersion(String version) {
        this.version=version;
    }

    public String getVersion(){
        return this.version;
    }
}
