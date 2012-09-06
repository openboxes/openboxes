import org.springframework.web.context.request.RequestContextHolder
import org.slf4j.MDC

class LoggingFilters {
	def filters = {
		all(controller:'*', action:'*') {
			before = {
				String sessionId = session?.user?.username//RequestContextHolder.getRequestAttributes()?.getSessionId()
				//log.info "SessionID " + sessionId
				MDC.put('sessionId', session?.user?.username?:"anonymous")
				MDC.put('ipAddress', request.getRemoteAddr())
				//request.getRemoteAddr()
				//request.getHeader("X-Forwarded-For")
				//request.getHeader("Client-IP")
			}
			after = {
			}
			afterView = {
				MDC.remove('sessionId')
				MDC.remove('ipAddress')
			}
		}
	}
}