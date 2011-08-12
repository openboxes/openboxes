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
}
