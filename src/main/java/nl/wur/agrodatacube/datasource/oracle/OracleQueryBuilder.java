/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.datasource.oracle;

import java.security.InvalidParameterException;
import nl.wur.agrodatacube.exec.ExecutorTask;
import nl.wur.agrodatacube.properties.AgroDataCubeProperties;
import nl.wur.agrodatacube.registry.AgroDataCubeRegistry;
import nl.wur.agrodatacube.resource.AdapterPostgresResource;
import nl.wur.agrodatacube.resource.AdapterQueryResource;
import nl.wur.agrodatacube.resource.AdapterResource;
import nl.wur.agrodatacube.resource.AdapterTableResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import nl.wur.agrodatacube.resource.query.ConfigurationParameter;
import nl.wur.agrodatacube.servlet.WorkerParameter;

/**
 * This class builds SQL statements for Oracle databases. We  need a
 * variant for each database due to differences in date functions, spatial
 * functions etc.
 *
 * @author rande001
 */
public class OracleQueryBuilder {

    /**
     * The task contains all the information we have to determine what needs to
     * be done.The resource has to be a AdapterTableResource.
     *
     * @param task
     * @return
     */
    public OracleQuery buildQuery(ExecutorTask task) {
        //
        // if this is a queryResource it contains the query so we can return that after including all supplied parameters.
        //

        AdapterResource resource = task.getResource();

//        if (2 > 1) {
//            return buildQueryForTableResource(task);
//        }
        if (resource instanceof AdapterQueryResource) {
            return buildQueryForQueryResource(task);
        } else if (resource instanceof AdapterTableResource) {
            return buildQueryForTableResource(task);
        }

        throw new RuntimeException(String.format("Unable to build a query for resource %s, type %s", resource.getName(), resource.getClass().getName()));
    }

    /**
     * Create a query for a query resource and include parameters. JDBC supports
     * the setObject but Postgres wants the types to be matchable.
     *
     * @param task
     * @return
     */
    private OracleQuery buildQueryForQueryResource(ExecutorTask task) {
        AdapterQueryResource resource = (AdapterQueryResource) task.getResource();
        OracleQuery result = new OracleQuery(resource);

        //
        // Get the inital part of the query
        //
        String query = resource.getBaseQuery();
        String where = " where ";

        //        
        // Base query is , separated. Check for geom element and if present replace by SDO_UTIL.TO_GEOJSON , transform and nr of decs dependant on output_epsg
        //
        String geom = "";
        if (resource.getGeometryColumn() != null) {
            int ndec = 3;
            if (task.getResultParameterValue("output_epsg") != null) {
                String outputEpsg = task.getResultParameterValue("output_epsg");
                if (!"28992".equalsIgnoreCase(outputEpsg)) {
                    ndec = 6;
                }
                geom = "SDO_UTIL.TO_GEOJSON (st_transform(".concat(resource.getGeometryColumn()).concat(",").concat(outputEpsg).concat("),").concat("" + ndec).concat(") as geom,");
            } else {
                geom = "SDO_UTIL.TO_GEOJSON (".concat(resource.getGeometryColumn()).concat(",").concat("" + ndec).concat(") as geom,");
            }
            query = query.replaceFirst("select", "select ".concat(geom));
        }

        if (resource.getBaseQuery().contains(where)) {
            where = " and ";
        }

        //
        // Check if the parameters for the task are valid (supported by the resource). While doing this we are also building the final query.
        //
        Iterator<Map.Entry<Object, Object>> it = task.getQueryParameters().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> currentParam = it.next();
            ConfigurationParameter findQueryParameter = task.getResource().findQueryParameter((String) currentParam.getKey());
            //
            // So add to query
            //
            WorkerParameter w = (WorkerParameter) currentParam.getValue();
            query = query.concat(where).concat(createExpression(findQueryParameter.getColumnName(), findQueryParameter.getDataType(), findQueryParameter.getOperator(findQueryParameter.getName()), (String) w.getValue(), true)); // todo
            result.addParamValues(findQueryParameter.prepareValue((String) w.getValue(), true)); // todo
            where = " and ";
        }
        result.setQuery(query);
        // query = query.concat(where).concat(" rownum < 10");

