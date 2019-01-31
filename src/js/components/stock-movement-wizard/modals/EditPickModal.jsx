import React, { Component } from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Translate } from 'react-localize-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import LabelField from '../../form-elements/LabelField';
import TextField from '../../form-elements/TextField';
import ArrayField from '../../form-elements/ArrayField';
import SelectField from '../../form-elements/SelectField';
import apiClient from '../../../utils/apiClient';
import { showSpinner, hideSpinner, fetchReasonCodes } from '../../../actions';


const FIELDS = {
  reasonCode: {
    type: SelectField,
    label: 'stockMovement.reasonCode.label',
    getDynamicAttr: props => ({
      options: props.reasonCodes,
    }),
  },
  availableItems: {
    type: ArrayField,
    fields: {
      lotNumber: {
        type: LabelField,
        label: 'stockMovement.lot.label',
      },
      expirationDate: {
        type: LabelField,
        label: 'stockMovement.expiry.label',
      },
      'binLocation.name': {
        type: LabelField,
        label: 'stockMovement.binLocation.label',
      },
      quantityAvailable: {
        type: LabelField,
        label: 'stockMovement.quantityAvailable.label',
        fixedWidth: '150px',
        attributes: {
          formatValue: value => (value ? value.toLocaleString('en-US') : null),
        },
      },
      quantityPicked: {
        type: TextField,
        label: 'stockMovement.quantityPicked.label',
        fixedWidth: '140px',
        attributes: {
          type: 'number',
        },
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.availableItems = [];
  _.forEach(values.availableItems, (item, key) => {
    if (item.quantityPicked > item.quantityAvailable) {
      errors.availableItems[key] = { quantityPicked: 'errors.higherTyPicked.label ' };
    }
    if (item.quantityPicked < 0) {
      errors.availableItems[key] = { quantityPicked: 'errors.negativeQtyPicked.label' };
    }
  });

  const pickedSum = _.reduce(
    values.availableItems, (sum, val) =>
      (sum + (val.quantityPicked ? _.toInteger(val.quantityPicked) : 0)),
    0,
  );


  if (_.some(values.availableItems, val => !_.isNil(val.quantityPicked)) &&
    !values.reasonCode && pickedSum !== values.quantityRequired) {
    errors.reasonCode = 'errors.differentTotalQty.label';
  }

  return errors;
}

/** Modal window where user can edit pick. */
/* eslint no-param-reassign: "error" */
class EditPickModal extends Component {
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

  componentDidMount() {
    if (!this.props.reasonCodesFetched) {
      this.fetchData(this.props.fetchReasonCodes);
    }
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
   * Loads chosen items, required quantity and reason codes into modal's form.
   * @public
   */
  onOpen() {
    this.setState({
      formValues: {
        availableItems: this.state.attr.fieldValue.availableItems,
        reasonCode: '',
        quantityRequired: this.state.attr.fieldValue.quantityRequired,
      },
    });
  }

  /**
   * Sends all changes made by user in this modal to API and updates data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    this.props.showSpinner();

    const url = `/openboxes/api/stockMovementItems/${this.state.attr.fieldValue['requisitionItem.id']}`;
    const payload = {
      picklistItems: _.map(values.availableItems, (avItem) => {
        // check if this picklist item already exists
        const picklistItem = _.find(
          _.filter(this.state.attr.fieldValue.picklistItems, listItem => !listItem.initial),
          item => item['inventoryItem.id'] === avItem['inventoryItem.id'],
        );
        if (picklistItem) {
          return {
            id: picklistItem.id,
            'inventoryItem.id': avItem['inventoryItem.id'],
            'binLocation.id': avItem['binLocation.id'] || '',
            quantityPicked: _.isNil(avItem.quantityPicked) ? '' : avItem.quantityPicked,
            reasonCode: values.reasonCode || '',
          };
        }
        return {
          'inventoryItem.id': avItem['inventoryItem.id'],
          'binLocation.id': avItem['binLocation.id'] || '',
          quantityPicked: _.isNil(avItem.quantityPicked) ? '' : avItem.quantityPicked,
          reasonCode: values.reasonCode || '',
        };
      }),
    };

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
   * Sums up quantity picked from all available items.
   * @param {object} values
   * @public
   */
  /* eslint-disable-next-line class-methods-use-this */
  calculatePicked(values) {
    return (
      <div>
        <div className="font-weight-bold pb-2"><Translate id="stockMovement.quantityPicked.label" />: {_.reduce(values.availableItems, (sum, val) =>
          (sum + (val.quantityPicked ? _.toInteger(val.quantityPicked) : 0)), 0)}
        </div>
        <hr />
      </div>
    );
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
        validate={validate}
        initialValues={this.state.formValues}
        formProps={{ reasonCodes: this.props.reasonCodes }}
        renderBodyWithValues={this.calculatePicked}
      >
        <div>
          <div className="font-weight-bold"><Translate id="stockMovement.productCode.label" />: {this.state.attr.fieldValue.productCode}</div>
          <div className="font-weight-bold"><Translate id="stockMovement.productName.label" />: {this.state.attr.fieldValue['product.name']}</div>
          <div className="font-weight-bold"><Translate id="stockMovement.quantityRequired.label" />: {this.state.attr.fieldValue.quantityRequired}</div>
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = state => ({
  reasonCodesFetched: state.reasonCodes.fetched,
  reasonCodes: state.reasonCodes.data,
});

export default connect(mapStateToProps, {
  fetchReasonCodes, showSpinner, hideSpinner,
})(EditPickModal);

EditPickModal.propTypes = {
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
  /** Function fetching reason codes */
  fetchReasonCodes: PropTypes.func.isRequired,
  /** Indicator if reason codes' data is fetched */
  reasonCodesFetched: PropTypes.bool.isRequired,
  /** Array of available reason codes */
  reasonCodes: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  /** Function updating page on which modal is located called when user saves changes */
  onResponse: PropTypes.func.isRequired,
};
