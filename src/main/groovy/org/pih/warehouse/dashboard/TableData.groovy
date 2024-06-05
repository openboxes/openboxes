package org.pih.warehouse.dashboard

class TableData implements Serializable {

    String number
    String name // Single value for displaying in the middle column of the table
    List<String> nameDataList // List of values, displaying each value in a new line within the same row
    String value
    String link // Link redirecting on clicking on the whole row
    String icon
    String valueLink // Link redirecting only on the value text
    String numberLink // Link redirecting only on the number text
    String nameLink // Link redirecting only on the name text
    List<String> nameLinksList // List of links applied to the nameDataList, matched by index

    TableData(String number, String name, String value = null, String link = null, String icon = null) {
        this.number = number
        this.name = name
        this.value = value
        this.link = link
        this.icon = icon
    }

    TableData(Map args) {
        this.number = args.number
        this.name = args.name
        this.value = args.value
        this.link = args.link
        this.icon = args.icon
        this.valueLink = args.valueLink
        this.numberLink = args.numberLink
        this.nameLink = args.nameLink
        this.nameDataList = args.listNameData
        this.nameLinksList = args.nameLinksList
    }

    Map toJson() {
        [
                "name"          : name.toJson(),
                "number"        : number.toJson(),
                "value"         : value.toJson(),
                "link"          : link.toJson(),
                "icon"          : icon.toJson(),
                "valueLink"     : valueLink ? valueLink.toJson() : null,
                "numberLink"    : numberLink ? numberLink.toJson() : null,
                "nameLink"      : nameLink ? nameLink.toJson() : null,
                "listNameData"  : nameDataList ? nameDataList.toJson() : null,
                "nameLinksList" : nameLinksList ? nameLinksList.toJson() : null,
        ]
    }
}
