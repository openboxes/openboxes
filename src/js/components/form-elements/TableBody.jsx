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
    showRowSaveIndicator,
    tableRef = () => {
    },
    addRow = (row = {}) => fields.push(row),
  } = props;
  const RowComponent = properties.subfield ? TableRow : fieldsConfig.rowComponent || TableRow;

  // properties for loading screen in modal
  const {
    isEmptyData,
    isLoading,
    emptyDataMessageId,
    defaultEmptyDataMessage,
  } = properties;

  // if data is still fetching then spinner will be displayed within the table
  if (isLoading) {
    return (<Spinner />);
  }

  // if there is no data to display then message will be displayed
  // fields.length is checked due to possibility of adding custom data
  if (isEmptyData && !fields.length) {
    return (translate(
      emptyDataMessageId,
      defaultEmptyDataMessage,
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
          showRowSaveIndicator,
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
