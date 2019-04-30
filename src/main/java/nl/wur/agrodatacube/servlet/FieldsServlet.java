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

    @GET
    @Path("/{fieldid}")
    public Response getFieldsInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }

    @POST
    @Path("/{fieldid}")
    public Response getFieldsInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }

    @GET
    @Path("/")
    public Response getInfoGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }

    @POST
    @Path("/")
    public Response getInfoPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        Properties props = parametersToProperties(uriInfo);
        return getResponse(props, token);
    }

    /**
     *
     * @param uriInfo
     * @param token
     * @return results for the apatial intersection for the field and the
     * resource
     */
    @GET
    @Path("/{fieldid}/{resource}")
    public Response getFieldsFieldIdResourceGet(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        java.util.Properties props = parametersToProperties(uriInfo);
        //
        // change the resource from fields to the appropriate one.
        //
        setResource(props.getProperty("resource"));
        props.remove("resource");
        if (getResource().equalsIgnoreCase("ndvi_image")) {
            NDVIImageServlet ndviImageServlet = new NDVIImageServlet();
            return ndviImageServlet.processRequest(props, token, getRemoteIP());
        } else if (getResource().equalsIgnoreCase("ahn_image")) {
            AHNImageServlet ahnImageServlet = new AHNImageServlet();
            return ahnImageServlet.processRequest(props, token, getRemoteIP());
        } else if (getResource().equalsIgnoreCase("meteostations")) {
            setResource("meteostations-field");
            return getResponse(props, token); // todo add more params.
        } else {
            return getResponse(props, token);
        }
    }

    /**
     * @param uriInfo
     * @param token
     * @return results for the apatial intersection for the field and the
     * resource
     */
    @POST
    @Path("/{fieldid}/{resource}")
    public Response getFieldsFieldIdResourcePost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        java.util.Properties props = parametersToProperties(uriInfo);
        setResource(props.getProperty("resource"));
        props.remove("resource");

        //
        // Call the appropriate method to do the work regardles of the HTTP Request method
        //
        if (getResource().equalsIgnoreCase("ndvi_image")) {
            NDVIImageServlet ndviImageServlet = new NDVIImageServlet(); // todo removes httpserveltrequest
            return ndviImageServlet.processRequest(props, token,getRemoteIP());
        } else {
            return getResponse(props, token); // todo add more params.
        }
    }

