/*
* Copyright 2019 Wageningen Environmental Research
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import nl.wur.agrodatacube.exception.InvalidParameterException;
import nl.wur.agrodatacube.servlet.WorkerParameter;

/**
 *
 * @author rande001
 */
public class NDVIAvailabilityChecker {

    /**
     * See if there is data for the supplied date and geometry (can be field
     * also).
     *
     * @param props
     * @return
     */
    public synchronized static ArrayList<String> checkAvalibility(java.util.Properties props) {
        AdapterTableResult result;
        ArrayList<Object> params = new ArrayList<>();

        //
        // Build the query
        //
        StringBuilder query = new StringBuilder();

        if (props.get("date") == null) {
            throw new InvalidParameterException("Missing date information in NDVIAvailabilityChecker");
        }

        WorkerParameter w = (WorkerParameter) props.get("date");
        String dateValue = null;
        if (w != null) {
            dateValue = (String) w.getValue();
        }
        if (dateValue == null) {
            throw new InvalidParameterException("Missing date information in NDVIAvailabilityChecker");
        }
        if (dateValue.length() != 8) {
            throw new InvalidParameterException(String.format("Invalid date %s information in NDVIAvailabilityChecker, use yyyymmdd format", dateValue));
        }

        Integer date;
        try {
            date = Integer.parseInt(dateValue);
        } catch (Exception e) {
            throw new InvalidParameterException(String.format("Invalid date %s information in NDVIAvailabilityChecker, use yyyymmdd format", dateValue));
        }
        if (props.get("fieldid") != null) {
            query.append("select distinct datum from gewaspercelen_ndvi where fieldid=? and datum = to_date(?,'yyyymmdd') order by 1");
            String fieldid = (String) ((WorkerParameter) props.get("fieldid")).getValue();
            params.add(Integer.parseInt(fieldid));
            params.add(date.toString());
        } else if (props.get("geometry") != null) {
            String fromFoo;
            String fooParam;
            if (props.get("epsg") != null) {
                fromFoo = "(select st_transform(st_geomfromewkt(?),28992) as geom) as t3";
                fooParam = "srid=".concat((String) ((WorkerParameter) props.get("epsg")).getValue());
            } else {
                fromFoo = "(select st_geomfromewkt(?) as geom) as t3";
                fooParam = "srid=28992";
            }
                fooParam = fooParam.concat(";").concat((String) ((WorkerParameter) props.get("geometry")).getValue());
            query.append("select distinct datum \n"
                    + "     from gewaspercelen_ndvi t1\n"
                    + "    where datum = to_date(?,'yyyymmdd') \n"
                    + "      and exists (select t2.fieldid\n"
                    + "                    from gewaspercelen t2 \n"
                    + "	                      , ".concat(fromFoo).concat(
                            "                   where st_intersects(t2.geom,t3.geom) "
                            + "                     and not st_touches(t2.geom,t3.geom) "
                            + "                     and t1.fieldid =t2.fieldid  "
                            + "                 )"));
            params.add(date.toString()); //Needs to be a string to match to_date function in Postgreql
            params.add(fooParam);
        } else {
            throw new InvalidParameterException("Missing geometry information in NDVIAvailabilityChecker");
        }

        //
        // Now execute the query and return the results.
        //
        PostgresAdapterDataSource postgresAdapterDataSource = (PostgresAdapterDataSource) AgroDataCubeRegistry.getInstance().findResource("ndvi").getDataSource();
        if (postgresAdapterDataSource == null) {
            throw new RuntimeException("No PostgresAdapterDataSource found for resource \"ndvi\" in NDVIAvailabilityChecker");
        }
        result = postgresAdapterDataSource.executeQuery(query.toString(), params);

        //
        // Now see if it resulted and process the data
        //        
        ArrayList<String> dates = new ArrayList<>();
        if (result.didSucceed()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            for (int i = 0; i < result.getRowCount(); i++) {
                dates.add(sdf.format(result.getRow(i).get(0)));
            }
        }
        result = null;
        return dates;
    }

