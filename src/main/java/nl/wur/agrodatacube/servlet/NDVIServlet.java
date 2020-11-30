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
 * @author Rande001
 */
@Path("/ndvi")

//@Api(value = "Provide information about height for the fields. Height is from the AHN 25m rasterdataset containg average height in a gridcell in cm compared to NAP")
@Produces({ "application/json" })
public class NDVIServlet extends Worker {
    public NDVIServlet() {
        super();
        setResource("ndvi");
    }

    /**
     * Retrieve the zonal statistics for the given geometry.
     *
     * @param uriInfo
     * @return
     */
    @GET
    @Path("")

//  @ApiOperation(value = "Return the zonal statistics for height using the supplied geometry. If no epsg is provided epsg = 28992 (RD) is assumed. Currently only epsg's 28992 and 4326 (WGS 84) are supported")
    public Response getNDVIForGeometry(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);

        props.put("_ignore_page_size_limit", new WorkerParameter("true", WorkerParameter.WorkerParameterType.SYSTEM));

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

//  @ApiOperation(value = "Return the zonal statistics for height using the supplied geometry. If no epsg is provided epsg = 28992 (RD) is assumed. Currently only epsg's 28992 and 4326 (WGS 84) are supported")
    public Response getNDVIForGeometrypPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return getResponse(props, token);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
