package util

class StringUtil {
	
	public static String mask(String value, String mask) {		
		return value.replaceFirst(".*", { match -> return "".padLeft(match.length(), mask)})
	}
}
