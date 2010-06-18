package org.pih.warehouse.web

class CategoryMenu {
    def addNode = {nodeElement, menuOption, tagList ->
        def nodes = [:]
        nodes[ menuOption.name ] =  menuOption

        def newList = tagList - nodeElement
        newList?.each {currentTag ->
            nodes[currentTag.name] = addNode(currentTag, menuOption, newList)
        }
        nodes
    }
}