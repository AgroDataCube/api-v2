/*
* Copyright 2019 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */



package nl.wur.agrodatacube.servlet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author rande001
 */
@Path("/version")    // niet logisch api/v1/rest/version -> servlet net als register.jsp
public class Version {
    @Produces({ "application/json" })
    @GET
    @Path("/")
    public Response Version() {
        String o = new String("{ \"version\" : \"2.1, september 20th, 2019\"}");

        return Response.status(200).entity(o).build();
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
