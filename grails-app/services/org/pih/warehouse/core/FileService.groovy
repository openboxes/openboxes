package org.pih.warehouse.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.bind.JAXBException;

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.pdf.PdfConversion;
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion;
import org.docx4j.jaxb.Context;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Body;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
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
	
	File generateLetter(Shipment shipmentInstance) { 
		
		File inputFile = findFile("templates/sea-shipment-letter.docx")
		
		if (!inputFile) {
			throw new FileNotFoundException("templates/sea-shipment-letter.docx");
		}
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputFile);

		// 2. Fetch the document part
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		Document wmlDocumentEl = (Document) documentPart.getJaxbElement();

		//xml --> string
		String xml = XmlUtils.marshaltoString(wmlDocumentEl, true);
		HashMap<String, String> mappings = new HashMap<String, String>();
		
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String date = formatter.format(shipmentInstance.getExpectedDeliveryDate());
		mappings.put("date", date);		
		
		ReferenceNumber containerNumber = shipmentInstance.getReferenceNumber("Container Number");
		if (containerNumber) { 
			mappings.put("containerNumber", containerNumber.identifier);
		}
		ReferenceNumber sealNumber = shipmentInstance.getReferenceNumber("Seal Number");
		if (sealNumber) { 
			mappings.put("sealNumber", sealNumber.identifier);
		}
		mappings.put("contents", "");
		
		//valorize template
		Object obj = XmlUtils.unmarshallFromTemplate(xml, mappings);

		//change  JaxbElement
		documentPart.setJaxbElement((Document) obj);

		Tbl table = createSampleTable(wordMLPackage, shipmentInstance, 4, 1000);
			//createPackingListTable(shipmentInstance, 10, 10, 100);
		//insertTable(wordMLPackage, "Packing List Table goes here", table);

		wordMLPackage.getMainDocumentPart().addObject(table);
		
		
		File tempFile = File.createTempFile("sea-shipment-letter-" + new Date(), ".docx")
		wordMLPackage.save(tempFile)		
		return tempFile;
	}
	
	
	void savePackageToFile(WordprocessingMLPackage wordMLPackage, String filePath) { 		
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		saver.save(filePath);
		log.info( "Saved output to:" + filePath );
	}
	
	
	void insertTable(WordprocessingMLPackage pkg, String afterText, Tbl table) throws Exception {
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
	
	
	
	public Tbl createSampleTable(WordprocessingMLPackage wmlPackage, Shipment shipmentInstance, int cols, int cellWidthTwips) {
		
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
		cellWidthTwips = writableWidthTwips/4;
		
		for (int i=1 ; i<=cols; i++) {
			TblGridCol gridCol = Context.getWmlObjectFactory().createTblGridCol();
			gridCol.setW(BigInteger.valueOf(cellWidthTwips));
			tblGrid.getGridCol().add(gridCol);
		}

		def previousContainer = null;		
		def shipmentItems = shipmentInstance?.shipmentItems?.sort { it?.container?.sortOrder } 
		// Iterate over shipment items and add them to the table 
		shipmentItems?.each { itemInstance ->
			
			log.info "previous: " + previousContainer + ", current: " + itemInstance?.container + ", same: " + (itemInstance?.container == previousContainer)
			Tr tr = Context.getWmlObjectFactory().createTr();
			tbl.getEGContentRowContent().add(tr);
			if (itemInstance?.container != previousContainer) { 
				addTc(wmlPackage, tr, itemInstance?.container?.name);
			}
			else { 
				addTc(wmlPackage, tr, "");
			}
			addTc(wmlPackage, tr, itemInstance?.product?.name);			
			addTc(wmlPackage, tr, String.valueOf(itemInstance?.quantity));			
			addTc(wmlPackage, tr, "item");
			previousContainer = itemInstance?.container;
			
		}
		return tbl;
	}
	
	protected void addTc(WordprocessingMLPackage wmlPackage, Tr tr, String text) {
		Tc tc = Context.getWmlObjectFactory().createTc();
		// wmlPackage.getMainDocumentPart().createParagraphOfText(text)		
		tc.getEGBlockLevelElts().add( createParagraphOfText(text) );
		tr.getEGContentCellContent().add( tc );
	}
	
	
	P createParagraphOfText(String simpleText) { 
		P para = Context.getWmlObjectFactory().createP();
		// Create the text element
		Text t = Context.getWmlObjectFactory().createText();
		t.setValue(simpleText);
		// Create the run
		R run = Context.getWmlObjectFactory().createR();
		run.getRunContent().add(t);
		para.getParagraphContent().add(run);
		
		return para;
	}
	
	
	
	def downloadAsPdf = {
		
		def inputfilepath = System.getProperty("user.dir");
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage()
		def mainPart = wordMLPackage.getMainDocumentPart()

		// create some styled heading...
		mainPart.addStyledParagraphOfText("Title", "Partners In Health")
		mainPart.addStyledParagraphOfText("Subtitle", "Generated at " + Calendar.getInstance().getTime().toString())

		
		// Add our list of assets to the document
		Shipment.list().each { shipment ->
			mainPart.addParagraphOfText(shipment?.name)
		}

		PdfConversion conversion = new Conversion(wordMLPackage);
		
		((Conversion)conversion).setSaveFO(new File(inputfilepath + ".fo"));
			OutputStream os = new FileOutputStream(inputfilepath + ".pdf");
		//response.setHeader("Content-disposition", "attachment; filename=letter.pdf");
		//conversion.output(response.outputStream);
	}

	def downloadAsDoc = {
		
		def inputfilepath = System.getProperty("user.dir");
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage()
		def mainPart = wordMLPackage.getMainDocumentPart()

		// create some styled heading...
		mainPart.addStyledParagraphOfText("Title", "Partners In Health")
		mainPart.addStyledParagraphOfText("Subtitle", "Generated at " + Calendar.getInstance().getTime().toString())

		
		// Add our list of assets to the document
		Shipment.list().each { shipment ->
			mainPart.addParagraphOfText(shipment?.name)
		}
			
		// write out our word doc to disk
		File file = File.createTempFile("wordexport-", ".docx")
		wordMLPackage.save(file);

		// and send it all back to the browser
		//response.setHeader("Content-disposition", "attachment; filename=assets.docx");
		//response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
		//response.outputStream << file.readBytes()
		file.delete();
	}
	
   
}
