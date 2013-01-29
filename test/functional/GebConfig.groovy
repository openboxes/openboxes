/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/ 

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.chrome.ChromeDriver

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.remote.DesiredCapabilities

//driver = {
//	def env = System.getenv()

//	if(env["headless"] == 'false'){
//		return new FirefoxDriver()
//	}
//	def capabilities = DesiredCapabilities.firefox()
//	capabilities.javascriptEnabled = true
//	return new HtmlUnitDriver(capabilities)
//}

// Use htmlunit as the default
// See: http://code.google.com/p/selenium/wiki/HtmlUnitDriver
driver = {
	//def driver = new HtmlUnitDriver()
	//driver.javascriptEnabled = true
	//driver
	def capabilities = DesiredCapabilities.firefox()
	capabilities.javascriptEnabled = true
	return new HtmlUnitDriver(capabilities)
}

environments {
	// run as “grails -Dgeb.env=chrome test-app”
	// See: http://code.google.com/p/selenium/wiki/ChromeDriver
	chrome {
		driver = { new ChromeDriver() }
	}
	// run as “grails -Dgeb.env=firefox test-app”
	// See: http://code.google.com/p/selenium/wiki/FirefoxDriver
	firefox {
		driver = { new FirefoxDriver() }
	}

}


//waiting {
//	timeout = 25
//	retryInterval = 0.5
//}

reportsDir = "target/geb-reports"
autoClearCookies = false

