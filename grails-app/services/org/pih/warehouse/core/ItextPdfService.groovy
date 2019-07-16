
package org.pih.warehouse.core

import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.PageSize
import com.lowagie.text.Phrase
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem


class ItextPdfService {

    boolean transactional = false
    def grailsApplication

    private getMessageTagLib() {
        return grailsApplication.mainContext.getBean('org.pih.warehouse.MessageTagLib')
    }

    void generateCertificateOfDonation(OutputStream outputStream,Shipment shipment){

        Document document = new Document(PageSize.A4.rotate())

        PdfWriter.getInstance(document, outputStream)

        document.open()

        float[] columnWidths = [1,2]

        PdfPTable tab = new PdfPTable(columnWidths)
        tab.setWidthPercentage(50)
        tab.setHorizontalAlignment(Element.ALIGN_LEFT)

        PdfPCell cell2 = null
        cell2 = new PdfPCell(new Phrase("Shipment number:"))
        cell2.setBorder(Rectangle.NO_BORDER)
        tab.addCell(cell2)

        cell2 = new PdfPCell(new Phrase(shipment.shipmentNumber))
        cell2.setBorder(Rectangle.NO_BORDER)
        tab.addCell(cell2)

        cell2 = new PdfPCell(new Phrase("Origin"))
        cell2.setBorder(Rectangle.NO_BORDER)
        tab.addCell(cell2)

        cell2 = new PdfPCell(new Phrase(shipment.origin.name))
        cell2.setBorder(Rectangle.NO_BORDER)
        tab.addCell(cell2)


        cell2 = new PdfPCell(new Phrase("Destination"))
        cell2.setBorder(Rectangle.NO_BORDER)
        tab.addCell(cell2)

        cell2 = new PdfPCell(new Phrase(shipment.destination.name))
        cell2.setBorder(Rectangle.NO_BORDER)
        tab.addCell(cell2)


        float[] widthtab2 = [2,2,5,2,2,5,2,2,2]
        PdfPTable tab2 = new PdfPTable(widthtab2)
        tab2.setWidthPercentage(100)
        tab2.setSpacingBefore(40)
        tab2.addCell(new PdfPCell(new Phrase("No")))
        tab2.addCell(new PdfPCell(new Phrase("Code")))
        tab2.addCell(new PdfPCell(new Phrase("Item Description")))
        tab2.addCell(new PdfPCell(new Phrase("UoM")))
        tab2.addCell(new PdfPCell(new Phrase("Batch No")))
        tab2.addCell(new PdfPCell(new Phrase("Exp Date")))
        tab2.addCell(new PdfPCell(new Phrase("Quantity")))
        tab2.addCell(new PdfPCell(new Phrase("Unit Price (USD)")))
        tab2.addCell(new PdfPCell(new Phrase("Total Cost")))

        def count = 0
        def totalCost = 0
        for(ShipmentItem shipmentItem : shipment.shipmentItems){
            count++
            if (shipmentItem?.product?.pricePerUnit) {
                totalCost = shipmentItem?.quantity*shipmentItem?.product?.pricePerUnit
            }

            tab2.addCell(new PdfPCell(new Phrase(count.toString())))
            tab2.addCell(new PdfPCell(new Phrase(shipmentItem.product.productCode)))
            tab2.addCell(new PdfPCell(new Phrase(shipmentItem.product.name)))
            if(shipmentItem.product.unitOfMeasure) {
                tab2.addCell(new PdfPCell(new Phrase(shipmentItem.product.unitOfMeasure)))
            }else {
                tab2.addCell(new PdfPCell(new Phrase("")))
            }
            tab2.addCell(new PdfPCell(new Phrase(shipmentItem.lotNumber)))
            tab2.addCell(new PdfPCell(new Phrase(shipmentItem.expirationDate.toString())))
            tab2.addCell(new PdfPCell(new Phrase(shipmentItem.quantity.toString())))
            if(shipmentItem.product.pricePerUnit){
                tab2.addCell(new PdfPCell(new Phrase(shipmentItem.product.pricePerUnit.toString())))
            }else {
                tab2.addCell(new PdfPCell(new Phrase("0")))
            }
            tab2.addCell(new PdfPCell(new Phrase(totalCost.toString())))
        }

        PdfPCell cell1 = null

        cell1 = new PdfPCell(new Phrase("Total  "))
        cell1.setColspan(8)
        cell1.setHorizontalAlignment(Element.ALIGN_RIGHT)
        tab2.addCell(cell1)

        cell1 = new PdfPCell(new Phrase(totalCost.toString()))
        tab2.addCell(cell1)


        float [] width = [5,5]
        PdfPTable tab3 = new PdfPTable(width)
        tab3.setTotalWidth(10)
        tab3.setHorizontalAlignment(Element.ALIGN_LEFT)
        tab3.addCell(new PdfPCell(new Phrase("Prepared on")))
        tab3.addCell(new PdfPCell(new Phrase("")))
        tab3.addCell(new PdfPCell(new Phrase("By")))
        tab3.addCell(new PdfPCell(new Phrase("")))
        tab3.addCell(new PdfPCell(new Phrase("Signature")))
        tab3.addCell(new PdfPCell(new Phrase("")))

        float [] width4 = [5]
        PdfPTable tab4 = new PdfPTable(width4)
        tab4.setWidthPercentage(30)
        tab4.setSpacingBefore(30)
        PdfPCell celltab3 = new PdfPCell(tab3)
        tab4.addCell(celltab3)

        float [] width5 = [5,5]
        PdfPTable tab6 = new PdfPTable(width5)
        tab6.setTotalWidth(10)
        tab6.setHorizontalAlignment(Element.ALIGN_LEFT)
        tab6.addCell(new PdfPCell(new Phrase("Prepared on")))
        tab6.addCell(new PdfPCell(new Phrase("")))
        tab6.addCell(new PdfPCell(new Phrase("By")))
        tab6.addCell(new PdfPCell(new Phrase("")))
        tab6.addCell(new PdfPCell(new Phrase("Signature")))
        tab6.addCell(new PdfPCell(new Phrase("")))
        tab6.addCell(new PdfPCell(new Phrase("Position")))
        tab6.addCell(new PdfPCell(new Phrase("")))

        float [] width6 = [5]
        PdfPTable tab7 = new PdfPTable(width6)
        tab7.setWidthPercentage(30)
        tab7.setSpacingBefore(30)
        PdfPCell celltab6 = new PdfPCell(tab6)
        tab7.addCell(celltab6)


        document.add(tab)
        document.add(tab2)
        document.add(tab4)
        document.add(tab4)
        document.add(tab7)
        document.add(tab7)
        document.close()

    }

