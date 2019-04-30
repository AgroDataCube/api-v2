/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.datasource;

import nl.wur.agrodatacube.datasource.postgres.PostgresAdapterDataSource;
import nl.wur.agrodatacube.registry.AgroDataCubeRegistry;
import nl.wur.agrodatacube.result.AdapterTableResult;
import java.util.ArrayList;

/**
 * Provide a geometry from the postgres database.
 *
 * @author rande001
 */
public class GeometryProvider {

    /**
     * Private constructor to hide default public constructor.
     *
     */
    private GeometryProvider() {
    }

    /**
     * Return the geometry for the field. The rsult is in 28992 since that is
     * where data in in.
     *
     * @param fieldid
     * @param format WKT, GML, GEOJSON
     *
     * @return
     */
    public synchronized static String getGeometry(int fieldid, String format) {
        AdapterTableResult result;
        ArrayList<Object> params = new ArrayList<>();
        params.add(fieldid);
        try {
            if (format.equalsIgnoreCase("gml")) {
                PostgresAdapterDataSource postgresAdapterDataSource = (PostgresAdapterDataSource) AgroDataCubeRegistry.getInstance().findResource("fields").getDataSource();
                result = postgresAdapterDataSource.executeQuery("select st_asgml(geom) from gewaspercelen where fieldid=?", params);
                if (result.didSucceed()) {
                    return (String) result.getRow(0).get(0);
                }
            }
            if (format.equalsIgnoreCase("wkt")) {
                // Only as ewkt supported so use that and remove srid info
                PostgresAdapterDataSource postgresAdapterDataSource = (PostgresAdapterDataSource) AgroDataCubeRegistry.getInstance().findResource("fields").getDataSource();
                result = postgresAdapterDataSource.executeQuery("select st_asewkt(geom) from gewaspercelen where fieldid=?", params);
                if (result.didSucceed()) {
                    return (String) result.getRow(0).get(0);
                }
            }
            if (format.equalsIgnoreCase("geojson")) {
                // Only as ewkt supported so use that and remove srid info
                PostgresAdapterDataSource postgresAdapterDataSource = (PostgresAdapterDataSource) AgroDataCubeRegistry.getInstance().findResource("fields").getDataSource();
                result = postgresAdapterDataSource.executeQuery("select st_asgeojson(geom,3) from gewaspercelen where fieldid=?", params);
                if (result.didSucceed()) {
                    return (String) result.getRow(0).get(0);
                }
            }
            throw new RuntimeException("GeometryProvider:getGeometry ".concat(String.format(" unknown format %s", format)));
        } catch (Exception e) {
            throw new RuntimeException("GeometryProvider:getGeometry ".concat(e.getMessage()));
        }
    }

    public static synchronized double[] getBoundingBox(int fieldid) {
        // BOX(159453.186000001 573518.544,159883.491 573805.851)

        try {
            ArrayList<Object> params = new ArrayList<>();
            params.add(fieldid);
            PostgresAdapterDataSource postgresAdapterDataSource = (PostgresAdapterDataSource) AgroDataCubeRegistry.getInstance().findResource("fields").getDataSource();
            AdapterTableResult result = postgresAdapterDataSource.executeQuery("select st_xmin(box) xmin, st_ymin(box) ymin, st_xmax(box) xmax, st_ymax(box) ymax , st_area(box) area from (select st_extent(st_transform(geom,28992)) box from gewaspercelen where fieldid=?) as foo", params);
            if (result.didSucceed()) {
                if (result.getRowCount() == 0) {
                    throw new RuntimeException(String.format("GeometryProvider:getBoundingBox, no bounding box for non existing field %d ", fieldid));
                }

                //
                // if an invalid fieldid is supplied st_extent returns a NULL row without values so result.getRow(0).get(0) is null.
                //
                if (result.getRow(0).get(0) == null) {
                    throw new RuntimeException(String.format("GeometryProvider:getBoundingBox, no bounding box for non existing field %d ", fieldid));
                }
                double[] resultArray = new double[5];
                resultArray[0] = (double) result.getRow(0).get(0);
                resultArray[1] = (double) result.getRow(0).get(1);
                resultArray[2] = (double) result.getRow(0).get(2);
                resultArray[3] = (double) result.getRow(0).get(3);
                resultArray[4] = (double) result.getRow(0).get(4); // area
                return resultArray;
            } else {
                throw new RuntimeException("GeometryProvider:getBoundingBox ".concat(result.getStatus()));
            }
        } catch (Exception e) {
            throw new RuntimeException("GeometryProvider:getBoundingBox ".concat(e.getMessage()));
        }
    }

    /**
     * Determine the bounding box of the supplied geomerty. is is in the
     * supplied epsg so we do a transform to 28992.
     *
     * @param geometry
     * @param epsg
     * @return
     */
    public static synchronized double[] getBoundingBox(String geometry, String epsg) {

        try {
            String geom = "SRID=".concat(epsg).concat(";").concat(geometry);
            ArrayList<Object> params = new ArrayList<>();
            params.add(geom);
            PostgresAdapterDataSource postgresAdapterDataSource
                    = (PostgresAdapterDataSource) AgroDataCubeRegistry.getInstance().findResource("fields").getDataSource();
            AdapterTableResult result
                    = postgresAdapterDataSource
                            .executeQuery("select st_xmin(box) xmin, st_ymin(box) ymin, st_xmax(box) xmax, st_ymax(box) ymax , st_area(box) area from (select st_extent(st_transform(st_geomfromewkt(?),28992)) box ) as foo", params);
            if (result.didSucceed()) {
                double[] resultArray = new double[5];
                resultArray[0] = (double) result.getRow(0).get(0);
                resultArray[1] = (double) result.getRow(0).get(1);
                resultArray[2] = (double) result.getRow(0).get(2);
                resultArray[3] = (double) result.getRow(0).get(3);
                resultArray[4] = (double) result.getRow(0).get(4);
                return resultArray;
            } else {
                throw new RuntimeException("GeometryProvider:getBoundingBox ".concat(result.getStatus()));
            }
        } catch (Exception e) {
            throw new RuntimeException("GeometryProvider:getBoundingBox ".concat(e.getMessage()));
        }
    }

    /**
     * Use Postgis to check if ageometry is valid.
     *
     * @param geometry
     * @param epsg
     * @return
     */
    public static synchronized String validateGeometry(String geometry, String epsg) {
        try {
            String geom = "SRID=".concat(epsg).concat(";").concat(geometry);
            ArrayList<Object> params = new ArrayList<>();
            params.add(geom);
            PostgresAdapterDataSource postgresAdapterDataSource
                    = (PostgresAdapterDataSource) AgroDataCubeRegistry.getInstance().findResource("fields").getDataSource();
            AdapterTableResult result
                    = postgresAdapterDataSource.executeQuery("select st_isvalid(st_geomfromewkt(?))", params);
            return result.getStatus();
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }
}
