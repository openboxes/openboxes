import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import { renderFormField } from '../../utils/form-utils';

const TableRow = (props) => {
  const {
    fieldsConfig, index, field, addRow, properties, removeRow, rowValues = {},
  } = props;

  const dynamicAttr = fieldsConfig.getDynamicRowAttr ? fieldsConfig.getDynamicRowAttr(props) : {};
  const rowIndex = properties.parentIndex || index;
  const className = `table-row ${rowIndex % 2 === 0 ? 'even-row' : ''} ${dynamicAttr.className ? dynamicAttr.className : ''}`;

  return (
    <div {...dynamicAttr} className={`d-flex flex-row border-bottom ${className}`}>
      { _.map(fieldsConfig.fields, (config, name) => (
        <div key={`${field}.${name}`} className="align-self-center mx-1" style={{ flex: '1 1 0', minWidth: 0 }}>
          { renderFormField(config, `${field}.${name}`, {
            ...properties,
            arrayField: true,
            addRow,
            removeRow,
            rowIndex: index,
            fieldValue: _.get(rowValues, name),
          })}
        </div>
      ))}
    </div>);
};

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
};

TableRow.defaultProps = {
  rowValues: {},
};
