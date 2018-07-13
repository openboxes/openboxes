import React, { Component } from 'react';
import { reduxForm, formValueSelector, change } from 'redux-form';
import { connect } from 'react-redux';
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
import apiClient from '../../utils/apiClient';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import { showSpinner, hideSpinner } from '../../actions';

const BTN_CLASS_MAPPER = {
  YES: 'btn btn-outline-success',
  NO: 'disabled btn btn-outline-secondary',
  EARLIER: 'btn btn-outline-warning',
};

const FIELDS = {
  editPageItems: {
    type: ArrayField,
    rowComponent: TableRowWithSubfields,
    getDynamicRowAttr: ({ rowValues }) => (
      {
        className: rowValues.statusCode === 'SUBSTITUTED' ? 'crossed-out' : '',
      }
    ),
    subfieldKey: 'substitutions',
    fields: {
      productCode: {
        type: LabelField,
        label: 'Code',
      },
      productName: {
        type: LabelField,
        label: 'Product',
      },
      quantityRequested: {
        type: LabelField,
        label: 'Qty requested',
      },
      quantityAvailable: {
        type: LabelField,
        label: 'Qty available',
      },
      monthlyConsumption: {
        type: LabelField,
        label: 'Monthly consumption',
      },
      substituteButton: {
        label: 'Substitute available',
        type: SubstitutionsModal,
        fieldKey: '',
        attributes: {
          title: 'Substitutes',
        },
        getDynamicAttr: ({ fieldValue, rowIndex }) => ({
          productCode: fieldValue.productCode,
          btnOpenText: fieldValue.substitutionStatus,
          btnOpenDisabled: fieldValue.substitutionStatus === 'NO',
          btnOpenClassName: BTN_CLASS_MAPPER[fieldValue.substitutionStatus],
          rowIndex,
          lineItem: fieldValue,
        }),
      },
      revisedQuantity: {
        label: 'Revised Qty',
        type: TextField,
        fieldKey: 'statusCode',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: fieldValue === 'SUBSTITUTED',
        }),
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
          field: `editPageItems[${rowIndex}].revisedQuantity`,
        }),
      },
    },
  },
};

class EditItemsPage extends Component {
  constructor(props) {
    super(props);

    this.state = { statusCode: '' };

    this.props.showSpinner();
  }

  componentDidMount() {
    this.props.change('stock-movement-wizard', 'editPageItems', []);
    this.fetchLineItems().then((resp) => {
      const { statusCode, editPage } = resp.data.data;
      const editPageItems = _.map(
        editPage.editPageItems,
        val => ({
          ...val,
          disabled: true,
          rowKey: _.uniqueId('lineItem_'),
          product: {
            ...val.product,
            label: `${val.productCode} ${val.productName}`,
          },
        }),
      );

      this.setState({ statusCode });

      this.props.change('stock-movement-wizard', 'editPageItems', editPageItems);
      this.props.hideSpinner();
    }).catch(() => {
      this.props.hideSpinner();
    });
  }

  // TODO
  // updateRequisitionItems() {
  // TODO const itemsToUpdate = filter items with revised qty
  // TODO const url = ``;
  // TODO payload = { itemsToUpdate }
  // TODO apiClient.post(url, payload)
  // }

  transitionToStep4() {
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}/status`;
    const payload = { status: 'PICKING' };

    apiClient.post(url, payload);
  }

  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}?stepNumber=3`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
  }

  // TODO
  nextPage(formValues) {
    // TODO check which items have filled revisedqty and reason
    // TODO make 'changes' field for those items
    // TODO this.updateRequisitionItems request
    // TODO then this.props.onSubmit();
    // TODO catch this.props.hideSpinner();
    // TODO if (statusCode === 'VERIFYING') { this.transitionToStep4(); }
    this.props.onSubmit();
  }

  render() {
    return (
      <form onSubmit={this.props.handleSubmit(values => this.nextPage(values))}>
        {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName))}
        <div>
          <button type="button" className="btn btn-outline-primary" onClick={this.props.previousPage}>
            Previous
          </button>
          <button type="submit" className="btn btn-outline-primary float-right">Next</button>
        </div>

      </form>
    );
  }
}

function validate(values) {
  const errors = {};
  errors.editPageItems = [];

  _.forEach(values.editPageItems, (item, key) => {
    if (!_.isEmpty(item.revisedQuantity) && _.isEmpty(item.reasonCode)) {
      errors.editPageItems[key] = { reasonCode: 'Reason code required' };
    } else if (_.isEmpty(item.revisedQuantity) && !_.isEmpty(item.reasonCode)) {
      errors.editPageItems[key] = { revisedQuantity: 'Revised quantity required' };
    }
  });
  return errors;
}
const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({
  stockMovementId: selector(state, 'requisitionId'),
});

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(connect(mapStateToProps, { change, showSpinner, hideSpinner })(EditItemsPage));

EditItemsPage.propTypes = {
  handleSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  change: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  stockMovementId: PropTypes.string.isRequired,
};
