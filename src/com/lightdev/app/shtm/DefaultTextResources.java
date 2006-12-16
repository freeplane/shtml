/*
 * Created on 23.11.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm;

import java.util.ResourceBundle;

public class DefaultTextResources implements TextResources {
private ResourceBundle resources;
    public DefaultTextResources(ResourceBundle resources) {
        super();
        this.resources = resources;
    }
    public String getString(String pKey) {
        return resources.getString(pKey);
    }

}