    /**
     * Return the dates in the YEAR from the date for which there is data. When
     * using large areas this can be slow.
     *
     * @param props
     * @return
     */
    public synchronized static ArrayList<String> getAvalibility(java.util.Properties props) {
        AdapterTableResult result;
        ArrayList<Object> params = new ArrayList<>();

        //
        // Build the query
        //
        StringBuilder query = new StringBuilder();

        if (props.get("date") == null) {
            throw new InvalidParameterException("Missing date information in NDVIAvailabilityChecker");
        }
        String dateValue = (String) ((WorkerParameter) props.get("date")).getValue();
        if (dateValue == null) {
            throw new InvalidParameterException("Missing date information in NDVIAvailabilityChecker");
        }
        if (dateValue.length() != 8) {
            throw new InvalidParameterException(String.format("Invalid date %s information in getAvailibility, use yyyymmdd format", dateValue));
        }

        Integer date;
        try {
            date = Integer.parseInt(dateValue);
            while (date > 10000) {
                date /= 10;
            }
        } catch (Exception e) {
            throw new InvalidParameterException(String.format("Invalid date %s information in NDVIAvailabilityChecker, use yyyymmdd format", dateValue));
        }
        String fooParam = "";
        if (props.get("fieldid") != null) {
            query.append("select distinct datum from gewaspercelen_ndvi where fieldid=? and extract (year from datum) = ? order by 1");
            String fieldid = (String) ((WorkerParameter) props.get("fieldid")).getValue();
            params.add(Integer.parseInt(fieldid));
            params.add(date);
        } else if (props.get("geometry") != null) {
            String fromFoo;
            if (props.get("epsg") != null) {
                fromFoo = "(select st_transform(st_geomfromewkt(?),28992) as geom) as t3";
                fooParam = "srid=".concat((String) ((WorkerParameter) props.get("epsg")).getValue());
            } else {
                fromFoo = "(select st_geomfromewkt(?) as geom) as t3";
                fooParam = "srid=28992";
            }
            fooParam = fooParam.concat(";").concat((String) ((WorkerParameter) props.get("geometry")).getValue());
            query.append("select distinct datum \n"
                    + "     from gewaspercelen_ndvi t1\n"
                    + "    where datum between to_date(?,'yyyymmdd') and to_date(?,'yyyymmdd') \n"
                    + "      and exists (select t2.fieldid\n"
                    + "                    from gewaspercelen t2 \n"
                    + "	                      , ".concat(fromFoo).concat(
                            "                   where st_intersects(t2.geom,t3.geom) "
                            + "                     and not st_touches(t2.geom,t3.geom) "
                            + "                     and t1.fieldid =t2.fieldid  "
                            + "                 )"));
            params.add(date.toString().concat("0101")); //Needs to be a string to match to_date function in Postgreql
            params.add(date.toString().concat("3112")); //Needs to be a string to match to_date function in Postgreql
            params.add(fooParam);
        } else {
            throw new InvalidParameterException("Missing geometry information in NDVIAvailabilityChecker");
        }

        //
        // Now execute the query and return the results.
        //
        PostgresAdapterDataSource postgresAdapterDataSource = (PostgresAdapterDataSource) AgroDataCubeRegistry.getInstance().findResource("ndvi").getDataSource();
        if (postgresAdapterDataSource == null) {
            throw new RuntimeException("No PostgresAdapterDataSource found for resource \"ndvi\" in NDVIAvailabilityChecker");
        }
        result = postgresAdapterDataSource.executeQuery(query.toString(), params);

        //
        // Now see if it resulted and process the data
        //        
        ArrayList<String> dates = new ArrayList<>();
        if (result.didSucceed()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            for (int i = 0; i < result.getRowCount(); i++) {
                dates.add(sdf.format(result.getRow(i).get(0)));
            }
        }
        result = null;
        return dates;
    }
}
