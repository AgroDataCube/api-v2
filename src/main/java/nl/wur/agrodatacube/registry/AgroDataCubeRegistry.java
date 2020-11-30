/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.registry;

import nl.wur.agrodatacube.datasource.AdapterDataSource;
import nl.wur.agrodatacube.datasource.DatasourceFactory;
import nl.wur.agrodatacube.datasource.postgres.PostgresAdapterDataSource;
import nl.wur.agrodatacube.resource.AdapterPostgresResource;
import nl.wur.agrodatacube.resource.AdapterResource;
import nl.wur.agrodatacube.resource.ResourceFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.ArrayList;

/**
 *
 * @author rande001
 */
public class AgroDataCubeRegistry {

    private static AgroDataCubeRegistry instance;

    private final ArrayList<AdapterResource> resources;
    private final ArrayList<AdapterDataSource> dataSources;
    private final java.util.Properties systemProperties;

    private AgroDataCubeRegistry() {
        resources = new ArrayList<>();
        dataSources = new ArrayList<>();
        systemProperties = new java.util.Properties();
    }

    public static AgroDataCubeRegistry getInstance() {
        try {
            if (instance == null) {
                instance = new AgroDataCubeRegistry();
                instance.loadFromFile();
            }
        } catch (Exception e) {
            System.out.println(new RuntimeException(e));
            instance = new AgroDataCubeRegistry(); // avoid NULL pointers 
        }
        return instance;
    }

    public AdapterResource findResource(String name) {
        for (int i = 0; i < resources.size(); i++) {
            if (name.equalsIgnoreCase(resources.get(i).getName())) {
                return resources.get(i);
            }
        }
        return null;
    }

    public AdapterDataSource findDatasource(String name) {
        for (int i = 0; i < dataSources.size(); i++) {
            if (name.equalsIgnoreCase(dataSources.get(i).getName())) {
                return dataSources.get(i);
            }
        }
        return null;
    }

    /**
     * return the datasource that contains the registration.
     *
     * @return
     */
    public PostgresAdapterDataSource getRegistrationDataSource() {
        for (int i = 0; i < dataSources.size(); i++) {
            if (dataSources.get(i).containsRegistrationData()) {
                return (PostgresAdapterDataSource) dataSources.get(i);
            }
        }
        throw new RuntimeException("No registrationdatasource found !!!");
    }

    private void loadFromFile() throws FileNotFoundException {
        try {
            JsonParser parser = new JsonParser();
            ClassLoader classLoader = getClass().getClassLoader();
            JsonObject registryJson = (JsonObject) parser.parse(new FileReader(classLoader.getResource("config.json").getFile()));

            JsonArray jsonDatasources = (JsonArray) registryJson.get("datasources");
            for (JsonElement o : jsonDatasources) {
                AdapterDataSource r = DatasourceFactory.create((JsonObject) o);
                dataSources.add(r);
                System.out.println(String.format("Added datasource %s, type = %s", r.getName(), r.getClass().getName()));
                System.out.flush();
            }

            JsonArray jsonRresources = (JsonArray) registryJson.get("resources");
            for (JsonElement o : jsonRresources) {
                AdapterResource r = ResourceFactory.create((JsonObject) o);
                resources.add(r);
                System.out.println(String.format("Added resource %s, datasource %s", r.getName(), r.getDataSource().getName()));
            }

            //
            // The system properties like defult page size, smtp info etc.
            //
            //
            // Now link parent and child resources
            //
            for (int i = 0; i < resources.size(); i++) {
                if (resources.get(i) instanceof AdapterPostgresResource) {
                    AdapterPostgresResource r = (AdapterPostgresResource) resources.get(i);
                    if (r.getParentName() != null) {
                        AdapterPostgresResource p = (AdapterPostgresResource) findResource(r.getParentName());
                        if (p == null) {
                            System.out.println(String.format("Unable to add non existing resource %s as parent to %s", r.getParentName(), r.getName()));
                        } else {
                            p.addChildren(r);
                            System.out.println(String.format("Added resource %s as child to %s", r.getName(), p.getName()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String getEmailSender() {
        return systemProperties.getProperty("emailsender", "info.agrodatacube@wur.nl");
    }

    public String getSMTPServer() {
        return systemProperties.getProperty("smtpserver", "smtprelay.wur.nl");
    }

    public int getPageSizeLimit() {
        String s = systemProperties.getProperty("page_size_limit", "50");
        return Integer.parseInt(s);
    }

}
