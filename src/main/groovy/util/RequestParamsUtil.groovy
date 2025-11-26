package util

/**
 * Utility methods for processing API request and query parameters.
 */
class RequestParamsUtil {

    /**
     * Binds a multi-value query param of the format ?x=1,2,3 or ?x=1&x=2&x=3 (the latter is preferred)
     * to a list of strings.
     */
    static List<String> asList(Object param) {
        switch (param.class) {
            case null:
                return null
            // Grails will automatically bind query params to an array if they're in the format ?x=1&x=2&x=3
            // so in that case all we need to do is convert to a list
            case List:
            case Object[]:
                return param as List<String>
            // If we're given query params in the format ?x=1,2,3 then we need to process them ourselves
            case String:
                return StringUtil.split(param)
            default:
                throw new IllegalArgumentException("Invalid type in request param: ${param.class}")
        }
    }
}
