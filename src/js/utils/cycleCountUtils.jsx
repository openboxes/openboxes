import _ from 'lodash';

import { IMPORT_CYCLE_COUNTS } from 'actions/types';
import cycleCountApi from 'api/services/CycleCountApi';

/**
 * Util method to remove lot number spaces from a cycle count item
 * @param cycleCountItem
 */
export const trimLotNumberSpaces = (cycleCountItem) => ({
  ...cycleCountItem,
  inventoryItem: {
    ...cycleCountItem.inventoryItem,
    lotNumber: cycleCountItem.inventoryItem?.lotNumber?.trim(),
  },
});

export const filterCountItems = (cycleCounts) =>
  cycleCounts.map((cc) => ({
    ...cc,
    cycleCountItems: cc.cycleCountItems.filter((item) => item.countIndex === 0),
  }));

export const moveCustomItemsToTheBottom = (cycleCountItems) => {
  const { false: originalItems = [], true: customItems = [] } = _.groupBy(cycleCountItems, 'custom');
  const customItemsSortedByCreationDate = _.sortBy(customItems, 'dateCreated');
  return [...originalItems, ...customItemsSortedByCreationDate];
};

export const buildMetadataMaps = (cycleCounts) => {
  const dateCounted = {};
  const countedBy = {};

  cycleCounts.forEach((cc) => {
    const firstItem = cc.cycleCountItems[0];
    if (firstItem) {
      dateCounted[cc.id] = firstItem.dateCounted;
      countedBy[cc.id] = firstItem.assignee;
    }
  });

  return { dateCounted, countedBy };
};

export const normalizeCycleCounts = (cycleCounts) => {
  const filtered = filterCountItems(cycleCounts).map((cc) => ({
    ...cc,
    cycleCountItems: moveCustomItemsToTheBottom(cc.cycleCountItems),
  }));

  const { dateCounted, countedBy } = buildMetadataMaps(filtered);

  const entities = filtered.reduce((acc, cc) => ({
    ...acc,
    [cc.id]: cc,
  }), {});

  return { entities, dateCounted, countedBy };
};

const emptyRow = (productId) => ({
  id: _.uniqueId('newRow'),
  product: {
    id: productId,
  },
  inventoryItem: {
    lotNumber: null,
    expirationDate: null,
  },
  binLocation: null,
  quantityCounted: null,
  comment: '',
});

export const addEmptyRow = (cycleCount) => ({
  ...cycleCount,
  cycleCountItems: [
    ...cycleCount.cycleCountItems,
    emptyRow(cycleCount.cycleCountItems[0]?.product?.id),
  ],
});

export const removeRow = (cycleCount, rowId) => ({
  ...cycleCount,
  cycleCountItems: cycleCount.cycleCountItems.filter((row) => rowId !== row.id),
});

export const setAllItemsUpdatedState = (cycleCount, updated) => ({
  ...cycleCount,
  cycleCountItems: cycleCount.cycleCountItems.map((item) => ({
    ...item,
    updated,
  })),
});

const removeItemFromCycleCounts = (cycleCounts, cycleCountId, itemId) => ({
  ...cycleCounts,
  [cycleCountId]: cycleCounts[cycleCountId]
    .filter((item) => item.cycleCountItemId !== itemId),
});

const mergeImportItems = (originalItem, importedItem) => ({
  ...originalItem,
  quantityCounted: importedItem ? importedItem.quantityCounted : originalItem.quantityCounted,
  comment: importedItem ? importedItem.comment : originalItem.comment,
  updated: true,
});

const createCustomItemsFromImport = (items) => (items
  ? items.map((item) => ({
    ...item,
    countIndex: 0,
    id: _.uniqueId('newRow'),
    custom: true,
    inventoryItem: {
      lotNumber: item.lotNumber,
      expirationDate: item.expirationDate,
    },
    product: {
      id: item.product.id,
      productCode: item.product.productCode,
    },
    binLocation: item.binLocation?.id ? {
      id: item.binLocation.id,
      name: item.binLocation.name,
    } : null,
  }))
  : []);

export const importCycleCounts = async ({
  importFile,
  locationId,
  currentCycleCountEntities,
  setImportErrors,
}) => {
  const response = await cycleCountApi.importCycleCountItems(
    importFile,
    locationId,
  );
  setImportErrors(response.data.errors);
  let cycleCounts = _.groupBy(response.data.data, 'cycleCountId');
  const assigneeImported = {};
  const countedByUpdates = {};
  const dateCountedUpdates = {};

  const importedTableData = currentCycleCountEntities.map((cycleCount) => {
    // After each iteration assign it to false again, so that the flag
    // can be reused for next cycle counts in the loop
    assigneeImported[cycleCount.id] = false;
    return {
      ...cycleCount,
      cycleCountItems: [
        ...cycleCount.cycleCountItems
          .map((item) => {
            const correspondingImportItem = cycleCounts[cycleCount.id]?.find(
              (cycleCountItem) => cycleCountItem.cycleCountItemId === item.id,
            );
            // Assign counted by and date counted only once to prevent performance issues
            // At this point, every item after being validated on the backend,
            // should have the same assignee and dateCounted set,
            // so we can make this operation only once
            // this is why we introduce the assigneeImported boolean flag
            if (correspondingImportItem && !assigneeImported[cycleCount.id]) {
              countedByUpdates[cycleCount.id] = correspondingImportItem.assignee;
              // Do not allow to clear the date counted dropdown
              // if dateCounted was not set in the sheet
              if (correspondingImportItem.dateCounted) {
                dateCountedUpdates[cycleCount.id] = correspondingImportItem.dateCounted;
              }
              // Mark the flag as true, so that it's not triggered for each item
              assigneeImported[cycleCount.id] = true;
            }

            if (correspondingImportItem) {
              // Remove items from the import that have a corresponding item
              // in the current cycle count. It allows us to treat items with
              // the wrong ID as new rows that do not already exist.
              cycleCounts = removeItemFromCycleCounts(
                cycleCounts,
                cycleCount.id,
                correspondingImportItem.cycleCountItemId,
              );
            }

            return mergeImportItems(item, correspondingImportItem);
          }),
        ...createCustomItemsFromImport(cycleCounts[cycleCount.id]),
      ],
    };
  });

  return {
    type: IMPORT_CYCLE_COUNTS,
    payload: {
      entities: _.keyBy(importedTableData, 'id'),
      dateCounted: dateCountedUpdates,
      countedBy: countedByUpdates,
    },
  };
};
