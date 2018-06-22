import React from 'react';
import PropTypes from 'prop-types';
import { FieldArray } from 'redux-form';

import TableBody from './TableBody';
import TableRow from './TableRow';

const TableRowWithSubfields = (props) => {
  const {
    fieldsConfig, index, field, properties,
  } = props;

  return [
    <TableRow key={field} {...props} />,
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
