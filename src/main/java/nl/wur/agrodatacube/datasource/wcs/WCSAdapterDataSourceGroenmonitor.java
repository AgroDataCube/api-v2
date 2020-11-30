/*
 * Copyright 2020 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 *
 * This class handles all wcs requests for the groenmonitor geoserver data.
 * 
 */
package nl.wur.agrodatacube.datasource.wcs;

import java.io.IOException;
import nl.wur.agrodatacube.datasource.GeometryProvider;
import nl.wur.agrodatacube.exec.ExecutorTask;
import nl.wur.agrodatacube.resource.AdapterImageResource;
import nl.wur.agrodatacube.result.AdapterImageResult;
import nl.wur.agrodatacube.result.AdapterResult;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import javax.net.ssl.HttpsURLConnection;
import nl.wur.agrodatacube.ssl.AgrodataCubeHttpsConnectionFactory;

/**
 * This class retrieves data from OGC WCS services.
 *
 * @author rande001
 */
public class WCSAdapterDataSourceGroenmonitor extends WCSAdapterDataSource {

    public WCSAdapterDataSourceGroenmonitor() {
        super();
        httpsConnectionFactory = new AgrodataCubeHttpsConnectionFactory();
    }

    /**
     * Task contains the resource we want data for, the geometry
     *
     * @param task
     * @return
     */
    @Override
    public AdapterResult execute(ExecutorTask task) {
        //
        // Validate if all supplied parameters are known and have a valid value.
        //
        validateQueryParameters(task);

        InputStream input = null;
        AdapterImageResult image = new AdapterImageResult();
        try {
            //
            // Check WCS for data http://data.groenmonitor.nl/geoserver/wcs?service=WCS&version=2.0.1&request=describecoverage&coverageid=ndvi_20200530 (404 als niet beschikbaar)
            //

            URL describeCoverageURL = new URL(getBaseUrl());
            String s = describeCoverageURL.getProtocol().concat("://").concat(describeCoverageURL.getAuthority()).concat(describeCoverageURL.getPath()).concat("?version=2.0.1&request=describecoverage&coverageid=").concat(String.format("groenmonitor:ndvi_%s", task.getQueryParameterValue("date")));
            describeCoverageURL = new URL(s);
            try {
                if (describeCoverageURL.getProtocol().toLowerCase().equalsIgnoreCase("https:")) {
//                HttpsURLConnection connection = (HttpsURLConnection) u.openConnection();
                    HttpsURLConnection connection = httpsConnectionFactory.getConnection(describeCoverageURL);
                    input = connection.getInputStream();
                } else {
                    HttpURLConnection connection = (HttpURLConnection) describeCoverageURL.openConnection();
                    input = connection.getInputStream(); // application/vnd.ogc.se_xml bij text dus fout

                    //
                    // Als input iets is als 
                    //
                    // <?xml version="1.0" encoding="UTF-8"?>
                    // <ows:ExceptionReport xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:ows="http://www.opengis.net/ows/2.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.0.0" xsi:schemaLocation="http://www.opengis.net/ows/2.0 http://schemas.opengis.net/ows/2.0/owsExceptionReport.xsd">
                    // 	<ows:Exception exceptionCode="NoSuchCoverage" locator="coverageId">
                    // 		<ows:ExceptionText>Could not find the requested coverage(s): groenmonitor:ndvi_20210808</ows:ExceptionText>
                    // 	</ows:Exception>
                    // </ows:ExceptionReport>
                    //
                    // dus Could not find the requested coverage bevat dan niet gevonden
                    //
                    byte[] buffer = new byte[81920];
                    int n;
                    StringBuilder describeResponseBuilder = new StringBuilder();
                    while ((n = input.read(buffer)) != -1) {
                        describeResponseBuilder.append(new String(buffer));
                    }
                    input.close();
                    if (describeResponseBuilder.toString().contains("Could not find the requested coverage")) {
                         image.setHttpStatusCode(HttpURLConnection.HTTP_NO_CONTENT);
                         image.setStatus("Geen NDVI beeld beschikbaar op " + task.getQueryParameterValue("date"));
                         return image;
                    }
                }
            } catch (IOException e) {
                image.setHttpStatusCode(HttpURLConnection.HTTP_NO_CONTENT);
                image.setStatus("Geen NDVI beeld beschikbaar op " + task.getQueryParameterValue("date"));
                return image;
            }
            //
            // Build the URL by retrieving data from the resource (in the task).
            //
            String resourceUrl = getBaseUrl();
            AdapterImageResource resource = (AdapterImageResource) task.getResource();
            String outputFormat = resource.getOutputFormat();

            //
            // Bij nat georeg kan format null zijn.
            if (outputFormat == null) {
                outputFormat = task.getQueryParameterValue("output_format", getDefaultOutputFormat());
            }
            resourceUrl = resourceUrl.concat("&format=").concat(outputFormat);

            //
            // Coveragename grenmonitor:ndvi_yyyymmdd
            //
            String coverageName = "";
            Iterator<Object> iterator = task.getQueryParameters().keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (key.equalsIgnoreCase("date")) {
                    coverageName = String.format("&coverage=groenmonitor:ndvi_%s", task.getQueryParameterValue(key));
                }
            }
            resourceUrl = resourceUrl.concat(coverageName);

            // if fieldid is set then return geom for that else return box for supplied geometry
            double area = 0.d;
            if (task.getQueryParameterValue("fieldid") != null) {
                double[] boundingBox = GeometryProvider.getBoundingBox(Integer.parseInt(task.getQueryParameterValue("fieldid")));
                task.setParameterValue("geometry", String.format("bbox=%f,%f,%f,%f", boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3]));
                task.setParameterValue("epsg", "28992");
                area = boundingBox[4];
            } else {
                String geometry = task.getQueryParameterValue("geometry");
                String epsg = task.getQueryParameterValue("epsg");
                if (epsg == null) {
                    epsg = "28992";
                }
                double[] boundingBox = GeometryProvider.getBoundingBox(geometry, epsg);
                task.setParameterValue("geometry", String.format("bbox=%f,%f,%f,%f", boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3]));
                task.setParameterValue("epsg", "28992");
                area = boundingBox[4];
                if (area > resource.getAreaLimit()) {
                    image.setStatus(String.format("Geometry area (m2) %.0f exceeds limit of %.0f", area, resource.getAreaLimit()));
                    image.setHttpStatusCode(403);
                    return image;
                }
            }
            image.setArea(area);
            resourceUrl = resourceUrl.concat("&crs=EPSG:").concat(task.getQueryParameterValue("epsg"));
            resourceUrl = resourceUrl.concat("&").concat(task.getQueryParameterValue("geometry"));

