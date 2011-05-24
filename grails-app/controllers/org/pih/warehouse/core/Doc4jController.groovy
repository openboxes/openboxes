package org.pih.warehouse.core

import org.apache.poi.hssf.usermodel.HSSFFont;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion;
import org.docx4j.convert.out.pdf.PdfConversion;

import org.docx4j.wml.Document;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.pih.warehouse.shipping.Shipment;

class Doc4jController {

	def fileService
	def shipmentService 
	
	def index = { 
		
	}
	
	
	def downloadLetter = { 
		
		def shipmentInstance = Shipment.get(params.id);
		
		if (!shipmentInstance) { 
			throw new Exception("Unable to locate shipment with ID ${params.id}")
		}
		
		def tempFile = fileService.generateLetter(shipmentInstance)
		def filename = shipmentInstance?.name + " - Certificate of Donation.docx"
		response.setHeader("Content-disposition", "attachment; filename=" + filename);
		response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
		response.outputStream << tempFile.readBytes()

		
	}
	
	
	
	def downloadPackingList = { 
		log.info params
		def shipmentInstance = Shipment.get(params.id);
		
		if (!shipmentInstance) {
			throw new Exception("Unable to locate shipment with ID ${params.id}")
		}

		
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
			
			// For some reason, this needs to be here or we get a File Not Found error (ERR_FILE_NOT_FOUND)
			render ""
			
			def filename = shipmentInstance?.name + " - Packing List.xls"
			log.info ("filename " + filename )
			response.setHeader("Content-disposition", "attachment; filename=" + filename);
			response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
			//response.outputStream << tempFile.readBytes()
			log.info ("workbook " + workbook)
			workbook.write(response.outputStream)
			return;
		} 
		catch (Exception e) { 
			log.error e
			throw e;
		}
	}
	
}
