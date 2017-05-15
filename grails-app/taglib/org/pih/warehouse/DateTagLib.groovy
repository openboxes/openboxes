/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse

// import java.text.SimpleDateFormat;
// import java.util.Date;

import com.ocpsoft.pretty.time.PrettyTime
import groovy.time.TimeDuration

class DateTagLib {

    static namespace = "g"

	def formatDate = { attrs, body ->
        def formatTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.FormatTagLib')

        def today = new Date()
        if (!attrs.format) {
            if (attrs.date >= today - 1) {
                attrs.format = "hh:mma z"
            } else if (attrs.date >= today - 30) {
                attrs.format = "MMM dd hh:mma z"
            } else if (attrs.date >= today - 365) {
                attrs.format = "MMM dd yyyy"
            }
        }
        out << formatTagLib.formatDate.call(attrs)
		//out << g.dateFormat([date:null])
	}

	def expirationDate = { attrs, body ->
        out << g.render(template: '/taglib/expirationDate', model: [attrs:attrs])
	}

	def relativeDate = { attrs, body ->
		
		Date now = new Date();
		Date date = attrs.date;
		
		if (date) { 
			def days = date - now;
			
			if (days == 0) { 
				out << "today";
			}
			else if (days > 0) { 
				out << "in ${days} days";
			} 
			else if (days < 0) { 
				out << "${-days} days ago"
			}
		}
	}

	def relativeTime = { attrs, body ->
		TimeDuration timeDuration = attrs.timeDuration;
		if (timeDuration) {
			if (timeDuration.years > 0) {
				out << "${timeDuration.years} years, ${timeDuration.days} days"
			}
			else if (timeDuration.days > 0) {
				out << "${timeDuration.days} days"
			}
			else if (timeDuration.hours > 0) {
				out << "${timeDuration.hours} hours"
			}
			else if (timeDuration.minutes > 0) {
				out << "${timeDuration.minutes} minutes"
			}
			else if (timeDuration.seconds > 0) {
				out << "${timeDuration.seconds} seconds"
			}
			else {
				out << "<span class='fade'>none</span>";
			}

		}
		else {
			out << "<span class='fade'>none</span>";
		}
	}

	def prettyDateFormat = { attrs, body ->
		def date = (attrs.date)?:new Date();
		def p = new PrettyTime();
		
		def now = new Date() 
		if (now - date < 1) { 
			out << "${g.message(code:'default.today.label')}"
		}
		else { 
			out << 	p.format(date);
		}
	}
		
}
