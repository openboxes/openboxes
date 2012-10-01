/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core;

import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.bind.JAXBException;

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.pdf.PdfConversion;
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.jaxb.Context;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Document;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
import org.pih.warehouse.shipping.ReferenceNumber;
import org.pih.warehouse.shipping.Shipment;

class FileService {
	boolean transactional = false
	
	
	public File findFile(String filePath){
		def file
		def appContext = ApplicationHolder.application.parentContext
		def archiveDirectory = filePath
		if (ApplicationHolder.application.isWarDeployed()){
			//archiveDirectory = "${File.separator}WEB-INF${File.separator}grails-app${File.separator}conf${File.separator}${filePath}"			
			archiveDirectory = "classpath:$filePath";
			file = appContext.getResource(archiveDirectory)?.getFile()
		} 
		else {
			archiveDirectory = "grails-app${File.separator}conf${File.separator}${filePath}"
			file = new File(archiveDirectory)
		}
		return file
	}
	
	
	/**
	 * 
	 * @param shipmentInstance
	 * @return
	 */
	File generateLetterAsDocx(Shipment shipmentInstance) { 
		// Save document to temporary file
		WordprocessingMLPackage wordMLPackage = generateLetter(shipmentInstance);
		File tempFile = File.createTempFile(shipmentInstance?.name + " - Certificate of Donation", ".docx")
		wordMLPackage.save(tempFile)
		return tempFile;
	}
	
	/**
	 * 
	 * @param shipmentInstance
	 * @return
	 */
	void generateLetterAsPdf(Shipment shipmentInstance, OutputStream outputStream) { 
		WordprocessingMLPackage wordMLPackage = generateLetter(shipmentInstance);
		convertToPdf(wordMLPackage, outputStream);				
	}
	
	
	/**
	 * Generate the 'Certificate of Donation' letter from a template.
	 * 
	 * @param shipmentInstance
	 * @return
	 */
	WordprocessingMLPackage generateLetter(Shipment shipmentInstance) { 
		
		File template = findFile("templates/cod-pl-template.docx")
		if (!template) {
			throw new FileNotFoundException("templates/cod-pl-template.docx");
		}
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(template);

		// 2. Fetch the document part
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		Document wmlDocumentEl = (Document) documentPart.getJaxbElement();

		//xml --> string
		def xml = XmlUtils.marshaltoString(wmlDocumentEl, true);
		def mappings = new HashMap<String, String>();
		
		def formatter = new SimpleDateFormat("MMM dd, yyyy");
		def date = formatter.format(shipmentInstance.getExpectedShippingDate());
		mappings.put("date", date);		

		String subtitle = "";				
		if ("Sea".equals(shipmentInstance?.shipmentType?.name)) { 
			ReferenceNumber containerNumber = shipmentInstance.getReferenceNumber("Container Number");
			if (containerNumber) {
				//mappings.put("containerNumber", containerNumber.identifier);
				subtitle = "Container #${containerNumber.identifier} ";
			}
			ReferenceNumber sealNumber = shipmentInstance.getReferenceNumber("Seal Number");
			if (sealNumber) {
				//mappings.put("sealNumber", sealNumber.identifier);
				subtitle += "Seal #${sealNumber.identifier}"
			}
			log.info("sea shipment " + subtitle)
		}
		else if ("Air".equals(shipmentInstance?.shipmentType?.name)) { 
			subtitle = "Freight Forwarder ${shipmentInstance?.shipmentMethod?.shipper?.name}"
			log.info("air shipment " + subtitle)
		}
		mappings.put("subtitle", subtitle)
		
		def value = ""
		if (shipmentInstance?.statedValue) { 		
			def decimalFormatter = new DecimalFormat("\$###,###.00")
			value = decimalFormatter.format(shipmentInstance?.statedValue);
		}
		mappings.put("value", value)
		
		log.debug("mappings: " + mappings)
		log.debug("xml before: " + xml)
		//valorize template
		Object obj = XmlUtils.unmarshallFromTemplate(xml, mappings);
		log.debug("xml after: " + xml)
		log.debug("mappings: " + mappings)
		
		//change  JaxbElement
		documentPart.setJaxbElement((Document) obj);

		// Create a new table for the Packing List
		Tbl table = createTable(wordMLPackage, shipmentInstance, 3, 1200);

		// Add table to document
		wordMLPackage.getMainDocumentPart().addObject(table);

		return wordMLPackage;				
	}
	
