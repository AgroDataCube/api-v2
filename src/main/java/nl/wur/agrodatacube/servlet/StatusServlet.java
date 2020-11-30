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

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import nl.wur.agrodatacube.formatter.JSONizer;
import nl.wur.agrodatacube.token.Registration;

@Path("/status")

//@Api(value = "Provide information about the token")
@Produces({ "application/json" })
public class StatusServlet extends Worker {
    public StatusServlet() {
        super();
        setResource("/status");
    }

    /**
     * Do the work.
     *
     * @param token
     * @return
     */
    private Response getTokenInfo(String token) {
        if (token == null) {
            return Response.status(407)
                           .type(MediaType.APPLICATION_JSON)
                           .entity("{ \"status\": \"No token supplied\"}")
                           .build();
        }

        if (!Registration.tokenIsKnown(token)) {
            return Response.status(403)
                           .entity(
                           "Unknown token. Please request a new token at https://agrodatacube.wur.nl/api/register.jsp or contact us at info.agrodatacube@wur.nl")
                           .build();
        }

        java.util.Properties props = Registration.getTokenInfo(token);

        return Response.ok().type(MediaType.APPLICATION_JSON).entity(JSONizer.toJson(props)).build();
    }

    /**
     * Handle GET requests
     *
     * @param token
     * @return
     */
    @GET
    @Path("")

//  @ApiOperation(value = "Provide information about the token (usae, requests left etc)")
    public Response getTokenInfoGet(@HeaderParam("token") String token) {
        return getTokenInfo(token);
    }

    /**
     * Handle post requests.
     *
     * @param token
     * @return
     */
    @POST
    @Path("")

//  @ApiOperation(value = "Provide information about the token (usae, requests left etc)")
    public Response getTokenInfoPost(@HeaderParam("token") String token) {
        return getTokenInfo(token);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
