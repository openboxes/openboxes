import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { AutoSizer, List } from 'react-virtualized';

import TableRow from './TableRow';

class TableBodyVirtualized extends Component {
  constructor(props) {
    super(props);

    this.rowRenderer = this.rowRenderer.bind(this);
    this.getRowHeight = this.getRowHeight.bind(this);
  }

  getRowHeight({ index }) {
    const { fieldsConfig: { subfieldKey, getDynamicRowAttr }, fields, properties } = this.props;

    if (!subfieldKey) {
      return 28;
    }

    const rowValues = fields.value[index];
    const subfields = rowValues[subfieldKey];

    if (!subfields) {
      return 28;
    }

    const dynamicAttr = getDynamicRowAttr ?
      getDynamicRowAttr({ ...properties, index, rowValues }) : {};

    if (dynamicAttr.hideSubfields) {
      return 28;
    }

    return 28 * (subfields.length + 1);
  }

  rowRenderer({
    key, index, style,
  }) {
    const {
      fieldsConfig, properties, fields,
      addRow = (row = {}) => fields.push(row),
    } = this.props;
    const field = `${fields.name}[${index}]`;
    const RowComponent = fieldsConfig.rowComponent || TableRow;

    return (
      <div key={key} style={style}>
        <RowComponent
          field={field}
          index={index}
          properties={properties}
          addRow={addRow}
          fieldsConfig={fieldsConfig}
          removeRow={() => fields.remove(index)}
          rowValues={fields.value[index]}
        />
      </div>
    );
  }

  render() {
    const { fields } = this.props;

    return (
      <div>
        <AutoSizer disableHeight>
          {({ width }) => (
            <List
              height={450}
              overscanRowCount={3}
              rowCount={fields.length || 0}
              rowHeight={this.getRowHeight}
              rowRenderer={this.rowRenderer}
              width={width}
              props={this.props.properties}
            />
          )}
        </AutoSizer>
      </div>
    );
  }
}

export default TableBodyVirtualized;

TableBodyVirtualized.propTypes = {
  fieldsConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  fields: PropTypes.oneOfType([
    PropTypes.shape({}),
    PropTypes.arrayOf(PropTypes.shape({})),
  ]).isRequired,
  properties: PropTypes.shape({}).isRequired,
  addRow: PropTypes.func,
};

TableBodyVirtualized.defaultProps = {
  addRow: undefined,
};
