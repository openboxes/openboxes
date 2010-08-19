package org.pih.warehouse.data

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
	
}