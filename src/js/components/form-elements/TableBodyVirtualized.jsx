import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { AutoSizer, List } from 'react-virtualized';

import TableRow from './TableRow';

class TableBodyVirtualized extends Component {
  constructor(props) {
    super(props);

    this.state = { isScrolling: false };

    this.rowRenderer = this.rowRenderer.bind(this);
    this.getRowHeight = this.getRowHeight.bind(this);
    this.debounceScrolling = _.debounce((isScrolling) => { this.setState({ isScrolling }); }, 1000);
  }

  getRowHeight({ index }) {
    const { fieldsConfig: { subfieldKey, getDynamicRowAttr }, fields, properties } = this.props;

    if (!subfieldKey) {
      return 50;
    }

    const rowValues = fields.get(index);
    const subfields = rowValues[subfieldKey];

    if (!subfields) {
      return 50;
    }

    const dynamicAttr = getDynamicRowAttr ?
      getDynamicRowAttr({ ...properties, index, rowValues }) : {};

    if (dynamicAttr.hideSubfields) {
      return 50;
    }

    return 50 * (subfields.length + 1);
  }

  rowRenderer({
    key, index, style, isScrolling,
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
          fieldPreview={this.state.isScrolling || isScrolling}
          addRow={addRow}
          fieldsConfig={fieldsConfig}
          removeRow={() => fields.remove(index)}
          rowValues={fields.get(index)}
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
              rowCount={fields.length}
              rowHeight={this.getRowHeight}
              rowRenderer={this.rowRenderer}
              width={width}
              props={{ ...this.props.properties, isScrolling: this.state.isScrolling }}
              onScroll={() => {
                if (!this.state.isScrolling) {
                  this.setState({ isScrolling: true });
                }
                this.debounceScrolling(false);
              }}
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
