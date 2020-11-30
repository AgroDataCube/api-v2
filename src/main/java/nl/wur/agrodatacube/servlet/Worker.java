/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.servlet;

//import agrodatacube.wur.nl.exec.ExecutorResult;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import nl.wur.agrodatacube.datasource.GeometryProvider;
import nl.wur.agrodatacube.exception.InvalidParameterException;
import nl.wur.agrodatacube.exec.ExecutorTask;
import nl.wur.agrodatacube.formatter.AdapterFormatFactory;
import nl.wur.agrodatacube.formatter.AdapterResultFormatter;
import nl.wur.agrodatacube.formatter.JSONizer;
import nl.wur.agrodatacube.registry.AgroDataCubeRegistry;
import nl.wur.agrodatacube.resource.AdapterResource;
import nl.wur.agrodatacube.resource.AdapterTableResource;
import nl.wur.agrodatacube.result.AdapterResult;
import nl.wur.agrodatacube.result.AdapterTableResult;
import nl.wur.agrodatacube.token.Registration;

/**
 * This class is the base of all servlets. It verifies tokens, executes queries
 * and returns results.
 *
 * @author rande001
 */
public class Worker {

    private String resource;
    private String baseResource;    // most cases same as resource but in some cases we need this because differs from resource.
    @Context
    private HttpServletRequest httpServletRequest;    // needed for logging and dashboard is filled using injection.
    String remoteIp = null;
    String requestURL = null;

    public Worker() {
    }

    /**
     * Convert the WORKER httpServletRequest body parameters to a property list.
     *
     * @return
     */
    protected Properties bodyParamsToProperties() {
        Properties props = new Properties();
        ServletInputStream inputStream = null;
        StringBuilder jsonRequestString = new StringBuilder();
        JSONObject jsonRequest = null;

        //
        // Read the supplied json object as String
        //
        try {
            if (httpServletRequest != null) {
                inputStream = httpServletRequest.getInputStream();
            }

            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonRequestString.append(line);
                }
                inputStream.close();
            }

