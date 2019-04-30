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

/**
 * In some early development stages it seemed when too many connections were required the system locked itself (deadlock ?). So we use this class to test.
 * @author rande001
 */
public class DataSourceTester {

    public static void main(String[] args) {
        PostgresAdapterDataSource d = new PostgresAdapterDataSource();
        d.setDatabase("agrodatacube");
        d.setHost("localhost");
        d.setPort(5433);
        d.setUsername("adctest");
        d.setPassword("adctest");
        d.setMaxConnections(2);

        for (int i = 0; i < 10;i++) {
            Thread t = new Tester(d, i);
            t.start();
        }
    }
}

class Tester extends Thread {

    PostgresAdapterDataSource dataSource;
    int nr;
    int times = 1000; // repeat 1000 times (each thread).
    Connection connection;

    public Tester(PostgresAdapterDataSource dataSource, int nr) {
        this.dataSource = dataSource;
        this.nr = nr;
    }

    @Override
    public void run() {
        for (int i = 0; i < times; i++) {
            try {
                //
                // Request a connection 
                //

                System.out.println(String.format("Threadnr %d: requests connection", nr));
                connection = dataSource.getConnection();
                System.out.println(String.format("Threadnr %d: has connection", nr));

                //
                // Keep it for randoms ms
                //
                long wait = (long) (Math.random() * 5000);
                System.out.println(String.format("Threadnr %d: waits %d ms", nr, wait));
                Thread.sleep(wait);
                System.out.println(String.format("Threadnr %d: woke up", nr));
                //
                // Return it.
                //

                System.out.println(String.format("Threadnr %d: returning connection to pool", nr));
                connection.close();
                connection=null;
                System.out.println(String.format("Threadnr %d: returned connection to pool", nr));

            } catch (Exception e) {
                System.out.println(String.format("Threadnr %d: %s", nr, e.getLocalizedMessage()));
            }
        }
    }

}
