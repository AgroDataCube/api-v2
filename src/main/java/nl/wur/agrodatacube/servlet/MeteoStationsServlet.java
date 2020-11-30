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
 * /meteostations
 *
 * output_epsg
 * result
 * page_size
 * page_offset
 * meteostationid
 *
 * @author rande001
 */
@Path("/meteostations")

//@Api(value = "Provide information for meteostations")
@Produces({ "application/json" })
public class MeteoStationsServlet extends Worker {
    public MeteoStationsServlet() {
        super();
        setResource("meteostations");
    }

    @GET
    @Path("/")
    public Response getMeteoDataInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);

        return getResponse(props, token);
    }

    @POST
    @Path("/")
    public Response getMeteoDataInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return getResponse(props, token);
    }

    @GET
    @Path("/{meteostationid}")
    public Response getMeteoDatastationInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);

        return getResponse(props, token);
    }    // String query = String.format("select meteostationid,name,wmocode,lon,lat,alt,source,provider,st_asgeojson(%s geom %s ,%d ) as geom from knmi_meteo_station order by meteostationid ", to4326_begin, to4326_end, getNumberOfdecimals());

    @GET
    @Path("/{meteostationid}/meteodata")
    public Response getMeteoDatastationInfoGetMd(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);

        props.remove("resource");
        setResource("meteodata");

        // setBaseResource("meteostations");
        return getResponse(props, token);
    }

    @POST
    @Path("/{meteostationid}")
    public Response getMeteoDatastationInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return getResponse(props, token);
    }    // String query = String.format("select meteostationid,name,wmocode,lon,lat,alt,source,provider,st_asgeojson(%s geom %s ,%d ) as geom from knmi_meteo_station order by meteostationid ", to4326_begin, to4326_end, getNumberOfdecimals());
}


//~ Formatted by Jindent --- http://www.jindent.com
