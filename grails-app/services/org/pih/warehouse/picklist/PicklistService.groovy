/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.picklist
class PicklistService{
  Picklist save(Map data){
    def itemsData = data.picklistItems ?: []
      data.remove("picklistItems")

      def picklist = Picklist.get(data.id) ?: new Picklist()
      picklist.properties = data
      picklist.name = picklist.requisition.name

      def picklistItems = itemsData.collect{ 
        itemData -> 
           def picklistItem = picklist.picklistItems?.find{i -> itemData.id  && i.id == itemData.id }
           if(picklistItem){
             picklistItem.properties = itemData
           }else{
             picklistItem = new PicklistItem(itemData)
             picklist.addToPicklistItems(picklistItem)
           }
           picklistItem
      }

      def itemsToDelete = picklist.picklistItems.findAll {
        dbItem ->!picklistItems.any{ clientItem-> clientItem.id == dbItem.id} 
      }
      itemsToDelete.each{picklist.removeFromPicklistItems(it)}
      picklist.save(flush:true)
      picklist.picklistItems?.each{it.save(flush:true)}
      picklist           
    
  }

}
