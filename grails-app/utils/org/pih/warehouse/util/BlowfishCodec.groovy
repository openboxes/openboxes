package org.pih.warehouse.util

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

class BlowfishCodec {

	static encode = { String target ->
		def cipher = getCipher(Cipher.ENCRYPT_MODE)
		return cipher.doFinal(target.bytes).encodeBase64()
	}
	
	static decode = { String target ->
		def cipher = getCipher(Cipher.DECRYPT_MODE)
		return new String(cipher.doFinal(target.decodeBase64()))
	}
	
	private static getCipher(mode) {
		def keySpec = new PBEKeySpec(getPassphrase())
		def cipher = Cipher.getInstance("Blowfish")
		def keyFactory = SecretKeyFactory.getInstance("Blowfish")
		cipher.init(mode, keyFactory.generateSecret(keySpec))
		return cipher
	}
	
	
	/**
	* The key used to create the cipher.  This is not the same as the password
	* that we want to encrypt/decrypt.  That's specified in the call argument(s).
	*
	* TODO This should be pulled from a properties file to make this more secure.
	* @return
	*/
	private static getPassphrase() { 
		"secret12".getBytes("UTF-8")
	}
	
	static void main(args) {
		if(args) {
			println encode(args[0])
		}
	}
}
