/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */



package nl.wur.agrodatacube.servlet.filter;

/**
 *
 * @author Rande001
 */
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class CorsFilter implements ContainerResponseFilter {
    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        response.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHttpHeaders()
                .add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, token");

//      response.getHttpHeaders().add("Access-Control-Allow-Credentials","true");
        response.getHttpHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

//      response.getHttpHeaders().add("Access-Control-Allow-Methods",
//          "GET, POST, PUT, DELETE, OPTIONS, HEAD");

        return response;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
