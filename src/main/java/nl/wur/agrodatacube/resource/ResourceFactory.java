/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.resource;

import nl.wur.agrodatacube.resource.query.QueryParameter;
import nl.wur.agrodatacube.registry.AgroDataCubeRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.wur.agrodatacube.exception.InvalidParameterException;

/**
 *
 * @author rande001
 */
public class ResourceFactory {

    /**
     * Prevent use of default public constructor.
     */
    private ResourceFactory() {
    }

    public static AdapterResource create(JsonObject json) {
        //
        // Base on type and create. 
        //
        String type = json.get("type").getAsString();
        AdapterResource s = null;
        if (type != null) {
            try {
                if (type.equalsIgnoreCase("image")) {
                    AdapterImageResource imageResource = new AdapterImageResource(json.get("name").getAsString());
                    imageResource.setCoverageName(json.get("coverageName").getAsString());
                    if (json.get("output_format") != null) {
                        imageResource.setOutputFormat(json.get("output_format").getAsString());
                    }
                    s = imageResource;
                } else if (type.equalsIgnoreCase("query")) {
                    s = new AdapterQueryResource(json.get("name").getAsString());
                    ((AdapterQueryResource) s).setQuery(json.get("query").getAsString());
                    if (json.get("orderBy") != null) {
                        ((AdapterQueryResource) s).setOrderBy(json.get("orderBy").getAsString());
                    }
                    if (json.get("geometryColumn") != null) {
                        ((AdapterQueryResource) s).setGeometryColumn(json.get("geometryColumn").getAsString());
                    }
                } else if (type.equalsIgnoreCase("table")) {
                    s = new AdapterTableResource(json.get("name").getAsString());
                    ((AdapterTableResource) s).setTableName(json.get("table").getAsString());
                    if (json.get("geometryColumn") != null) {
                        ((AdapterTableResource) s).setGeometryColumn(json.get("geometryColumn").getAsString());
                    }
                    if (json.get("rasterColumn") != null) {
                        ((AdapterTableResource) s).setRasterColumn(json.get("rasterColumn").getAsString());
                    }
                    if (json.get("linkColumns") != null) {
                        ((AdapterTableResource) s).setLinkColumns(json.get("linkColumns").getAsString());
                    }
                    if (json.get("orderBy") != null) {
                        ((AdapterTableResource) s).setOrderBy(json.get("orderBy").getAsString());
                    }
                    ((AdapterTableResource) s).setColumns(json.get("columns").getAsString());
                    if (json.get("parent") != null) {
                        ((AdapterTableResource) s).setParentName(json.get("parent").getAsString());
                    }
                } else {
                    System.out.println(String.format("Unable to build resource for %s", json.toString()));
                }

                if (s != null) {
                    s.setNeedsToken(json.get("needsToken").getAsBoolean());
                    addPossibleQueryParameters(s, json.get("queryParameters"));
                    if (json.get("requiresGeometry") != null) {
                        s.setRequiresGeometry(json.get("requiresGeometry").getAsBoolean());
                    }
                    s.setDataSource(AgroDataCubeRegistry.getInstance().findDatasource(json.get("datasource").getAsString()));
                    return s;
                }
            } catch (Exception e) {
                throw new RuntimeException(String.format("Exception %s in ResourceFactory , resource %s", e.getMessage(), s.name));
            }
        } else {
            throw new InvalidParameterException(String.format("Resource with invalid type \"%s\"", type));
        }
        throw new InvalidParameterException(String.format("Unknown resource type \"%s\" !!", type));
    }

    /**
     * Add query parameters. TODO: Sometimes resources which are referred to are
     * not yet loaded.
     *
     * @param r
     * @param get
     */
    private static void addPossibleQueryParameters(AdapterResource r, JsonElement get) {
        if (get == null) {
            return;
        }

        JsonArray list = (JsonArray) get;
        for (int i = 0; i < list.size(); i++) {
            QueryParameter q = new QueryParameter();
            JsonObject jq = (JsonObject) list.get(i);
            if (jq.get("columnName") != null) {
                q.setColumnName(jq.get("columnName").getAsString());
            }
            q.setType(jq.get("type").getAsString());
            q.setName(jq.get("name").getAsString());
            if (jq.get("resource") != null) {
                q.setResourceName(jq.get("resource").getAsString());
            }
            r.addQueryParameter(q);
            if (jq.get("joinColumn") != null) {
                q.setJoinColumn(jq.get("joinColumn").getAsString());
            }

            //
            // a query parameter can be stored in a different table and we there fore can add some extra fields.
            //
//            if (jq.get("extraColumns") != null) {
//                ((AdapterTableResource) r).setExtraColumns(jq.get("extraColumns").getAsString());
//            }

        }
    }
}
