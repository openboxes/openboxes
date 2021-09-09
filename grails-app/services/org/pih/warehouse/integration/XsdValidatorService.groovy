/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.integration

import org.pih.warehouse.integration.xml.XmlXsdValidator

class XsdValidatorService {

    private String XSD_PATH = "src/java/org/pih/warehouse/integration/xml/"

    boolean validateXml(def xmlFileContents) {
        String VALIDATOR_XSD = ""
        String rootNodeName = getXsdType(xmlFileContents)

        switch (rootNodeName){
            case "AcceptanceStatus" :
                VALIDATOR_XSD = XSD_PATH + "acceptancestatus/acceptance_status_v1.xsd"
                break
            case "Execution" :
                VALIDATOR_XSD = XSD_PATH + "execution/order_execution.xsd"
                break
            case "Order" :
                VALIDATOR_XSD = XSD_PATH + "order/order_create_request.xsd"
                break
            case "DocumentUpload" :
                VALIDATOR_XSD = XSD_PATH + "pod/document_upload.xsd"
                break
            case "Trip" :
                VALIDATOR_XSD = XSD_PATH + "trip/trip_notification_v1.xsd"
                break
            default:
                VALIDATOR_XSD = ""
                return false
        }

        //validate
        return XmlXsdValidator.validateXmlSchema(VALIDATOR_XSD, xmlFileContents)
    }

    def getXsdType(String xmlFileContents) {
        def xmlRootNode = new XmlSlurper().parseText(xmlFileContents)
        return xmlRootNode.name()
    }
}
