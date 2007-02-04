/*
 * Created on 23.11.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm;


import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
/**
 * Default implementation of TextResources based on java.util.ResourceBundle
 * 
 * @author Dimitri Polivaev
 * 14.01.2007
 */
public class DefaultTextResources implements TextResources {
    private Properties properties;
    private ResourceBundle resources;
    public DefaultTextResources(ResourceBundle languageResources){
        this(languageResources, null);
    }
    public DefaultTextResources(ResourceBundle languageResources, Properties properties) {
        super();
        this.resources = languageResources;
        this.properties = properties;
    }
    public String getString(String pKey) {
        try{
            return resources.getString(pKey);
        }
        catch(MissingResourceException ex){
            if(properties != null){
                return properties.getProperty(pKey);
            }
            throw ex;
        }
    }
}
