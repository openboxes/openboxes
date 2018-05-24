import _ from 'lodash';
import React, { Component } from 'react';
import { reduxForm, initialize } from 'redux-form';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import validate from './validate';
import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import ArrayField from '../form-elements/ArrayField';
import ButtonField from '../form-elements/ButtonField';
import { renderFormField } from '../../utils/form-utils';
import { PRODUCTS_MOCKS } from '../../mockedData';

const FIELDS = {
  lineItems: {
    type: ArrayField,
    addButton: 'Add line',
    fields: {
      productCode: {
        type: SelectField,
        label: 'Requisition items',
        attributes: {
          openOnClick: false,
          options: PRODUCTS_MOCKS,
        },
      },
      quantity: {
        type: TextField,
        label: 'Quantity',
      },
      deleteButton: {
        type: ButtonField,
        label: 'Delete',
        buttonLabel: 'Delete',
        getDynamicAttr: ({ removeRow }) => ({
          onClick: removeRow,
        }),
        attributes: {
          className: 'btn btn-outline-danger',
        },
      },
    },
  },
};

class AddItemsPage extends Component {
  componentDidMount() {
    const lineItems = new Array(20).fill({});
    this.props.initialize('stock-movement-wizard', { lineItems }, true);
  }

  render() {
    const { handleSubmit, previousPage } = this.props;

    return (
      <form onSubmit={handleSubmit}>
        {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName))}
        <div>
          <button type="button" className="btn btn-outline-primary" onClick={previousPage}>
            Previous
          </button>
          <button type="submit" className="btn btn-outline-primary">Next</button>
        </div>
      </form>
    );
  }
}

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(connect(null, { initialize })(AddItemsPage));

AddItemsPage.propTypes = {
  initialize: PropTypes.func.isRequired,
  handleSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
};
