package org.pih.warehouse.regression.platform.dashboard

import org.pih.warehouse.dashboard.*
import spock.lang.Specification

class TableDataSpec extends Specification {

    void 'the positional constructor sets the main columns'() {
        when:
        TableData row = new TableData('S-1', 'Boxes', '3', '/stockMovement/show/1', 'air.png')

        then:
        [row.number, row.name, row.value, row.link, row.icon] == ['S-1', 'Boxes', '3', '/stockMovement/show/1', 'air.png']
        row.valueLink == null

        when:
        row = new TableData('S-2', 'Crates')

        then:
        [row.number, row.name, row.value, row.link, row.icon] == ['S-2', 'Crates', null, null, null]
    }

    void 'the map constructor also sets the per-column links and the name list'() {
        when:
        TableData row = new TableData(number: '1', name: 'Gloves', value: '10', link: '/row', icon: 'i',
                valueLink: '/value', numberLink: '/number', nameLink: '/name',
                listNameData: ['Gloves', 'Masks'], nameLinksList: ['/g', '/m'])

        then:
        row.valueLink == '/value'
        row.numberLink == '/number'
        row.nameLink == '/name'
        row.nameDataList == ['Gloves', 'Masks']
        row.nameLinksList == ['/g', '/m']
    }

    void 'graph data keeps its data and link'() {
        given:
        def table = [toJson: { -> [rows: 1] }]

        when:
        GraphData graph = new GraphData(table, '/link')

        then:
        graph.toJson() == [data: [rows: 1], link: '/link']
        new GraphData(table).link == null
    }
}