	/**
	 * 
	 * @param wordMLPackage
	 * @param filePath
	 */
	void savePackageToFile(WordprocessingMLPackage wordMLPackage, String filePath) { 		
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		saver.save(filePath);
		log.info( "Saved output to:" + filePath );
	}
	
	
	/**
	 * 
	 * @param pkg
	 * @param afterText
	 * @param table
	 * @throws Exception
	 */
	void insertTableAfter(WordprocessingMLPackage pkg, String afterText, Tbl table) throws Exception {
		Body b = pkg.getMainDocumentPart().getJaxbElement().getBody();
		int addPoint = -1, count = 0;
		for (Object o : b.getEGBlockLevelElts()) {
			if (o instanceof P && getElementText(o).startsWith(afterText)) {
				addPoint = count + 1;
				break;
			}
			count++;
		}
		if (addPoint != -1)
			b.getEGBlockLevelElts().add(addPoint, table);
		else {
			// didn't find paragraph to insert after...
		}
	}

	String getElementText(Object jaxbElem) throws Exception {
		StringWriter sw = new StringWriter();
		TextUtils.extractText(jaxbElem, sw);
		return sw.toString();
	}
	
   
	/**
	 * 
	 * @param shipmentInstance
	 * @param cols
	 * @param cellWidthTwips
	 * @return
	 */
	public Tbl createPackingListTable(Shipment shipmentInstance, int cols, int cellWidthTwips) {
		
		Tbl tbl = Context.getWmlObjectFactory().createTbl();		
		// w:tblPr
		// xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
		log.info("Namespace: " + Namespaces.W_NAMESPACE_DECLARATION)
		
		TblPr tblPr = null;
		try {
			String strTblPr = "<w:tblPr " + Namespaces.W_NAMESPACE_DECLARATION + "><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblLook w:val=\"04A0\"/></w:tblPr>";
			tblPr = (TblPr)XmlUtils.unmarshalString(strTblPr);
		} catch (JAXBException e) {
			// Shouldn't happen
			e.printStackTrace();
		}
		tbl.setTblPr(tblPr);
		
		/*
		// <w:tblGrid><w:gridCol w:w="4788"/>
		TblGrid tblGrid = Context.getWmlObjectFactory().createTblGrid();
		tbl.setTblGrid(tblGrid);
		// Add required <w:gridCol w:w="4788"/>
		for (int i=1 ; i<=cols; i++) {
			TblGridCol gridCol = Context.getWmlObjectFactory().createTblGridCol();
			gridCol.setW(BigInteger.valueOf(cellWidthTwips));
			tblGrid.getGridCol().add(gridCol);
		}
				
		// Now the rows
		for (int j=1 ; j<=rows; j++) {
			//shipmentInstance?.shipmentItems.each { item -> 
		
			Tr tr = Context.getWmlObjectFactory().createTr();
			tbl.getEGContentRowContent().add(tr);
			createPackingListCell(tr, cellWidthTwips);	

			
		}
		*/
		return tbl;
	}
	
