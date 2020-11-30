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
 * Return admin information. This is public available so no token needed.
 *
 * @author Rande001
 */
@Path("/regions")

//@Api(value = "Information about administrative regions")
@Produces({ "application/json" })
public class AdminServlet extends Worker {
    public AdminServlet() {
        super();
        setResource("/regions");
    }

    @GET
    @Path("/municipalities")
    public Response getMunicipalitiesInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("municipalities");

        Properties props = parametersToProperties(uriInfo);

        return getResponse(props, token);
    }

    @POST
    @Path("/municipalities")
    public Response getMunicipalitiesInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("municipalities");

        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return getResponse(props, token);
    }

    @GET
    @Path("/municipalities/{municipalityid}")
    public Response getMunicipalitiesMunicipalityGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("municipalities");

        Properties props = parametersToProperties(uriInfo);

        return getResponse(props, token);
    }

    @POST
    @Path("/municipalities/{municipalityid}")
    public Response getMunicipalitiesMunicipalityPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("municipalities");

        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return getResponse(props, token);
    }

    @GET
    @Path("/postalcodes")
    public Response getPostalcodesInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("postalcodes");

        Properties props = parametersToProperties(uriInfo);

        return getResponse(props, token);
    }

    @POST
    @Path("/postalcodes")
    public Response getPostalcodesInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("postalcodes");

        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return getResponse(props, token);
    }

    @GET
    @Path("/postalcodes/{postalcodeid}")
    public Response getPostalcodesPostalcodeGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("postalcodes");

        Properties props = parametersToProperties(uriInfo);

        return getResponse(props, token);
    }

    @POST
    @Path("/postalcodes/{postalcodeid}")
    public Response getPostalcodesPostalcodePost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("postalcodes");

        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return getResponse(props, token);
    }

    @GET
    @Path("/provences")
    public Response getProvencesInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("provinces");

        Properties props = parametersToProperties(uriInfo);

        return getResponse(props, token);
    }

    @POST
    @Path("/provences")
    public Response getProvencesInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("provinces");

        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return getResponse(props, token);
    }

    @GET
    @Path("/provinces")
    public Response getProvincesInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("provinces");

        Properties props = parametersToProperties(uriInfo);

        return getResponse(props, token);
    }

    @POST
    @Path("/provinces")
    public Response getProvincesInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("provinces");

        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return getResponse(props, token);
    }

    @GET
    @Path("/provinces/{provinceid}")
    public Response getProvincesProvinceGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("provinces");

        Properties props = parametersToProperties(uriInfo);

        return getResponse(props, token);
    }

    @POST
    @Path("/provinces/{provinceid}")
    public Response getProvincesProvincePost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        setResource("provinces");

        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return getResponse(props, token);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
