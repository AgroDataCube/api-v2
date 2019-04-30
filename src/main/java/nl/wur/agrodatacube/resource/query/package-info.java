/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */


/**
 * This package contains the definitions of parameters. The types known are
 * - integer        : valid integer accoridng to Integer.parseInt()
 * - float          : valid integer accoridng to double.parseDouble()
 * - year           : yyyy
 * - date           : yyyymmdd format
 * - partialdate    : one of yyyy, yyyymm, yyyymmdd
 * - string         : all accepted
 * 
 * Each of these has an own validator.
 */
package nl.wur.agrodatacube.resource.query;
