/*
* Copyright 2020 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.servlet;

import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


/**
 *
 * @author Yke van Randen
 */
@Path("/bis")
@Produces({ "application/json" })
public class PFBServlet extends Worker {

    public PFBServlet() {
        super();
        setResource("pfb");

    }
   
    @GET
    @Path("/pfb")
    public Response getSoilParamsInfoGet(@Context UriInfo uriInfo,@HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }
}
