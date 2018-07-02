import React from 'react';
import { reduxForm } from 'redux-form';
import PropTypes from 'prop-types';
import _ from 'lodash';

import ArrayField from '../form-elements/ArrayField';
import TextField from '../form-elements/TextField';
import { renderFormField } from '../../utils/form-utils';
import LabelField from '../form-elements/LabelField';
import SelectField from '../form-elements/SelectField';
import { REASON_CODE_MOCKS } from '../../mockedData';
import ValueSelectorField from '../form-elements/ValueSelectorField';
import SubstitutionsModal from './modals/SubstitutionsModal';
import TableRowWithSelector from '../form-elements/TableRowWithSelector';

const FIELDS = {
  lineItems: {
    type: ArrayField,
    rowComponent: TableRowWithSelector,
    rowAttributes: {
      formName: 'stock-movement-wizard',
      rowSelector: 'substituted',
    },
    getDynamicRowAttr: ({ selectedValue }) => (
      {
        className: selectedValue ? 'crossed-out' : '',
      }
    ),
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
      substituteButton: {
        label: 'Substitute available',
        type: ValueSelectorField,
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `lineItems[${rowIndex}].product.productCode`,
        }),
        component: SubstitutionsModal,
        componentConfig: {
          attributes: {
            btnOpenText: 'Yes',
            title: 'Substitutes',
          },
          getDynamicAttr: ({ selectedValue, rowIndex }) => ({
            productCode: selectedValue,
            rowIndex,
          }),
        },
      },
      revisedQuantity: {
        label: 'Revised Qty',
        type: ValueSelectorField,
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `lineItems[${rowIndex}].substituted`,
        }),
        component: TextField,
        componentConfig: {
          attributes: {
            type: 'number',
          },
          getDynamicAttr: ({ selectedValue }) => ({
            disabled: !!selectedValue,
          }),
        },
      },
      reasonCode: {
        type: ValueSelectorField,
        label: 'Reason code',
        component: SelectField,
        componentConfig: {
          attributes: {
            options: REASON_CODE_MOCKS,
          },
          getDynamicAttr: ({ selectedValue }) => ({
            disabled: !selectedValue,
          }),
        },
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `lineItems[${rowIndex}].revisedQuantity`,
        }),
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

function validate(values) {
  const errors = {};
  errors.lineItems = [];

  _.forEach(values.lineItems, (item, key) => {
    if (!_.isEmpty(item.revisedQuantity) && _.isEmpty(item.reasonCode)) {
      errors.lineItems[key] = { reasonCode: 'Reason code required' };
    } else if (_.isEmpty(item.revisedQuantity) && !_.isEmpty(item.reasonCode)) {
      errors.lineItems[key] = { revisedQuantity: 'Revised quantity required' };
    }
  });
  return errors;
}

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
