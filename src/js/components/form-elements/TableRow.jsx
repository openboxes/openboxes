import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { renderFormField } from '../../utils/form-utils';

class TableRow extends Component {
  shouldComponentUpdate(nextProps) {
    return !_.isEqualWith(this.props, nextProps, (objValue, othValue) => {
      if (typeof objValue === 'function' || typeof othValue === 'function') {
        return true;
      }

      return undefined;
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

    const dynamicRowAttr = fieldsConfig.getDynamicRowAttr ?
      fieldsConfig.getDynamicRowAttr({ ...properties, index, rowValues }) : {};
    const rowIndex = !_.isNil(properties.parentIndex) ? properties.parentIndex : index;
    const className = `table-row ${rowIndex % 2 === 0 ? 'even-row' : ''} ${dynamicRowAttr.className ? dynamicRowAttr.className : ''}`;

    return (
      <div {...dynamicRowAttr} className={className}>
        <div className="d-flex flex-row border-bottom table-inner-row">
          {_.map(fieldsConfig.fields, (config, name) => {
            const dynamicAttr = config.getDynamicAttr ? config.getDynamicAttr(properties) : {};
            const { hide } = dynamicAttr;
            if (!hide) {
              return (
                <div
                  key={`${field}.${name}`}
                  className="align-self-center mx-1"
                  style={{
                    flex: config.fixedWidth ? `0 1 ${config.fixedWidth}` : `${config.flexWidth || '12'} 1 0`,
                    minWidth: 0,
                  }}
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
                    fieldRef: el => rowRef(el, name),
                  })}
                </div>
              );
            }
            return null;
})}
        </div>
      </div>);
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
