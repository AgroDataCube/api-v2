/*
* Copyright 2019 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */



package nl.wur.agrodatacube.servlet;

/**
 *
 * @author rande001
 */
public class WorkerParameter {
    private Object              value;
    private WorkerParameterType type;

    public enum WorkerParameterType { URI, QUERY, SYSTEM, AUTHENTICATION }

    ;
    public WorkerParameter(Object value, WorkerParameterType type) {
        this.value = value;
        this.type  = type;
    }

    public boolean isQueryWorkerParameter() {
        return type == WorkerParameterType.QUERY;
    }

    public boolean isUriWorkerParameter() {
        return type == WorkerParameterType.URI;
    }

    public Object getValue() {
        return value;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
