import React from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import TableRow from 'components/form-elements/TableRow';
import Spinner from 'components/spinner/Spinner';
import { translateWithDefaultMessage } from 'utils/Translate';

const TableBody = (props) => {
  const {
    translate,
    fieldsConfig,
    properties,
    fields,
    tableRef = () => {
    },
    addRow = (row = {}) => fields.push(row),
  } = props;
  const RowComponent = properties.subfield ? TableRow : fieldsConfig.rowComponent || TableRow;

  const {
    emptySubstitutions,
    loadingSubstitutions,
  } = properties;

  if (loadingSubstitutions) {
    return (<Spinner />);
  }

  if (emptySubstitutions && !fields.length) {
    return (translate(
      'react.stockMovement.noSubstitutionsAvailable.message',
      'There are no substitutions available',
    ));
  }

  return (
    fields.map((field, index) => (
      <RowComponent
        key={properties.subfield ? `${properties.parentIndex}-${index}` : index}
        field={field}
        index={index}
        properties={{
          ...properties,
          rowCount: fields.length || 0,
        }}
        addRow={addRow}
        fieldsConfig={fieldsConfig}
        removeRow={() => fields.remove(index)}
        rowValues={fields.value[index]}
        rowRef={(el, fieldName) => tableRef(el, fieldName, index)}
      />))
  );
};

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

/** @component */
export default connect(mapStateToProps)(TableBody);

TableBody.propTypes = {
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
};

TableBody.defaultProps = {
  addRow: undefined,
  tableRef: undefined,
};
