import React from 'react';
import { reduxForm } from 'redux-form';
import PropTypes from 'prop-types';
import _ from 'lodash';

import validate from './validate';
import ArrayField from '../form-elements/ArrayField';
import TextField from '../form-elements/TextField';
import ButtonField from '../form-elements/ButtonField';
import { renderFormField } from '../../utils/form-utils';
import LabelField from '../form-elements/LabelField';
import SelectField from '../form-elements/SelectField';
import { REASON_CODE_MOCKS } from '../../mockedData';

const FIELDS = {
  lineItems: {
    type: ArrayField,
    fields: {
      product: {
        type: LabelField,
        label: 'Requisition items',
        attributes: {
          formatValue: value => (value.name),
        },
      },
      quantity: {
        type: LabelField,
        label: 'Qty requested',
      },
      maxQuantity: {
        type: LabelField,
        label: 'Qty available',
      },
      monthlyConsumption: {
        type: LabelField,
        label: 'Monthly consumption',
      },
      button: {
        type: ButtonField,
        label: 'Substitute available',
        buttonLabel: 'Yes',
        attributes: {
          className: 'btn btn-outline-primary',
        },
      },
      revisedQuantity: {
        type: TextField,
        label: 'Revised qty',
      },
      reasonCode: {
        type: SelectField,
        label: 'Reason code',
        attributes: {
          options: REASON_CODE_MOCKS,
        },
      },
    },
  },
};

const EditItemsPage = (props) => {
  const { handleSubmit, previousPage } = props;

  return (
    <form onSubmit={handleSubmit}>
      {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName))}
      <div>
        <button type="button" className="btn btn-outline-primary" onClick={previousPage}>
          Previous
        </button>
        <button type="submit" className="btn btn-outline-primary float-right">Next</button>
      </div>
    </form>
  );
};

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(EditItemsPage);

EditItemsPage.propTypes = {
  handleSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
};
