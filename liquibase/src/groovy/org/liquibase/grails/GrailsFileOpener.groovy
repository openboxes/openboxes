package org.liquibase.grails;

import liquibase.FileOpener;
import liquibase.ClassLoaderFileOpener;

import grails.util.GrailsUtil;
import java.io.*;
import java.net.URL;
import java.util.Enumeration;

import org.codehaus.groovy.grails.commons.ApplicationHolder;

class GrailsFileOpener implements FileOpener {
    
    def fileOpener;

    public GrailsFileOpener() {
        if (ApplicationHolder.getApplication().isWarDeployed()) {
            fileOpener = new ClassLoaderFileOpener();
        } else {
            fileOpener = new DevFileOpener();
        }
    }

    InputStream getResourceAsStream(String file) throws IOException {
        fileOpener.getResourceAsStream(file);
    }

    Enumeration<URL> getResources(String packageName) throws IOException {
        fileOpener.getResources(packageName);
    }

    ClassLoader toClassLoader() {
        fileOpener.toClassLoader();
    }

}

class DevFileOpener implements FileOpener {

    InputStream getResourceAsStream(String file) throws IOException {
        if (GrailsUtil.grailsVersion.startsWith('1.0')) {
            return getClass().getClassLoader().getResourceAsStream("grails-app/migrations/"+file);
        } else {
            return new FileInputStream(new File(grails.util.BuildSettingsHolder.settings.baseDir.path + "/grails-app/migrations/" + file));
        }
    }

    Enumeration<URL> getResources(String packageName) throws IOException {
        getClass().classLoader.getResources("grails-app/migrations/"+packageName);
    }

    public ClassLoader toClassLoader() {
        getClass().classLoader
    }

}

