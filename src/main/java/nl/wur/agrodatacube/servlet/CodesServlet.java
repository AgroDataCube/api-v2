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

//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author rande001
 */
@Path("/codes")
//@Api(value = "Provide information about codes (cropcodes, soilcodes etc) used in the Agrodatacube. Crops codes are derived and corrected from the originating datasets )")
@Produces({"application/json"})
public class CodesServlet extends Worker {

    public CodesServlet() {
        super();
        setResource("codes");
    }

    @GET
//    @ApiOperation(value = "Provide information for a given cropcode")
    @Path("/cropcodes/{cropcode}")
    public Response getCropcodesCropcode(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("cropcodes");
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }

    @GET
    @Path("/cropcodes")
    public Response getCropcodes(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("cropcodes");
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props,token);
    }

    @GET
    @Path("/soilcodes/{soilcode}")
    public Response getSoilcodesSoilpcode(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("soilcodes");
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }

    @GET
    @Path("/soilcodes")
    public Response getSoilcodes(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("soilcodes");
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props,token);
    }    
    
    @POST
//    @ApiOperation(value = "Provide information for a given cropcode")
    @Path("/cropcodes/{cropcode}")
    public Response getCropcodesCropcodePost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("cropcodes");
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }

    @POST
    @Path("/cropcodes")
    public Response getCropcodesPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("cropcodes");
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props,token);
    }

    @POST
    @Path("/soilcodes/{soilcode}")
    public Response getSoilcodesSoilpcodePost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("soilcodes");
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }

    @POST
    @Path("/soilcodes")
    public Response getSoilcodesPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("soilcodes");
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props,token);
    }        
}
