import _ from 'lodash';

const PENDING = 'PENDING';
const CANCELED = 'CANCELED';

function extractItem(item, status) {
  const { destinationBinLocation, destinationZone } = item;

  let destinationBin = {};
  if (status === PENDING) {
    destinationBin = {
      id: null, name: null, zoneId: null, zoneName: null,
    };
  } else {
    destinationBin = {
      id: destinationBinLocation && destinationBinLocation.id ? destinationBinLocation.id : null,
      name: destinationBinLocation && destinationBinLocation.name ?
        destinationBinLocation.name : null,
      zoneId: destinationZone && destinationZone.id ? destinationZone.id : null,
      zoneName: destinationZone && destinationZone.name ? destinationZone.name : null,
    };
  }

  return {
    ...item,
    destinationBinLocation: destinationBin,
    quantity: status === PENDING ? '' : item.quantity,
  };
}

function extractSplitItem(item, splitItem, status) {
  let destinationBin = {};

  if (status === PENDING) {
    destinationBin = {
      id: null, name: null, zoneId: null, zoneName: null,
    };
  } else {
    destinationBin = {
      id: splitItem.destinationBinLocation.id ? splitItem.destinationBinLocation.id : null,
      name: splitItem.destinationBinLocation.name ? splitItem.destinationBinLocation.name : null,
      zoneId: splitItem.destinationZone ? splitItem.destinationZone.id : null,
      zoneName: splitItem.destinationZone ? splitItem.destinationZone.name : null,
    };
  }

  return {
    ...splitItem,
    destinationBinLocation: destinationBin,
    quantity: status === PENDING ? '' : splitItem.quantity,
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
