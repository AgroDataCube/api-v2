/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.datasource.postgres;

import java.sql.Connection;
import java.sql.SQLException;
import org.postgresql.ds.PGPoolingDataSource;

/**
 * Create a class that implements the methods in this interface and add to the
 * war.
 *
 * @author Rande001
 */
public class AgroDataCubePostgresPool {

    private PGPoolingDataSource pool = null;

    public AgroDataCubePostgresPool() {
        pool = new PGPoolingDataSource();
        // pool.setReceiveBufferSize(port);
        pool.setApplicationName("AgroDataCube V 2.1 November 2020");
    }

    public Connection getConnection() {
        try {
            synchronized (this) {
                Connection con = pool.getConnection();
                con.setReadOnly(true);               
                return con;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to get db connection from pool (" + ex.getMessage() + ")");
        }
    }

//    public static void setInstance(AgroDataCubePostgresPool instance) {
//        AgroDataCubePostgresPool.instance = instance;
//    }
//
    public void setUsername(String username) {
        pool.setUser(username);
    }

    public void setPassword(String password) {
        pool.setPassword(password);
    }

    public void setHost(String host) {
        pool.setServerName(host);
    }

    public void setPort(int port) {
        pool.setPortNumber(port);
    }

    public void setDatabase(String database) {
        pool.setDatabaseName(database);
    }

    public void setMaxConnections(int maxConnections) {
        pool.setMaxConnections(maxConnections);
    }
}
