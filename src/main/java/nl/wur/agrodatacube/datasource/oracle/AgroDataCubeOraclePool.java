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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import oracle.jdbc.pool.OracleDataSource;

/**
 * Create a class that implements the methods in this interface and add to the
 * war.
 *
 * @author Rande001
 */
public class AgroDataCubeOraclePool {

    private static OracleDataSource pool = null;
    private static String username;
    private static String password;
    private static String host;
    private static String database;
    private static int port;

    public static synchronized Connection getConnection() throws SQLException {
        if (pool == null) {
            pool = new OracleDataSource();
            //System.out.println("Connecting to "+"jdbc:oracle:thin:@".concat(host).concat(":" + port).concat("/").concat(database));
            pool.setURL("jdbc:oracle:thin:@".concat(host).concat(":" + port).concat("/").concat(database));
            // jdbc:oracle:thin:@scomp1270:1522/geodb_ganzen
            pool.setUser(username);
            pool.setPassword(password);
            Properties cacheProps = new Properties();
            cacheProps.setProperty("MinLimit", "1");
            cacheProps.setProperty("MaxLimit", "5");
            cacheProps.setProperty("InitialLimit", "1");
            cacheProps.setProperty("ConnectionWaitTimeout", "5");
            cacheProps.setProperty("ValidateConnection", "true");
        }
        return pool.getConnection();
    }

    public AgroDataCubeOraclePool() {
//        try {
//            pool = new OracleDataSource();
//        } catch (SQLException ex) {
//            throw new RuntimeException(String.format("AgroDataCubeOraclePool : %s", ex.getMessage()));
//        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setMaxConnections(int maxConnections) {
        //todo
    }
}