//    /**
//     * Return the data for the supplied field.
//     *
//     * @param fieldid
//     * @param resourceName
//     * @return
//     */
//    private Response getResponse(java.util.Properties props) { 
//
//        try {
//            AdapterResource resource = AgroDataCubeRegistry.getInstance().findResource(getResource());
//            if (resource == null) {
//                return Response.status(404).build();
//            }
//
//            ExecutorTask task = new ExecutorTask(resource);
//            task.addQueryParameters(props);
//
//            AdapterResult r = task.execute();
//
//            // TODO: Image andere formatter en BWW ook
//            AdapterResultFormatter formatter = AdapterFormatFactory.getDefaultFormatter(r);
//            return Response.status(200).type(r.getMimeType()).entity(formatter.format(r)).build();
//        } catch (Exception e) {
//            return Response.status(200).entity("{\"status\": " + JSONizer.toJson(e.getMessage()) + "}").build();
//        }
//    }
//    
//    @GET
//    @Path("/{fieldid}/soilparams")
//    @ApiOperation(value = "Return the intersections off the field with the soilphysical parameters (for more detailed information see http://content.alterra.wur.nl/Webdocs/PDFFiles/Alterrarapporten/AlterraRapport2387.pdf). The soilparamid returned can be used to retrieve a sample profile")
//    public Response getBofekMsg(
//            @ApiParam(value = "Id off the field you want ndvi information for", required = true) @PathParam("fieldid") Integer fieldid,
//            @ApiParam(value = "Depending on supplied value (nrhits or alldata) return either nrhits or all rows ", required = false) @QueryParam("result") String results,
//            @ApiParam(value = "Output geometry epsg. Default is 28992 only allowed value 4326", required = false) @QueryParam("output_epsg") Integer output_epsg,
//            @ApiParam(value = "A token allows accesss to resources.", required = true) @HeaderParam("token") String token,
//            @ApiParam(value = "Define the number of resources returned min = 0 , max = 50 ", required = false) @QueryParam("page_size") Integer page_size,
//            @ApiParam(value = "Define the rank of the first resource to be returned. NOTE first is 0 NOT 1 ", required = false) @QueryParam("page_offset") Integer page_offset) {
//        reset();
//        setResults(results);
//        if (page_offset != null) {
//            setOffset(page_offset);
//        }
//        if (page_size != null) {
//            setPageSize(page_size);
//        }
//        setOutputEpsg(28992);
//        String to4326_begin = "";
//        String to4326_end = "";
//        if (output_epsg != null) {
//            if (output_epsg == 4326) {
//                to4326_begin = "st_transform(";
//                to4326_end = ",4326)";
//                setOutputEpsg(4326);
//            }
//        }
//        
//        String query = String.format("select * from (select fieldid"
//                                         + ", bofek2012 as soilparamid "
//                                         + ", pawn"
//                                         + ", st_asgeojson( %s st_intersection(b.geom,p.geom) %s , %d ) as geom"
//                                         + ", st_area(st_intersection(b.geom,p.geom)) as area "
//                                         + ", st_perimeter(st_intersection(b.geom,p.geom)) as perimeter "
//                                     + " from bofek2012 b"
//                                         + ", gewaspercelen p  "
//                                    + " where p.fieldid=%d "
//                                       + "and st_intersects(p.geom,b.geom) and not st_touches(p.geom, b.geom)) as foo order by fieldid, area desc", to4326_begin, to4326_end, getNumberOfdecimals(), fieldid);
//        return doWorkWithTokenValidation(query, token);
//    }
//    
//    @GET
//    @Path("/{fieldid}")
//    @ApiOperation(value = "Return geometry and crop information for the field.")
//    public Response getMsg(
//            @ApiParam(value = "Id off the field you want ndvi information for", required = true) @PathParam("fieldid") Integer fieldid,
//            @ApiParam(value = "Depending on supplied value (nrhits or alldata) return either nrhits or all rows ", required = false) @QueryParam("result") String results,
//            @ApiParam(value = "Output geometry epsg. Default is 28992 only allowed value 4326", required = false) @QueryParam("output_epsg") Integer output_epsg,
//            @ApiParam(value = "A token allows accesss to resources. ", required = true) @HeaderParam("token") String token,
//            @ApiParam(value = "Define the number of resources returned min = 0 , max = 50 ", required = false) @QueryParam("page_size") Integer page_size,
//            @ApiParam(value = "Define the rank of the first resource to be returned. NOTE first is 0 NOT 1 ", required = false) @QueryParam("page_offset") Integer page_offset) { // TODO Reg exp voor alleen numbers
//        reset();
//        setResults(results);
//        if (page_offset != null) {
//            setOffset(page_offset);
//        }
//        if (page_size != null) {
//            setPageSize(page_size);
//        }
//        setOutputEpsg(28992);
//        String to4326_begin = "";
//        String to4326_end = "";
//        if (output_epsg != null) {
//            if (output_epsg == 4326) {
//                to4326_begin = "st_transform(";
//                to4326_end = ",4326)";
//                setOutputEpsg(4326);
//            }
//        }
//        String query = String.format("select fieldid"
//                + " , year"
//                + " , crop_code"
//                + " , crop_name"
//                + " , grondgebruik"
//                + ", st_asgeojson(%s geom %s , %d  ) as geom"
//                + ", st_area(geom) as area"
//                + ", st_perimeter(geom) as perimeter"
//                + "  from gewaspercelen "
//                + " where fieldid= %d "
//                + "order by fieldid", to4326_begin, to4326_end, getNumberOfdecimals(), fieldid);
//        return doWorkWithTokenValidation(query, token);
//    }
//    @GET
//    @Path("/")
//    @ApiOperation(value = "Return the geometry and soil information of the intersections of the supplied geometry and the fields. If no epsg is provided epsg = 28992 (RD) is assumed. Currently only epsg's 28992 and 4326 (WGS 84) are supported")
//    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
//    public Response getFieldForParameterGet(
//            @ApiParam(value = "Output geometry epsg. Default is 28992 only allowed value 4326", required = false) @QueryParam("output_epsg") Integer output_epsg,
//            @ApiParam(value = "Define the number of resources returned min = 0 , max = 50 ", required = false) @QueryParam("page_size") Integer page_size,
//            @ApiParam(value = "Depending on supplied value (nrhits or alldata) return either nrhits or all rows ", required = false) @QueryParam("result") String results,
//            @ApiParam(value = "Define the rank of the first resource to be returned. NOTE first is 0 NOT 1 ", required = false) @QueryParam("page_offset") Integer page_offset,
//            @ApiParam(value = "The year for which you want information. ") @QueryParam("year") Integer year,
//            @ApiParam(value = "The cropcode for which you want information. ", required = false) @QueryParam("cropcode") String cropcode,
//            @ApiParam(value = "The cropname for which you want information. Matchins done case non sesitive using contains ", required = false) @QueryParam("cropname") String cropname,
//            @ApiParam(value = "A valid WKT geometry (point or polygon). Default in epsg 28992 when not please supply epsg", required = false) @QueryParam("geometry") String geom,
//            @ApiParam(value = "The epsg for the supplied geomery. Default 28992 (RD). Currently 4326 (WGS84) and 28992 (RD) are supported", required = false) @QueryParam("epsg") Integer epsg,
//            @ApiParam(value = "A token allows accesss to resources. ", required = true) @HeaderParam("token") String token
//    ) {
//        reset();
//        setResults(results);
//        if (page_offset != null) {
//            setOffset(page_offset);
//        }
//        if (page_size != null) {
//            setPageSize(page_size);
//        }
//        
//        ArrayList<Object> params = new ArrayList<>();
//        setOutputEpsg(28992);
//        String to4326_begin = "";
//        String to4326_end = "";
//        if (output_epsg != null) {
//            if (output_epsg == 4326) {
//                to4326_begin = "st_transform(";
//                to4326_end = ",4326)";
//                setOutputEpsg(4326);
//            }
//        }
//        String query = String.format("select * from ( select year"
//                                       + " , crop_code"
//                                       + " , crop_name"
//                                       + " , fieldid"
//                                       + " , st_asgeojson(%s p.geom %s , %d ) as geom "
//                                       + " , st_area(p.geom) as area "
//                                       + " , st_perimeter(p.geom) as perimeter "
//                                    + " from gewaspercelen p "
//                                   + " where fieldid > 0 ", to4326_begin, to4326_end, getNumberOfdecimals()); // force where clause usage so next can be AND
//        if (geom != null) {
//            String theGeom = transformTo28992EWKT(epsg, geom);
//                query = String.format(
//                          "with foo1 as (select st_geomfromewkt(?) as geom) "
//                        + "select * from (select year"
//                             + ", crop_code"
//                             + ", crop_name"
//                             + ", fieldid"
//                             + ", st_asgeojson(%s st_intersection(p.geom,foo1.geom) %s , %d ) as geom "
//                             + ", st_area(st_intersection(p.geom,foo1.geom)) as area "
//                             + ", st_perimeter(st_intersection(p.geom,foo1.geom)) as perimeter "
//                        + "  from gewaspercelen p , foo1 "
//                        + " where st_intersects(p.geom, foo1.geom) and not st_touches(p.geom, foo1.geom)", to4326_begin, to4326_end, getNumberOfdecimals());
//                params.add(theGeom);
//        }
//
//        // Cropcode 
//        if (cropcode != null) {
//            query = query.concat(String.format(" and crop_code = '%s'", cropcode));
//        }
//
//        // Cropname
//        if (cropname != null) {
//            query = query.concat(" and lower(crop_name) like lower(?)");
//            params.add("%".concat(cropname.trim().concat("%")));
//        }
//
//        // Year
//        if (year != null) {
//            query = query.concat(String.format(" and year = %d", year));
//        }
//        
//        query = query.concat(" ) as foo2 order by area desc");
//        return doWorkWithTokenValidation(query, token, params);
//    }
//
//    @POST
//    @Path("/")
//    @ApiOperation(value = "Return the geometry and soil information of the intersections of the supplied geometry and the fields. If no epsg is provided epsg = 28992 (RD) is assumed. Currently only epsg's 28992 and 4326 (WGS 84) are supported")
//    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
//    public Response getFieldForParameterPost(
//            @ApiParam(value = "Output geometry epsg. Default is 28992 only allowed value 4326", required = false) @QueryParam("output_epsg") Integer output_epsg,
//            @ApiParam(value = "Define the number of resources returned min = 0 , max = 50 ", required = false) @QueryParam("page_size") Integer page_size,
//            @ApiParam(value = "Depending on supplied value (nrhits or alldata) return either nrhits or all rows ", required = false) @QueryParam("result") String results,
//            @ApiParam(value = "Define the rank of the first resource to be returned. NOTE first is 0 NOT 1 ", required = false) @QueryParam("page_offset") Integer page_offset,
//            @ApiParam(value = "The year for which you want information. ") @QueryParam("year") Integer year,
//            @ApiParam(value = "The cropcode for which you want information. ", required = false) @QueryParam("cropcode") String cropcode,
//            @ApiParam(value = "The cropname for which you want information. Matchins done case non sesitive using contains ", required = false) @QueryParam("cropname") String cropname,
//            @ApiParam(value = "A valid WKT geometry (point or polygon). Default in epsg 28992 when not please supply epsg", required = false) @QueryParam("geometry") String geom,
//            @ApiParam(value = "The epsg for the supplied geomery. Default 28992 (RD). Currently 4326 (WGS84) and 28992 (RD) are supported", required = false) @QueryParam("epsg") Integer epsg,
//            @ApiParam(value = "A token allows accesss to resources. ", required = true) @HeaderParam("token") String token
//    ) {
//        reset();
//        setResults(results);
//        if (page_offset != null) {
//            setOffset(page_offset);
//        }
//        if (page_size != null) {
//            setPageSize(page_size);
//        }
//        
//        ArrayList<Object> params = new ArrayList<>();
//        setOutputEpsg(28992);
//        String to4326_begin = "";
//        String to4326_end = "";
//        if (output_epsg != null) {
//            if (output_epsg == 4326) {
//                to4326_begin = "st_transform(";
//                to4326_end = ",4326)";
//                setOutputEpsg(4326);
//            }
//        }
//        String query = String.format("select year, crop_code, crop_name, fieldid , st_asgeojson(%s p.geom %s , %d ) as geom , st_area(p.geom) as area , st_perimeter(p.geom) as perimeter from gewaspercelen p where fieldid > 0 ", to4326_begin, to4326_end, getNumberOfdecimals()); // force where clause usage so next can be AND
//        
//        if (geom != null) {
//            String theGeom = transformTo28992EWKT(epsg, geom);
//                query = String.format("with foo as (select st_geomfromewkt(?) as geom) "
//                        + "select year"
//                        + "     , crop_code"
//                        + "     , crop_name"
//                        + "     , fieldid"
//                        + "     , st_asgeojson(%s st_intersection(foo.geom,p.geom) %s , %d ) as geom "
//                        + "     , st_area(st_intersection(foo.geom,p.geom)) as area"
//                        + "     , st_perimeter(st_intersection(foo.geom,p.geom)) as perimeter "
//                        + "  from gewaspercelen p , foo "
//                        + " where st_intersects(p.geom, foo.geom) "
//                        + "   and not st_touches(foo.geom,p.geom) ", to4326_begin, to4326_end, getNumberOfdecimals());
//                params.add(theGeom);            
//        }
//
//        // Cropcode 
//        if (cropcode != null) {
//            query = query.concat(String.format(" and crop_code = '%s'", cropcode));
//        }
//
//        // Cropname
//        if (cropname != null) {
//            query = query.concat(" and lower(crop_name) like lower(?)");
//            params.add("%".concat(cropname.trim().concat("%")));
//        }
//
//        // Year
//        if (year != null) {
//            query = query.concat(String.format(" and year = %d", year));
//        }
//        
//        query = query.concat(" order by st_area(st_intersection(foo.geom,p.geom)  desc ");
//        return doWorkWithTokenValidation(query, token, params);
//    }
//    
    //
    // METEO 
    //
    /**
     * Lever de meteo stations voor het opgegeven veld.
     *
     * @param fieldid
     * @param output_epsg
     * @param token
     * @param results
     * @param page_size
     * @param page_offset
     * @return
     */
