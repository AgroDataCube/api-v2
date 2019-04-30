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
import nl.wur.agrodatacube.datasource.AdapterDataSource;
import java.util.ArrayList;

/**
 *
 * @author rande001
 */
public abstract class AdapterResource {

    protected String name;
    protected AdapterDataSource dataSource;
    protected ArrayList<QueryParameter> possibleQueryParameters;  // parameters that influence the returned result
    protected ArrayList<QueryParameter> possibleResultParameters;  // parameters that influence how result is returned (paging, nrofhits)
    private boolean resourcesNeedsToken;                          
    // todo: child resources or child queries
    private boolean requiresGeometry;

    public AdapterResource(String name) {
        this.name = name;
        possibleQueryParameters = new ArrayList<>();
        requiresGeometry=false;
    }

    public String getName() {
        return name;
    }

    public AdapterDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(AdapterDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Add a possible query parameter. Do not confuse with supplied parameters in url.
     * 
     * @param q 
     */
    public final void addQueryParameter(QueryParameter q) {
        possibleQueryParameters.add(q);
    }

    public QueryParameter findQueryParameter(String s) {
        for (QueryParameter q : possibleQueryParameters) {
            if (q.getName().equalsIgnoreCase(s)) {
                return q;
            }
        }
        return null;
    }

    public boolean needsToken() {
        return resourcesNeedsToken;
    }

    public void setNeedsToken(boolean resourcesNeedsToken) {
        this.resourcesNeedsToken = resourcesNeedsToken;
    }

    /**
     * Return if this resource requires a geometry. Normally false but for WCS it is mandatory.
     *
     * @return
     */
    public boolean requiresGeometry() {
        return requiresGeometry;
    }

    public void setRequiresGeometry(boolean requiresGeometry) {
        this.requiresGeometry = requiresGeometry;
    }
    
    

    /**
     * IS this the resource that contains the fields ?. This is needed by a WCS dat source to get the bounding box.
     * 
     * @return 
     */
    public boolean isFieldsResource() {
        return "fields".equalsIgnoreCase(name);
    }
    
    public ArrayList<QueryParameter> getPossibleQueryParameters() {
        return possibleQueryParameters;
    }
    
}
