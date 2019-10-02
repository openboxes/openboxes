/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
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
