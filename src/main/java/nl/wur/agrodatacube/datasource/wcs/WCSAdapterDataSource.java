/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.datasource.wcs;

import nl.wur.agrodatacube.datasource.AdapterDataSource;
import nl.wur.agrodatacube.datasource.GeometryProvider;
import nl.wur.agrodatacube.exec.ExecutorTask;
import nl.wur.agrodatacube.resource.AdapterImageResource;
import nl.wur.agrodatacube.result.AdapterImageResult;
import nl.wur.agrodatacube.result.AdapterResult;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import javax.net.ssl.HttpsURLConnection;
import nl.wur.agrodatacube.ssl.AgrodataCubeHttpsConnectionFactory;

/**
 * This class retrieves data from OGC WCS services. 
 *
 * @author rande001
 */
public class WCSAdapterDataSource extends AdapterDataSource {

    private String baseUrl = null;  // "http://scomp1250:6080/arcgis/services/groenmonitor/DMC_NDVI/ImageServer/WCSServer?SERVICE=WCS&VERSION=1.0.0";
    private String coverageKeyName = "coverage";// dependant on wcs version this can be coverage or identifier or coverageName
    private String version;
    private String defaultOutputFormat = "image/tiff"; // depends on the implementation of the service (vendor dependant, could be read from getcapabilities)
    private AgrodataCubeHttpsConnectionFactory httpsConnectionFactory;

    public WCSAdapterDataSource() {
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
            // Old test junk
            //String coverageUrl = baseUrl.concat("&REQUEST=GetCoverage&coverage=1&format=GeoTIFF&RESX=25&RESY=25&crs=EPSG:28992&bbox=200000,400000,210000,425000");
            //URL u = new URL(coverageUrl);//
            //URL u  = new URL("https://geodata.nationaalgeoregister.nl/ahn25m/wcs?request=GetCoverage&service=wcs&version=1.1.2&crs=epsg:28992&bbox=200000,400000,210000,425000&coverage=ahn25m&format=image/tiff");
            // URL u = new URL("http://localhost/recl_infiltr.tif");

            //
            // Build the URL by retrieving data from the resource (in the task).
            //
            String resourceUrl = baseUrl;
            AdapterImageResource resource = (AdapterImageResource) task.getResource();
            String outputFormat = resource.getOutputFormat();
            
            //
            // Bij nat georeg kan format null zijn.
            if (outputFormat == null) {
                outputFormat = task.getQueryParameterValue("output_format", getDefaultOutputFormat());
            }
            resourceUrl = resourceUrl.concat("&format=").concat(outputFormat);
            resourceUrl = resourceUrl.concat("&".concat(getCoverageKeyName()).concat("=").concat(resource.getCoverageName()));

            // if fieldid is set then return geom for that else return box for supplied geometry
            double area=0.d;
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
            }
            image.setArea(area);
            resourceUrl = resourceUrl.concat("&crs=EPSG:").concat(task.getQueryParameterValue("epsg"));
            resourceUrl = resourceUrl.concat("&").concat(task.getQueryParameterValue("geometry"));
            
//            if (task.getResultParameterValue("output_epsg")!= null) {
//                resourceUrl = resourceUrl.concat("&targetCRS=EPSG:").concat(task.getResultParameterValue("output_epsg"));
//            }

            // Other parameters but first drop epsg, geometry and fieldid
            task.removeQueryParameter("epsg");
            task.removeQueryParameter("geometry");
            task.removeQueryParameter("fieldid");

            Iterator<Object> iterator = task.getQueryParameters().keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (key.equalsIgnoreCase("date")) {
                    resourceUrl = resourceUrl.concat("&TIME=").concat(createDatexpression(task.getQueryParameterValue(key)));
                }
            }
            URL u = new URL(resourceUrl);

            System.out.println(resourceUrl);
            String contentType;
            if (baseUrl.toLowerCase().contains("https:")) {
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
            image.setStatus(e.getClass().getName()+" : " + e.getLocalizedMessage());
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

    /**
     * Set the name of the key from the key value pair in the url that defines
     * the coverage. Should be coverage but ESRI sometimes uses identifier.
     *
     * @param coverage
     */
    public void setCoverageKeyName(String coverage) {
        coverageKeyName = coverage;
    }

    /**
     * Coverage can be identifier or coverage dependant on wcs
     * version/implementation.
     *
     * @return
     */
    public String getCoverageKeyName() {
        return coverageKeyName;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDefaultOutputFormat() {
        return defaultOutputFormat;
    }

    /**
     * Return a valid date expression that can be used by WCS (ESRI ARCGIS Servert). The value has been validated so it is a valid date (yyyymmdd).
     *
     * @param queryParameterValue
     * @return
     */
    private String createDatexpression(String queryParameterValue) throws ParseException {
        
        SimpleDateFormat sdf = new SimpleDateFormat(("yyyyMMdd"));
        Date d = sdf.parse(queryParameterValue);
        sdf.applyPattern("yyy-MM-dd");
        return sdf.format(d);        
    }

}