//    @GET
//    @Path("/{fieldid}/meteostations")
//    @ApiOperation(value = "Provide the meteostations assigned to this field. Rank is based on distance.")
//    
//    public Response getMeteoForField(@PathParam("fieldid") Integer fieldid,
//            @ApiParam(value = "A token allows accesss to resources. ", required = true) @HeaderParam("token") String token,
//            @ApiParam(value = "Define the number of resources returned min = 0 , max = 50 ", required = false) @QueryParam("page_size") Integer page_size,
//            @ApiParam(value = "Depending on supplied value (nrhits or alldata) return either nrhits or all rows ", required = false) @QueryParam("result") String results,
//            @ApiParam(value = "Define the rank of the first resource to be returned. NOTE first is 0 NOT 1 ", required = false) @QueryParam("page_offset") Integer page_offset) {
//        reset();
//        setResults(results);
//        if (page_offset != null) {
//            setOffset(page_offset);
//        }
//        if (page_size != null) {
//            setPageSize(page_size);
//        }
//        String query = String.format("select fieldid"
//                                       + " , meteostationid"
//                                       + " , rank"
//                                       + " , distance "
//                                     + "from gewaspercelen_meteostation "
//                                   + " where fieldid= %d order by rank desc", fieldid);
//        return doWorkWithTokenValidation(query, token);
//    }
//    
//    @GET
//    @Path("/{fieldid}/ahn")
//    @ApiOperation(value = "Zonal statistics AHN (Height in cm) ", notes = "Return the zonal statistics for the selected fieldid and AHN. In case of a multipolygon multiple rows can be returned, one for each polygon")
//    public Response getAHNForField(@PathParam("fieldid") Integer fieldid,
//            @ApiParam(value = "Depending on supplied value (nrhits or alldata) return either nrhits or all rows ", required = false) @QueryParam("result") String results,
//            @ApiParam(value = "A token allows accesss to resources. ", required = true) @HeaderParam("token") String token) {
//        reset();
//        setResults(results);
//        String query = String.format(
//                "select   max(st_area(geom)) area,    round((sum(d.count*d.mean)/sum(d.count))::numeric,3)  mean, min( d.min) min ,max(d.max) as max "
//                + "from (SELECT (ST_SummaryStats(ST_Clip(r.rast,p.geom))).*, p.geom "
//                + " FROM ahn r, gewaspercelen p"
//                + " WHERE p.fieldid = %d AND st_intersects(r.rast, p.geom)) as d", fieldid);
//        return doWorkWithTokenValidation(query, token);
//    }
}
