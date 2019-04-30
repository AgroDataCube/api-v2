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
 * @author Rande001
 */
@Path("/soiltypes")
//@Api(value = "Provide information about soils")
@Produces({"application/json"})
public class SoilServlet extends Worker {

    public SoilServlet() {
        super();
        setResource("soiltypes");
    }

    @GET
    @Path("/")
    public Response getSoilInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
        //return Response.status(200).entity("{\"status\": \"/fields? not yet implemented \"}").build();
    }
    
    @POST
    @Path("/")
    public Response getSoilInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);
        props.put("resourcename", "fields");
        return getResponse(props, token);
        //return Response.status(200).entity("{\"status\": \"/fields? not yet implemented \"}").build();
    }
}
