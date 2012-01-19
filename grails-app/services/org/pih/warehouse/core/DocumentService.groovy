package org.pih.warehouse.core;

import java.text.DecimalFormat
import java.text.SimpleDateFormat

import javax.xml.bind.JAXBException

import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.docx4j.TextUtils
import org.docx4j.XmlUtils
import org.docx4j.convert.out.pdf.PdfConversion
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion
import org.docx4j.jaxb.Context
import org.docx4j.openpackaging.io.SaveToZipFile
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
import org.docx4j.openpackaging.parts.relationships.Namespaces
import org.docx4j.wml.Body
import org.docx4j.wml.BooleanDefaultTrue
import org.docx4j.wml.Document
import org.docx4j.wml.P
import org.docx4j.wml.R
import org.docx4j.wml.RPr
import org.docx4j.wml.Tbl
import org.docx4j.wml.TblGrid
import org.docx4j.wml.TblGridCol
import org.docx4j.wml.TblPr
import org.docx4j.wml.TblWidth
import org.docx4j.wml.Tc
import org.docx4j.wml.TcPr
import org.docx4j.wml.Text
import org.docx4j.wml.Tr
import org.docx4j.wml.TrPr
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.Shipment

class DocumentService {
	
	boolean transactional = false
	
	/**
	 * 
	 * @param filePath
	 * @return
	 */
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
	File generateChecklistAsDocx() {
		// Save document to temporary file
		WordprocessingMLPackage wordMLPackage = generateChecklist();
		File tempFile = File.createTempFile("Checklist", ".docx")
		wordMLPackage.save(tempFile)
		return tempFile;
	}
   
	/**
	 * @param shipmentInstance
	 * @return
	 */
	OutputStream generateChecklistAsPdf() {
		WordprocessingMLPackage wordMLPackage = generateChecklist();
		return convertToPdf(wordMLPackage);
	}


