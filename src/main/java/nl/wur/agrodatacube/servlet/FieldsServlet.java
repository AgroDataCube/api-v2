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
 * We need support for urls /fields/{id}/{resource} - soilparams, soiltypes,
 * ndvi, ahn, meteostations /fields/{id} /fields
 *
 * @author rande001
 */
@Path("fields")

//@Api(value = "Provide data about agricultural fields (Gewaspercelen). A field is defined as a given crop on a given geometry in a given year. So fields have a limited lifespan.")
@Produces({"application/json"})
public class FieldsServlet extends Worker {

    public FieldsServlet() {
        setResource("fields");
    }

    /**
     * Return data for a given field but the data is not from the resource
     * fields. Could be ndvi etc.
     *
     * @param props
     * @param token
     * @return
     */
    private Response getFieldsFieldIdResource(Properties props, String token) {

        //
        // change the resource from fields to the appropriate one.
        //
        if (getBaseResource() != null) {
            setResource(getBaseResource());
        } else {
            setResource(props.getProperty("resource"));
            props.remove("resource");
        }

        //
        // Call the appropriate method to do the work regardles of the HTTP Request method
        //
        if (getResource().equalsIgnoreCase("ndvi_image")) {
            NDVIImageServlet ndviImageServlet = new NDVIImageServlet();
            return ndviImageServlet.processRequest(props, token, getRemoteIP());
        } else if (getResource().equalsIgnoreCase("ahn_image")) {
            AHNImageServlet ahnImageServlet = new AHNImageServlet();
            return ahnImageServlet.processRequest(props, token, getRemoteIP());
        } else if (getResource().equalsIgnoreCase("soilparams")) {
            setResource("soilparams");
            return getResponse(props, token);
        } else if (getResource().equalsIgnoreCase("meteostations")) {
            setResource("meteostations-field");
            return getResponse(props, token);   // todo add more params.
        } else {
            return getResponse(props, token);
        }
    }

    /**
     *
     * @param uriInfo
     * @param token
     * @param referer
     * @return results for the spatial intersection for the field and the
     * resource.
     */
    @GET
    @Path("/{fieldid}/{resource}")
    public Response getFieldsFieldIdResourceGet(@Context UriInfo uriInfo, @HeaderParam("token") String token, @HeaderParam("referer") String referer) {
        Properties props = parametersToProperties(uriInfo);
        WorkerParameter resource = (WorkerParameter) props.get("resource");
        setResource((String) resource.getValue());
        setBaseResource("fields");
        if (token == null) {
            System.out.println(String.format("No token supplied so checking for default token for referer %s", referer));
            token = getTokenForReferer(referer);
            System.out.println(String.format("Token from db for referer %s => %s", referer, token));
        }
//        if (!resourceExists(props)) {
//            return Response.status(404).build();
//        }
        setBaseResource(null);
        return getFieldsFieldIdResource(props, token);
    }

    /**
     * @param uriInfo
     * @param token
     * @return results for the spatial intersection for the field and the
     * resource
     */
    @POST
    @Path("/{fieldid}/{resource}")
    public Response getFieldsFieldIdResourcePost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = bodyParamsToProperties();
        setBaseResource("fields");
        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));
        return getFieldsFieldIdResource(props, token);
    }

    @GET
    @Path("/{fieldid}")
    public Response getFieldsInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token, @HeaderParam("Referer") String referer) {
        if (token == null) {
            System.out.println(String.format("No token supplied so checking for default token for referer %s", referer));
            token = getTokenForReferer(referer);
            System.out.println(String.format("Token from db for referer %s => %s", referer, token));
        }
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }

    @POST
    @Path("/{fieldid}")
    public Response getFieldsInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token, @HeaderParam("referer") String referer) {
        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));
        if (token == null) {
            System.out.println(String.format("No token supplied so checking for default token for referer %s", referer));
            token = getTokenForReferer(referer);
            System.out.println(String.format("Token from db for referer %s => %s", referer, token));
        }

        return getResponse(props, token);
    }

    @GET
    @Path("/")
    public Response getInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token, @HeaderParam("referer") String referer) {
        Properties props = parametersToProperties(uriInfo);
        if (token == null) {
            System.out.println(String.format("No token supplied so checking for default token for referer %s", referer));
            token = getTokenForReferer(referer);
            System.out.println(String.format("Token from db for referer %s => %s", referer, token));
        }
        return getResponse(props, token);
    }

    @POST
    @Path("/")
    public Response getInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = bodyParamsToProperties();

        // if (props.isEmpty())
        props.putAll(parametersToProperties(uriInfo));

        return getResponse(props, token);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
