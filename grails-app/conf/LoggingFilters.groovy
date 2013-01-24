/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
import org.springframework.web.context.request.RequestContextHolder
import org.slf4j.MDC

class LoggingFilters {
	def filters = {
		all(controller:'*', action:'*') {
			before = {
				
				String sessionId = session?.user?.username//RequestContextHolder.getRequestAttributes()?.getSessionId()
				//log.info "SessionID " + sessionId
				MDC.put('username', session?.user?.username?:"Anonymous")
				MDC.put('location', session?.warehouse?.name?:"No location")
				MDC.put('ipAddress', request?.remoteAddr?:"No IP address")
				MDC.put('requestUri', request?.queryString?:"No request URI")
				MDC.put('queryString', request?.queryString?:"No query string")
			}
			after = {
			}
			afterView = {
				MDC.remove('username')
				MDC.remove('location')
				MDC.remove('ipAddress')
				MDC.remove('requestUri')
				MDC.remove('queryString')
				//MDC.remove('params')
			}
		}
	}
}