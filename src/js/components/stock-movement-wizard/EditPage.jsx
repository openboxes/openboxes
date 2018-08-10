import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';

import ArrayField from '../form-elements/ArrayField';
import TextField from '../form-elements/TextField';
import { renderFormField } from '../../utils/form-utils';
import LabelField from '../form-elements/LabelField';
import SelectField from '../form-elements/SelectField';
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
        flexWidth: '0.7',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
        label: 'Code',
      },
      productName: {
        type: LabelField,
        flexWidth: '6',
        label: 'Product Name',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
      },
      quantityRequested: {
        type: LabelField,
        label: 'Qty requested',
        flexWidth: '0.8',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityAvailable: {
        type: LabelField,
        label: 'Qty available',
        flexWidth: '0.8',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityConsumed: {
        type: LabelField,
        label: 'Monthly consumption',
        flexWidth: '1.35',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      substituteButton: {
        label: 'Substitution',
        type: SubstitutionsModal,
        fieldKey: '',
        flexWidth: '1',
        attributes: {
          title: 'Substitutes',
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, stockMovementId, onResponse,
        }) => ({
          productCode: fieldValue.productCode,
          btnOpenText: fieldValue.substitutionStatus,
          btnOpenDisabled: fieldValue.substitutionStatus === 'NO' || fieldValue.statusCode === 'SUBSTITUTED',
          btnOpenClassName: BTN_CLASS_MAPPER[fieldValue.substitutionStatus || 'HIDDEN'],
          rowIndex,
          lineItem: fieldValue,
          stockMovementId,
          onResponse,
        }),
      },
      quantityRevised: {
        label: 'Revised Qty',
        type: TextField,
        fieldKey: 'statusCode',
        flexWidth: '1',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({ fieldValue, subfield }) => ({
          disabled: fieldValue === 'SUBSTITUTED' || subfield,
        }),
      },
      reasonCode: {
        type: SelectField,
        label: 'Reason code',
        flexWidth: '1.4',
        fieldKey: 'quantityRevised',
        getDynamicAttr: ({ fieldValue, subfield, reasonCodes }) => ({
          disabled: !fieldValue || subfield,
          options: reasonCodes,
          showValueTooltip: true,
        }),
      },
    },
  },
};

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

/**
 * The third step of stock movement(for movements from a depot) where user can see the
 * stock available and adjust quantities or make substitutions based on that information.
 */
class EditItemsPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      statusCode: '',
      redoAutopick: false,
      revisedItems: [],
      values: { ...this.props.initialValues, editPageItems: [] },
    };

    this.saveNewItems = this.saveNewItems.bind(this);
    this.props.showSpinner();
  }

  componentDidMount() {
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

      this.setState({ values: { ...this.state.values, editPageItems } });
      this.props.hideSpinner();
    }).catch(() => {
      this.props.hideSpinner();
    });
  }

  /**
   * Fetches data using function given as an argument(reducers components).
   * @param {function} fetchFunction
   * @public
   */
  fetchData(fetchFunction) {
    this.props.showSpinner();
    fetchFunction()
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Sends data of revised items with post method.
   * @param {object} values
   * @public
   */
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
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;
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

  /**
   * Transition to next stock movement status (PICKING)
   * after sending createPicklist: 'true' to backend autopick functionality is invoked.
   * @public
   */
  transitionToStep4() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'PICKING', createPicklist: 'true' };

    return apiClient.post(url, payload);
  }

  /**
   * Fetches 3rd step data from current stock movement.
   * @public
   */
  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=3`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
  }

  /**
   * Saves current stock movement progress (line items) and goes to the next stock movement step.
   * @param {object} formValues
   * @public
   */
  nextPage(formValues) {
    this.props.showSpinner();
    this.reviseRequisitionItems(formValues)
      .then(() => {
        if (this.state.statusCode === 'VERIFYING' || this.state.redoAutopick) {
          this.transitionToStep4()
            .then(() => this.props.onSubmit(formValues))
            .catch(() => this.props.hideSpinner());
        } else {
          this.props.onSubmit(formValues);
        }
      }).catch(() => this.props.hideSpinner());
  }

  /**
   * Saves changes made in subsitution modal and updates data.
   * @param {object} editPageItems
   * @public
   */
  saveNewItems(editPageItems) {
    this.setState({
      values: {
        ...this.state.values,
        editPageItems: [],
      },
    }, () => this.setState({
      values: {
        ...this.state.values,
        editPageItems,
      },
    }));
  }

  render() {
    return (
      <Form
        onSubmit={values => this.nextPage(values)}
        validate={validate}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values }) => (
          <form onSubmit={handleSubmit}>
            {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                stockMovementId: values.stockMovementId,
                reasonCodes: this.props.reasonCodes,
                onResponse: this.saveNewItems,
              }))}
            <div>
              <button type="button" className="btn btn-outline-primary btn-form" onClick={() => this.props.previousPage(values)}>
                Previous
              </button>
              <button type="submit" className="btn btn-outline-primary btn-form float-right">Next</button>
            </div>
          </form>
        )}
      />
    );
  }
}

const mapStateToProps = state => ({
  reasonCodesFetched: state.reasonCodes.fetched,
  reasonCodes: state.reasonCodes.data,
});

export default connect(mapStateToProps, {
  fetchReasonCodes, showSpinner, hideSpinner,
})(EditItemsPage);

EditItemsPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /**
   * Function called with the form data when the handleSubmit()
   * is fired from within the form component.
   */
  onSubmit: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function fetching reason codes */
  fetchReasonCodes: PropTypes.func.isRequired,
  /** Indicator if reason codes' data is fetched */
  reasonCodesFetched: PropTypes.bool.isRequired,
  /** Array of available reason codes */
  reasonCodes: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};
