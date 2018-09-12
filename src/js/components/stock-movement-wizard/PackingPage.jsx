import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';
import Alert from 'react-s-alert';

import ArrayField from '../form-elements/ArrayField';
import TextField from '../form-elements/TextField';
import { renderFormField } from '../../utils/form-utils';
import LabelField from '../form-elements/LabelField';
import SelectField from '../form-elements/SelectField';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import DateField from '../form-elements/DateField';
import PackingSplitLineModal from './modals/PackingSplitLineModal';

const debouncedUsersFetch = _.debounce((searchTerm, callback) => {
  if (searchTerm) {
    apiClient.get(`/openboxes/api/generic/person?name=${searchTerm}`)
      .then(result => callback(
        null,
        {
          complete: true,
          options: _.map(result.data.data, obj => (
            {
              value: {
                ...obj,
              },
              name: obj.name,
            }
          )),
        },
      ))
      .catch(error => callback(error, { options: [] }));
  } else {
    callback(null, { options: [] });
  }
}, 500);

const FIELDS = {
  packingPageItems: {
    type: ArrayField,
    fields: {
      productCode: {
        type: LabelField,
        flexWidth: '0.7',
        label: 'Code',
      },
      productName: {
        type: LabelField,
        label: 'Product Name',
        flexWidth: '6',
        attributes: {
          className: 'text-left ml-1',
        },
      },
      'binLocation.name': {
        type: LabelField,
        label: 'Bin Location',
        flexWidth: '1',
      },
      lotNumber: {
        type: LabelField,
        label: 'Lot/Serial No',
        flexWidth: '1',
      },
      expirationDate: {
        type: DateField,
        label: 'Expires',
        flexWidth: '1',
        attributes: {
          dateFormat: 'MM/DD/YYYY',
          disabled: true,
        },
      },
      shipped: {
        type: LabelField,
        label: 'Shipped',
        flexWidth: '0.8',
      },
      uom: {
        type: LabelField,
        label: 'UOM',
        flexWidth: '0.8',
      },
      recipient: {
        type: SelectField,
        label: 'Recipient',
        flexWidth: '2.5',
        fieldKey: '',
        attributes: {
          async: true,
          required: true,
          showValueTooltip: true,
          openOnClick: false,
          autoload: false,
          loadOptions: debouncedUsersFetch,
          cache: false,
          options: [],
          labelKey: 'name',
        },
      },
      pallet: {
        type: TextField,
        label: 'Pallet',
        flexWidth: '0.8',
      },
      box: {
        type: TextField,
        label: 'Box',
        flexWidth: '0.8',
      },
      splitLine: {
        type: PackingSplitLineModal,
        label: 'Split Line',
        flexWidth: '1',
        fieldKey: '',
        attributes: {
          title: 'Split Line',
        },
        getDynamicAttr: ({
          fieldValue, stockMovementId, onResponse,
        }) => ({
          productCode: fieldValue.productCode,
          btnOpenText: 'Split Line',
          btnOpenClassName: 'btn btn-outline-success',
          lineItem: fieldValue,
          stockMovementId,
          onResponse,
        }),
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.PackingPageItems = [];
  return errors;
}

/**
 * The fifth step of stock movement(for movements from a depot) where user can see the
 * packing information.
 */
class PackingPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      values: { ...this.props.initialValues, packingPageItems: [] },
    };

    this.props.showSpinner();
  }

  componentDidMount() {
    this.fetchLineItems().then((resp) => {
      const { packingPageItems } = resp.data.data;
      this.setState({ values: { ...this.state.values, packingPageItems } });
      this.props.hideSpinner();
    }).catch(() => {
      this.props.hideSpinner();
    });
  }

  /**
   * Saves packing data and refreshes page data
   * @param {object} formValues
   * @public
   */
  saveAndRefresh(formValues) {
    this.props.showSpinner();
    this.savePackingData(formValues)
      .then(() => {
        this.props.hideSpinner();
        Alert.success('Changes saved successfully!');
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Transition to next stock movement status
   * @public
   */
  transitionToNextStep() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'ISSUED' };

    return apiClient.post(url, payload);
  }

  /**
   * Fetches 5th step data from current stock movement.
   * @public
   */
  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=5`;

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
    this.savePackingData(formValues)
      .then(() => this.props.onSubmit(formValues))
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Saves packing data
   * @param {object} PackingPageItems
   * @public
   */
  savePackingData(PackingPageItems) {
    // TODO: save packing data request
  }

  saveSplittedLines() {}

  render() {
    return (
      <Form
        onSubmit={values => this.nextPage(values)}
        validate={validate}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            <span>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.saveAndRefresh(values)}
                className="float-right py-1 mb-1 btn btn-outline-secondary align-self-end"
              >
                <span><i className="fa fa-save pr-2" />Save & Refresh</span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                stockMovementId: values.stockMovementId,
                onResponse: this.saveSplittedLines(),
              }))}
              <div>
                <button type="button" className="btn btn-outline-primary btn-form" onClick={() => this.props.previousPage(values)}>
                  Previous
                </button>
                <button type="submit" className="btn btn-outline-primary btn-form float-right">Next</button>
              </div>
            </form>
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = state => ({
  recipients: state.users.data,
  recipientsFetched: state.users.fetched,
});

export default (connect(mapStateToProps, {
  showSpinner, hideSpinner,
})(PackingPage));

PackingPage.propTypes = {
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
};
