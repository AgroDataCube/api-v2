/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.resource.query;

/**
 * This class contains query parameter info.
 *
 * @author rande001
 */
public class QueryParameter {

    private static int counter = 0;
    String name;
    String type; // integer, float, date, year, string.  Is based on db type of column
    String columnName; // when null then results param like page_size,page_start etc. So non SQL parameter.
    String resourceName; // table or resource or ...
    String joinColumn;
    private int id;

    /**
     * In most cases operator is "=" only with from and todate different.
     *
     * @param columnName
     * @return
     */
    public static String getOperator(String columnName) {
        if ("fromdate".equalsIgnoreCase(columnName)) {
            return " >= ";
        } else if ("todate".equalsIgnoreCase(columnName)) {
            return " <= ";
        } else {
            return " = ";
        }
    }

    public QueryParameter() {
        id = ++counter;
    }

    public QueryParameter(String name, String type, String columnName) {
        this.name = name.toLowerCase();
        this.type = type.toLowerCase();
        this.columnName = columnName;
    }

    /**
     * See if o is valid for type.
     *
     * @param o
     * @return
     */
    public boolean valueIsValid(String o) {
        try {
            if (type.equalsIgnoreCase("integer")) {
                Integer.parseInt(o);
            } else if (type.equalsIgnoreCase("float")) {
                Double.parseDouble(o);
            } else if (type.equalsIgnoreCase("year")) {
                Integer.parseInt(o);
                if (o.length() != 4) {
                    return false;
                }
                return validatePartialDate(o);
            } else if (type.equalsIgnoreCase("partialdate")) {
                Integer.parseInt(o);
                return validatePartialDate(o);
            } else if (type.equalsIgnoreCase("date")) {
                Integer.parseInt(o);
                if (o.length() != 8) {
                    return false;
                }
                return validatePartialDate(o);
            }
            // date ddmmyyyy
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Convert the string to an object of the appropriate class. This can also
     * depend on createExpression in PostgresQueryBuilder.
     *
     * @param o
     * @return
     */
    public Object prepareValue(String o) {
        if (o == null) {
            return null;
        }

        if (type.equalsIgnoreCase("integer")) {
            return Integer.parseInt(o);
        } else if (type.equalsIgnoreCase("float")) {
            return Double.parseDouble(o);
        } else if (type.equalsIgnoreCase("string")) {
            return "%".concat(o).concat("%");
        } else if (type.equalsIgnoreCase("year")) {
            return Integer.parseInt(o);
        } else if (type.equalsIgnoreCase("partialdate")) {
            if (o.length() == 4) {
                return Integer.parseInt(o);
            } else if (o.length() == 6) {
                return o; // matched with to_char so must be char
            } else if (o.length() == 8) {
                return o;
            }

        }
        return o;
    }

    /**
     * A partial date is valid if it consist of valid values for yyyy mm and dd
     * depending on what is provided.
     *
     * @param o integer using yyyy, yyyymm, yyyymmdd format.
     * @return
     */
    private boolean validatePartialDate(String o) {
        if (o.length() == 4) {
            int i = Integer.parseInt(o);
            if (i < 1900) {
                return false;
            }
            if (i > 2100) {
                return false;
            }
        } else if (o.length() == 6) {
            int i = Integer.parseInt(o);
            int im = i % 100;
            int iy = i / 100;
            if (iy < 1900) {
                return false;
            }
            if (iy > 2100) {
                return false;
            }
            if (im < 1 | im > 12) {
                return false;
            }
        } else if (o.length() == 8) {
            int i = Integer.parseInt(o);
            int id = i % 100;
            int im = i / 100;
            int iy = im / 100;
            im = im % 100;
            if (iy < 1900) {
                return false;
            }
            if (iy > 2100) {
                return false;
            }
            if (im < 1 | im > 12) {
                return false;
            }
            // todo day
        }
        return true;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String tableName) {
        this.resourceName = tableName;
    }

    public String getJoinColumn() {
        if (joinColumn != null) {
            return joinColumn;
        }
        return columnName;
    }

    public void setJoinColumn(String joinColumn) {
        this.joinColumn = joinColumn;
    }

}
