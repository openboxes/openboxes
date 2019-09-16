/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core

import org.pih.warehouse.util.LocalizationUtil;


class LocalizationUtilTest {

	void testGetLocalizedString() {
	
		Locale locale = new Locale("en","US")
	
		assert LocalizationUtil.getLocalizedString("Default Value",locale) == "Default Value"
		
		assert LocalizationUtil.getLocalizedString("Default Value|fr:French Value|es:Spanish Value",locale) == "Default Value"
		
		locale = new Locale("fr")
		
		assert LocalizationUtil.getLocalizedString("Default Value|fr:French Value|es:Spanish Value", locale) == "French Value"
		
		locale = new Locale("es")
		
		assert LocalizationUtil.getLocalizedString("Default Value|fr:French Value|es:Spanish Value", locale) == "Spanish Value"
					
	}	

	void testGetDefaultString() {		
		assert LocalizationUtil.getDefaultString("Default Value|fr:French Value|es:Spanish Value") == "Default Value"
	}
}


