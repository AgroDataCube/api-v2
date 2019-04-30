/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.exec;


/**
 *
 * @author rande001
 */

public class Executor {

//    /**
//     * Execute a query that returns a list objects.
//     *
//     * @param query
//     * @return
//     * @throws Exception
//     */
//    public static String execute(String query) throws Exception {
//        AdapterTableResult result = new PostgresAdapterDataSource().executeQuery(query + " LIMIT " + Paging.DEFAULT_PAGE_LIMIT + " OFFSET 0", new ArrayList<>());
//        return new AdapterTableResultGeoJsonFormatter().format(result);
//
//    }


//    public static ExecutorResult executeAnalyticsTask(AnalyticsTask t) throws Exception {
//        
//        //
//        // fetch query
//        //
//        String query = new PostgresAdapterDataSource().getPredefinedQuery(t.getName());
//        ArrayList<Object> params = new ArrayList<>();
//        params.add(t.getEwktGeometry28992());
//        AdapterTableResult r = new PostgresAdapterDataSource().executeQuery(query, params);
//        return new ExecutorResult(t.getFormatter().format(r), 0.0); // TODO Formatter
//    }
}
