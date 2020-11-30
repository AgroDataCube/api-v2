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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author Rande001
 */
@Path("/sources")
@Produces({ "application/json" })
public class SourcesServlet extends Worker {
    @GET
    @Path("/")

    /**
     * All documentation has been moved to Postman (https://documenter.getpostman.com/view/3862510/RVnSHh76).
     */
    public Response getInformation() {
        return Response.ok()
                       .entity(
                       "{ \"Please see the information at \" : \"https://documenter.getpostman.com/view/5525378/S1EQSdKN\" }")
                       .build();
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
