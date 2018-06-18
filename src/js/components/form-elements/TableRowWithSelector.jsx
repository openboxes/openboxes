import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { formValueSelector } from 'redux-form';
import PropTypes from 'prop-types';
import TableRow from './TableRow';

const withValueSelector = ({
  fieldsConfig,
  index,
  field,
  ...props
}) => {
  const { formName, rowSelector } = fieldsConfig.rowAttributes || {};

  const selector = formName ? formValueSelector(formName) : null;
  const selectedField = rowSelector ? `${field}.${rowSelector}` : field;

  return connect(state => ({
    fieldsConfig,
    index,
    field,
    ...props,
    selectedValue: selector ? selector(state, selectedField) : null,
  }))(TableRow);
};

withValueSelector.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
};

class TableRowWithSelector extends Component {
  shouldComponentUpdate(nextProps) {
    return !_.isEqualWith(this.props, nextProps, (objValue, othValue) => {
      if (typeof objValue === 'function' || typeof othValue === 'function') {
        return true;
      }

      return undefined;
    });
  }

  render() {
    const Row = withValueSelector(this.props);

    return (
      <Row />
    );
  }
}

export default TableRowWithSelector;
