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

//import agrodatacube.wur.nl.result.DateExpression;
//import io.swagger.annotations.Api;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * This servlet returns meteodata, for stations, dates etc. Possible urls are
 * 
 * @author Rande001
 */
@Path("/meteodata")
//@Api(value = "Provide meteodata on station level or on field level. Fields have meteostations assigned to them (based on nearest distance).")
@Produces({"application/json"})
public class MeteoDataServlet extends Worker {

    public MeteoDataServlet() {
        super();
        setResource("meteodata");
    }

    @GET
    @Path("/")
    public Response getMeteoDataInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props,token);
    }
    
    @POST
    @Path("/")
    public Response getMeteoDataInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props,token);
    }
    
    @GET
    @Path("/{stationid}/{date}")
    public Response getMeteoDataInfoStationDateGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);        
        return getResponse(props,token);
    }
    
    @POST
    @Path("/{stationid}/{date}")
    public Response getMeteoDataInfoStationDatePost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);        
        return getResponse(props,token);
    }

    @GET
    @Path("/{stationid}")
    public Response getMeteoDataInfoStationGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);        
        return getResponse(props,token);
    }
    
    @POST
    @Path("/{stationid}")
    public Response getMeteoDataInfoStationPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);        
        return getResponse(props,token);
    }   
}
