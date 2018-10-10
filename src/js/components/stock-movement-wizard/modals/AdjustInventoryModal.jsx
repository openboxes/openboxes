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
      binLocation: {
        type: SelectField,
        label: 'Bin',
        fieldKey: 'inventoryItem.id',
        getDynamicAttr: ({ fieldValue, bins, hasBinLocationSupport }) => ({
          disabled: !!fieldValue || !hasBinLocationSupport,
          options: bins,
          labelKey: 'name',
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
          formatValue: value => (value ? value.toLocaleString('en-US') : null),
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
      formValues: {},
    };
    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = nextProps;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(nextProps) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.setState({ attr });
  }

  /**
   * Loads available inventories for chosen items into modal's form.
   * @public
   */
  onOpen() {
    this.setState({
      formValues: {
        adjustInventory: _.map(this.state.attr.fieldValue.availableItems, item => ({
          ...item,
          binLocation: {
            id: item['binLocation.id'],
            name: item['binLocation.name'],
          },
        })),
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

    const url = `/openboxes/api/stockAdjustments?location.id=${this.props.locationId}`;
    const payload = _.map(values.adjustInventory, (adItem) => {
      if (!adItem['inventoryItem.id']) {
        return {
          'binLocation.id': adItem.binLocation || '',
          lotNumber: adItem.lotNumber,
          expirationDate: adItem.expirationDate,
          quantityAdjusted: parseInt(adItem.quantityAdjusted, 10),
          comments: adItem.comments,
        };
      }
      return {
        'inventoryItem.id': adItem['inventoryItem.id'] || '',
        'binLocation.id': adItem['binLocation.id'] || '',
        quantityAdjusted: parseInt(adItem.quantityAdjusted, 10),
        comments: adItem.comments,
      };
    });

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
        formProps={{
          bins: this.props.bins,
          hasBinLocationSupport: this.props.hasBinLocationSupport,
        }}
      />
    );
  }
}

const mapStateToProps = state => ({
  hasBinLocationSupport: state.location.currentLocation.hasBinLocationSupport,
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(AdjustInventoryModal);

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
  /** Is true when currently selected location supports bins */
  hasBinLocationSupport: PropTypes.bool.isRequired,
  /** Available bin locations fetched from API. */
  bins: PropTypes.arrayOf(PropTypes.shape({})),
  /** Location ID (origin of stock movement). To be used in stockAdjustments request. */
  locationId: PropTypes.string.isRequired,
};

AdjustInventoryModal.defaultProps = {
  bins: [],
};
