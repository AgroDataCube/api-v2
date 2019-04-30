/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.properties;

import java.io.File;
import java.io.FileInputStream;

/**
 *
 * @author Rande001
 */
public class AgroDataCubeProperties {

    private static final String PAGE_SIZE_LIMIT = "paging.size_limit";
    private static final String DEFAULT_PAGE_SIZE = "paging.default_size";

    private static final String MAIL_SENDER = "email.sender_address";
    private static final String MAIL_SMTPSERVER = "email.smtpserver";

    private static java.util.Properties props = null;

    /**
     * Retrieve a property from the properties map.
     *
     * @param key name of the key for which we want to retrieve the value
     * @param defaultValue the default value if the key is not present (requires
     * JDK >= 1.7)
     *
     * The file should reside on the tomcat folder (parent of webapps folder).
     *
     * @return
     */
    public static String getValue(String key) {
        return getValue(key, null);
    }
    
    public static synchronized String getValue(String key,
            String defaultValue) {
        
        // todo improve fixed OS independant location
        String[] filenames = new String[4];
        filenames[0] = "/var/lib/tomcat/webapps/agrodatacube_v2.properties";           // Productie WENR
        filenames[1]= "/opt/tomcat/agrodatacube/webapps/agrodatacube_v2.properties";   // Test site WENR
        filenames[2]= "D:/software/tomcat/apache-tomcat-8.0.53 - dev/agrodatacube_v2.properties";   // Development
        filenames[3] = "agrodatacube_v2.properties";                                   // Development WENR

        int i = 0;
        boolean ok = false;
        if (props == null) {
            while (i < filenames.length) {
                File file = new File(filenames[i]);
                try {
                    props = new java.util.Properties();
                    props.load(new FileInputStream(file));
                    System.out.println(String.format("Loaded properties from file %s", file.getAbsoluteFile()));
                    ok = true;
                    break;
                } catch (Exception e) {
                    System.out.println(String.format("Unable to open properties file %s, %s", file.getAbsoluteFile(), e.getMessage()));
                }
                i++;
            }
            if (! ok) {
                throw new RuntimeException(("Unable to open properties file !!!!!!!!"));
            }
        }

        return props.getProperty(key, defaultValue);
    }

    public static Integer getDefaultPageSizeLimit() {
        return Integer.parseInt(getValue(PAGE_SIZE_LIMIT, "50"));
    }

    public static String getEmailSender() {
        return getValue(MAIL_SENDER, "info.agrodatacube@wur.nl");
    }

    public static String getSMTPServer() {
        return getValue(MAIL_SMTPSERVER, "smtprelay.wur.nl");
    }
}
