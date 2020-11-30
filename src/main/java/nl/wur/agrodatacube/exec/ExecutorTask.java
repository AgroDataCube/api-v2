/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.exec;

import nl.wur.agrodatacube.resource.AdapterResource;
import nl.wur.agrodatacube.result.AdapterResult;
import nl.wur.agrodatacube.result.AdapterTableResult;
import nl.wur.agrodatacube.result.ResultParameter;
import nl.wur.agrodatacube.token.Registration;
import nl.wur.agrodatacube.token.TokenValidationResult;
import nl.wur.agrodatacube.token.TokenValidator;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import nl.wur.agrodatacube.datasource.GeometryProvider;
import nl.wur.agrodatacube.properties.AgroDataCubeProperties;
import nl.wur.agrodatacube.servlet.WorkerParameter;

/**
 * An executor task contains data what to with what. So it contains
 * <li>
 * <ul>the geometry and crs/epsg</ul>
 * <ul>the resource</ul>
 * <ul>the datasource</ul>
 * </li>
 *
 * @author rande001
 */
public class ExecutorTask {

    private final AdapterResource resource;             // resourceclass
    private final java.util.Properties queryParameters; // supplied query parameters.
    private final java.util.Properties resultParameters; // supplied result parameters e.g pae_size, page_offset etc
    private boolean includeChildren = true;
    private String remoteIp; // needed for dashboard
//    private boolean byPassTokens = false; Gebruikt voor resourceexists
//    private boolean ignorePageSizeLimit = false;
    private String requestURL;

    public ExecutorTask(AdapterResource resource) {
        this.resource = resource;
        queryParameters = new java.util.Properties();
        resultParameters = new java.util.Properties();
        resultParameters.put("page_size", ""+AgroDataCubeProperties.getDefaultPageSize());
        resultParameters.put("page_offset", "0");
    }

    public AdapterResult execute() {
        //
        // Extract the optional token and remove from parameter list immediatly so avoid problems in further processing (parameter "token" not allowed etc)
        //

        String token = getQueryParameterValue("token");
        removeQueryParameter("token");

//        if (!byPassTokens) {
            //
            // In some cases needed see worker.resourceExists
            //
            if (resource.needsToken()) {

                // moved part to worker.getResponse(props,token).
                TokenValidationResult tokenAllowsAccess = TokenValidator.tokenAllowsAccess(token, this.getResource().getName());
                if (!tokenAllowsAccess.isOk()) {
                    AdapterResult result = new AdapterTableResult();
                    result.setStatus(String.format("This token does not allow access to the resource \"%s\"", this.getResource().getName()));
                    result.setHttpStatusCode(403);
                    return result;
                }
            }
//        }

        if (resource.requiresGeometry()) {
            if (!this.hasGeometryParameter()) {
                AdapterResult result = new AdapterTableResult();
                result.setStatus("Missing required geometry (supply either a fieldid or a geometry) ! ");
                result.setHttpStatusCode(422);
                return result;
            }
        }

        //
        // If geometry parameter is supplied then do a validation.
        //
        if (getQueryParameterValue("geometry") != null) {
            String geom = getQueryParameterValue("geometry");
            String epsg = getQueryParameterValue("epsg", "28992");
            String isOk = GeometryProvider.validateGeometry(geom, epsg);
            if (!"ok".equalsIgnoreCase(isOk)) {
                AdapterResult result = new AdapterTableResult();
                result.setStatus(isOk);
                result.setHttpStatusCode(422);
                return result;
            }
        }
        //
        // If token is valid and provides access then do it. Also log the area.
        //
        AdapterResult result = resource.getDataSource().execute(this);
        
        // 
        // Log the area, only if we are not in bypasstoken (internal mode) and the resourec needs a token
        //
        
//        if (!this.byPassTokens) { // In some cases neede for internal use 
            if (resource.needsToken()) {
                Integer tokenId= Registration.getTokenid(token);
                Registration.updateUsageInformation(token, result.getArea(), remoteIp, this.requestURL);
            }
//        }
        return result;
    }

