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
import ValueSelectorField from '../form-elements/ValueSelectorField';
import SubstitutionsModal from './modals/SubstitutionsModal';
import apiClient from '../../utils/apiClient';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import { showSpinner, hideSpinner, fetchReasonCodes } from '../../actions';

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
    getDynamicRowAttr: ({ rowValues, subfield }) => {
      let className = rowValues.statusCode === 'SUBSTITUTED' ? 'crossed-out ' : '';
      if (!subfield) { className += 'font-weight-bold'; }
      return { className };
    },
    subfieldKey: 'substitutionItems',
    fields: {
      productCode: {
        type: LabelField,
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
        label: 'Code',
      },
      productName: {
        type: LabelField,
        label: 'Product Name',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
      },
      quantityRequested: {
        type: LabelField,
        label: 'Qty requested',
        fixedWidth: '130px',
      },
      quantityAvailable: {
        type: LabelField,
        label: 'Qty available',
        fixedWidth: '130px',
      },
      quantityConsumed: {
        type: LabelField,
        label: 'Monthly consumption',
        fixedWidth: '170px',
      },
      substituteButton: {
        label: 'Substitute available',
        type: SubstitutionsModal,
        fieldKey: '',
        fixedWidth: '200px',
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
        fixedWidth: '140px',
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
        fixedWidth: '400px',
        component: SelectField,
        componentConfig: {
          getDynamicAttr: ({ selectedValue, subfield, reasonCodes }) => ({
            disabled: !selectedValue || subfield,
            options: reasonCodes,
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

    this.state = {
      statusCode: '',
      redoAutopick: false,
      revisedItems: [],
    };

    this.props.showSpinner();
  }

  componentDidMount() {
    this.props.change('stock-movement-wizard', 'editPageItems', []);

    if (!this.props.reasonCodesFetched) {
      this.fetchData(this.props.fetchReasonCodes);
    }

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

      this.setState({
        statusCode,
        revisedItems: _.filter(editPageItems, item => item.statusCode === 'CHANGED'),
      });

      this.props.change('stock-movement-wizard', 'editPageItems', editPageItems);
      this.props.hideSpinner();
    }).catch(() => {
      this.props.hideSpinner();
    });
  }

  fetchData(fetchFunction) {
    this.props.showSpinner();
    fetchFunction()
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  reviseRequisitionItems(values) {
    const itemsToRevise = _.filter(
      values.editPageItems,
      (item) => {
        if (item.quantityRevised && item.reasonCode) {
          const oldRevision = _.find(
            this.state.revisedItems,
            revision => revision.requisitionItemId === item.requisitionItemId,
          );
          return _.isEmpty(oldRevision) ? true :
            (oldRevision.quantityRevised !== item.quantityRevised);
        }
        return false;
      },
    );
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}`;
    const payload = {
      lineItems: _.map(itemsToRevise, item => ({
        id: item.requisitionItemId,
        quantityRevised: item.quantityRevised,
        reasonCode: item.reasonCode,
      })),
    };

    if (payload.lineItems.length) {
      this.setState({ redoAutopick: true });
      return apiClient.post(url, payload);
    }

    return Promise.resolve();
  }

  transitionToStep4() {
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}/status`;
    const payload = { status: 'PICKING', createPicklist: 'true' };

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
        if (this.state.statusCode === 'VERIFYING' || this.state.redoAutopick) {
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
            reasonCodes: this.props.reasonCodes,
          }))}
        <div>
          <button type="button" className="btn btn-outline-primary btn-form" onClick={this.props.previousPage}>
            Previous
          </button>
          <button type="submit" className="btn btn-outline-primary btn-form float-right">Next</button>
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
    } else if (_.isNil(item.quantityRevised) && !_.isEmpty(item.reasonCode) && item.statusCode !== 'SUBSTITUTED') {
      errors.editPageItems[key] = { quantityRevised: 'Revised quantity required' };
    }
    if (parseInt(item.quantityRevised, 10) === item.quantityRequested) {
      errors.editPageItems[key] = {
        quantityRevised: 'Revised quantity can\'t be the same as requested quantity',
      };
    }
    if (_.isNil(item.quantityRevised) && (item.quantityRequested > item.quantityAvailable) && (item.statusCode !== 'SUBSTITUTED')) {
      errors.editPageItems[key] = { quantityRevised: 'Revise quantity! Quantity available is lower than requested' };
    }
    if (!_.isEmpty(item.quantityRevised) && (item.quantityRevised > item.quantityAvailable)) {
      errors.editPageItems[key] = { quantityRevised: 'Revised quantity exceeds quantity available' };
    }
  });
  return errors;
}
const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({
  stockMovementId: selector(state, 'requisitionId'),
  reasonCodesFetched: state.reasonCodes.fetched,
  reasonCodes: state.reasonCodes.data,
});

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(connect(mapStateToProps, {
  change, fetchReasonCodes, showSpinner, hideSpinner,
})(EditItemsPage));

EditItemsPage.propTypes = {
  handleSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  change: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  stockMovementId: PropTypes.string.isRequired,
  fetchReasonCodes: PropTypes.func.isRequired,
  reasonCodesFetched: PropTypes.bool.isRequired,
  reasonCodes: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};