	/**
	 * 
	 * @param tr
	 * @param cellWidthTwips
	 */
	void createPackingListCell(Tr tr, int cellWidthTwips) { 
		
		Tc tc = Context.getWmlObjectFactory().createTc();
		tr.getEGContentCellContent().add(tc);
		
		TcPr tcPr = Context.getWmlObjectFactory().createTcPr();
		tc.setTcPr(tcPr);
		
		// <w:tcW w:w="4788" w:type="dxa"/>
		TblWidth cellWidth = Context.getWmlObjectFactory().createTblWidth();
		tcPr.setTcW(cellWidth);
		cellWidth.setType("dxa");
		cellWidth.setW(BigInteger.valueOf(cellWidthTwips));
		
		// Cell content - an empty <w:p/>
		P paragraph = Context.getWmlObjectFactory().createP()
		//R run = Context.getWmlObjectFactory().createR();
		//Text text = Context.getWmlObjectFactory().createText();
		//text.setValue("testing");
		//run.getRunContent().add(text)
		//paragraph.getParagraphContent().add(run);
		tc.getEGBlockLevelElts().add(paragraph);

	} 
	
	
	/**
	 * 
	 * @param wmlPackage
	 * @param shipmentInstance
	 * @param cols
	 * @param cellWidthTwips
	 * @return
	 */
	public Tbl createTable(WordprocessingMLPackage wmlPackage, Shipment shipmentInstance, int cols, int cellWidthTwips) {
		
		Tbl tbl = Context.getWmlObjectFactory().createTbl();
		// w:tblPr
		log.info("Namespace: " + Namespaces.W_NAMESPACE_DECLARATION)
		
		TblPr tblPr = null;
		try {
			String strTblPr =
				"<w:tblPr " + Namespaces.W_NAMESPACE_DECLARATION + "><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblLook w:val=\"04A0\"/></w:tblPr>";
			tblPr = (TblPr)XmlUtils.unmarshalString(strTblPr);
		} catch (JAXBException e) {
			log.error("Exception occurred while creating the table prolog")
		}
		tbl.setTblPr(tblPr);
		
		// <w:tblGrid><w:gridCol w:w="4788"/>
		TblGrid tblGrid = Context.getWmlObjectFactory().createTblGrid();
		tbl.setTblGrid(tblGrid);
		// Add required <w:gridCol w:w="4788"/>
		int writableWidthTwips = wmlPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();		
		cellWidthTwips = writableWidthTwips/3;
		
		for (int i=1 ; i<=cols; i++) {
			TblGridCol gridCol = Context.getWmlObjectFactory().createTblGridCol();
			gridCol.setW(BigInteger.valueOf(cellWidthTwips));
			tblGrid.getGridCol().add(gridCol);
		}

		// Create a repeating header
		Tr trHeader = Context.getWmlObjectFactory().createTr();
		tbl.getEGContentRowContent().add(trHeader);
		BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
		
		TrPr trPr = Context.getWmlObjectFactory().createTrPr();
		trHeader.setTrPr(trPr)
		

		//TrPr trPr = trHeader.getTrPr();
		trPr.getCnfStyleOrDivIdOrGridBefore().add(Context.getWmlObjectFactory().createCTTrPrBaseTblHeader(bdt));
		addTc(wmlPackage, trHeader, "Pallet/Box #", true);
		addTc(wmlPackage, trHeader, "Item", true);
		addTc(wmlPackage, trHeader, "Qty", true);
		
				
		def previousContainer = null;		
		def shipmentItems = shipmentInstance?.shipmentItems?.sort { it?.container?.sortOrder } 
		// Iterate over shipment items and add them to the table 
		shipmentItems?.each { itemInstance ->
			
			log.info "previous: " + previousContainer + ", current: " + itemInstance?.container + ", same: " + (itemInstance?.container == previousContainer)
			Tr tr = Context.getWmlObjectFactory().createTr();
			tbl.getEGContentRowContent().add(tr);
			if (itemInstance?.container != previousContainer) { 
				addTc(wmlPackage, tr, itemInstance?.container?.name, false);
			}
			else { 
				addTc(wmlPackage, tr, "", false);
			}
			addTc(wmlPackage, tr, itemInstance?.product?.name, false);			
			addTc(wmlPackage, tr, String.valueOf(itemInstance?.quantity), false);			
			previousContainer = itemInstance?.container;
			
		}
		return tbl;
	}
	
	/**
	 * 
	 * @param wmlPackage
	 * @param tr
	 * @param text
	 * @param applyBold
	 */
	void addTc(WordprocessingMLPackage wmlPackage, Tr tr, String text, boolean applyBold) {
		Tc tc = Context.getWmlObjectFactory().createTc();
		// wmlPackage.getMainDocumentPart().createParagraphOfText(text)		
		tc.getEGBlockLevelElts().add( createParagraphOfText(text, applyBold) );
		tr.getEGContentCellContent().add( tc );
	}
	
	/**
	 * 
	 * @param simpleText
	 * @param applyBold
	 * @return
	 */
	P createParagraphOfText(String simpleText, boolean applyBold) { 
		P para = Context.getWmlObjectFactory().createP();
		// Create the text element
		Text t = Context.getWmlObjectFactory().createText();
		t.setValue(simpleText);
		// Create the run
		R run = Context.getWmlObjectFactory().createR();
		run.getRunContent().add(t);
		//run.getRPr().setB(true);
		// Set bold property 
		if (applyBold) {
			RPr rpr = Context.getWmlObjectFactory().createRPr();
			BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
			rpr.setB(bdt)
			run.setRPr(rpr);
		}		
		para.getParagraphContent().add(run);
		
		return para;
	}
	
	/**
	 * 
	 * @param wordMLPackage
	 * @return
	 */
	void convertToPdf(WordprocessingMLPackage wordMLPackage, OutputStream outputStream) {
		wordMLPackage.setFontMapper(new IdentityPlusMapper());
		PdfConversion conversion = new Conversion(wordMLPackage);
		//((Conversion)conversion).setSaveFO(new File(inputfilepath + ".fo"));
		//OutputStream outputStream = new FileOutputStream(inputfilepath + ".pdf");
		conversion.output(outputStream);
		//return outputStream;
	}

	
	
   
}
