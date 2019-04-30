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
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
@Path("/ahn")
//@Api(value = "Provide information about height for the fields. Height is from the AHN 25m rasterdataset containg average height in a gridcell in cm compared to NAP")
@Produces({"application/json"})
public class AHNServlet extends Worker {

    public AHNServlet() {
        super();
        setResource("ahn");
    }

    /**
     * Retrieve the zonal statistics for the given geometry.
     *
     * @param uriInfo
     * @return
     */
    @GET
    @Path("")
    public Response getAHNForGeometry(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        java.util.Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);

    }
    /**
     * Retrieve the zonal statistics for the given geometry.
     *
     * @param uriInfo
     * @return
     */
    @POST
    @Path("")
    public Response getAHNForGeometrypPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        java.util.Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);

    }
    
}
