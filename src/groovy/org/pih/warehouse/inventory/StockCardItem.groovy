package org.pih.warehouse.inventory

class StockCardItem {

    Integer id;
    //StockCard stockCard;
    Date entryDate;
    Integer startingBalance;
    Integer quantityIncoming;
    Integer quantityOutgoing;
    Integer remainingBalance;

    static belongsTo = [ stockCard : StockCard ]

    static constraints = {
		stockCard(blank:true);
		entryDate(blank:true);
		startingBalance(blank:true);
		remainingBalance(blank:true);
		quantityIncoming(blank:true);
		quantityOutgoing(blank:true);
    }

    int compareTo(obj) {
		entryDate.compareTo(obj.entryDate)
    }


}
