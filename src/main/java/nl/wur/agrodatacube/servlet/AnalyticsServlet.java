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

import java.text.ParseException;
import java.text.SimpleDateFormat;

//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.wur.agrodatacube.datasource.postgres.PostgresAdapterDataSource;
import nl.wur.agrodatacube.formatter.AdapterTableResultCSVFormatter;
import nl.wur.agrodatacube.formatter.JSONizer;
import nl.wur.agrodatacube.registry.AgroDataCubeRegistry;
import nl.wur.agrodatacube.resource.AdapterQueryResource;
import nl.wur.agrodatacube.resource.query.ConfigurationParameter;
import nl.wur.agrodatacube.token.Registration;

/**
 * Analytics are predefined optimized queries and they have fixed results so no
 * further actions needed.
 *
 * @author rande001
 */
@Path("/analytics")
@Produces({"application/json"})
public class AnalyticsServlet extends Worker {

    @Context
    private HttpServletRequest httpServletRequest;    // needed for logging and dashboard is filled using injection.

    @GET
    @Path("/{analytics_context}/{analytics_topic}")

//  @ApiOperation(value = "Return all the information for the given analytics.")
    public Response ExecuteAnalyticsQueryGet(@Context UriInfo uriInfo, @HeaderParam("token") String token,
            @PathParam("analytics_topic") String topic,
            @PathParam("analytics_context") String context) {
        //
        // Fetch the query from the config table and execute that.
        //
        try {
            Response tokenResponse = checkToken(token);
            if (tokenResponse != null) return tokenResponse;
            Properties params = parametersToProperties(uriInfo, false);
            AnalyticsTask t = new AnalyticsTask();
            t.setRequestURL(uriInfo.getAbsolutePath().toString());
            t.setRemoteIp(getRemoteIP());

            t.setName(context.concat("-").concat(topic));
            t.setRequestURL(requestURL);
            t.setToken(token);

            //
            // specifiek RVO
            //
            AdapterTableResultCSVFormatter formatter = new AdapterTableResultCSVFormatter();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            formatter.setNullValue("NULL");

            //
            // Nu weer generiek
            //
            formatter.setDateFormatter(sdf);
            t.setFormatter(formatter);

            AdapterQueryResource resource = (AdapterQueryResource) AgroDataCubeRegistry.getInstance().findResource(t.getName());
            PostgresAdapterDataSource dataSource = (PostgresAdapterDataSource) resource.getDataSource();
            String queryString = "select (" + resource.getBaseQuery() + "(";
            Iterator<Object> it = params.keySet().iterator();
            int i = 0;

            while (it.hasNext()) {
                String key = (String) it.next();
                WorkerParameter w = (WorkerParameter) params.get(key);

                if (w.isQueryWorkerParameter()) {
                    if (i > 0) {
                        queryString = queryString.concat(",");
                    }

                    i++;
                    queryString = queryString.concat(key).concat(" := ");
                    queryString = queryString.concat(formatValue(w.getValue(), key, resource));
                }
            }

            queryString = queryString.concat(")).* as foo");
            Registration.updateUsageInformation(token, 0., remoteIp, requestURL);
            return Response.status(200)
                    .entity(t.getFormatter().format(dataSource.executeQuery(queryString, new ArrayList<>())))
                    .build();
        } catch (Exception e) {
            return Response.status(500).entity("{\"status\": " + JSONizer.toJson(e.getMessage()) + "}").build();
        }
    }

    /**
     * Format parameter. Gebruik resource voor juiste datatype
     *
     * @param value
     * @return
     */
    private String formatValue(Object value, String key, AdapterQueryResource resource) throws ParseException {
        if (value == null) {
            return " NULL ";
        }

        ConfigurationParameter p = resource.findQueryParameter(key);

        if (p == null) {
            throw new RuntimeException(String.format("parameter %s is not allowed for this resource", key));
        }

        if (p.getDataType().equalsIgnoreCase("string")) {
            return (String) value;
        }

        if (p.getDataType().equalsIgnoreCase("date")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date d = sdf.parse((String) value);

            sdf.applyPattern("dd-MMM-yyyy");

            return "'" + sdf.format(d) + "'";
        }

        return value.toString();
    }

    /**
     * See if a geometry is a 28992 (RD New) or not, if it is not 28992
     * transform the geometry to 28992 and return that.
     *
     * @param epsg
     * @param geom
     * @return
     */
    protected String transformTo28992EWKT(Integer epsg, String geom) {
        if (epsg == null) {
            return "SRID=28992;" + geom;
        }

        if (epsg == 28992) {
            return "SRID=28992;" + geom;
        }

        //
        // Convert the geometry to (RD) 28992 and return the ewkt.
        //
        try {

            // return new String(Executor.executeForImage("select st_asewkt(st_transform(st_geomfromewkt('SRID="+epsg+";"+geom+"'),28992))"));
            throw new RuntimeException(("Worker.transformTo28992EWKT not implemented!"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
