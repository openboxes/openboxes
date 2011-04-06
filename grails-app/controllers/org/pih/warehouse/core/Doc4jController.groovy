package org.pih.warehouse.core

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
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
			
			// Container, Item, Lot, Quantity, Unit, Recipient
			int counter = 0;
			HSSFRow row     = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Shipment Name");
			row.createCell(1).setCellValue(shipmentInstance?.name);
			
			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Shipment Type");
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
			
			// ... instead we'll just display all reference numbers
			shipmentInstance.referenceNumbers.each {
				row = sheet.createRow((short)counter++);
				row.createCell(0).setCellValue(it?.referenceNumberType?.name);
				row.createCell(1).setCellValue(it?.identifier);
			}
			

			row = sheet.createRow((short)counter++);

			row = sheet.createRow((short)counter++);
			row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue("From");
			row.createCell(1).setCellValue(shipmentInstance?.origin?.name);

			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("To");
			row.createCell(1).setCellValue(shipmentInstance?.destination?.name);

			row = sheet.createRow((short)counter++);

			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));
			cellStyle.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
			
			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Expected Shipment Date");
			Cell expectedShipmentDateCell = row.createCell(1);
			expectedShipmentDateCell.setCellValue(shipmentInstance?.expectedShippingDate);
			expectedShipmentDateCell.setCellStyle(cellStyle);
			
			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Actual Shipment Date");
			Cell actualShipmentDateCell = row.createCell(1);
			if (shipmentInstance?.actualShippingDate) { 
				actualShipmentDateCell.setCellValue(shipmentInstance?.actualShippingDate);
				actualShipmentDateCell.setCellStyle(cellStyle);
			}
			else { 
				actualShipmentDateCell.setCellValue("Not available");
				actualShipmentDateCell.setCellStyle(cellStyle);
			}
			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Expected Arrival Date");
			Cell expectedArrivalDateCell = row.createCell(1);
			expectedArrivalDateCell.setCellValue(shipmentInstance?.expectedDeliveryDate);
			expectedArrivalDateCell.setCellStyle(cellStyle);

			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Actual Arrival Date");
			Cell actualArrivalDateCell = row.createCell(1);
			if (shipmentInstance?.actualDeliveryDate) { 
				actualArrivalDateCell.setCellValue(shipmentInstance?.actualDeliveryDate);
				actualArrivalDateCell.setCellStyle(cellStyle);
			}
			else { 
				actualArrivalDateCell.setCellValue("Not available");
				actualArrivalDateCell.setCellStyle(cellStyle);
			}
			row = sheet.createRow((short)counter++);

			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Comments");
			row.createCell(1).setCellValue("");

			row = sheet.createRow((short)counter++);

			row = sheet.createRow((short)counter++);
			row.createCell(0).setCellValue("Container");
			row.createCell(1).setCellValue("Item");
			row.createCell(2).setCellValue("Lot");
			row.createCell(3).setCellValue("Quantity");
			row.createCell(4).setCellValue("Unit");
			row.createCell(5).setCellValue("Recipient");

			
			shipmentInstance.shipmentItems.sort { it?.container?.sortOrder }. each { itemInstance ->	
				log.debug "Adding item  to packing list " + itemInstance?.product?.name + " -> " + itemInstance?.container?.name			
				row = sheet.createRow((short)counter++);
				row.createCell(0).setCellValue(itemInstance?.container?.name);
				row.createCell(1).setCellValue(itemInstance?.product?.name);
				row.createCell(2).setCellValue(itemInstance?.lotNumber);
				row.createCell(3).setCellValue(itemInstance?.quantity);
				row.createCell(4).setCellValue("item");
				row.createCell(5).setCellValue(itemInstance?.recipient?.name);
			}
			
			//render "nothing"
			
			def filename = shipmentInstance?.name + " - Packing List (3).xls"
			response.setHeader("Content-disposition", "attachment; filename=" + filename);
			response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
			//response.outputStream << tempFile.readBytes()
			workbook.write(response.outputStream)
			return;
		} 
		catch (Exception e) { 
			log.error e
			throw e;
		}
	}
	
	

	
	
	
	   
	
}
