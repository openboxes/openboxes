import React from 'react';
import { reduxForm } from 'redux-form';
import PropTypes from 'prop-types';
import _ from 'lodash';

import validate from './validate';
import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import DateField from '../form-elements/DateField';
import { renderFormField } from '../../utils/form-utils';
import { LOCATION_MOCKS, USERNAMES_MOCKS, STOCK_LIST_MOCKS } from '../../mockedData';

const FIELDS = {
  description: {
    type: TextField,
    label: 'Description',
    attributes: {
      required: true,
    },
  },
  origin: {
    type: SelectField,
    label: 'Origin',
    attributes: {
      required: true,
      options: LOCATION_MOCKS,
    },
  },
  destination: {
    type: SelectField,
    label: 'Destination',
    attributes: {
      required: true,
      options: LOCATION_MOCKS,
    },
  },
  stockList: {
    type: SelectField,
    label: 'Stock list',
    attributes: {
      options: STOCK_LIST_MOCKS,
    },
  },
  requestedBy: {
    type: SelectField,
    label: 'Requested-by',
    attributes: {
      required: true,
      options: USERNAMES_MOCKS,
    },
  },
  dateRequested: {
    type: DateField,
    label: 'Date requested',
    attributes: {
      required: true,
      dateFormat: 'DD/MMM/YYYY',
    },
  },
};

const CreateStockMovement = (props) => {
  const { handleSubmit } = props;
  return (
    <form onSubmit={handleSubmit}>
      {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName))}
      <div className="row col-md-6">
        <button type="submit" className="btn btn-outline-primary text-right">
          Next
        </button>
      </div>
    </form>
  );
};

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(CreateStockMovement);

CreateStockMovement.propTypes = {
  handleSubmit: PropTypes.func.isRequired,
};
