/*
 * Copyright 2019 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.datasource;

import nl.wur.agrodatacube.datasource.postgres.PostgresAdapterDataSource;
import nl.wur.agrodatacube.datasource.wcs.WCSAdapterDataSource;
import com.google.gson.JsonObject;
import nl.wur.agrodatacube.datasource.oracle.OracleAdapterDataSource;
import nl.wur.agrodatacube.datasource.wcs.WCSAdapterDataSourceGroenmonitor;

/**
 * Create the data sources.
 *
 * @author rande001
 */
public class DatasourceFactory {

    private DatasourceFactory() {
    }

    /**
     * Create a data source based on the supplied properties (including type).
     * Name and type are required for each datasource. The remaining attributes
     * depend on the type.
     *
     * @param json
     * @return
     */
    public static AdapterDataSource create(JsonObject json) {
        try {
        String type = json.get("type").getAsString();
        if (type.equalsIgnoreCase("postgres")) {
            PostgresAdapterDataSource pgsource = new PostgresAdapterDataSource();
            pgsource.setName(json.get("name").getAsString());
            if (json.get("file") != null) {
                pgsource.addDataFromFile(json.get("file").getAsString());
            } else {
                pgsource.setUsername(json.get("username").getAsString());
                pgsource.setPassword(json.get("password").getAsString());
                pgsource.setHost(json.get("host").getAsString());
                pgsource.setPort(json.get("port").getAsInt());
                pgsource.setDatabase(json.get("database").getAsString());
                pgsource.setMaxConnections(json.get("maxconnections").getAsInt());
                pgsource.setContainsRegistrationData(json.get("containsregistrationdata").getAsString());
            }
            return pgsource;
        }
        if (type.equalsIgnoreCase("oracle")) {
            OracleAdapterDataSource pgsource = new OracleAdapterDataSource();
            pgsource.setName(json.get("name").getAsString());
            if (json.get("file") != null) {
                pgsource.addDataFromFile(json.get("file").getAsString());
            } else {
                // todo name erbij voor json
                pgsource.setUsername(json.get("username").getAsString());
                pgsource.setPassword(json.get("password").getAsString());
                pgsource.setHost(json.get("host").getAsString());
                pgsource.setPort(json.get("port").getAsInt());
                pgsource.setDatabase(json.get("database").getAsString());
                pgsource.setMaxConnections(json.get("maxconnections").getAsInt());
                pgsource.setContainsRegistrationData(json.get("containsregistrationdata").getAsString());
            }
            return pgsource;
        }
        if (type.equalsIgnoreCase("wcs")) {
            // todo version
            WCSAdapterDataSource source = new WCSAdapterDataSource();
            source.setName(json.get("name").getAsString());
            source.setBaseUrl(json.get("url").getAsString());
            source.setVersion(json.get("url").getAsString());
            if (json.get("file") != null) {
                System.out.println("WCS Datasources do not support the property type !!!!!!!!!!!!!!");
            }
            return source;
        }
        if (type.equalsIgnoreCase("wcs-gm")) {
            // todo version
            WCSAdapterDataSource source = new WCSAdapterDataSourceGroenmonitor();
            source.setName(json.get("name").getAsString());
            source.setBaseUrl(json.get("url").getAsString());
            source.setVersion(json.get("url").getAsString());
            if (json.get("file") != null) {
                System.out.println("WCS Datasources do not support the property type !!!!!!!!!!!!!!");
            }
            return source;
        }
        throw new RuntimeException(String.format("Unknown data source type \"%s\" !", type));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to get datasources %s",e.getLocalizedMessage()));
        }
    }
}
