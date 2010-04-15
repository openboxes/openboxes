package org.pih.warehouse

class StockCardEntry {

    Integer id;
    StockCard stockCard;
    Date entryDate;
    Integer startingBalance;
    Integer quantityIncoming;
    Integer quantityOutgoing;
    Integer remainingBalance;

    static belongsTo = [ StockCard ]

    static constraints = {
	stockCard(blank:true);
	entryDate(blank:true);
	startingBalance(blank:true);
	remainingBalance(blank:true);
	quantityIncoming(blank:true);
	quantityOutgoing(blank:true);
    }
}
