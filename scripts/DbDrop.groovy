target(dbDrop: "The description of the script goes here!") {
	def cmd = 'mysql -u root --password=root -e "drop database warehouse_dev; create database warehouse_dev default character set utf8; " '.execute(); 
	cmd.waitFor()
	println "Exit Value ${cmd.exitValue()}"
	if (cmd.exitValue()) {
		println( "Error: " + cmd.err.toString())
		println( " : " + cmd.class.name);
	}

}

setDefaultTarget("dbDrop")
