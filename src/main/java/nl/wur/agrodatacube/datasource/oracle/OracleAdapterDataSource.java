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

import nl.wur.agrodatacube.datasource.AdapterDataSource;
import nl.wur.agrodatacube.datasource.metadata.ColumnMetadata;
import nl.wur.agrodatacube.exec.ExecutorTask;
import nl.wur.agrodatacube.properties.AgroDataCubeProperties;
//import nl.wur.agrodatacube.resource.AdapterPostgresResource;
import nl.wur.agrodatacube.result.AdapterResult;
import nl.wur.agrodatacube.result.AdapterTableResult;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import nl.wur.agrodatacube.resource.AdapterTableResource;

/**
 *
 * @author Yke
 */
public class OracleAdapterDataSource extends AdapterDataSource {

    private AgroDataCubeOraclePool pool = null;

    public OracleAdapterDataSource() {
        pool = new AgroDataCubeOraclePool();
    }

    public AdapterTableResult executeQuery(String query, ArrayList<Object> paramValues) {
        return executeQuery(new OracleQuery(query, paramValues));
    }

    /**
     *
     * @param oracleQuery
     * @param params
     * @return
     */
    public AdapterTableResult executeQuery(OracleQuery oracleQuery) {

        Connection connection = null;
        AdapterTableResult result;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            connection.setReadOnly(true);
            ps = connection.prepareStatement(oracleQuery.getQuery());//, ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(1000);
            for (int i = 0; i < oracleQuery.getParamValues().size(); i++) {
                ps.setObject(i + 1, oracleQuery.getParamValues().get(i));
            }

            result = new AdapterTableResult();
            result.setQueryString(oracleQuery.getQuery());
            rs = ps.executeQuery();
            int nrOfQueryColumns = rs.getMetaData().getColumnCount();
            int nrOfTableColumns = nrOfQueryColumns + oracleQuery.getChildren().size();
            result.setColumnCount(nrOfTableColumns);

            String[] columnNames = new String[nrOfQueryColumns];
            int i = 0;
            for (; i < nrOfQueryColumns; i++) {
                result.setColumnName(i, rs.getMetaData().getColumnName(i + 1));
                columnNames[i] = rs.getMetaData().getColumnName(i + 1);
                // todo make a metadata datasource like geometry, registration etc
            }
            ArrayList<ColumnMetadata> c = getMetadata(columnNames);
            result.addColumnMetadata(c);

            //
            // Create extra columns for childqueries.
            //
            for (int j = 0; j < oracleQuery.getChildren().size(); j++) {
                result.setColumnName(j + nrOfQueryColumns, oracleQuery.getChildren().get(j).getName());
            }

            //
            // Now scroll thru the results of the query and add the values. After that execute the subqueries.
            //
            while (rs.next()) {
                ArrayList<Object> values = new ArrayList<>();
                for (int v = 0; v < nrOfQueryColumns; v++) {
                    values.add(rs.getObject(v + 1));
                }
                // Add dummy results for childqueries

                for (int j = 0; j < oracleQuery.getChildren().size(); j++) {
                    OracleQuery child = oracleQuery.getChildren().get(j);
                    AdapterTableResource resource = (AdapterTableResource) child.getResource();
                    String[] linkColumns = resource.getLinkColumns();
                    child.clearParameterValues();
                    for (int l = 0; l < linkColumns.length; l++) {
                        child.addParamValues(rs.getObject(linkColumns[l]));
                    }

                    AdapterTableResult childResult = executeQuery(child);
                    values.add(childResult);

                    // TODO: Add  the metadata for child queries
                    result.addColumnMetadata(childResult.getColumnMetadata());
                }
                result.addRow(values);
            }
            rs.close();
            rs = null;
            connection.close();
            ps.close();
            ps = null;
            //result.setColumnCount(rs.getMetaData().get);
            //result = AdapterTableResult.fromSQLResulSet(, postgresQuery);
            result.setQueryString(oracleQuery.getQuery());
        } catch (Exception e) {
            try {
                connection.close();
            } catch (Exception q) {
                ;
            }
            try {
                ps.close();
            } catch (Exception q) {
                ;
            }
            result = new AdapterTableResult();
            result.setStatus(e.getMessage()); //Jsonizer.tojson
            result.setQueryString(oracleQuery.getQuery());
        } finally {
            try {
                if (ps != null) {
                    connection.close();
                }
            } catch (Exception q) {;
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception q) {;
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception qq) {;
            }
            try {
                connection.close();
            } catch (Exception e) {;
            }
        }
        return result;
    }

    /**
     * For analytics predefined queries are used. TODO : USe
     * AdapterQueryResources.
     *
     * @param name
     * @return
     */
    public String getPredefinedQuery(String name) {
        String result = "";
        PreparedStatement ps = null;
        ResultSet rs;
        Connection connection = null;

        try {
            connection = getConnection();
            ps = connection.prepareStatement("select query from config where lower(code) = lower(?)");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString(1);
            }
            rs.close();
            connection.close();
            if (result.length() == 0) {
                throw new RuntimeException("predefined query\"" + name.toLowerCase() + "\" not found in config tabel");
            }
        } catch (Exception e) {
            throw new RuntimeException("OracleAdapterDataSource.getPredefinedQuery : " + e.getMessage());
        } finally {
            try {
                connection.close();
            } catch (Exception qq1) {;
            }
            try {
                ps.close();
            } catch (Exception qq1) {;
            }
            try {
                connection.close();
            } catch (Exception qq1) {;
            }
        }
        return result;
    }

    /**
     * Return a connection.
     *
     * @return
     * @throws Exception
     */
    public Connection getConnection() throws Exception {
        return pool.getConnection();
    }

    /**
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

        //
        // Now build the query since that is complex we use a queryBuilder.
        //
        OracleQueryBuilder qb = new OracleQueryBuilder();
        OracleQuery q = qb.buildQuery(task);

        return executeQuery(q);
    }

    public void setUsername(String Username) {
        pool.setUsername(Username);
    }

    public void setPassword(String Password) {
        pool.setPassword(Password);
    }

    public void setHost(String Host) {
        pool.setHost(Host);
    }

    public void setPort(int Port) {
        pool.setPort(Port);
    }

    public void setPort(String port) {
        pool.setPort(Integer.parseInt(port));
    }

    public void setDatabase(String Database) {
        pool.setDatabase(Database);
    }

    public void setMaxConnections(int i) {
        pool.setMaxConnections(i);
    }

    public void setMaxConnections(String s) {
        pool.setMaxConnections(Integer.parseInt(s));
    }

    /**
     * Add username, password,db, port and host from a file. The name of the
     * properties must be lowercase. The file is the properties file loaded by
     * AgroDataCubeProperties singleton.
     * <name>.<port>=9876
     *
     * @param filename
     */
    public void addDataFromFile(String filename) {

        setUsername(AgroDataCubeProperties.getValue(getName().concat(".").concat("username")));
        setPassword(AgroDataCubeProperties.getValue(getName().concat(".").concat("password")));
        setHost(AgroDataCubeProperties.getValue(getName().concat(".").concat("host")));
        setDatabase(AgroDataCubeProperties.getValue(getName().concat(".").concat("database")));
        setPort(AgroDataCubeProperties.getValue(getName().concat(".").concat("port")));
        setMaxConnections(AgroDataCubeProperties.getValue(getName().concat(".").concat("maxconnections")));
        if (AgroDataCubeProperties.getValue(getName().concat(".").concat("containsregistrationdata")) != null) {
            setContainsRegistrationData(AgroDataCubeProperties.getValue(getName().concat(".").concat("containsregistrationdata")));
        }
    }

    /**
     * Fetch the metadata for this columns.
     *
     * @param columnName
     * @return
     */
    private ArrayList<ColumnMetadata> getMetadata(String[] columnNames) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ColumnMetadata> list = new ArrayList<ColumnMetadata>();

        String query = "select column_name, description,units,decimals from column_metadata where lower(column_name) in (";

        String komma = "";
        for (String q : columnNames) {
            query = query.concat(komma).concat("'").concat(q.trim().toLowerCase()).concat("'");
            komma = ",";
        }
        query = query.concat(") order by 1");
        try {
            ps = getConnection().prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                ColumnMetadata c = new ColumnMetadata(rs.getString(1));
                int ndec = rs.getInt(4);
                if (rs.wasNull()) {
                    ndec = 5;
                };
                c.setDecimals(ndec);
                c.setDescription(rs.getString(2));
                c.setUnits(rs.getString(3));
                list.add(c);
            }
            rs.close();
            ps.getConnection().close();
            ps.close();
            ps = null;
        } catch (Exception e) {
            try {
                rs.close();
            } catch (Exception q1) {;
            }
            try {
                ps.close();
            } catch (Exception q2) {;
            }
            list = new ArrayList<>();
            throw new RuntimeException("getMetadata : ".concat(e.getLocalizedMessage()));
        }
        return list;
    }

}
