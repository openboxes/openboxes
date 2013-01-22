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
				MDC.put('sessionId', session?.user?.username?:"anonymous")
				MDC.put('remoteAddr', request?.getRemoteAddr()?:"unknown")
				MDC.put('forwardedFor', request?.getHeader("X-Forward-For")?:"unknown")
				MDC.put('clientIp', request?.getHeader("Client-IP")?:"unknown")
			}
			after = {
			}
			afterView = {
				MDC.remove('sessionId')
				MDC.remove('remoteAddr')
				MDC.remove('forwardedFor')
				MDC.remove('clientIp')
			}
		}
	}
}