import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import LabelField from 'components/form-elements/LabelField';
import TableRowWithSubfields from 'components/form-elements/TableRowWithSubfields';
import EditPickModal from 'components/replenishment/EditPickModal';
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
          cellClassName: 'text-left',
          showValueTooltip: true,
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
      'currentBinLocation.name': {
        type: LabelField,
        label: 'react.replenishment.transferTo.label',
        defaultMessage: 'Transfer to',
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
      buttonEditPick: {
        label: 'react.stockMovement.editPick.label',
        defaultMessage: 'Edit pick',
        type: EditPickModal,
        fixedWidth: '90px',
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
        fixedWidth: '100px',
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
      sorted: false,
    };

    this.revertUserPick = this.revertUserPick.bind(this);
    this.fetchReplenishment = this.fetchReplenishment.bind(this);
    this.nextPage = this.nextPage.bind(this);
    this.printTransferOrder = this.printTransferOrder.bind(this);
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

  recreatePickList() {
    this.props.showSpinner();
    const url = `/openboxes/api/replenishments/${this.props.match.params.replenishmentId}/picklists`;

    apiClient.post(url)
      .then(() => {
        this.fetchReplenishment();
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

  nextPage() {
    if (this.state.values.replenishment.status === 'PENDING') {
      this.props.showSpinner();
      const url = `/openboxes/api/replenishments/${this.props.match.params.replenishmentId}`;
      const payload = { status: 'APPROVED' };
      apiClient.post(url, flattenRequest(payload))
        .then(() => {
          this.props.hideSpinner();
          this.props.nextPage(this.state.values);
        })
        .catch(() => this.props.hideSpinner());
    } else {
      this.props.nextPage(this.state.values);
    }
  }

  printTransferOrder() {
    const url = `/openboxes/replenishment/print/${this.props.match.params.replenishmentId}`;
    window.open(url, '_blank');
  }

  revertUserPick(itemId) {
    this.props.showSpinner();

    const revertPicklistUrl = `/openboxes/api/replenishments/${itemId}/picklistItem`;

    apiClient.post(revertPicklistUrl, {})
      .then(() => {
        this.props.hideSpinner();
        this.fetchReplenishment();
      })
      .catch(() => { this.props.hideSpinner(); });
  }

  refresh() {
    confirmAlert({
      title: this.props.translate('react.replenishment.confirmRefresh.label', 'Confirm refresh'),
      message: this.props.translate(
        'react.replenishment.confirmRefresh.content.label',
        'This button will redo the autopick on all items. Are you sure you want to continue?',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => {
            this.recreatePickList();
          },
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  saveAndExit() {
    window.location = `/openboxes/stockTransfer/show/${this.props.match.params.replenishmentId}`;
  }

  sortByBins() {
    if (this.state.sorted) {
      const sortedPickItemsByDefault = this.state.values.replenishment.replenishmentItems.map(item => ({ ...item, picklistItems: _.sortBy(item.picklistItems, ['binLocation.name', 'quantity']) }));
      this.setState({
        values: {
          replenishment: {
            ...this.state.values.replenishment,
            replenishmentItems: _.sortBy(sortedPickItemsByDefault, ['product.productCode']),
          },
        },
        sorted: false,
      });
      return;
    }

    const sortedPickItemsByBinName = this.state.values.replenishment.replenishmentItems.map(item => ({ ...item, picklistItems: _.sortBy(item.picklistItems, ['zone.name', 'binLocation.name']) }));
    this.setState({
      values: {
        replenishment: {
          ...this.state.values.replenishment,
          replenishmentItems: _.sortBy(sortedPickItemsByBinName, ['picklistItems[0].zone.name', 'picklistItems[0].binLocation.name']),
        },
      },
      sorted: true,
    });
  }

  render() {
    return (
      <Form
        onSubmit={() => this.nextPage()}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values.replenishment}
        render={({ handleSubmit }) => (
          <div className="d-flex flex-column">
            <div className="submit-buttons d-flex justify-content-end buttons-container">
              <button
                type="button"
                onClick={() => this.printTransferOrder()}
                className="mb-1 btn btn-outline-secondary btn-xs ml-1"
              >
                <span>
                  <i className="fa fa-print pr-2" />
                  <Translate id="react.stockTransfer.printTransferOrder.label" defaultMessage="Print Transfer Order" />
                </span>
              </button>
              <button
                type="button"
                onClick={() => this.refresh()}
                className="mb-1 btn btn-outline-secondary btn-xs ml-1"
              >
                <span>
                  <i className="fa fa-refresh pr-2" />
                  <Translate id="react.default.button.refresh.label" defaultMessage="Reload" />
                </span>
              </button>
              <button
                type="button"
                onClick={() => this.saveAndExit()}
                className="mb-1 btn btn-outline-secondary btn-xs ml-1 bg-white border-0"
              >
                <span>
                  <i className="fa fa-sign-out pr-2" />
                  <Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" />
                </span>
              </button>
              <button
                type="button"
                onClick={() => this.sortByBins()}
                className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
              >
                {this.state.sorted ?
                  <span>
                    <i className="fa fa-sort pr-2" />
                    <Translate id="react.replenishment.originalOrder.label" defaultMessage="Original order" />
                  </span>
                  :
                  <span>
                    <i className="fa fa-sort pr-2" />
                    <Translate id="react.replenishment.sortByBins.label" defaultMessage="Sort by bins" />
                  </span>
                }
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