        //
        // Somehow include order by. Ignore if count is requested.
        //
        if (task.returnJustnrOfHits()) { // This parameter has no value
            result.setQuery("select count(*) nrofhits from (".concat(result.getQuery()).concat(" ) as foo"));
            result.removeChildren(); // Not needed for the count and also the link columns are not available.
            System.out.println(result.getQuery());
            return result;
        } else {// if (!task.returnAllData()) { alldata is disabled since march4th 2019

            //
            // todo: add order by 
            //
            StringBuilder orderBy = new StringBuilder(" order by ");
            if (resource.getOrderBy() != null) {
                String komma = "";
                for (String o : resource.getOrderBy()) {
                    orderBy.append(komma).append(o);
                }
                result.setQuery(result.getQuery().concat(orderBy.toString()));
            }
            //
            // Add paging if supplied
            //
            int page_size = AgroDataCubeProperties.getDefaultPageSize();

//            if (task.getResultParameterValue("page_size") != null) {
//                int user_page_size = Integer.parseInt(task.getResultParameterValue("page_size"));
//                if (user_page_size > page_size) {
//                    throw new InvalidParameterException(String.format("Value %d for page__size exceeds limit %d", user_page_size, page_size));
//                }
//                result.setQuery(result.getQuery().concat(" limit ").concat(task.getResultParameterValue("page_size")));
//            }
//            if (task.getResultParameterValue("page_offset") != null) {
//                //
//                // Off set is in pages here but postgres needs row number. First row = 0. page_offset 1 => first page so row 0
//                //
//                int offset = (Integer.parseInt(task.getResultParameterValue("page_offset")));
//                if (offset < 0) {
//                    offset = 0;
//                }
//                offset *= page_size;
//                result.setQuery(result.getQuery().concat(" offset ").concat("" + offset));
//            }
        }
        