    public AdapterResource getResource() {
        return resource;
    }

    /**
     * Since parameters are extracted from the url they always are strings.
     * Later we check if the value can be converted to the right type.
     *
     * @param paramName
     * @param value
     */
    public void setQueryParameter(String paramName, String value) {
        //
        // Properties so duplicate keys are automatically overwritten.
        //
        queryParameters.put(paramName.toLowerCase(), value);
    }

    public Properties getQueryParameters() {
        return queryParameters;
    }

    /**
     * See if a geometry parameter is available. This can be a fieldid or a
     * geometry.
     *
     * @return
     */
    private boolean hasGeometryParameter() {
        /**
         * Try to find the query parameter with name
         *
         * @param paramName
         * @return
         */
        if (ExecutorTask.this.getQueryParameterValue("fieldid") != null) {
            return true;
        }
        if (ExecutorTask.this.getQueryParameterValue("geometry") != null) {
            return true;
        }
        return false;
    }

    /**
     * Return the value for a given query parameter. Value is a WorkerParameter
     * and not an object anymore.
     *
     * @param paramName
     * @return
     */
    public String getQueryParameterValue(String paramName) {
        return getQueryParameterValue(paramName, null);
    }

    public String getResultParameterValue(String paramName) {
        return (String) resultParameters.get(paramName.toLowerCase());
    }

    /**
     * Return the value for a given parameter, if it is not set return a default
     * value.
     *
     * @param paramName
     * @return
     */
    public String getQueryParameterValue(String paramName, String defaultValue) {
        WorkerParameter w = (WorkerParameter) queryParameters.get(paramName.toLowerCase());
        if (w != null) {
            return w.getValue().toString();
        }
        return defaultValue;
    }

    /**
     * Add query parameters. externally they are query parameters, internally
     * they are query of result parameters.
     *
     * @param props
     */
    public void addQueryParameters(java.util.Properties props) {
        Iterator<Map.Entry<Object, Object>> iterator = props.entrySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next().getKey();
            if (ResultParameter.isResultParameter(key)) {
                resultParameters.put(key, ((WorkerParameter) props.get(key)).getValue());
            } else {
                queryParameters.put(key, props.get(key));
            }
        }
    }

    /**
     * Set the value of parameter. Value must be a non null value. if value is
     * null adding add crashes.
     *
     * @param paramName
     * @param value
     */
    public void setParameterValue(String paramName, String value) {
        if (value != null) {
            queryParameters.put(paramName.toLowerCase(), new WorkerParameter(value, WorkerParameter.WorkerParameterType.QUERY));
        } else {
            queryParameters.put(paramName.toLowerCase(), new WorkerParameter("set", WorkerParameter.WorkerParameterType.QUERY)); // Fake value because null values are not allowed.            
        }
    }

    public boolean returnJustnrOfHits() {
        String s = getResultParameterValue("result");
        if (s == null) {
            return false;
        }
        if (s.equalsIgnoreCase("nrofhits")) {
            return true;
        }
        // todo alldata
        return false;
    }

    public boolean returnNoGeom() {
        String s = getResultParameterValue("result");
        if (s == null) {
            return false;
        }
        if (s.equalsIgnoreCase("nogeom")) {
            return true;
        }
        // todo alldata
        return false;
    }

    public void removeQueryParameter(String name) {
        queryParameters.remove(name);
    }

    public void disableChildren() {
        includeChildren = false;
    }

    public boolean childrenIncluded() {
        return includeChildren;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

//    public void setByPassTokens() {
//        byPassTokens = true;
//    }
//
//    public boolean isIgnorePageSizeLimit() {
//        return ignorePageSizeLimit;
//    }
//
//    public void setIgnorePageSizeLimit(boolean ignorePageSizeLimit) {
//        this.ignorePageSizeLimit = ignorePageSizeLimit;
//    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }
    
}