    void exportPackingListPdf(Shipment ShipmentInstance,OutputStream outputStream){

        Document document = new Document(PageSize.A4.rotate())

        PdfWriter.getInstance(document, outputStream)

        document.open()

        float[] columnWidths = [2,2,2,5,3,3,2,2,2]

        PdfPTable table = new PdfPTable(columnWidths)
        table.setWidthPercentage(100);
        table.addCell(new PdfPCell(new Phrase("Pallet")))
        table.addCell(new PdfPCell(new Phrase("Box")))
        table.addCell(new PdfPCell(new Phrase("Code")))
        table.addCell(new PdfPCell(new Phrase("Product")))
        table.addCell(new PdfPCell(new Phrase("Lot number")))
        table.addCell(new PdfPCell(new Phrase("Expires")))
        table.addCell(new PdfPCell(new Phrase("Qty")))
        table.addCell(new PdfPCell(new Phrase("Units")))
        table.addCell(new PdfPCell(new Phrase("Recipient")))

        ShipmentInstance.shipmentItems.sort(). each { itemInstance ->
            if (itemInstance?.container?.parentContainer) {
                table.addCell(new PdfPCell(new Phrase(itemInstance?.container?.parentContainer?.name)))
                table.addCell(new PdfPCell(new Phrase(itemInstance?.container?.name)))
            }
            else if (itemInstance?.container) {
                table.addCell(new PdfPCell(new Phrase(itemInstance?.container?.name)))
                table.addCell(new PdfPCell(new Phrase("")))
            }
            else {
                table.addCell(new PdfPCell(new Phrase(itemInstance?.container?.name)))
                table.addCell(new PdfPCell(new Phrase("")))
            }
            table.addCell(new PdfPCell(new Phrase(itemInstance?.inventoryItem?.product?.productCode)))
            table.addCell(new PdfPCell(new Phrase(itemInstance?.inventoryItem?.product?.name)))
            table.addCell(new PdfPCell(new Phrase(itemInstance?.inventoryItem?.lotNumber)))
            table.addCell(new PdfPCell(new Phrase(itemInstance?.inventoryItem?.expirationDate.toString())))
            table.addCell(new PdfPCell(new Phrase(itemInstance?.quantity.toString())))
            table.addCell(new PdfPCell(new Phrase("each")))
            table.addCell(new PdfPCell(new Phrase("")))
        }


        document.add(table)
        document.close()
    }

