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
  HIDDEN: 'btn invisible',
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
    subfieldKey: 'substitutionItems',
    fields: {
      productCode: {
        type: LabelField,
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-4',
        }),
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
        getDynamicAttr: ({
          fieldValue, rowIndex, stockMovementId,
        }) => ({
          productCode: fieldValue.productCode,
          btnOpenText: fieldValue.substitutionStatus,
          btnOpenDisabled: fieldValue.substitutionStatus === 'NO' || fieldValue.statusCode === 'SUBSTITUTED',
          btnOpenClassName: BTN_CLASS_MAPPER[fieldValue.substitutionStatus || 'HIDDEN'],
          rowIndex,
          lineItem: fieldValue,
          stockMovementId,
        }),
      },
      quantityRevised: {
        label: 'Revised Qty',
        type: TextField,
        fieldKey: 'statusCode',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({ fieldValue, subfield }) => ({
          disabled: fieldValue === 'SUBSTITUTED' || subfield,
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
          getDynamicAttr: ({ selectedValue, subfield }) => ({
            disabled: !selectedValue || subfield,
          }),
        },
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `editPageItems[${rowIndex}].quantityRevised`,
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

  reviseRequisitionItems(values) {
    const itemsToRevise = _.filter(
      values.editPageItems,
      item => item.quantityRevised && item.reasonCode,
    );
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}`;
    const payload = {
      lineItems: _.map(itemsToRevise, item => ({
        id: item.requisitionItemId,
        quantityRevised: item.quantityRevised,
        reasonCode: item.reasonCode,
      })),
    };
    return apiClient.post(url, payload);
  }

  transitionToStep4() {
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}/status`;
    const payload = { status: 'PICKING' };

    return apiClient.post(url, payload);
  }

  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}?stepNumber=3`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
  }

  nextPage(formValues) {
    this.props.showSpinner();
    this.reviseRequisitionItems(formValues)
      .then(() => {
        if (this.state.statusCode === 'VERIFYING') {
          this.transitionToStep4()
            .then(() => this.props.onSubmit())
            .catch(() => this.props.hideSpinner());
        } else {
          this.props.onSubmit();
        }
      }).catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <form onSubmit={this.props.handleSubmit(values => this.nextPage(values))}>
        {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
          stockMovementId: this.props.stockMovementId,
        }))}
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
    if (!_.isEmpty(item.quantityRevised) && _.isEmpty(item.reasonCode)) {
      errors.editPageItems[key] = { reasonCode: 'Reason code required' };
    } else if (_.isEmpty(item.quantityRevised) && !_.isEmpty(item.reasonCode)) {
      errors.editPageItems[key] = { quantityRevised: 'Revised quantity required' };
    }
  });
  return errors;
}
const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({
  stockMovementId: selector(state, 'requisitionId'),
  editPageItems: selector(state, 'editPageItems'),
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
  editPageItems: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};