            if (jsonRequestString.length() != 0) {
                JSONParser jsonParser = new JSONParser();
                jsonRequest = (JSONObject) jsonParser.parse(jsonRequestString.toString());
                if (jsonRequest.isEmpty()) {
                } else {
                    Iterator<String> iterator = jsonRequest.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        Object value = jsonRequest.get(key);

                        // if (value instanceof java.lang.String) {
                        // props.put(key.toLowerCase(), value);
                        // } else
                        if (value instanceof java.util.LinkedList) {
                            LinkedList l = (LinkedList) value;
                            String element = l.get(0).toString();
                            props.put(key.toLowerCase(), new WorkerParameter(element, WorkerParameter.WorkerParameterType.QUERY));
                        } else {
                            props.put(key.toLowerCase(), new WorkerParameter(value.toString(), WorkerParameter.WorkerParameterType.QUERY));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }

        return props;
    }

    protected Properties parametersToProperties(UriInfo uriInfo) {
        try {
            return parametersToProperties(uriInfo, true);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error processing parameters %s", e.getLocalizedMessage()));
        }
    }

    /**
     * Convert the supplied query and path parameters to a property list.In some
     * cases parametervalue is LIST<String>.TODO: Change this to QueryParameters
     * so we can include preferred operator (= form uri params, like for filter
     * params).
     *
     * @param uriInfo
     * @param IncludePathParameters
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    protected Properties parametersToProperties(UriInfo uriInfo, boolean IncludePathParameters) throws UnsupportedEncodingException {
        //
        // equal parameters
        //
        Properties props = new java.util.Properties();
        Iterator<Map.Entry<String, List<String>>> iterator = uriInfo.getQueryParameters().entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> element = iterator.next();
            Object value = element.getValue();

            if (value instanceof java.lang.String) {
                props.put(element.getKey().toLowerCase(), new WorkerParameter(URLDecoder.decode(value.toString(), "UTF-8"), WorkerParameter.WorkerParameterType.QUERY));    // getValue always a list for query parameters.
            } else if (value instanceof java.util.LinkedList) {
                LinkedList l = (LinkedList) value;
                props.put(element.getKey().toLowerCase(), new WorkerParameter(URLDecoder.decode(l.get(0).toString(), "UTF-8"), WorkerParameter.WorkerParameterType.QUERY));
            }
        }

        if (IncludePathParameters) {

            //
            // Like can be used or no change from now
            //
            iterator = uriInfo.getPathParameters().entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, List<String>> element = iterator.next();
                Object value = element.getValue();

                if (value instanceof java.lang.String) {
                    props.put(element.getKey().toLowerCase(), new WorkerParameter(URLDecoder.decode(value.toString(), "UTF-8"), WorkerParameter.WorkerParameterType.URI));    // getValue always a list for path parameters.
                } else if (value instanceof java.util.ArrayList) {
                    ArrayList l = (ArrayList) value;
                    props.put(element.getKey().toLowerCase(), new WorkerParameter(URLDecoder.decode(l.get(0).toString(), "UTF-8"), WorkerParameter.WorkerParameterType.URI));
                }
            }
        }

        return props;
    }

//    /**
//     * Check if this resource exists. we only use PATH parameters.
//     *
//     * @param props
//     * @return
//     */
//    protected boolean resourceExists(Properties props) {
//        if (1 < 2) {
//            if (props.getProperty("resource") != null) {
//                setResource((String) props.getProperty("resource"));
//            }
//            props.remove("resource");
//            return true;
//        }
//
//        //
//        // Create a new property list with the resource and the path parameters.
//        //
//        Properties p = new Properties();
//        String requestResource = props.getProperty("resource");
//
//        if (requestResource == null) {
//            requestResource = getBaseResource();
//            if (requestResource == null) {
//                requestResource = getResource();
//            }
//        }
//
//        Iterator<Map.Entry<Object, Object>> iterator = props.entrySet().iterator();
//
//        while (iterator.hasNext()) {
//            Map.Entry<Object, Object> next = iterator.next();
//            WorkerParameter w = (WorkerParameter) next.getValue();
//
//            if (w.isUriWorkerParameter()) {
//                String key = (String) next.getKey();
//                p.put(key, w);
//            }
//        }
//
//        //
//        // If no PATH params no resource specified so we cannot check whether it exists and therefore it exists.
//        //
//        if (p.isEmpty()) {
//            return true;
//        }
//
//        try {
//            AdapterResource adapterResource = AgroDataCubeRegistry.getInstance().findResource(requestResource);
//
//            if (adapterResource == null) {
//                return false;
//            }
//
//            //
//            // See if a resource with the supplied id exists. This is to be able to generate a http 404 error when no dat found.
//            // This can cause issues in e.g. /fields/1/soilindex if soilindex needs not token, so no token is upplied.
//            //
//            // So we allow bypass of token validation here
//            //
//            p.remove("resource");
//
//            ExecutorTask task = new ExecutorTask(adapterResource);
//
//            task.setByPassTokens();
//            p.put("result", new WorkerParameter("nogeom", WorkerParameter.WorkerParameterType.QUERY));
//            task.addQueryParameters(p);
//            task.setRemoteIp(null);    // avoid logging.
//
//            AdapterTableResult r = (AdapterTableResult) task.execute();
//
//            // something can have gone wrong and it now returns a 404 due to to no results.
//            return r.getRowCount() > 0;
//        } catch (Exception e) {
//            if (e instanceof InvalidParameterException) {
//                throw new InvalidParameterException(e.getLocalizedMessage());
//            } else {
//                throw new InternalError(e);
//            }
//        }
//    }
    protected String getBaseResource() {
        return baseResource;
    }

    public void setBaseResource(String baseResource) {
        this.baseResource = baseResource;
    }

    /**
     * Get the ip address of the client. If an httpServletRequest is available
     * get it from there if not see if one is supplied in a different way.
     *
     * @return
     * @throws Exception
     */
    protected String getRemoteIP() {
        if (remoteIp != null) {
            return remoteIp;
        }
        try {
            if (httpServletRequest != null) {
                setRequestURL(httpServletRequest.getRequestURI());
                remoteIp = httpServletRequest.getHeader("x-forwarded-for");
                if (remoteIp == null) {
                    return httpServletRequest.getRemoteAddr();
                } else {
                    return remoteIp;
                }
            }
            throw new Exception("No HTTPServletRequest in Worker.getRemoteIp !!!!!!!!!!!");
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public String getResource() {
        if (resource != null) {
            return resource;
        }

        return "No resource name available !!!!!!!!!!!!";
    }

    protected final void setResource(String s) {
        if (s != null) {
            resource = s;
        }
    }

    /**
     * Do the work. All parameters needed (included optional token) are in the
     * property list props.
     *
     * @param props
     * @return
     */
    private Response getResponse(Properties props) {

        //
        // Either use specific resource in URI or use global resource for class.
        //
        String requestResource = props.getProperty("resource");

        if (requestResource == null) {
            requestResource = getResource();
        }

        //
        // If geometry parameter is supplied then do a validation.
        //
        if (props.get("geometry") != null) {
            WorkerParameter wGeom = (WorkerParameter) props.get("geometry");
            String geom = null;

            if (wGeom != null) {
                geom = (String) wGeom.getValue();
            }

            WorkerParameter wepsg = (WorkerParameter) props.get("epsg");
            String epsg = null;

            if (wepsg != null) {
                epsg = (String) wepsg.getValue();
            }

            if (epsg == null) {
                epsg = "28992";
            }

            String isOk = GeometryProvider.validateGeometry(geom, epsg);

            if (!"ok".equalsIgnoreCase(isOk)) {
                AdapterResult result = new AdapterTableResult();

                result.setStatus(isOk);
                result.setHttpStatusCode(422);

                try {
                    return Response.status(422)
                            .type(result.getMimeType())
                            .entity(AdapterFormatFactory.getDefaultFormatter(result).format(result))
                            .build();
                } catch (Exception e) {
                    ;
                }
            }
        }

        try {
            AdapterResource adapterResource = AgroDataCubeRegistry.getInstance().findResource(requestResource);

            if (adapterResource == null) {
                return Response.status(404).build();
            }

            //
            // Workaround.
            //
            if (requestResource.equalsIgnoreCase("fields")) {
                if (props.isEmpty()) {
                    adapterResource.setRequiresGeometry(false);
                }
                AdapterTableResource x = (AdapterTableResource) adapterResource;
                x.setOrderBy("fieldid");    // not area
            }

            // System.out.println("Execute for ".concat(requestResource));
            // System.out.flush();
            ExecutorTask task = new ExecutorTask(adapterResource);

//            if (props.containsKey("_ignore_page_size_limit")) {
//                task.setIgnorePageSizeLimit(true);
//                props.remove("_ignore_page_size_limit");
//            }
            AdapterResultFormatter formatter = null;

            if (props.get("output_format") != null) {
                String outputFormat = (String) ((WorkerParameter) props.get("output_format")).getValue();

                formatter = AdapterResultFormatter.createFormatter(outputFormat);
                props.remove("output_format");
            }

            task.addQueryParameters(props);
            task.setRemoteIp(getRemoteIP());
            task.setRequestURL(getRequestURL());

            AdapterResult r = task.execute();

            if (formatter == null) {
                formatter = AdapterFormatFactory.getDefaultFormatter(r);
            }

            if (r.didSucceed()) {
                return Response.status(200).type(r.getMimeType()).entity(formatter.format(r)).build();
            } else {
                return Response.status(r.getHttpStatusCode()).type(r.getMimeType()).entity(formatter.format(r)).build();    // Er is dus iets mis  maar wat ?
            }
        } catch (Exception e) {
            if (e instanceof InvalidParameterException) {
                return Response.status(400).entity("{\"status\": " + JSONizer.toJson(e.getMessage()) + "}").build();
            } else {
                return Response.status(500).entity("{\"status\": " + JSONizer.toJson(e.getMessage()) + "}").build();
            }
        }
    }

    /**
     * see if the token is known, not exceeding limits etc. If ok return null
     * @param token
     * @return 
     */
    protected Response checkToken(String token) {
        if (token != null) {
            if (!Registration.tokenIsKnown(token)) {
                return Response.status(403)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(
                                "Unknown token. Please request a new token at https://agrodatacube.wur.nl/api/register.jsp or contact us at info.agrodatacube@wur.nl")
                        .build();
            }

            if (Registration.tokenExceedsLimits(token)) {
                return Response.status(403)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(
                                "Token exceeded limits. Please request a new token at https://agrodatacube.wur.nl/api/register.jsp or contact us at info.agrodatacube@wur.nl")
                        .build();
            }

            if (Registration.tokenIsExpired(token)) {
                return Response.status(403)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(
                                "Token is expired. Please request a new token at https://agrodatacube.wur.nl/api/register.jsp or contact us at info.agrodatacube@wur.nl")
                        .build();
            }

        }
        return null;        
    }

    /**
     * Add the optional token, if present, to the properties and process the
     * request.
     *
     * @param props
     * @param token
     * @return
     */
    protected Response getResponse(Properties props, String token) {

        //
        // If PATH parameters are present we need to validate if the request resource exists, if not return a HTTP 404.
        // For this wee need the base_resource from the url.
        //
        try {
            Response tokenResponse =  checkToken(token);
            if (tokenResponse != null) return tokenResponse;
            //
            // Now see if resource exists.
            //
//            if (!resourceExists(props)) {
//                return Response.status(404).build();
//            }
            //
            // See if this token has access to this resource.
            //
        } catch (Exception e) {
            if (e instanceof InvalidParameterException) {
                return Response.status(422).entity("{\"status\": " + JSONizer.toJson(e.getMessage()) + "}").build();
            } else {
                return Response.status(500).entity("{\"status\": " + JSONizer.toJson(e.getMessage()) + "}").build();
            }
        }

        if (token != null) {
            props.put("token", new WorkerParameter(token, WorkerParameter.WorkerParameterType.AUTHENTICATION));
        }

        return getResponse(props);
    }

    /**
     *
     * @param referer
     * @return
     */
    protected String getTokenForReferer(String referer) {
        if (referer == null) {
            return null;
        }
        return Registration.getTokenForReferer(referer);
    }

    public String getRequestURL() {
        return requestURL;
    }

    /**
     * Set request url, remove numeric path values for e.g. field etc. So the urls will be more comparable. This probably does not work for /soilcodes/code/Zn21.
     * 
     * @param requestURL 
     */
    public void setRequestURL(String requestURL) {
        
        if (requestURL==null) return; // Should not be possible.
        
        String[] r = requestURL.split("/");
        
        String cleanedURL = "";
        for (String s: r) {
            try { 
                Integer.parseInt(s);
                s = "..";
            }
            catch (Exception ignore) {
                ; // 
            }
            if (s != null) {
                cleanedURL=cleanedURL.concat("/").concat(s);
            }
        }
        this.requestURL = cleanedURL;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
