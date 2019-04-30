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

import nl.wur.agrodatacube.datasource.postgres.PostgresAdapterDataSource;
import nl.wur.agrodatacube.datasource.postgres.PostgresQuery;
import nl.wur.agrodatacube.formatter.JSONizer;
import nl.wur.agrodatacube.registry.AgroDataCubeRegistry;
import nl.wur.agrodatacube.resource.AdapterQueryResource;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Analytics are predefined optimized queries and they have fixed results so no
 * further actions needed.
 *
 * @author rande001
 */
@Path("/analytics")
@Produces({"application/json"})

public class AnalyticsServlet extends Worker {

//    @POST
//    @Path("/{analytics_context}/{analytics_topic}")
//    @ApiOperation(value = "Return all the information for the given analytics.")
//    public Response ExecuteAnalyticsQueryPost(
//            @ApiParam(value = "A token allows accesss to resources. ", required = false) @HeaderParam("token") String token,
//            @ApiParam(value = "Analytics topic", required = true) @PathParam("analytics_topic") String topic,
//            @ApiParam(value = "Analytics context (e.g. a specific project)", required = true) @PathParam("analytics_context") String context,
//            @ApiParam(value = "Id of the field (perceel)", required = false) @QueryParam("field") Integer field,
//            @ApiParam(value = "A valid WKT geometry (point or polygon). Default in epsg 28992 when not please supply epsg", required = true) @QueryParam("geometry") String geom,
//            @ApiParam(value = "The epsg for the supplied geomery. Default 28992 (RD). Currently 4326 (WGS84) and 28992 (RD) are supported", required = false) @QueryParam("epsg") Integer epsg
//    ) {
//        reset();
//
//        //
//        // Fetch the query from the config table and execute that.
//        //
//        AnalyticsTask t = new AnalyticsTask();
//        t.setSuppliedGeometry(geom);
//        t.setSuppliedEpsg(epsg);
//        t.setName(context.concat("-").concat(topic));
//        t.setEwktGeometry28992(transformTo28992EWKT(epsg, geom));
//        t.setPerceelid(field);
//        t.setToken(token);
//        return executeAnalyticsTask(t);
//    }

    @GET
    @Path("/{analytics_context}/{analytics_topic}")
//    @ApiOperation(value = "Return all the information for the given analytics.")
    public Response ExecuteAnalyticsQueryGet(
            @HeaderParam("token") String token,
            @PathParam("analytics_topic")String topic,
            @PathParam("analytics_context") String context,
            Integer field,
            @QueryParam("geometry") String geom,
            @QueryParam("epsg") Integer epsg
    ) {

        //
        // Fetch the query from the config table and execute that.
        //
        
        try {
        AnalyticsTask t = new AnalyticsTask();
        t.setSuppliedGeometry(geom);
        t.setSuppliedEpsg(epsg);
        t.setName(context.concat("-").concat(topic));
        t.setEwktGeometry28992(transformTo28992EWKT(epsg, geom));
        t.setPerceelid(field);
        t.setToken(token);

        // todo : other parameters
        
        AdapterQueryResource resource = (AdapterQueryResource) AgroDataCubeRegistry.getInstance().findResource(t.getName());
        PostgresAdapterDataSource dataSource = (PostgresAdapterDataSource) resource.getDataSource();
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(t.getEwktGeometry28992());
        
        PostgresQuery postgresQuery = new PostgresQuery(resource.getBaseQuery(), params);
        return Response.status(200).entity(t.getFormatter().format(dataSource.executeQuery(postgresQuery))).build();
        }
        catch (Exception e) {
            return Response.status(500).entity("{\"status\": " + JSONizer.toJson(e.getMessage()) + "}").build();
        }        
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
            //return new String(Executor.executeForImage("select st_asewkt(st_transform(st_geomfromewkt('SRID="+epsg+";"+geom+"'),28992))"));       
            throw new RuntimeException(("Worker.transformTo28992EWKT not implemented!"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
