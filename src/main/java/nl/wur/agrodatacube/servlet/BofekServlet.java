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
 * This servlet returns information for the soil physical units.
 *
 * @author rande001
 */
@Path("/soilparams")
@Produces({ "application/json" })
public class BofekServlet extends Worker {
    public BofekServlet() {
        setResource("soilparams");
    }

    @GET
    @Path("/{soilparamid}")
    public Response getSoilParamsInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }

    @GET
    @Path("/")
    public Response getSoilParamsInfoGetAll(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }

    @POST
    @Path("/{soilparamid}")
    public Response getSoilParamsInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));
        return getResponse(props, token);
    }

    @POST
    @Path("/")
    public Response getSoilParamsInfoPostAll(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));
        return getResponse(props, token);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
