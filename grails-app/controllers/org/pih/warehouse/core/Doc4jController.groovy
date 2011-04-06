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
			sheet.autoSizeColumn((short)0);
			// Container, Item, Lot, Quantity, Unit, Recipient
			int counter = 0;
			HSSFRow row     = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("Shipment Name");
			row.createCell((short)1).setCellValue(shipmentInstance?.name);
			
			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("Shipment Type");
			row.createCell((short)1).setCellValue(shipmentInstance?.shipmentType?.name);

			def shipmentWorkflow = shipmentService.getShipmentWorkflow(shipmentInstance)			
			if (shipmentWorkflow) { 
				shipmentWorkflow.referenceNumberTypes.each { 
					row = sheet.createRow((short)counter++);
					row.createCell((short)0).setCellValue(it?.name);
					row.createCell((short)1).setCellValue("");
				}			
			}


			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("");
			row.createCell((short)1).setCellValue("");

			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("From");
			row.createCell((short)1).setCellValue(shipmentInstance?.origin?.name);

			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("To");
			row.createCell((short)1).setCellValue(shipmentInstance?.destination?.name);

			row = sheet.createRow((short)counter++);
			//row.createCell((short)0).setCellValue("");
			//row.createCell((short)1).setCellValue("");

			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));
			
			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("Expected Shipment Date");
			Cell expectedShipmentDateCell = row.createCell((short)1);
			expectedShipmentDateCell.setCellValue(shipmentInstance?.expectedShippingDate);
			expectedShipmentDateCell.setCellStyle(cellStyle);
			
			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("Actual Shipment Date");
			Cell actualShipmentDateCell = row.createCell((short)1);
			actualShipmentDateCell.setCellValue(new Date());
			actualShipmentDateCell.setCellStyle(cellStyle);

			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("Expected Arrival Date");
			Cell expectedArrivalDateCell = row.createCell((short)1);
			expectedShipmentDateCell.setCellValue(shipmentInstance?.expectedDeliveryDate);
			actualShipmentDateCell.setCellStyle(cellStyle);

			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("Actual Arrival Date");
			Cell actualArrivalDateCell = row.createCell((short)1);
			actualArrivalDateCell.setCellValue(new Date());
			actualArrivalDateCell.setCellStyle(cellStyle);

			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("");
			row.createCell((short)1).setCellValue("");

			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("Comments");
			row.createCell((short)1).setCellValue("");

			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("");
			row.createCell((short)1).setCellValue("");


			row = sheet.createRow((short)counter++);
			row.createCell((short)0).setCellValue("Container");
			row.createCell((short)1).setCellValue("Item");
			row.createCell((short)2).setCellValue("Lot");
			row.createCell((short)3).setCellValue("Quantity");
			row.createCell((short)4).setCellValue("Unit");
			row.createCell((short)5).setCellValue("Recipient");

			
			shipmentInstance.shipmentItems.sort { it?.container?.sortOrder }. each { itemInstance ->	
				log.info "Adding item  to packing list " + itemInstance?.product?.name + " -> " + itemInstance?.container?.name			
				row = sheet.createRow((short)counter++);
				row.createCell((short)0).setCellValue(itemInstance?.container?.name);
				row.createCell((short)1).setCellValue(itemInstance?.product?.name);
				row.createCell((short)2).setCellValue(itemInstance?.lotNumber);
				row.createCell((short)3).setCellValue(itemInstance?.quantity);
				row.createCell((short)4).setCellValue("item");
				row.createCell((short)5).setCellValue(itemInstance?.recipient?.name);
			}
			
			render "nothing"
			
			def filename = shipmentInstance?.name + " - Packing List.xls"
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
