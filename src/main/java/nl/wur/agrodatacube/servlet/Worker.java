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
import nl.wur.agrodatacube.exec.ExecutorTask;
import nl.wur.agrodatacube.formatter.AdapterFormatFactory;
import nl.wur.agrodatacube.formatter.AdapterResultFormatter;
import java.util.ArrayList;
import javax.ws.rs.core.Response;
import nl.wur.agrodatacube.formatter.JSONizer;
import nl.wur.agrodatacube.registry.AgroDataCubeRegistry;
import nl.wur.agrodatacube.resource.AdapterResource;
import nl.wur.agrodatacube.result.AdapterResult;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import nl.wur.agrodatacube.datasource.GeometryProvider;
import nl.wur.agrodatacube.exception.InvalidParameterException;
import nl.wur.agrodatacube.resource.AdapterTableResource;
import nl.wur.agrodatacube.result.AdapterTableResult;

/**
 * This class is the base of all servlets. It verifies tokens, executes queries
 * and returns results.
 *
 * WENR location in RD (28929) POINT(174098.355412451 444323.980737271)
 *
 * 170000 440000,170000 450000, 180000 450000, 180000 440000,170000 440000
 *
 * @author rande001
 */
public class Worker {

    private String resource;
    @Context
    private HttpServletRequest httpServletRequest; // needed for logging and dashboard.
    String remoteIp;

    protected final void setResource(String s) {
        resource = s;
    }

    public String getResource() {
        if (resource != null) {
            return resource;
        }
        return "No resource name available !!!!!!!!!!!!";
    }

    public Worker() {

    }

    /**
     * Convert the supplied query and path parameters to a property list. In
     * some cases parametervalue is LIST<String>.
     *
     * @param uriInfo
     * @return
     */
    protected java.util.Properties parametersToProperties(UriInfo uriInfo) {
        java.util.Properties props = new java.util.Properties();
        Iterator<Map.Entry<String, List<String>>> iterator = uriInfo.getQueryParameters().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> element = iterator.next();
            Object value = element.getValue();
            if (value instanceof java.lang.String) {
                props.put(element.getKey().toLowerCase(), value);  // getValue always a list for query parameters.
            } else if (value instanceof java.util.LinkedList) {
                LinkedList l = (LinkedList) value;
                props.put(element.getKey().toLowerCase(), l.get(0));
            }
        }

        iterator = uriInfo.getPathParameters().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> element = iterator.next();
            Object value = element.getValue();
            if (value instanceof java.lang.String) {
                props.put(element.getKey().toLowerCase(), value);  // getValue always a list for path parameters.
            } else if (value instanceof java.util.ArrayList) {
                ArrayList l = (ArrayList) value;
                props.put(element.getKey().toLowerCase(), l.get(0));
            }
        }

        return props;
    }

    /**
     * Get the ip adres of the client. If an httpServletRequest is available get
     * it from there if not see if one is supplied in a different way.
     *
     * @return
     * @throws Exception
     */
    protected String getRemoteIP() {
        try {
            if (httpServletRequest != null) {
                String ipAddress = httpServletRequest.getHeader("x-forwarded-for");
                if (ipAddress == null) {
                    return httpServletRequest.getRemoteAddr();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
        return remoteIp;
    }

    protected Response getResponse(java.util.Properties props, String token) {
        if (token != null) {
            props.put("token", token);
        }
        return getResponse(props);
    }

    /**
     * Do the work. All parameters needed (included otional token) are in
     * thepropertylist props.
     *
     * @param props
     * @return
     */
    private Response getResponse(java.util.Properties props) {
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
            String geom = props.getProperty("geometry");
            String epsg = props.getProperty("epsg", "28992");
            String isOk = GeometryProvider.validateGeometry(geom, epsg);
            if (!"ok".equalsIgnoreCase(isOk)) {
                AdapterResult result = new AdapterTableResult();
                result.setStatus(isOk);
                result.setHttpStatusCode(422);
                try {
                    return Response.status(422).type(result.getMimeType()).entity(AdapterFormatFactory.getDefaultFormatter(result).format(result)).build();
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
                x.setOrderBy("fieldid"); // not area
            }

            //System.out.println("Execute for ".concat(requestResource));
            //System.out.flush();
            ExecutorTask task = new ExecutorTask(adapterResource);
            AdapterResultFormatter formatter = null;
            if (props.get("output_format") != null) {
                formatter = AdapterResultFormatter.createFormatter((String) props.get("output_format"));
                props.remove("output_format");
            }

            task.addQueryParameters(props);
            task.setRemoteIp(getRemoteIP());
            AdapterResult r = task.execute();

            if (formatter == null) {
                formatter = AdapterFormatFactory.getDefaultFormatter(r);
            }

            if (r.didSucceed()) {
                return Response.status(200).type(r.getMimeType()).entity(formatter.format(r)).build();
            } else {
                return Response.status(r.getHttpStatusCode()).type(r.getMimeType()).entity(formatter.format(r)).build(); // Er is dus iets mis  maar wat ?
            }
        } catch (Exception e) {
            if (e instanceof InvalidParameterException) {
                return Response.status(422).entity("{\"status\": " + JSONizer.toJson(e.getMessage()) + "}").build();
            } else {
                return Response.status(500).entity("{\"status\": " + JSONizer.toJson(e.getMessage()) + "}").build();
            }
        }
    }
}
