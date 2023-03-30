import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { AutoSizer, InfiniteLoader, List } from 'react-virtualized';

import TableRow from 'components/form-elements/TableRow';
import { translateWithDefaultMessage } from 'utils/Translate';


const ROW_HEIGHT = 28;


class TableBodyVirtualized extends Component {
  constructor(props) {
    super(props);

    this.rowRenderer = this.rowRenderer.bind(this);
    this.getRowHeight = this.getRowHeight.bind(this);
    this.bindListRef = this.bindListRef.bind(this);
    this.getHeight = this.getHeight.bind(this);
  }

  componentDidUpdate() {
    if (this.list) {
      this.list.recomputeRowHeights();
    }
  }

  getHeight() {
    const { fieldsConfig: { subfieldKey, getDynamicRowAttr }, fields, properties } = this.props;
    let height = 0;
    const maxTableHeight = window.innerHeight < 900 ?
      // 0.35 * window.innerHeight = 35vh from table-content class in StockMovement.scss
      0.35 * window.innerHeight : 0.40 * window.innerHeight;

    if (!subfieldKey) {
      height = fields.value.reduce((acc, field) => {
        const dynamicAttr = getDynamicRowAttr ?
          getDynamicRowAttr({ ...properties, rowValues: field }) : {};
        // If a row is supposed to be hidden or the height is already higher than max height,
        // return this height without increasing it
        if (dynamicAttr.hideRow || acc > maxTableHeight) {
          return acc;
        }
        // If a row is not hidden and height has not yet reached maximum,
        // increase it by the row height
        return acc + ROW_HEIGHT;
      }, height);
    } else {
      _.forEach(fields.value, (field) => {
        const dynamicAttr = getDynamicRowAttr ?
          getDynamicRowAttr({ ...properties, rowValues: field }) : {};
        const subfields = field[subfieldKey];

        if (dynamicAttr.hideRow) {
          return; // Lodash's forEach version of continue
        }

        if (!height) {
          height = ROW_HEIGHT * (subfields.length + 1);
        } else if (height + (ROW_HEIGHT * (subfields.length + 1)) > maxTableHeight) {
          height = maxTableHeight;
        } else {
          height += (ROW_HEIGHT * (subfields.length + 1));
        }
      });
    }
    return height || ROW_HEIGHT;
  }

  getRowHeight({ index }) {
    const { fieldsConfig: { subfieldKey, getDynamicRowAttr }, fields, properties } = this.props;
    const rowValues = fields?.value?.[index];

    const dynamicAttr = getDynamicRowAttr && rowValues ?
      getDynamicRowAttr({ ...properties, index, rowValues }) : {};

    if (dynamicAttr.hideRow) {
      return 0;
    }

    if (!subfieldKey) {
      return ROW_HEIGHT;
    }


    const subfields = rowValues ? rowValues[subfieldKey] : null;

    if (!subfields) {
      return ROW_HEIGHT;
    }

    if (dynamicAttr.hideSubfields) {
      return ROW_HEIGHT;
    }

    return ROW_HEIGHT * (subfields.length + 1);
  }

  rowRenderer({
    key, index, style,
  }) {
    const {
      fieldsConfig, properties, fields, tableRef = () => {},
      addRow = (row = {}) => fields.push(row),
    } = this.props;
    const field = `${fields.name}[${index}]`;
    const RowComponent = fieldsConfig.rowComponent || TableRow;
    const { totalCount } = properties;

    if (fields.value[index]) {
      const dynamicRowAttr = fieldsConfig.getDynamicRowAttr ?
        fieldsConfig.getDynamicRowAttr({
          ...properties,
          rowValues: fields.value[index],
        }) : {};

      if (dynamicRowAttr.hideRow) {
        return null;
      }

      return (
        <div key={key} style={style}>
          <RowComponent
            field={field}
            index={index}
            properties={{
              ...properties,
              rowCount: totalCount,
            }}
            addRow={addRow}
            fieldsConfig={fieldsConfig}
            removeRow={() => fields.remove(index)}
            rowValues={fields.value[index]}
            rowRef={(el, fieldName) => tableRef(el, fieldName, index)}
          />
        </div>
      );
    }
    return (
      <div key={key} style={style}>
        {this.props.translate('react.default.loading.label', 'Loading...')}
      </div>
    );
  }

  bindListRef(ref) {
    this.list = ref;
  }

  render() {
    // eslint-disable-next-line max-len
    const { properties, pageSize } = this.props;
    const {
      totalCount, loadMoreRows, isRowLoaded, isFirstPageLoaded,
    } = properties;

    const loadPage = isFirstPageLoaded ? () => {} : loadMoreRows;

    return (
      <div>
        <InfiniteLoader
          loadMoreRows={loadPage}
          isRowLoaded={isRowLoaded}
          rowCount={totalCount}
          minimumBatchSize={pageSize}
        >
          {({ onRowsRendered }) => (
            <AutoSizer disableHeight>
              {({ width }) => (
                <List
                  ref={this.bindListRef}
                  height={this.getHeight()}
                  onRowsRendered={onRowsRendered}
                  rowCount={totalCount}
                  rowHeight={this.getRowHeight}
                  rowRenderer={this.rowRenderer}
                  width={width}
                  props={properties}
                />
              )}
            </AutoSizer>
          )}
        </InfiniteLoader>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  pageSize: state.session.pageSize,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {})(TableBodyVirtualized);

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
  tableRef: PropTypes.func,
  pageSize: PropTypes.number.isRequired,
  translate: PropTypes.func.isRequired,
};

TableBodyVirtualized.defaultProps = {
  addRow: undefined,
  tableRef: undefined,
};
