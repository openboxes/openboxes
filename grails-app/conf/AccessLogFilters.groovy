import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class AccessLogFilters {

	private static final Log accessLog = LogFactory.getLog('accessLog')

	def filters = {
		all(controller:'*', action:'*') {
			before = {
				accessLog.info("$controllerName:$actionName")
			}
		}
	}
}