/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.datasource.postgres;

import nl.wur.agrodatacube.resource.AdapterResource;
import java.util.ArrayList;

/**
 *
 * @author rande001
 */
public class PostgresQuery {

    private String query;
    private ArrayList<Object> paramValues = new ArrayList<>();
    private ArrayList<PostgresQuery> children = new ArrayList<>();
    private AdapterResource resource;

    public PostgresQuery(String query, ArrayList<Object> paramValues) {
        this.query = query;
        this.paramValues = paramValues;        
    }

    
    public PostgresQuery(AdapterResource resource) {
        this.resource=resource;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ArrayList<Object> getParamValues() {
        return paramValues;
    }

    public void addParamValues(Object paramValue) {
        this.paramValues.add(paramValue);
    }

    public void addPostgresQuery(PostgresQuery child) {
        children.add(child);
    }

    public ArrayList<PostgresQuery> getChildren() {
        return children;
    }

    public String getName() {
        if (resource != null) {
            return resource.getName();
        }
        throw new RuntimeException("No resource in PostgresQuery !!!");
    }

    public AdapterResource getResource() {
        return resource;
    }

    void clearParameterValues() {
        paramValues.clear();
    }

    public void removeChildren() {
        children.clear();
    }

}
