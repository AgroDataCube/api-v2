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

import nl.wur.agrodatacube.datasource.NDVIAvailabilityChecker;
import nl.wur.agrodatacube.formatter.JSONizer;
import nl.wur.agrodatacube.mimetypes.Mimetypes;
import com.google.gson.JsonObject;
//import io.swagger.annotations.Api;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
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
import nl.wur.agrodatacube.raster.NDVIRecalculator;
import nl.wur.agrodatacube.raster.ReProjector;
import nl.wur.agrodatacube.result.AdapterResult;
import nl.wur.agrodatacube.result.AdapterTableResult;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.Operations;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.parameter.GeneralParameterValue;

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
@Path("/ndvi_image")
//@Api(value = "Provide information about height for the fields. Height is from the AHN 25m rasterdataset containg average height in a gridcell in cm compared to NAP")
@Produces({"application/json"})
public class NDVIImageServlet extends Worker {

    public NDVIImageServlet() {
        super();
        setResource("ndvi_image");
    }

    /**
     * Retrieve the zonal statistics for the given geometry.
     *
     * @param uriInfo
     * @return
     */
    @GET
    @Path("")
    public Response getNDVIForGeometry(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        java.util.Properties props = parametersToProperties(uriInfo);
        return processRequest(props, token, getRemoteIP());
    }

    /**
     * Retrieve the zonal statistics for the given geometry.
     *
     * @param uriInfo
     * @return
     */
    @POST
    @Path("")
    public Response getNDVIForGeometrypPost(@Context UriInfo uriInfo, @HeaderParam("token") String token) {
        java.util.Properties props = parametersToProperties(uriInfo);
        return processRequest(props, token, getRemoteIP());
    }

    /**
     * NDVI values from geodesk server are in bytes they must be converted to
     * floating point.So read the returned image and convert from unsigned byte
     * to floating point.
     *
     * @param props
     * @param token
     * @param remoteIp
     * @return
     */
    protected Response processRequest(java.util.Properties props, String token, String remoteIp) {
        //
        // Set remote ip. This is needed for logging and accounting. If this class is called as a servlet (uri) it will be filled using http request else caller must suppy this.
        //
        this.remoteIp = remoteIp;
        //
        // Create the normal response for images.
        //
        String output_epsg = props.getProperty("output_epsg"); // todo check if always converted to 28992 then no effect, perhaps use output_epsg.
        if (output_epsg == null) {
            output_epsg = "28992";
        }

        //
        // Validate the geometry.
        //
        if (props.get("geometry") != null) {
            String geom = props.getProperty("geometry");
            String epsg = props.getProperty("epsg", "28992");
            String isOk = GeometryProvider.validateGeometry(geom, epsg);
            if (!"ok".equalsIgnoreCase(isOk)) {
                AdapterResult result = new AdapterTableResult();
                result.setStatus(isOk);
                result.setHttpStatusCode(422);
                try {
                    return Response.status(422).type(result.getMimeType()).entity(AdapterFormatFactory.getDefaultFormatter(result).format(result)).build();
                } catch (Exception e) {
                    ;
                }
            }
        }

        //
        // First see if there is data for this date. Use only the year part. we can use the ndvi request for this and then filter unique dates.
        //
        try {
            ArrayList<String> availability = NDVIAvailabilityChecker.checkAvalibility(props);
            if (availability.size() < 1) { // No data on that date.
                availability = NDVIAvailabilityChecker.getAvalibility(props);
                String a = "[ ";
                String komma = "";
                for (String q : availability) {
                    a = a.concat(komma).concat(q);
                    komma = ", ";
                }
                a = a.concat(" ]");
                JsonObject o = new JsonObject();
                o.addProperty("status", String.format("No NDVI data available for the request area on date %s (date format = yyyymmdd). Please select a date from %s", props.get("date"), a));
                return Response.ok().status(200).entity(JSONizer.toJson(o)).build();
            }
        } catch (Exception e) {
            JsonObject o = new JsonObject();
            o.addProperty("status", e.getMessage());
            if (e instanceof InvalidParameterException) {
                return Response.ok().status(422).entity(JSONizer.toJson(o)).build();
            } else {
                return Response.ok().status(200).entity(JSONizer.toJson(o)).build();
            }
        }
        Response response = getResponse(props, token);

        // TODO: Kan dus fout zijn bv bij point. Dus http status wijzigen
        //
        // if the status is ok then response.getentity is a byte[] array that we need to convert.
        //
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            try {
                byte[] resultByteRaster = (byte[]) response.getEntity();
                //byte[] floatRaster = convertNDVIRaster(resultByteRaster, output_epsg);
                ReProjector r = new ReProjector(new NDVIRecalculator());
                byte[] floatRaster = r.convertRasterToFloatAndReproject(resultByteRaster, output_epsg, 250);
                response = Response.status(200).type(Mimetypes.MIME_TYPE_GEOTIFF).entity(floatRaster).build();
                //response = Response.status(200).type(Mimetypes.MIME_TYPE_GEOTIFF).entity(resultByteRaster).build();
            } catch (Exception e) {
                if (e instanceof InvalidParameterException) {
                    response = Response.status(422).type(MediaType.APPLICATION_JSON).entity("{ \"status\" : " + JSONizer.toJson(e.getLocalizedMessage()) + " }").build();
                } else {
                    response = Response.status(500).type(MediaType.APPLICATION_JSON).entity("{ \"status\" : " + JSONizer.toJson(e.getLocalizedMessage()) + " }").build();
                }
            }
        }
        return response;
    }

    /**
     * @param inputData the raster as it is received from the WCS. That means
     * byte values (0..250) these need to be converted to floating poitn values
     * between 0 and 1.
     *
     * @throws Exception
     */
    private byte[] convertNDVIRaster(byte[] inputData, String output_epsg) throws Exception {

        final org.opengis.referencing.crs.CoordinateReferenceSystem sourceCRS = org.geotools.referencing.CRS.decode("EPSG:28992", true);

        final Hints hint = new Hints();
        hint.put(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, sourceCRS);
        GeoTiffReader reader = new GeoTiffReader(new ByteArrayInputStream(inputData), hint);
        GridCoverage2D inputCoverage = reader.read(null);

        int rows = (int) Math.round(inputCoverage.getGridGeometry().getGridRange2D().getSize().getHeight());
        int columns = (int) Math.round(inputCoverage.getGridGeometry().getGridRange2D().getSize().getWidth());
        System.out.println(String.format("Rows = %d, columns = %d", rows, columns));

        //
        // Some fixed issues
        //
        int bands = 1;
        int[] bandOffsets = {0};

        //
        // Create a new geotiff
        //
        // final File geotiff = new File("d:/temp/1202018-20180703-float.tif");
        GridCoverageFactory factory = new GridCoverageFactory();
        SampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_FLOAT, columns, rows, bands, columns * bands, bandOffsets);
        DataBuffer buffer = new java.awt.image.DataBufferFloat(columns * rows * bands);
        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorModel colorModel = new ComponentColorModel(colorSpace, false, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_FLOAT);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, null);
        BufferedImage bi = new BufferedImage(colorModel, raster, false, null);
        ReferencedEnvelope envelope
                = new ReferencedEnvelope(inputCoverage.getGridGeometry().getEnvelope2D().getMinX(), inputCoverage.getGridGeometry().getEnvelope2D().getMaxX(), inputCoverage.getGridGeometry().getEnvelope2D().getMinY(), inputCoverage.getGridGeometry().getEnvelope2D().getMaxY(), sourceCRS);
