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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.wur.agrodatacube.datasource.GeometryProvider;
import nl.wur.agrodatacube.exception.InvalidParameterException;
import nl.wur.agrodatacube.formatter.AdapterFormatFactory;
import nl.wur.agrodatacube.formatter.JSONizer;
import nl.wur.agrodatacube.mimetypes.Mimetypes;
import nl.wur.agrodatacube.raster.AHNRecalculator;
import nl.wur.agrodatacube.raster.ReProjector;
import nl.wur.agrodatacube.result.AdapterResult;
import nl.wur.agrodatacube.result.AdapterTableResult;

/**
 *
 * @author rande001
 */

/**
 * Retrieve data from the AHN (Dutch Raster Height Map). sources (PDOK) AHN1
 * WCS:
 * https://geodata.nationaalgeoregister.nl/ahn1/wcs?request=GetCapabilities&service=wcs
 * AHN2 WCS:
 * https://geodata.nationaalgeoregister.nl/ahn2/wcs?request=GetCapabilities&service=wcs
 * AHN3 WCS:
 * https://geodata.nationaalgeoregister.nl/ahn3/wcs?request=GetCapabilities&service=wcs
 *
 * @author Rande001
 */
@Path("/ahn_image")

//@Api(value = "Provide information about height for the fields. Height is from the AHN 25m rasterdataset containg average height in a gridcell in cm compared to NAP")
@Produces({ "application/json" })
public class AHNImageServlet extends Worker {
    public AHNImageServlet() {
        super();
        setResource("ahn_image");
    }

    protected Response processRequest(Properties props, String token, String remoteIp) {
        this.remoteIp = remoteIp;

        //
        // define output epsg if not defined.
        //
        String output_epsg = (String) ((WorkerParameter) props.get("output_epsg")).getValue();

        if (output_epsg == null) {
            output_epsg = "28992";
        }

        //
        // Validate the geometry.
        //
        if (props.get("geometry") != null) {
            String geom = (String) ((WorkerParameter) props.get("geometry")).getValue();
            String epsg = null;

            if (props.get("epsg") != null) {
                epsg = (String) ((WorkerParameter) props.get("epsg")).getValue();
            }

            if (epsg == null) {
                epsg = "28992";
            }

            String isOk = GeometryProvider.validateGeometry(geom, epsg);

            if (!"ok".equalsIgnoreCase(isOk)) {
                AdapterResult result = new AdapterTableResult();

                result.setStatus(isOk);
                result.setHttpStatusCode(422);

                try {
                    return Response.status(422)
                                   .type(result.getMimeType())
                                   .entity(AdapterFormatFactory.getDefaultFormatter(result).format(result))
                                   .build();
                } catch (Exception e) {
                    ;
                }
            }
        }

        Response response = getResponse(props, token);

        //
        // if the status is ok then response.getentity is a fp32[] array that we do not need to convert since we are using fp32.
        //
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            try {
                byte[] resultByteRaster = (byte[]) response.getEntity();

                // byte[] floatRaster = convertNDVIRaster(resultByteRaster, output_epsg);
                ReProjector r           = new ReProjector(new AHNRecalculator());
                byte[]      floatRaster = r.convertRasterToFloatAndReproject(resultByteRaster, output_epsg, -32768);

                response = Response.status(200).type(Mimetypes.MIME_TYPE_GEOTIFF).entity(floatRaster).build();
            } catch (Exception e) {
                if (e instanceof InvalidParameterException) {
                    response = Response.status(422)
                                       .type(MediaType.APPLICATION_JSON)
                                       .entity("{ \"status\" : " + JSONizer.toJson(e.getLocalizedMessage()) + " }")
                                       .build();
                } else {
                    response = Response.status(500)
                                       .type(MediaType.APPLICATION_JSON)
                                       .entity("{ \"status\" : " + JSONizer.toJson(e.getLocalizedMessage()) + " }")
                                       .build();
                }
            }
        }

        return response;
    }

    /**
     * Retrieve the zonal statistics for the given geometry.
     *
     * @param uriInfo
     * @param token
     * @return
     */
    @GET
    @Path("")
    public Response getAHNForGeometry(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);

        return processRequest(props, token, getRemoteIP());
    }

    /**
     * Retrieve the zonal statistics for the given geometry.
     *
     * @param uriInfo
     * @param token
     * @return
     */
    @POST
    @Path("")
    public Response getAHNForGeometrypPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return processRequest(props, token, getRemoteIP());
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
