import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import { FieldArray } from 'redux-form';

import TableBody from './TableBody';
import { renderFormField } from '../../utils/form-utils';

const TableRowWithSubfields = (props) => {
  const {
    fieldsConfig, index, field, addRow, properties, removeRow,
  } = props;

  const dynamicAttr = fieldsConfig.getDynamicRowAttr ? fieldsConfig.getDynamicRowAttr(props) : {};

  return [
    <tr key={field} {...dynamicAttr}>
      { _.map(fieldsConfig.fields, (config, name) => (
        <td key={`${field}.${name}`} className="align-middle">
          { !config.subfield && renderFormField(config, `${field}.${name}`, {
            ...properties,
            arrayField: true,
            addRow,
            removeRow,
            rowIndex: index,
          })}
        </td>
      ))}
    </tr>,
    <FieldArray
      key={`${field}.${fieldsConfig.subfieldKey}`}
      name={`${field}.${fieldsConfig.subfieldKey}`}
      component={TableBody}
      fieldsConfig={fieldsConfig}
      properties={{
        ...properties,
        parentIndex: index,
        subfield: true,
      }}
    />,
  ];
};

export default TableRowWithSubfields;

TableRowWithSubfields.propTypes = {
  fieldsConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  index: PropTypes.number.isRequired,
  field: PropTypes.string.isRequired,
  addRow: PropTypes.func.isRequired,
  removeRow: PropTypes.func.isRequired,
  properties: PropTypes.shape({}).isRequired,
};