    void generatePackingList (OutputStream outputStream ,Shipment ShipmentInstance){

        Document document = new Document(PageSize.A4.rotate())

        PdfWriter.getInstance(document, outputStream)

        document.open()

        float[] tabWidth = [5]
        PdfPTable tab = new PdfPTable(tabWidth)
        tab.setWidthPercentage(100)
        PdfPCell myCell = new PdfPCell(new Phrase("Packing List"))
        myCell.setBorder(Rectangle.NO_BORDER)
        myCell.setHorizontalAlignment(Element.ALIGN_CENTER)
        tab.addCell(myCell)

        float[] width = [5,6]

        float[] width1 = [5,5]
        PdfPTable mainTable = new PdfPTable(width1)
        mainTable.setWidthPercentage(100)
        mainTable.setSpacingBefore(45)
        mainTable.getDefaultCell().setBorder(Rectangle.NO_BORDER)

        PdfPCell cell = null;

        PdfPTable table = new PdfPTable(width)
        table.setWidthPercentage(30)
        table.setHorizontalAlignment(Element.ALIGN_LEFT)


        PdfPCell cell2 = null
        cell2 = new PdfPCell(new Phrase("Shipment number:"))
        cell2.setBorder(Rectangle.NO_BORDER)
        table.addCell(cell2)



        cell2 = new PdfPCell(new Phrase(ShipmentInstance.shipmentNumber))
        cell2.setBorder(Rectangle.NO_BORDER)
        table.addCell(cell2)

        cell2 = new PdfPCell(new Phrase("Shipment Type:"))
        cell2.setBorder(Rectangle.NO_BORDER)
        table.addCell(cell2)

        cell2 = new PdfPCell(new Phrase(ShipmentInstance.shipmentType?.name))
        cell2.setBorder(Rectangle.NO_BORDER)
        table.addCell(cell2)

        cell2 = new PdfPCell(new Phrase("Origin:"))
        cell2.setBorder(Rectangle.NO_BORDER)
        table.addCell(cell2)

        cell2 = new PdfPCell(new Phrase(ShipmentInstance.origin.name))
        cell2.setBorder(Rectangle.NO_BORDER)
        table.addCell(cell2)

        cell2 = new PdfPCell(new Phrase("Destination:"))
        cell2.setBorder(Rectangle.NO_BORDER)
        table.addCell(cell2)

        cell2 = new PdfPCell(new Phrase(ShipmentInstance.destination.name))
        cell2.setBorder(Rectangle.NO_BORDER)
        table.addCell(cell2)

        cell= new PdfPCell(table)
        cell.setBorder(Rectangle.NO_BORDER)
        mainTable.addCell(cell)


        PdfPTable table1 = new PdfPTable(width)
        table1.setWidthPercentage(30)
        table1.setHorizontalAlignment(Element.ALIGN_RIGHT)

        PdfPCell cell3 = null

        cell3 = new PdfPCell(new Phrase("Expected shipping date:"))
        cell3.setBorder(Rectangle.NO_BORDER)
        cell3.setHorizontalAlignment(Rectangle.ALIGN_RIGHT)
        table1.addCell(cell3)

        cell3 = new PdfPCell(new Phrase(ShipmentInstance?.expectedShippingDate.toString()))
        cell3.setBorder(Rectangle.NO_BORDER)
        cell3.setHorizontalAlignment(Rectangle.ALIGN_RIGHT)
        table1.addCell(cell3)

        cell3 = new PdfPCell(new Phrase("Actual shipping date:"))
        cell3.setBorder(Rectangle.NO_BORDER)
        cell3.setHorizontalAlignment(Rectangle.ALIGN_RIGHT)
        table1.addCell(cell3)

        if(ShipmentInstance.actualShippingDate){
            cell3 = new PdfPCell(new Phrase(ShipmentInstance.actualShippingDate.toString()))
        }else {
            cell3 = new PdfPCell(new Phrase("Not available"))
        }

        cell3.setBorder(Rectangle.NO_BORDER)
        cell3.setHorizontalAlignment(Rectangle.ALIGN_RIGHT)
        table1.addCell(cell3)

        cell3 = new PdfPCell(new Phrase("Expected arrival date:"))
        cell3.setBorder(Rectangle.NO_BORDER)
        cell3.setHorizontalAlignment(Rectangle.ALIGN_RIGHT)
        table1.addCell(cell3)

        if(ShipmentInstance.expectedDeliveryDate){
            cell3 = new PdfPCell(new Phrase(ShipmentInstance.expectedDeliveryDate.toString()))
        }else{
            cell3 = new PdfPCell(new Phrase("Not available"))
        }

        cell3.setBorder(Rectangle.NO_BORDER)
        cell3.setHorizontalAlignment(Rectangle.ALIGN_RIGHT)
        table1.addCell(cell3)

        cell3 = new PdfPCell(new Phrase("Actual delivery date:"))
        cell3.setBorder(Rectangle.NO_BORDER)
        cell3.setHorizontalAlignment(Rectangle.ALIGN_RIGHT)
        table1.addCell(cell3)

        if(ShipmentInstance.actualDeliveryDate){
            cell3 = new PdfPCell(new Phrase(ShipmentInstance.actualDeliveryDate.toString()))
        }else{
            cell3 = new PdfPCell(new Phrase("Not available"))
        }

        cell3.setBorder(Rectangle.NO_BORDER)
        cell3.setHorizontalAlignment(Rectangle.ALIGN_RIGHT)
        table1.addCell(cell3)


        cell3 = new PdfPCell(new Phrase("Comment:"))
        cell3.setBorder(Rectangle.NO_BORDER)
        cell3.setHorizontalAlignment(Rectangle.ALIGN_RIGHT)
        table1.addCell(cell3)

        cell3 = new PdfPCell(new Phrase(""))
        cell3.setBorder(Rectangle.NO_BORDER)
        cell3.setHorizontalAlignment(Rectangle.ALIGN_RIGHT)
        table1.addCell(cell3)

        cell = new PdfPCell(table1)
        cell.setBorder(Rectangle.NO_BORDER)
        mainTable.addCell(cell)
        mainTable.setSpacingAfter(90)

        float [] itemWidth = [2,2,2,2,2,2,5,2,3,2,2,3,3]

        PdfPTable iteamTable = new PdfPTable(itemWidth)
        iteamTable.setWidthPercentage(100)

        iteamTable.addCell(new PdfPCell(new Phrase("Pallet")))
        iteamTable.addCell(new PdfPCell(new Phrase("Box unit")))
        iteamTable.addCell(new PdfPCell(new Phrase("Bin")))
        iteamTable.addCell(new PdfPCell(new Phrase("SKU")))
        iteamTable.addCell(new PdfPCell(new Phrase("Mfg")))
        iteamTable.addCell(new PdfPCell(new Phrase("Vendor")))
        iteamTable.addCell(new PdfPCell(new Phrase("Product")))
        iteamTable.addCell(new PdfPCell(new Phrase("Lot number")))
        iteamTable.addCell(new PdfPCell(new Phrase("Expires")))
        iteamTable.addCell(new PdfPCell(new Phrase("Qty")))
        iteamTable.addCell(new PdfPCell(new Phrase("Units")))
        iteamTable.addCell(new PdfPCell(new Phrase("Recipient")))
        iteamTable.addCell(new PdfPCell(new Phrase("Comments")))


        for (ShipmentItem shipmentItem : ShipmentInstance.shipmentItems){
            if(shipmentItem.container.parentContainer){
                iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.container.parentContainer.name)))
                iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.container.name)))
            }else if (shipmentItem.container){
                iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.container.name)))
                iteamTable.addCell(new PdfPCell(new Phrase("")))
            }else  {
                iteamTable.addCell(new PdfPCell(new Phrase("")))
                iteamTable.addCell(new PdfPCell(new Phrase("")))
            }

            if(shipmentItem.binLocation) {
                iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.binLocation.name)))
            }else {
                iteamTable.addCell(new PdfPCell(new Phrase("")))
            }
            iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.product.productCode.toString())))
            iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.product.manufacturerCode)))
            iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.product.vendorCode.toString())))

            if(shipmentItem.product.name){
                iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.product.name)))
            }else{
                iteamTable.addCell(new PdfPCell(new Phrase("")))
            }

            iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.lotNumber)))
            iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.expirationDate.toString())))
            iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.quantity.toString())))
            iteamTable.addCell(new PdfPCell(new Phrase("Each")))
            if(shipmentItem.recipient){
                iteamTable.addCell(new PdfPCell(new Phrase(shipmentItem.recipient.name.toString())))
            }else{
                iteamTable.addCell(new PdfPCell(new Phrase("")))
            }
            iteamTable.addCell(new PdfPCell(new Phrase("")))


        }

        document.add(tab)
        document.add(mainTable)
        document.add(iteamTable)
        document.close()
    }

}