//        System.out.println(
//                String.format("LL = (%f, %f), UR = (%f,%f)", inputCoverage.getGridGeometry().getEnvelope2D().getMinX(), inputCoverage.getGridGeometry().getEnvelope2D().getMinY(), inputCoverage.getGridGeometry().getEnvelope2D().getMaxX(), inputCoverage.getGridGeometry().getEnvelope2D().getMaxY()));

        float[] newValues = new float[bands];
        Raster inputRaster = inputCoverage.getRenderedImage().getData();
        byte[] b = new byte[inputRaster.getNumDataElements()];
        for (int columnNr = 0; columnNr < columns; columnNr++) {
            for (int rowNr = 0; rowNr < rows; rowNr++) {
                try {
                    inputRaster.getDataElements(columnNr, rowNr, b);
                    if (java.lang.Byte.toUnsignedInt(b[0]) > 250) {
                        newValues[0] = -9999.f;
                    } else {
                        newValues[0] = java.lang.Byte.toUnsignedInt(b[0]) / 250.f;
                    }
                    raster.setDataElements(columnNr, rowNr, newValues);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("convertNDVIRaster: Rows = %d, Columns = %d, rowNr = %d, columnNr = %d", rows, columns, rowNr, columnNr));
                }
            }
        }
        GridCoverage2D newRaster = factory.create("newRaster", bi, envelope);

        //
        // Now see if we need to transform. Only if output_epsg != 28992
        //
        if (!"28992".equalsIgnoreCase(output_epsg)) {
            final org.opengis.referencing.crs.CoordinateReferenceSystem targetCRS = org.geotools.referencing.CRS.decode("EPSG:".concat(output_epsg), true);
            GridCoverage2D covtransformed = (GridCoverage2D) Operations.DEFAULT.resample(newRaster, targetCRS);
            newRaster = covtransformed;
        }

        //
        // write the new geotiff
        //
        // todo metadata add no data value etc.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GeoTiffWriter writer = new GeoTiffWriter(outputStream);
        writer.write(newRaster, new GeneralParameterValue[0]);
        return outputStream.toByteArray();
    }

}
