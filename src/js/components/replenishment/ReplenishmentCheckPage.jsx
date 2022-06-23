import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import LabelField from 'components/form-elements/LabelField';
import TableRowWithSubfields from 'components/form-elements/TableRowWithSubfields';
import apiClient, { flattenRequest } from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';


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
        headerAlign: 'left',
        attributes: {
          cellClassName: 'text-left',
        },
      },
      'product.name': {
        type: LabelField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        flexWidth: '2',
        headerAlign: 'left',
        attributes: {
          showValueTooltip: true,
          cellClassName: 'text-left',
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
        headerAlign: 'right',
        attributes: {
          cellClassName: 'text-right',
        },
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
        headerAlign: 'right',
        attributes: {
          cellClassName: 'text-right',
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

    this.fetchReplenishment = this.fetchReplenishment.bind(this);
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
            quantity: suggestion.quantityPicked,
            initial: true,
          });
        }
      });

      return { ...pickPageItem, picklistItems: _.concat(initialPicks, _.sortBy(pickPageItem.picklistItems, ['binLocation.name', 'initial'])) };
    }

    return pickPageItem;
  }

  completeReplenishment() {
    if (this.state.values.replenishment.status === 'APPROVED') {
      this.props.showSpinner();
      const url = `/openboxes/api/replenishments/${this.props.match.params.replenishmentId}`;
      const payload = { status: 'COMPLETED' };
      apiClient.post(url, flattenRequest(payload))
        .then(() => {
          window.location = `/openboxes/stockTransfer/show/${this.props.match.params.replenishmentId}`;
          this.props.hideSpinner();
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  previousPage() {
    this.props.previousPage({});
  }

  render() {
    return (
      <Form
        onSubmit={() => this.completeReplenishment()}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values.replenishment}
        render={({ handleSubmit }) => (
          <div className="d-flex flex-column">
            <form onSubmit={handleSubmit} className="print-mt">
              <div className="table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  replenishmentId: this.props.match.params.replenishmentId,
                  translate: this.props.translate,
                }))}
              </div>
              <div className="submit-buttons">
                <button
                  type="button"
                  onClick={() => this.previousPage()}
                  className="btn btn-outline-primary btn-form btn-xs"
                ><Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="button"
                  onClick={() => this.completeReplenishment()}
                  className="btn btn-outline-success float-right btn-xs mr-3"
                ><Translate id="react.stockTransfer.completeStockTransfer.label" defaultMessage="Complete Stock Transfer" />
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
  previousPage: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({
    replenishment: PropTypes.arrayOf(PropTypes.shape({})),
  }).isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      replenishmentId: PropTypes.string,
    }),
  }).isRequired,
  replenishmentTranslationsFetched: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
};
