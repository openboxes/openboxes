package org.pih.warehouse.util

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import sun.misc.BASE64Decoder;


class Base64Codec {
		
	static decode = { String target ->
		return new String(target.decodeBase64())
	}
	
	static encode = { String target ->
		return (target.bytes).encodeBase64()
	}
	
	static void main(args) {
		String encodedPassword = encode("secret") 
		println "encoded: " + encodedPassword
		String decodedPassword = decode(encodedPassword);
		println "decoded: " + decodedPassword
	}
}
