/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.data

import org.apache.commons.lang.StringEscapeUtils

class DataService {
	
	static transactional = true
	
	def importData() { 
		def sql = Sql.newInstance("jdbc:mysql://localhost:3306/mydb", "user", "pswd", "com.mysql.jdbc.Driver")
		def people = sql.dataSet("PERSON")
		new File("users.csv").splitEachLine(",") {fields ->
			people.add(
				first_name: fields[0],
				last_name: fields[1],
				email: fields[2]
			)
		}
	}

	
	def exportData() { 
		def sql = Sql.newInstance("jdbc:mysql://localhost:3306/mydb", "user", "pswd", "com.mysql.jdbc.Driver")
		def people = sql.dataSet("PERSON")
		
		people.each { 
			log.info it;
		}
		
	}

    String generateCsv(csvrows) {
        def sw = new StringWriter()
        if (csvrows) {
            def columnHeaders = csvrows[0].keySet().collect { value -> StringEscapeUtils.escapeCsv(value) }
            sw.append(columnHeaders.join(",")).append("\n")
            csvrows.each { row ->
                def values = row.values().collect { value ->
                    if (value?.toString()?.isNumber()) {
                        value
                    }
                    else {
                        //'"' + value.toString().replace('"','""') + '"'
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                }
                sw.append(values.join(","))
                sw.append("\n")
            }
        }
        return sw.toString()
    }
	
}