	/**
	 * Generate the Checklist from a template.
	 *
	 * @param 
	 * @return
	 */
	WordprocessingMLPackage generateChecklist() {

		File template = findFile("templates/receive-shipment-template.docx")
		if (!template) {
			throw new FileNotFoundException("Could not find template");
		}

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(template);

		// 2. Fetch the document part
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		Document wmlDocumentEl = (Document) documentPart.getJaxbElement();

		//xml --> string
		def xml = XmlUtils.marshaltoString(wmlDocumentEl, true);
		def mappings = new HashMap<String, String>();

		/*	   
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
		 */

		//valorize template
		Object obj = XmlUtils.unmarshallFromTemplate(xml, mappings);

		//change  JaxbElement
		documentPart.setJaxbElement((Document) obj);

		// Create a new table for the Packing List
		//Tbl table = createTable(wordMLPackage, shipmentInstance, 3, 1200);

		// Add table to document
		//wordMLPackage.getMainDocumentPart().addObject(table);

		return wordMLPackage;
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
	 * @param shipmentInstance
	 * @return
	 */
	OutputStream generateLetterAsPdf(Shipment shipmentInstance) {
		WordprocessingMLPackage wordMLPackage = generateLetter(shipmentInstance);
		return convertToPdf(wordMLPackage);
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
	OutputStream convertToPdf(WordprocessingMLPackage wordMLPackage) {
		PdfConversion conversion = new Conversion(wordMLPackage);
		
		((Conversion)conversion).setSaveFO(new File(inputfilepath + ".fo"));
		OutputStream outputStream = new FileOutputStream(inputfilepath + ".pdf");
		conversion.output(outputStream);
		return outputStream;
	}

	
	
	void generatePackingList(OutputStream outputStream, Shipment shipmentInstance) { 
		// TODO Move to PoiService
		
		try {
			HSSFWorkbook workbook = new HSSFWorkbook();
			CreationHelper createHelper = workbook.getCreationHelper();
			HSSFSheet sheet = workbook.createSheet();
			//sheet.autoSizeColumn(0);
			//sheet.autoSizeColumn(1);
			//sheet.autoSizeColumn(2);
			//sheet.autoSizeColumn(3);
			//sheet.autoSizeColumn(4);
			//sheet.autoSizeColumn(5);
			sheet.setColumnWidth((short)0, (short) ((50 * 8) / ((double) 1 / 20)))
			sheet.setColumnWidth((short)1, (short) ((50 * 15) / ((double) 1 / 20)))
			sheet.setColumnWidth((short)2, (short) ((50 * 5) / ((double) 1 / 20)))
			sheet.setColumnWidth((short)3, (short) ((50 * 2) / ((double) 1 / 20)))
			sheet.setColumnWidth((short)4, (short) ((50 * 2) / ((double) 1 / 20)))
			sheet.setColumnWidth((short)5, (short) ((50 * 5) / ((double) 1 / 20)))

			// Bold font
			Font boldFont = workbook.createFont();
			boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			
			// Bold cell style
			CellStyle boldStyle = workbook.createCellStyle();
			boldStyle.setFont(boldFont);

			// Bold and align center cell style
			CellStyle boldAndCenterStyle = workbook.createCellStyle();
			boldAndCenterStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			boldAndCenterStyle.setFont(boldFont);

			// Align center cell style
			CellStyle alignCenterCellStyle = workbook.createCellStyle();
			alignCenterCellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			
			// Wrap text cell style
			CellStyle wrapTextCellStyle = workbook.createCellStyle();
			wrapTextCellStyle.setWrapText(true);
			
			// Date cell style
			CellStyle dateStyle = workbook.createCellStyle();
			dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));
			dateStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);

			// SHIPMENT NAME
			int counter = 0;
			HSSFRow row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Shipment Name");
			row.getCell(0).setCellStyle(boldStyle);
			row.createCell(1).setCellValue(shipmentInstance?.name);

			// SHIPMENT TYPE
			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Shipment Type");
			row.getCell(0).setCellStyle(boldStyle);
			row.createCell(1).setCellValue(shipmentInstance?.shipmentType?.name);

			/*
			// Doesn't seem to work this way, so I'm just going to print out all reference numbers
			def shipmentWorkflow = shipmentService.getShipmentWorkflow(shipmentInstance)
			if (shipmentWorkflow) {
				shipmentWorkflow.referenceNumberTypes.each {
					def referenceNumber = shipmentInstance.getReferenceNumber(it?.name)
					log.info ("reference #: " + it?.name + " " + referenceNumber)
					if (referenceNumber) {
						row = sheet.createRow((short)counter++);
						row.createCell(0).setCellValue(referenceNumber?.referenceNumberType?.name);
						row.createCell(1).setCellValue(referenceNumber?.identifier);
					}
				}
			}
			*/
			
			// REFERENCE NUMBERS
			shipmentInstance.referenceNumbers.each {
				row = sheet.createRow((short)counter++);
				row.createCell(0).setCellValue(it?.referenceNumberType?.name);
				row.getCell(0).setCellStyle(boldStyle);
				row.createCell(1).setCellValue(it?.identifier);
			}

			// EMPTY ROW
			row = sheet.createRow((short)counter++);

			// FROM
			row = sheet.createRow((short)counter++);
			row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue("From");
			row.getCell(0).setCellStyle(boldStyle);
			row.createCell(1).setCellValue(shipmentInstance?.origin?.name);
			row = sheet.createRow((short)counter++);
			
			row.createCell(0).setCellValue("To");
			row.getCell(0).setCellStyle(boldStyle);
			row.createCell(1).setCellValue(shipmentInstance?.destination?.name);

			// EMPTY ROW
			row = sheet.createRow((short)counter++);
			
			// EXPECTED SHIPMENT DATE
			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Expected Shipment Date");
			row.getCell(0).setCellStyle(boldStyle);
			Cell expectedShipmentDateCell = row.createCell(1);
			expectedShipmentDateCell.setCellValue(shipmentInstance?.expectedShippingDate);
			expectedShipmentDateCell.setCellStyle(dateStyle);
			
			// ACTUAL SHIPMENT DATE
			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Actual Shipment Date");
			row.getCell(0).setCellStyle(boldStyle);
			Cell actualShipmentDateCell = row.createCell(1);
			if (shipmentInstance?.actualShippingDate) {
				actualShipmentDateCell.setCellValue(shipmentInstance?.actualShippingDate);
				actualShipmentDateCell.setCellStyle(dateStyle);
			}
			else {
				actualShipmentDateCell.setCellValue("Not available");
			}
			
			// EXPECTED ARRIVAL DATE
			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Expected Arrival Date");
			row.getCell(0).setCellStyle(boldStyle);
			Cell expectedArrivalDateCell = row.createCell(1);
			expectedArrivalDateCell.setCellValue(shipmentInstance?.expectedDeliveryDate);
			expectedArrivalDateCell.setCellStyle(dateStyle);

			// ACTUAL ARRIVAL DATE
			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Actual Arrival Date");
			row.getCell(0).setCellStyle(boldStyle);
			Cell actualArrivalDateCell = row.createCell(1);
			if (shipmentInstance?.actualDeliveryDate) {
				actualArrivalDateCell.setCellValue(shipmentInstance?.actualDeliveryDate);
				actualArrivalDateCell.setCellStyle(dateStyle);
			}
			else {
				actualArrivalDateCell.setCellValue("Not available");
			}
			
			// EMPTY ROW
			row = sheet.createRow((short)counter++);

			// COMMENTS
			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Comments");
			row.getCell(0).setCellStyle(boldStyle);
			row.createCell(1).setCellValue(shipmentInstance?.additionalInformation);
			row.getCell(1).setCellStyle(wrapTextCellStyle);

			// EMPTY ROW
			row = sheet.createRow((short)counter++);

			// ITEM TABLE HEADER
			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Container");
			row.getCell(0).setCellStyle(boldStyle);
			row.createCell(1).setCellValue("Item");
			row.getCell(1).setCellStyle(boldStyle);
			row.createCell(2).setCellValue("Lot");
			row.getCell(2).setCellStyle(boldStyle);
			row.createCell(3).setCellValue("Quantity");
			row.getCell(3).setCellStyle(boldAndCenterStyle);
			row.createCell(4).setCellValue("Unit");
			row.getCell(4).setCellStyle(boldAndCenterStyle);
			row.createCell(5).setCellValue("Recipient");
			row.getCell(5).setCellStyle(boldStyle);
			
			
			shipmentInstance.shipmentItems.sort { it?.container?.sortOrder }. each { itemInstance ->
				log.debug "Adding item  to packing list " + itemInstance?.product?.name + " -> " + itemInstance?.container?.name
				row = sheet.createRow((short)counter++);
				row.createCell(0).setCellValue(itemInstance?.container?.name);
				row.createCell(1).setCellValue(itemInstance?.product?.name);
				row.createCell(2).setCellValue(itemInstance?.lotNumber);
				row.createCell(3).setCellValue(itemInstance?.quantity);
				row.getCell(3).setCellStyle(alignCenterCellStyle)
				row.createCell(4).setCellValue("item");
				row.getCell(4).setCellStyle(alignCenterCellStyle)
				row.createCell(5).setCellValue(itemInstance?.recipient?.name);
			}
			
			
			log.info ("workbook " + workbook)
			workbook.write(outputStream)
		}
		catch (Exception e) {
			log.error e
			throw e;
		}
	}		
	
	
   
}