//            if (task.getResultParameterValue("output_epsg")!= null) {
//                resourceUrl = resourceUrl.concat("&targetCRS=EPSG:").concat(task.getResultParameterValue("output_epsg"));
//            }
            URL u = new URL(resourceUrl);

            System.out.println(resourceUrl);
            String contentType;
            if (resourceUrl.toLowerCase().contains("https:")) {
//                HttpsURLConnection connection = (HttpsURLConnection) u.openConnection();
                HttpsURLConnection connection = httpsConnectionFactory.getConnection(u);
                input = connection.getInputStream();
                contentType = connection.getContentType();
            } else {
                HttpURLConnection connection = (HttpURLConnection) u.openConnection();
                input = connection.getInputStream(); // application/vnd.ogc.se_xml bij text dus fout
                contentType = connection.getContentType();
            }
            byte[] buffer = new byte[81920];
            int n;
            while ((n = input.read(buffer)) != -1) {
                image.addImageData(buffer, n);
            }
            input.close();
            if (contentType.contains("xml")) {
                throw new RuntimeException(new String(image.getBytes()));
            }
        } catch (Exception e) {
            image.setStatus(e.getClass().getName() + " : " + e.getLocalizedMessage());
            image.setHttpStatusCode(500);
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception ex) {;
            }
        }
        return image;
    }
}