        result.setQuery(query);
        // System.out.println("Constructed query :" + result.getQuery());
        return result;
    }

    /**
     * Prepare the child resource query. We do not have parameters but the names
     * of the link columns are known. So we can use those for parameter
     * instances.
     *
     * @param resource
     * @return
     */
    private OracleQuery buildQueryForChildTableResource(AdapterPostgresResource resource) {
        OracleQuery result = new OracleQuery(resource);

        //
        // It will be a simple strait forward query, no complexity.
        //
        String query = resource.getBaseQuery();
        String where = " where ";

        for (String c : resource.getLinkColumns()) {
            query = query.concat(String.format("%s %s = ?", where, c));
            where = " and ";
        }
        result.setQuery(query);

        //
        // Now the children
        //
        for (int i = 0; i < resource.getChildren().size(); i++) {
            result.addPostgresQuery(buildQueryForChildTableResource(resource.getChildren().get(i)));
        }
        return result;
    }

    /**
     * Create a valid sql expression for different types.
     *
     * @param columnName
     * @param type -> data type
     * @param operator -> sql operator
     * @param value -> Needed if it is a date and based on duck typing create
     * the right expression.
     * @return
     */
    private String createExpression(String columnName, String type, String operator, String value, boolean matchExact) {

        //
        // Dates need to be handled especially, all others are simple straight forward.
        //
        //
        // In most cases operator is "=" but with fromdat and todate we need different ones.
        if (type.equalsIgnoreCase("integer")) {
            return columnName.concat(" ").concat(operator).concat(" ").concat("? ");
        } else if (type.equalsIgnoreCase("float")) {
            return columnName.concat(" ").concat(operator).concat(" ").concat("? ");
        } else if (type.equalsIgnoreCase("string")) {
            if (!matchExact) {
                return "lower(".concat(columnName).concat(") ").concat(" like ").concat(" ").concat("lower(?) ");
            } else {
                return columnName.concat(" = ").concat("?");
            }
        } else if (type.equalsIgnoreCase("year")) {
            return "extract ( year from ".concat(columnName).concat(") ").concat(operator).concat(" ? ");
        } else if (type.equalsIgnoreCase("date")) {
            return columnName.concat(" ").concat(operator).concat(" to_date(?,'yyyymmdd') ");
        } else if (type.equalsIgnoreCase("partialdate")) {
            if (value.length() == 4) {
                return "extract ( year from ".concat(columnName).concat(") ").concat(operator).concat(" ? ");
            } else if (value.length() == 6) {
                return "to_char(".concat(columnName).concat(",'yyyymm') ").concat(operator).concat(" ? ");
            } else if (value.length() == 8) {
                return columnName.concat(" ").concat(operator).concat(" to_date(?,'yyyymmdd') ");
            }
        }
        throw new RuntimeException(String.format("Unable to create where SQL clause form column \"%s\", type=\"%s\", value=\"%s\"", columnName, type, value));
    }

    /**
     * Build the final SQL query based on the supplied query parameters. This
     * part is a bit complex because of the many possible situations
     *
     * <PRE>
     * 1 the resource is a postgis raster
     *    - user supplied geometry, use that with transform if needed
     *    - user supplied a field, use that from gewaspercelen
     *    - user supplied non spatial query params incorporate that as foo from
     *
     * 2 the resource has a geometry
     *    - user supplied geometry -> spatial join
     *    - user supplied (also) non spatial properties
     *
     * 3 the resource has a no geometry
     *    - user supplied geometry -> spatial join with gewaspercelen and intersect usergeom with geom and join on joincolumn
     *    - user supplied a field -> extra from gewaspercelen and join on joincolumn
     *    - user supplied (also) non spatial properties
     *
     *  Bases on Postgres implementation so adaptations needed.
     * 
     * </PRE>
     *
     * @param task
     * @return
     */
    private OracleQuery buildQueryForTableResource(ExecutorTask task) {

        java.util.Properties selectClause = new java.util.Properties(); // name -> query expression
        ArrayList<String> fromClause = new ArrayList<>();
        ArrayList<String> whereClause = new ArrayList<>();
        ArrayList<String> orderClause = new ArrayList<>();
        int geomResourceFilterIndex = 0; // Only used if geometry is supplied in the query parameters
        int filterResourceIndex = 0; // Only used if one the query parameters is not available in the base table

        OracleQuery result = new OracleQuery(task.getResource());
        String output_epsg = null;
        if (task.getResultParameterValue("output_epsg") != null) {
            output_epsg = task.getResultParameterValue("output_epsg");
        }

        AdapterTableResource resource = (AdapterTableResource) task.getResource();

        if (resource.getOrderBy() != null) {
            orderClause.addAll(Arrays.asList(resource.getOrderBy()));
        }

        for (String column : resource.getColumns()) {
            // whatif column ='*'
            selectClause.put(column, "t1.".concat(column));
        }

        String geomValue = task.getQueryParameterValue("geometry");
        String epsgValue = task.getQueryParameterValue("epsg");

        if (resource.isRasterResource()) {
            throw new RuntimeException("Support for ORACLE rasters is not yet implemented");
        } 
//       else 


//BISUSER >select t1.PLAATS as PLAATS    , t1.GVG as GVG
//  2    from pfb_alg  t1
//  3       ,  (select st_geometry.st_geomfromtext('POINT(100000 400000)',28992) geom from dual)   t2
//  4   where  t2.geom.st_intersects(st_geometry(t1.shape)) = 1
//  5     and  t2.geom.st_touches(st_geometry(t1.shape)) =0;
// where  t2.geom.st_intersects(st_geometry(t1.shape)) = 1

// Via sdo_geometry

//select sdo_geom.sdo_length(SDO_GEOM.SDO_INTERSECTION(t1.shape, t2.geom,0.001),0.001) as perimeter 
//     , sdo_geom.sdo_area(SDO_GEOM.SDO_INTERSECTION(t1.shape, t2.geom,0.001),0.001) as area  
//  from (select sdo_geometry('POLYGON((168375 469700, 168375 473700, 172375 473700, 172375 469700, 168375 469700))',null) geom from dual)  t2 
//     , pfb_alg t1
// where sdo_RELATE(t1.shape, t2.geom,'mask=anyinteract') = 'TRUE'

        if (task.getQueryParameterValue("geometry") != null) {
            fromClause.add(resource.getTableName());
            if (resource.isGeometryResource()) {
                if (epsgValue != null) {
                    // fromClause.add(" (select st_transform(st_geomfromewkt(?),28992) geom) ");
                    fromClause.add(" (select sdo_geometry(?,?) geom from dual) ");
                    result.addParamValues(geomValue);
                    result.addParamValues(epsgValue);
                } else {
                    fromClause.add(" (select sdo_geometry(?,null) geom from dual) ");
                    result.addParamValues(geomValue);
                    //result.addParamValues(null); // TODO epsgValue
                }
                whereClause.add(String.format(" sdo_relate(t1.shape, t2.geom,'mask=anyinteract')='TRUE'", geomResourceFilterIndex));
                if (task.getQueryParameterValue("noclip") == null) {
                    selectClause.put("area", "sdo_geom.sdo_area(SDO_GEOM.SDO_INTERSECTION(t1.shape, t2.geom,0.001),0.001)");// get_sdo_geom
                    selectClause.put("perimeter", "sdo_geom.sdo_length(SDO_GEOM.SDO_INTERSECTION(t1.shape, t2.geom,0.001),0.001)");
                } else {
                    selectClause.put("area", "sdo_geom.sdo_area(t1.shape,0.001)");
                    selectClause.put("perimeter", "sdo_geom.sdo_length(t1.shape,0.001)");
                }
//                if (!task.returnNoGeom()) {
//                    if (output_epsg != null) {
//                        if (task.getQueryParameterValue("noclip") == null) {
//                            selectClause.put("geom", "(st_transform(st_intersection(t1.geom,t2.geom)," + output_epsg + "),6) ");
//                        } else {
//                            selectClause.put("geom", "(st_transform(t1.geom," + output_epsg + "),6) ");
//                        }
//                    } else {
//                        if (task.getQueryParameterValue("noclip") == null) {
//                            selectClause.put("geom", "SDO_UTIL.TO_GEOJSON (st_intersection(t1.geom,t2.geom),3)");
//                        } else {
//                            selectClause.put("geom", "SDO_UTIL.TO_GEOJSON (t1.geom,3)");
//                        }
//                    }
//                }
//                orderClause.clear();
//                orderClause.add("area desc");
            } else {
                // 
                // because we are here, a geometry is valid so we have to be able to process this.
                // create a new from foo with query add gewaspercelen and add a spatial join an join fields to the resource
                //

                if (epsgValue != null) {
                    fromClause.add(" (select st_transform(st_geomfromewkt(?),28992) geom) ");
                    result.addParamValues("SRID=" + task.getQueryParameterValue("epsg", null) + ";" + geomValue);
                } else {
                    fromClause.add(" (select st_geomfromewkt(?) geom) ");
                    result.addParamValues("SRID=28992;" + geomValue);
                }
                fromClause.add("gewaspercelen");
                // Link 1st resource to gewaspercelen on fieldid.
                whereClause.add(String.format("t%d.fieldid=t%d.fieldid", 1, fromClause.size())); // YR sep 2019 Or distinct (lareg queries ???)
                task.removeQueryParameter("geometry");
                task.removeQueryParameter("epsg");
                whereClause.add(String.format(" st_intersects(t%d.geom,t%d.geom)", fromClause.size(), fromClause.size() - 1));
                whereClause.add(String.format(" not st_touches(t%d.geom,t%d.geom)", fromClause.size(), fromClause.size() - 1));
            }
            geomResourceFilterIndex = fromClause.size();
        } else {
            fromClause.add(resource.getTableName());

            if (resource.getGeometryColumn() != null) {
                selectClause.put("area", "sdo_geom.sdo_area(t1.shape,0.001)");
                selectClause.put("perimeter", "sdo_geom.sdo_length(t1.shape,0.001)");
                if (!task.returnNoGeom()) {
                    if (output_epsg != null) {
                        selectClause.put("geom", "SDO_UTIL.TO_GEOJSON (st_transform(t1.geom," + output_epsg + "),6) ");
                    } else {
                        selectClause.put("geom", "' {\"type\": \"Point\",\"coordinates\": ['||x||','||y||']}'"); // SDO_UTIL.TO_GEOJSON nog niet in deze versie, specifiek voor pfb_alg
                    }
                }
                if (orderClause.isEmpty()) {
                    orderClause.add("area desc"); // avoid overwriting defined order by
                }
            }
        }

        //
        // In some cases multiple tables are uses (e.g. api/v2/rest/ndvi). gewaspercelen is used for spatial query and gewaspercelen_ndvi for date query.
        // So we need to link them. This is doen 20 lines up.
        //
        //
        // Process all parameters. Ignore geometry and epsg
        //
        Iterator<Map.Entry<Object, Object>> it = task.getQueryParameters().entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Object, Object> next = it.next();
            String keyName = (String) next.getKey();
            Object keyValue = ((WorkerParameter) next.getValue()).getValue();
            boolean matchExact = ((WorkerParameter) next.getValue()).isUriWorkerParameter();
            ConfigurationParameter q = task.getResource().findQueryParameter(keyName);
            AdapterTableResource filterResource = null;
            if (q.getResourceName() != null) {
                filterResource = (AdapterTableResource) AgroDataCubeRegistry.getInstance().findResource(q.getResourceName());

                if (filterResource == null) {
                    throw new RuntimeException(String.format("Unable to find resource %s, used as filter resource", q.getResourceName()));
                }
                fromClause.add(filterResource.getTableName());
//                for (String e : resource.getExtraCcolumns()) {
//                    selectClause.put(e, String.format("t%d.%s", fromClause.size(), e));
//                }
                filterResourceIndex = fromClause.size();
                if (filterResource.getGeometryColumn() != null) {
//                    System.out.println("\tJointype spatial");
                    if (resource.isGeometryResource()) {
                        whereClause.add(String.format(" st_intersects(t1.geom,t%d.geom)", filterResourceIndex));
                        whereClause.add(String.format(" not st_touches(t1.geom,t%d.geom)", filterResourceIndex));
                        whereClause.add(createExpression("t" + filterResourceIndex + ".".concat(q.getColumnName()), q.getDataType(), q.getOperator(q.getName()), (String) keyValue, matchExact));
                        result.addParamValues(q.prepareValue((String) keyValue, matchExact));
                        if (!task.returnNoGeom()) {
                            if (output_epsg == null) {
                                if (task.getQueryParameterValue("noclip") == null) {
                                    selectClause.put("geom", String.format("SDO_UTIL.TO_GEOJSON (st_intersection(t1.geom,t%d.geom),3) ", filterResourceIndex)); //todo output epsg
                                } else {
                                    selectClause.put("geom", String.format("SDO_UTIL.TO_GEOJSON (t1.geom,3) ")); //todo output epsg
                                }
                            } else {
                                if (task.getQueryParameterValue("noclip") == null) {
                                    selectClause.put("geom", String.format("SDO_UTIL.TO_GEOJSON (st_transform(st_intersection(t1.geom,t%d.geom)," + output_epsg + "),6) ", filterResourceIndex)); //todo output epsg
                                } else {
                                    selectClause.put("geom", String.format("SDO_UTIL.TO_GEOJSON (t1.geom,3) ")); //todo output epsg
                                }
                            }
                        }
                        if (task.getQueryParameterValue("noclip") == null) {
                            selectClause.put("area", String.format("sdo_geom.sdo_area(st_intersection(t1.geom,t%d.geom))", filterResourceIndex));
                        } else {
                            selectClause.put("area", String.format("sdo_geom.sdo_area(t1.geom)"));
                        }
                    } else {
                        whereClause.add("t" + filterResourceIndex + ".".concat(q.getName().concat("=").concat(keyValue.toString())));
                        whereClause.add(String.format("t1.%s=t%d.%s", q.getJoinColumn(), filterResourceIndex, q.getJoinColumn())); // todo Join column sometimes not fieldid
                    }
                } else {
                    whereClause.add("t" + filterResourceIndex + ".".concat(q.getName().concat("=").concat(keyValue.toString())));
                    whereClause.add(String.format("t1.%s=t%d.%s", q.getJoinColumn(), filterResourceIndex, q.getJoinColumn())); // todo Join column sometimes not fieldid
//                    System.out.println("\tJointype equi ");
                }
            } else {
                //
                // No filter resource so either a local column or a geometry.
                //

                if ("geometry".equalsIgnoreCase(keyName)) {
                    // No action needed
                } else if ("epsg".equalsIgnoreCase(keyName)) {
                    // no action needed
                } else {
                    if (q.getColumnName() != null) { // noclip e.g. has no column
                        whereClause.add(createExpression("t1.".concat(q.getColumnName()), q.getDataType(), q.getOperator(q.getName()), (String) keyValue, matchExact));
                        result.addParamValues(q.prepareValue((String) keyValue, matchExact));
                    }
                    //whereClause.add(.concat(q.getColumnName().concat("=").concat(keyValue.toString())));
                }
            }

        }
        // temp
        //whereClause.add(" rownum < 10");
        //
        // Start building the query.
        //
        String queryString = "";
        String selectKeyword = "select ";
        Iterator<Map.Entry<Object, Object>> iterator = selectClause.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> next = iterator.next();
            String keyName = (String) next.getKey();
            queryString = queryString.concat(selectKeyword).concat(selectClause.getProperty(keyName));
            if (!keyName.equalsIgnoreCase("*")) { // valid aliass
                queryString = queryString.concat(" as ").concat(keyName);
            }
            selectKeyword = "\n    , ";
        }
        String from = "\n  from ";
        int aliasNr = 1;
        for (String aaa : fromClause) {
            queryString = queryString.concat(from).concat(aaa).concat(String.format("  t%d", aliasNr));
            aliasNr++;
            from = "\n     , ";
        }
        from = "\n where ";
        for (String aaa : whereClause) {
            queryString = queryString.concat(from).concat(aaa);
            from = "\n   and ";
        }

        //
        // 27 03 2020 als page limit ignored dan niet sorteren.
        //
