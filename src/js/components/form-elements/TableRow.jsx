import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

import { renderFormField } from 'utils/form-utils';
import RowSaveIndicator from 'utils/RowSaveIndicator';

class TableRow extends Component {
  shouldComponentUpdate(nextProps) {
    return !_.isEqualWith(this.props, nextProps, (objValue, othValue) => {
      if (typeof objValue === 'function' || typeof othValue === 'function') {
        return true;
      }

      return undefined;
    });
  }

  // eslint-disable-next-line class-methods-use-this
  rowRender(
    properties, fieldsConfig, focusFieldMap, field, addRow,
    removeRow, index, rowValues, rowRef,
  ) {
    return _.map(fieldsConfig.fields, (config, name) => {
      const dynamicAttr = config.getDynamicAttr ? config.getDynamicAttr(properties) : {};
      const { attributes } = config;
      const { hide } = dynamicAttr;
      const flexWidth = dynamicAttr.flexWidth || config.flexWidth;
      const fixedWidth = dynamicAttr.fixedWidth || config.fixedWidth;
      if (!hide) {
        return (
          <div
            key={`${field}.${name}`}
            className={`align-self-center ${attributes && attributes.cellClassName ? attributes.cellClassName : ''}`}
            style={{
              flex: fixedWidth ? `0 1 ${fixedWidth}` : `${flexWidth || '12'} 1 0`,
              minWidth: 0,
            }}
            role="cell"
            aria-label={config?.defaultMessage}
          >
            {renderFormField(config, `${field}.${name}`, {
              ...properties,
              arrayField: true,
              arrowsNavigation: fieldsConfig.arrowsNavigation,
              focusLeft: focusFieldMap[name].left,
              focusRight: focusFieldMap[name].right,
              focusThis: name,
              addRow,
              removeRow,
              rowIndex: index,
              fieldValue: config.fieldKey === '' ? rowValues : _.get(rowValues, config.fieldKey || name),
              fieldRef: (el) => rowRef(el, name),
            })}
          </div>
        );
      }
      return null;
    });
  }

  render() {
    const {
      fieldsConfig, index, field, addRow, properties, removeRow, rowValues = {}, rowRef = () => {},
    } = this.props;
    const fieldNames = _.keys(fieldsConfig.fields);
    const focusFieldMap = {};
    _.forEach(fieldNames, (name, ind) => {
      focusFieldMap[name] = {
        left: ind > 0 ? fieldNames[ind - 1] : null,
        right: ind < fieldNames.length - 1 ? fieldNames[ind + 1] : null,
      };
    });

    const dynamicRowAttr = fieldsConfig.getDynamicRowAttr
      ? fieldsConfig.getDynamicRowAttr({ ...properties, index, rowValues }) : {};
    const rowIndex = !_.isNil(properties.parentIndex) ? properties.parentIndex : index;
    const className = `table-row ${rowIndex % 2 === 0 ? 'even-row' : ''} ${dynamicRowAttr.className ? dynamicRowAttr.className : ''}`;
    const tooltip = dynamicRowAttr.tooltip ? dynamicRowAttr.tooltip : null;

    if (dynamicRowAttr.hideRow) {
      return null;
    }

    return (
      <div {...dynamicRowAttr} className={className} role="row">
        {tooltip && (
          <Tooltip
            title={tooltip}
            theme="transparent"
            delay="150"
            duration="250"
            hideDelay="50"
          >
            <div className="d-flex flex-row border-bottom table-inner-row">
              {properties.isAutosaveEnabled
                && <RowSaveIndicator lineItemSaveStatus={rowValues.rowSaveStatus} />}
              {this.rowRender(
                properties, fieldsConfig, focusFieldMap, field, addRow,
                removeRow, index, rowValues, rowRef,
              )}
            </div>
          </Tooltip>
        )}
        {!tooltip && (
          <div className="d-flex flex-row border-bottom table-inner-row">
            {properties.isAutosaveEnabled
              && <RowSaveIndicator lineItemSaveStatus={rowValues.rowSaveStatus} />}
            {this.rowRender(
              properties, fieldsConfig, focusFieldMap, field, addRow,
              removeRow, index, rowValues, rowRef,
            )}
          </div>
        )}
      </div>
    );
  }
}

export default TableRow;

TableRow.propTypes = {
  fieldsConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  index: PropTypes.number.isRequired,
  field: PropTypes.string.isRequired,
  addRow: PropTypes.func.isRequired,
  removeRow: PropTypes.func.isRequired,
  properties: PropTypes.shape({}).isRequired,
  rowValues: PropTypes.shape({}),
  rowRef: PropTypes.func,
};

TableRow.defaultProps = {
  rowValues: {},
  rowRef: undefined,
};
