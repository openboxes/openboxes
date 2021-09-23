import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';

import ArrayField from '../form-elements/ArrayField';
import ButtonField from '../form-elements/ButtonField';
import LabelField from '../form-elements/LabelField';
import { renderFormField } from '../../utils/form-utils';

import EditPickModal from './EditPickModal';
import { showSpinner, hideSpinner } from '../../actions';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import apiClient from '../../utils/apiClient';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

const FIELDS = {
  replenishmentItems: {
    type: ArrayField,
    rowComponent: TableRowWithSubfields,
    subfieldKey: 'picklistItems',
    getDynamicRowAttr: ({ rowValues, subfield }) => {
      let className = rowValues.initial ? 'crossed-out ' : '';
      if (!subfield) { className += 'font-weight-bold'; }
      return { className };
    },
    fields: {
      'product.productCode': {
        type: LabelField,
        label: 'react.stockMovement.productCode.label',
        defaultMessage: 'Code',
        flexWidth: '0.5',
      },
      'product.name': {
        type: LabelField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        flexWidth: '2',
        headerAlign: 'left',
        attributes: {
          showValueTooltip: true,
        },
      },
      'currentZone.name': {
        type: LabelField,
        label: 'react.replenishment.currentZone.label',
        defaultMessage: 'Current Zone',
        flexWidth: '0.5',
        attributes: {
          showValueTooltip: true,
        },
      },
      'currentBinLocation.name': {
        type: LabelField,
        label: 'react.replenishment.currentBinLocation.label',
        defaultMessage: 'Current Bin Location',
        flexWidth: '1',
        attributes: {
          showValueTooltip: true,
        },
        getDynamicAttr: ({ subfield }) => ({
          formatValue: (value) => {
            if (subfield || value) {
              return value;
            }
            return 'DEFAULT';
          },
        }),
      },
      quantityNeeded: {
        type: LabelField,
        label: 'react.replenishment.quantityInBin.label',
        defaultMessage: 'Qty Needed',
        flexWidth: '1',
      },
      'zone.name': {
        type: LabelField,
        label: 'react.replenishment.zone.label',
        defaultMessage: 'Zone',
        flexWidth: '0.5',
        attributes: {
          showValueTooltip: true,
        },
      },
      'binLocation.name': {
        type: LabelField,
        label: 'react.replenishment.bin.label',
        defaultMessage: 'Bin',
        flexWidth: '1',
        attributes: {
          showValueTooltip: true,
        },
      },
      lotNumber: {
        type: LabelField,
        label: 'react.replenishment.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
      },
      expirationDate: {
        type: LabelField,
        label: 'react.replenishment.exp.label',
        defaultMessage: 'Exp',
        flexWidth: '1',
      },
      quantity: {
        type: LabelField,
        label: 'react.stockMovement.quantityToTransfer.label',
        defaultMessage: 'Qty to Transfer',
        flexWidth: '1',
      },
      buttonEditPick: {
        label: 'react.stockMovement.editPick.label',
        defaultMessage: 'Edit pick',
        flexWidth: '0.5',
        type: EditPickModal,
        fieldKey: '',
        attributes: {
          title: 'react.stockMovement.editPick.label',
          defaultTitleMessage: 'Edit Pick',
        },
        getDynamicAttr: ({ fieldValue, refetchReplenishment, subfield }) => ({
          subfield,
          itemId: _.get(fieldValue, 'id'),
          btnOpenText: 'react.default.button.edit.label',
          btnOpenDefaultText: 'Edit',
          btnOpenClassName: 'btn btn-outline-primary',
          onResponse: refetchReplenishment,
        }),
      },
      revert: {
        type: ButtonField,
        label: 'react.default.button.undoEdit.label',
        defaultMessage: 'Undo edit',
        flexWidth: '1',
        fieldKey: '',
        buttonLabel: 'react.default.button.undoEdit.label',
        buttonDefaultMessage: 'Undo edit',
        getDynamicAttr: ({
          fieldValue, revertUserPick, subfield,
        }) => ({
          hidden: subfield,
          onClick: _.get(fieldValue, 'id') ? () => revertUserPick(_.get(fieldValue, 'id')) : () => null,
        }),
        attributes: {
          className: 'btn btn-outline-danger',
        },
      },
    },
  },
};