//        if (!task.isIgnorePageSizeLimit()) {
            from = " order by ";
            if (!task.returnJustnrOfHits()) {
                for (String aaa : orderClause) {
                    queryString = queryString.concat(from).concat(aaa);
                    from = " ,";
                }
            }
//        }
        //
        // If nrofhits is set no order by and enclose this query.
        //
        if (task.returnJustnrOfHits()) { // todo when nrofhits no children
            queryString = "select count(*) nrofhits from (".concat(queryString).concat(") as foo");
            orderClause.clear();
        } else { // if (!task.returnAllData()) {
//            if (!task.isIgnorePageSizeLimit()) {
                int page_size = AgroDataCubeProperties.getDefaultPageSize();
                int page_offset = 0;
                if (task.getResultParameterValue("page_size") != null) {
                    page_size = Integer.parseInt(task.getResultParameterValue("page_size"));
                    if (page_size > AgroDataCubeProperties.getPageSizeLimit()) {
                        throw new InvalidParameterException(String.format("Value %d for page_size exceeds limit %d", page_size, AgroDataCubeProperties.getPageSizeLimit()));
                    }
                }
                if (task.getResultParameterValue("page_offset") != null) {
                    page_offset = Integer.parseInt(task.getResultParameterValue("page_offset"));
                    if (page_offset < 0) {
                        throw new InvalidParameterException(String.format("Value %d for page_offset must be >= 0", page_offset));
                    }
                    page_offset *= page_size; // Change from pages to rownrs
                }
                // todo orderby if overlay then area first
                //queryString = queryString.concat(String.format(" offset %d limit %d", page_offset, page_size));
//            }
        }

        result.setQuery(queryString);

        //
        // Add the child optional queries. When nrofhits the linkcolumns are not present and they are unwanted.
        //
        if (!task.returnJustnrOfHits()) {
            for (AdapterPostgresResource r
                    : resource.getChildren()) {
                AdapterTableResource theRresource = (AdapterTableResource) r;
                result.addPostgresQuery(buildQueryForChildTableResource(theRresource)); // TODO Cascade into children.
            }
        }

        return result;
    }

}
