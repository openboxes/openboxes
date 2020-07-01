import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';
import Alert from 'react-s-alert';
import update from 'immutability-helper';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';

import ArrayField from '../../form-elements/ArrayField';
import TextField from '../../form-elements/TextField';
import { renderFormField } from '../../../utils/form-utils';
import LabelField from '../../form-elements/LabelField';
import SelectField from '../../form-elements/SelectField';
import apiClient, { flattenRequest } from '../../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../../actions';
import PackingSplitLineModal from '../modals/PackingSplitLineModal';
import { debounceUsersFetch } from '../../../utils/option-utils';
import Translate, { translateWithDefaultMessage } from '../../../utils/Translate';

const FIELDS = {
  packPageItems: {
    type: ArrayField,
    arrowsNavigation: true,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    fields: {
      productCode: {
        type: LabelField,
        flexWidth: '0.7',
        headerAlign: 'left',
        label: 'react.stockMovement.code.label',
        defaultMessage: 'Code',
        attributes: {
          className: 'text-left ml-1',
        },
      },
      productName: {
        type: LabelField,
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product Name',
        flexWidth: '3',
        headerAlign: 'left',
        attributes: {
          className: 'text-left ml-1',
        },
      },
      binLocationName: {
        type: LabelField,
        label: 'react.stockMovement.binLocation.label',
        defaultMessage: 'Bin location',
        flexWidth: '1',
        getDynamicAttr: ({ hasBinLocationSupport }) => ({
          hide: !hasBinLocationSupport,
        }),
      },
      lotNumber: {
        type: LabelField,
        label: 'react.stockMovement.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
      },
      expirationDate: {
        type: LabelField,
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '1',
      },
      quantityShipped: {
        type: LabelField,
        label: 'react.stockMovement.quantityShipped.label',
        defaultMessage: 'Qty shipped',
        flexWidth: '0.8',
      },
      uom: {
        type: LabelField,
        label: 'react.stockMovement.uom.label',
        defaultMessage: 'UoM',
        flexWidth: '0.8',
      },
      recipient: {
        type: SelectField,
        label: 'react.stockMovement.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '2.5',
        fieldKey: '',
        attributes: {
          async: true,
          required: true,
          showValueTooltip: true,
          openOnClick: false,
          autoload: false,
          cache: false,
          options: [],
          labelKey: 'name',
          filterOptions: options => options,
        },
        getDynamicAttr: props => ({
          loadOptions: props.debouncedUsersFetch,
          disabled: props.showOnly,
        }),
      },
      palletName: {
        type: TextField,
        label: 'react.stockMovement.packLevel1.label',
        defaultMessage: 'Pack level 1',
        flexWidth: '0.8',
        getDynamicAttr: ({ showOnly }) => ({
          disabled: showOnly,
        }),
      },
      boxName: {
        type: TextField,
        label: 'react.stockMovement.packLevel2.label',
        defaultMessage: 'Pack level 2',
        flexWidth: '0.8',
        getDynamicAttr: ({ showOnly }) => ({
          disabled: showOnly,
        }),
      },
      splitLineItems: {
        type: PackingSplitLineModal,
        label: 'react.stockMovement.splitLine.label',
        defaultMessage: 'Split line',
        flexWidth: '1',
        fieldKey: '',
        attributes: {
          title: 'react.stockMovement.splitLine.label',
          btnOpenText: 'react.stockMovement.splitLine.label',
          btnOpenDefaultText: 'Split line',
          btnOpenClassName: 'btn btn-outline-success',
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, onSave, formValues, showOnly,
        }) => ({
          lineItem: fieldValue,
          btnOpenDisabled: showOnly,
          onSave: splitLineItems => onSave(formValues, rowIndex, splitLineItems),
        }),
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.packPageItems = [];

  _.forEach(values.packPageItems, (item, key) => {
    if (!_.isEmpty(item.boxName) && _.isEmpty(item.palletName)) {
      errors.packPageItems[key] = { boxName: 'react.stockMovement.error.boxWithoutPallet.label' };
    }
  });
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
      values: { ...this.props.initialValues, packPageItems: [] },
      totalCount: 0,
    };

    this.saveSplitLines = this.saveSplitLines.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);

    this.debouncedUsersFetch =
      debounceUsersFetch(this.props.debounceTime, this.props.minSearchLength);

    this.props.showSpinner();
  }

  componentDidMount() {
    if (this.props.stockMovementTranslationsFetched) {
      this.dataFetched = true;

      this.fetchAllData();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.stockMovementTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchAllData();
    }
  }

  setPackPageItems(response) {
    const { data } = response.data;
    this.setState({
      values: {
        ...this.state.values,
        packPageItems: _.uniqBy(_.concat(this.state.values.packPageItems, data), 'shipmentItemId'),
      },
    });
  }

  dataFetched = false;

  /**
   * Fetches all required data.
   * @public
   */
  fetchAllData() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=5`;

    apiClient.get(url)
      .then((resp) => {
        const { statusCode } = resp.data.data;
        const { totalCount } = resp.data;

        this.setState({ values: { ...this.state.values, statusCode }, totalCount }, () => {
          this.props.hideSpinner();
        });
      }).catch(() => {
        this.props.hideSpinner();
      });

    if (!this.props.isPaginated) {
      this.fetchLineItems().then((response) => {
        this.setPackPageItems(response);
      });
    }
  }

  loadMoreRows({ startIndex, stopIndex }) {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?offset=${startIndex}&max=${stopIndex - startIndex > 0 ? stopIndex - startIndex : 1}&stepNumber=5`;
    apiClient.get(url)
      .then((response) => {
        this.setPackPageItems(response);
      });
  }

  isRowLoaded({ index }) {
    return !!this.state.values.packPageItems[index];
  }

  /**
   * Fetches 5th step data from current stock movement.
   * @public
   */
  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=5`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
  }

  /**
   * Saves packing data
   * @param {object} formValues
   * @public
   */
  save(formValues) {
    this.props.showSpinner();
    this.savePackingData(formValues.packPageItems)
      .then((resp) => {
        const { data } = resp.data;
        this.setState({ values: { ...this.state.values, packPageItems: data } });
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.stockMovement.alert.saveSuccess.label', 'Changes saved successfully'), { timeout: 3000 });
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Refetch the data, all not saved changes will be lost.
   * @public
   */
  refresh() {
    confirmAlert({
      title: this.props.translate('react.stockMovement.message.confirmRefresh.label', 'Confirm refresh'),
      message: this.props.translate(
        'react.stockMovement.confirmRefresh.message',
        'Are you sure you want to refresh? Your progress since last save will be lost.',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => this.fetchAllData(),
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  /**
   * Transition to next stock movement status
   * @public
   */
  transitionToNextStep() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const status = 'CHECKING';
    const payload = { status };

    if (this.state.values.statusCode !== 'PACKED') {
      return apiClient.post(url, payload);
    }
    return Promise.resolve();
  }

  /**
   * Saves current stock movement progress (line items) and goes to the next stock movement step.
   * @param {object} formValues
   * @public
   */
  nextPage(formValues) {
    this.props.showSpinner();
    this.savePackingData(formValues.packPageItems)
      .then(() => {
        this.transitionToNextStep()
          .then(() => {
            this.props.hideSpinner();
            this.props.nextPage(formValues);
          })
          .catch(() => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Saves packing data
   * @param {object} packPageItems
   * @public
   */
  savePackingData(packPageItems) {
    const updateItemsUrl = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/updateShipmentItems`;
    const payload = {
      id: this.state.values.stockMovementId,
      stepNumber: '5',
      packPageItems,
    };

    if (payload.packPageItems.length) {
      return apiClient.post(updateItemsUrl, flattenRequest(payload))
        .catch(() => Promise.reject(new Error('react.stockMovement.error.saveRequisitionItems.label')));
    }

    return Promise.resolve();
  }

  /**
   * Saves split line items
   * @param {object} formValues
   * @param {number} lineItemIndex
   * @param {object} splitLineItems
   * @public
   */
  saveSplitLines(formValues, lineItemIndex, splitLineItems) {
    this.props.showSpinner();
    this.savePackingData(update(formValues.packPageItems, {
      [lineItemIndex]: {
        splitLineItems: { $set: splitLineItems },
      },
    }))
      .then((resp) => {
        const { data } = resp.data;
        this.setState({
          values: {
            ...this.state.values,
            packPageItems: data,
          },
          totalCount: this.state.totalCount + (splitLineItems.length - 1),
        });
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    const { showOnly } = this.props;
    return (
      <Form
        onSubmit={values => this.nextPage(values)}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        validate={validate}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            { !showOnly ?
              <span>
                <button
                  type="button"
                  onClick={() => this.refresh()}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
                >
                  <span><i className="fa fa-refresh pr-2" />
                    <Translate id="react.default.button.refresh.label" defaultMessage="Reload" />
                  </span>
                </button>
                <button
                  type="button"
                  disabled={invalid}
                  onClick={() => this.save(values)}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-1"
                >
                  <span><i className="fa fa-save pr-2" />
                    <Translate id="react.default.button.save.label" defaultMessage="Save" />
                  </span>
                </button>
                <button
                  type="button"
                  disabled={invalid}
                  onClick={() => this.savePackingData(values.packPageItems).then(() => { window.location = `/openboxes/stockMovement/show/${values.stockMovementId}`; })}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
                >
                  <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
                </button>
              </span>
                :
              <button
                type="button"
                disabled={invalid}
                onClick={() => { window.location = '/openboxes/stockMovement/list?direction=OUTBOUND'; }}
                className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs mr-2"
              >
                <span><i className="fa fa-sign-out pr-2" /> <Translate id="react.default.button.exit.label" defaultMessage="Exit" /> </span>
              </button> }
            <form onSubmit={handleSubmit}>
              {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                onSave: this.saveSplitLines,
                formValues: values,
                debouncedUsersFetch: this.debouncedUsersFetch,
                hasBinLocationSupport: this.props.hasBinLocationSupport,
                totalCount: this.state.totalCount,
                loadMoreRows: this.loadMoreRows,
                isRowLoaded: this.isRowLoaded,
                isPaginated: this.props.isPaginated,
                showOnly,
              }))}
              <div>
                <button
                  type="button"
                  className="btn btn-outline-primary btn-form btn-xs"
                  disabled={showOnly || invalid}
                  onClick={() => this.savePackingData(values.packPageItems)
                    .then(() => this.props.previousPage(values))}
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button type="submit" className="btn btn-outline-primary btn-form float-right btn-xs" disabled={showOnly || invalid}>
                  <Translate id="react.default.button.next.label" defaultMessage="Next" />
                </button>
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
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
  isPaginated: state.session.isPaginated,
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
  nextPage: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  /** Is true when currently selected location supports bins */
  hasBinLocationSupport: PropTypes.bool.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
  /** Return true if show only */
  showOnly: PropTypes.bool.isRequired,
};