/* eslint class-methods-use-this: ["error",{ "exceptMethods": ["checkForInitialPicksChanges"] }] */
class ReplenishmentSecondPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: { replenishment: { ...this.props.initialValues } },
    };

    this.revertUserPick = this.revertUserPick.bind(this);
    this.fetchReplenishment = this.fetchReplenishment.bind(this);
    this.nextPage = this.nextPage.bind(this);
  }

  componentDidMount() {
    if (this.props.replenishmentTranslationsFetched) {
      this.dataFetched = true;
      this.fetchReplenishment();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.replenishmentTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
      this.fetchReplenishment();
    }
  }

  dataFetched = false;

  fetchReplenishment() {
    this.props.showSpinner();
    const url = `/openboxes/api/replenishments/${this.props.match.params.replenishmentId}`;

    return apiClient.get(url)
      .then((resp) => {
        const replenishment = resp.data.data;
        this.setState({
          values: {
            replenishment: {
              ...replenishment,
              replenishmentItems: _.map(
                replenishment.replenishmentItems,
                item => this.checkForInitialPicksChanges(item),
              ),
            },
          },
        }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }
  checkForInitialPicksChanges(pickPageItem) {
    if (pickPageItem.picklistItems.length) {
      const initialPicks = [];
      _.forEach(pickPageItem.suggestedItems, (suggestion) => {
        // search if suggested picks are inside picklist
        // if no -> add suggested pick as initial pick (to be crossed out)
        // if yes -> compare quantityPicked of item in picklist with suggestion
        const pick = _.find(
          pickPageItem.picklistItems,
          item => _.get(suggestion, 'inventoryItem.id') === _.get(item, 'inventoryItem.id') && _.get(item, 'binLocation.id') === _.get(suggestion, 'binLocation.id'),
        );
        if (_.isEmpty(pick) || (pick.quantity !== suggestion.quantityPicked)) {
          initialPicks.push({
            ...suggestion,
            initial: true,
          });
        }
      });

      return { ...pickPageItem, picklistItems: _.concat(initialPicks, _.sortBy(pickPageItem.picklistItems, ['binLocation.name', 'initial'])) };
    }

    return pickPageItem;
  }

  nextPage() {
    if (this.state.values.replenishment.status === 'PENDING') {
      this.props.showSpinner();
      const url = `/openboxes/api/replenishments/${this.props.match.params.replenishmentId}`;
      const payload = { status: 'PLACED' };
      apiClient.post(url, payload)
        .then(() => {
          this.props.hideSpinner();
          this.props.nextPage(this.state.values);
        })
        .catch(() => this.props.hideSpinner());
    } else {
      this.props.nextPage(this.state.values);
    }
  }

  revertUserPick(itemId) {
    this.props.showSpinner();

    const revertPicklistUrl = `/openboxes/api/replenishments/${itemId}/picklists`;

    apiClient.post(revertPicklistUrl, {})
      .then(() => {
        this.props.hideSpinner();
        this.fetchReplenishment();
      })
      .catch(() => { this.props.hideSpinner(); });
  }

  render() {
    return (
      <Form
        onSubmit={() => this.nextPage()}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values.replenishment}
        render={({ handleSubmit }) => (
          <div className="d-flex flex-column">
            <div className="submit-buttons">
              <button
                type="submit"
                className="btn btn-outline-primary btn-form float-right btn-xs mt-0 mb-3"
              ><Translate id="react.replenishment.next.label" defaultMessage="Next" />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="print-mt">
              <div className="table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  replenishmentId: this.props.match.params.replenishmentId,
                  refetchReplenishment: this.fetchReplenishment,
                  locationId: this.props.locationId,
                  translate: this.props.translate,
                  revertUserPick: this.revertUserPick,
                }))}
              </div>
              <div className="submit-buttons">
                <button
                  type="submit"
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                ><Translate id="react.replenishment.next.label" defaultMessage="Next" />
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
  replenishmentTranslationsFetched: state.session.fetchedTranslations.replenishment,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(
  mapStateToProps,
  {
    showSpinner, hideSpinner,
  },
)(ReplenishmentSecondPage);

ReplenishmentSecondPage.propTypes = {
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({}).isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      replenishmentId: PropTypes.string,
    }),
  }).isRequired,
  locationId: PropTypes.string.isRequired,
  replenishmentTranslationsFetched: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
};
