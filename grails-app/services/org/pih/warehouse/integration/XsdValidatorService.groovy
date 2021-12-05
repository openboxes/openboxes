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
import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class XsdValidatorService {

    def namespaceMap = [
            'execution': 'http://www.etrucknow.com/edi/carrier/trip_execution/v1',
            'Acceptance_Status': 'http://www.etrucknow.com/edi/carrier/acceptance_status/v1',
            'Trip':  'http://www.etrucknow.com/edi/carrier/trip_notification/v1',
            'Order': 'http://www.etrucknow.com/edi/business_partner/order_create/v1',
            'DocumentUpload': 'http://www.etrucknow.com/edi/business_partner/document_upload/v1'
    ]

    boolean validateXml(String xmlContents) {
        String rootElementName = getXsdType(xmlContents)
        String xsdFileName = "xsd/${rootElementName}.xsd"
        XmlXsdValidator.validateXmlSchema(xsdFileName, xmlContents)
        return true
    }

    def getXsdType(String xmlFileContents) {
        def xmlRootNode = new XmlSlurper().parseText(xmlFileContents)
        return xmlRootNode.name()
    }

    def resolveEmptyNamespace (String path) {
        File xmlFile = new File(path)
        //Parse XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
        factory.setNamespaceAware(true)
        DocumentBuilder builder = factory.newDocumentBuilder()
        Document document = builder.parse(new FileInputStream(xmlFile))

        Node root = document.getFirstChild()
        NamedNodeMap attr = root.getAttributes()
        Node node = attr.getNamedItem("xmlns")

        if(node.nodeValue == "" ) {
            def rootNodeName = root.getNodeName()
            node.setTextContent(namespaceMap.getAt(rootNodeName))
            // write to xml
            TransformerFactory tf = TransformerFactory.newInstance()
            Transformer transformer = tf.newTransformer()
            DOMSource src = new DOMSource(document)
            StreamResult res = new StreamResult(new File(path))
            transformer.transform(src, res)
        }
    }
}
