import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import { renderFormField } from '../../utils/form-utils';

const TableRow = (props) => {
  const {
    fieldsConfig, index, field, addRow, properties, removeRow, rowValues = {},
  } = props;

  const dynamicAttr = fieldsConfig.getDynamicRowAttr ? fieldsConfig.getDynamicRowAttr(props) : {};

  return (
    <tr {...dynamicAttr}>
      { _.map(fieldsConfig.fields, (config, name) => (
        <td key={`${field}.${name}`} className="align-middle">
          { renderFormField(config, `${field}.${name}`, {
            ...properties,
            arrayField: true,
            addRow,
            removeRow,
            rowIndex: index,
            fieldValue: _.get(rowValues, name),
          })}
        </td>
      ))}
    </tr>);
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
