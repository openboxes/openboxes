import React, { Component } from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import TextField from '../../form-elements/TextField';
import ArrayField from '../../form-elements/ArrayField';
import LabelField from '../../form-elements/LabelField';
import SelectField from '../../form-elements/SelectField';
import DateField from '../../form-elements/DateField';
import { showSpinner, hideSpinner } from '../../../actions';
import apiClient from '../../../utils/apiClient';

const FIELDS = {
  adjustInventory: {
    addButton: 'Add new lot number',
    type: ArrayField,
    disableVirtualization: true,
    fields: {
      'binLocation.name': {
        type: SelectField,
        label: 'Bin',
        fieldKey: 'inventoryItem.id',
        getDynamicAttr: ({ fieldValue, bins }) => ({
          disabled: !!fieldValue,
          options: bins,
        }),
      },
      lotNumber: {
        type: TextField,
        label: 'Lot #',
        fieldKey: 'inventoryItem.id',
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: !!fieldValue,
        }),
      },
      expirationDate: {
        type: DateField,
        label: 'Expiry Date',
        fieldKey: 'inventoryItem.id',
        getDynamicAttr: ({ fieldValue }) => ({
          dateFormat: 'YYYY/MM/DD',
          disabled: !!fieldValue,
        }),
      },
      quantityAvailable: {
        type: LabelField,
        label: 'Previous Qty',
        fixedWidth: '150px',
        attributes: {
          formatValue: value => (value.toLocaleString('en-US')),
        },
      },
      quantityAdjusted: {
        type: TextField,
        label: 'Current Qty',
        fixedWidth: '140px',
        attributes: {
          type: 'number',
        },
      },
      comments: {
        type: TextField,
        label: 'Comments',
      },
    },
  },
};

/** Modal window where user can adjust existing inventory or add a new one. */
class AdjustInventoryModal extends Component {
  constructor(props) {
    super(props);

    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.state = {
      attr,
      bins: [],
      formValues: {},
    };
    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
    this.fetchBins = this.fetchBins.bind(this);
  }

  componentDidMount() {
    this.fetchBins();
  }

  /**
   * Loads available inventories for chosen items into modal's form.
   * @public
   */
  onOpen() {
    this.setState({
      formValues: {
        adjustInventory: this.state.attr.fieldValue.availableItems,
      },
    });
  }

  /**
   * Sends all changes made by user in this modal to API and update data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    this.props.showSpinner();

    const url = '/openboxes/api/stockAdjustments';
    const payload = _.map(values.adjustInventory, adItem => ({
      'inventoryItem.id': adItem['inventoryItem.id'] || '',
      'binLocation.id': adItem['binLocation.id'] || '',
      quantityAvailable: adItem.quantityAvailable,
      quantityAdjusted: adItem.quantityAdjusted,
      comments: adItem.comments,
    }));

    return apiClient.post(url, payload).then(() => {
      apiClient.get(`/openboxes/api/stockMovements/${this.state.attr.stockMovementId}?stepNumber=4`)
        .then((resp) => {
          const { pickPageItems } = resp.data.data.pickPage;
          this.props.onResponse(pickPageItems);

          this.props.hideSpinner();
        })
        .catch(() => { this.props.hideSpinner(); });
    }).catch(() => { this.props.hideSpinner(); });
  }

  /**
   * Fetches available bin locations from API.
   * @public
   */
  fetchBins() {
    this.props.showSpinner();
    const url = '/openboxes/api/internalLocations';

    return apiClient.get(url)
      .then((response) => {
        const bins = _.map(response.data.data, bin => (
          { value: bin.id, label: bin.name }
        ));
        this.setState({ bins }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    if (this.state.attr.subfield) {
      return null;
    }

    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.onSave}
        fields={FIELDS}
        initialValues={this.state.formValues}
        formProps={{ bins: this.state.bins }}
      />
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(AdjustInventoryModal);

AdjustInventoryModal.propTypes = {
  /** Name of the field */
  fieldName: PropTypes.string.isRequired,
  /** Configuration of the field */
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function updating page on which modal is located called when user saves changes */
  onResponse: PropTypes.func.isRequired,
};
