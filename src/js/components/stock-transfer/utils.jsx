import _ from 'lodash';

const PENDING = 'PENDING';
const CANCELED = 'CANCELED';

function extractItem(item, status) {
  const { destinationBinLocation, destinationZone } = item;
  return {
    ...item,
    destinationBinLocation: {
      id: destinationBinLocation && destinationBinLocation.id ? destinationBinLocation.id : '',
      name: destinationBinLocation && destinationBinLocation.name ? destinationBinLocation.name : '',
      zoneId: destinationZone && destinationZone.id ? destinationZone.id : null,
      zoneName: destinationZone && destinationZone.name ? destinationZone.name : null,
    },
    quantity: status === PENDING ? '' : item.quantity,
  };
}

function extractSplitItem(item, splitItem, status) {
  return {
    ...splitItem,
    destinationBinLocation: {
      id: splitItem.destinationBinLocation.id ? splitItem.destinationBinLocation.id : '',
      name: splitItem.destinationBinLocation.name ? splitItem.destinationBinLocation.name : '',
      zoneId: splitItem.destinationBinLocation.zone ?
        splitItem.destinationBinLocation.zone.id : null,
      zoneName: splitItem.destinationBinLocation.zone ?
        splitItem.destinationBinLocation.zone.name : null,
    },
    quantity: status === PENDING ? '' : item.quantity,
    quantityOnHand: item.quantityOnHand,
    referenceId: item.id, // set a referenceId from original item
  };
}

export function extractStockTransferItems(stockTransfer) {
  const stockTransferItems = [];
  _.forEach(stockTransfer.stockTransferItems, (item) => {
    stockTransferItems.push(extractItem(item, stockTransfer.status));
    if (item.splitItems.length > 0) {
      _.forEach(item.splitItems, (splitItem) => {
        stockTransferItems.push(extractSplitItem(item, splitItem, stockTransfer.status));
      });
    }
  });
  return stockTransferItems;
}

export function extractNonCanceledItems(stockTransfer) {
  const stockTransferItems = [];
  _.forEach(stockTransfer.stockTransferItems, (item) => {
    if (item.status !== CANCELED) {
      stockTransferItems.push(extractItem(item, stockTransfer.status));
    }
    if (item.splitItems.length > 0) {
      _.forEach(item.splitItems, (splitItem) => {
        stockTransferItems.push(extractSplitItem(item, splitItem, stockTransfer.status));
      });
    }
  });
  return stockTransferItems;
}

export function prepareRequest(stockTransfer, status) {
  const originalItems = _.filter(stockTransfer.stockTransferItems, item => !item.referenceId);
  const stockTransferItems = _.map(originalItems, item => ({
    ...item,
    splitItems: _.filter(
      stockTransfer.stockTransferItems,
      splitItem => splitItem.referenceId === item.id,
    ),
    quantity: item.quantity === '' ? item.quantityOnHand : item.quantity,
  }));

  return { ...stockTransfer, stockTransferItems, status };
}
