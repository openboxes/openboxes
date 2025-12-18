import _ from 'lodash';
import moment from 'moment';

import { IMPORT_CYCLE_COUNTS } from 'actions/types';
import cycleCountApi from 'api/services/CycleCountApi';
import { NEW_ROW } from 'consts/cycleCount';
import { DateFormat } from 'consts/timeFormat';

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
  id: _.uniqueId(NEW_ROW),
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

const removeItemFromCycleCounts = ({
  cycleCounts,
  cycleCountId,
  itemId,
}) => ({
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

const createCustomItemsFromImport = (items, locale) => (items
  ? items.map((item) => ({
    ...item,
    countIndex: 0,
    id: _.uniqueId(NEW_ROW),
    custom: true,
    inventoryItem: {
      lotNumber: item.lotNumber,
      expirationDate: item.expirationDate
        ? moment(item.expirationDate)
          .locale(locale)
          .format(DateFormat.MMM_DD_YYYY)
        : null,
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
  locale,
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
              cycleCounts = removeItemFromCycleCounts({
                cycleCounts,
                cycleCountId: cycleCount.id,
                itemId: correspondingImportItem.cycleCountItemId,
              });
            }

            return mergeImportItems(item, correspondingImportItem);
          }),
        ...createCustomItemsFromImport(cycleCounts[cycleCount.id], locale),
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

const mergeImportItemsRecount = (originalItem, importedItem, reasonCodes) => ({
  ...originalItem,
  quantityRecounted: importedItem ? importedItem.quantityRecounted : originalItem.quantityRecounted,
  comment: importedItem ? importedItem.comment : originalItem.comment,
  rootCause: (importedItem && importedItem.rootCause && {
    id: importedItem.rootCause,
    label: reasonCodes?.find?.((reasonCode) => reasonCode?.id === importedItem.rootCause)?.label,
    value: importedItem.rootCause,
  }) || (importedItem ? null : originalItem.rootCause),
  updated: true,
});

const createCustomItemsRecountFromImport = (items, locale, reasonCodes) => (items
  ? items.map((item) => ({
    ...item,
    countIndex: 1,
    id: _.uniqueId(NEW_ROW),
    custom: true,
    inventoryItem: {
      lotNumber: item.lotNumber,
      expirationDate: item.expirationDate
        ? moment(item.expirationDate)
          .locale(locale)
          .format(DateFormat.MMM_DD_YYYY)
        : null,
    },
    product: {
      id: item.product.id,
      productCode: item.product.productCode,
    },
    binLocation: item.binLocation?.id ? {
      id: item.binLocation.id,
      name: item.binLocation.name,
    } : null,
    rootCause: item?.rootCause ? {
      id: item.rootCause,
      label: reasonCodes?.find?.((reasonCode) => reasonCode?.id === item.rootCause)?.label,
      value: item.rootCause,
    } : null,
  }))
  : []);

export const importCycleCountsRecount = async ({
  importFile,
  locationId,
  tableData,
  setImportErrors,
  locale,
  recountedBy,
  defaultRecountedBy,
  dateRecounted,
  reasonCodes,
}) => {
  const response = await cycleCountApi.importCycleCountItemsRecount(
    importFile,
    locationId,
  );
  setImportErrors(response.data.errors);
  let cycleCounts = _.groupBy(response.data.data, 'cycleCountId');
  const recountAssigneeImported = {};
  const recountedByUpdates = {};
  const dateRecountedUpdates = {};

  // eslint-disable-next-line no-param-reassign
  tableData.current = tableData.current.map((cycleCount) => {
    // After each iteration assign it to false again, so that the flag
    // can be reused for next cycle counts in the loop
    recountAssigneeImported[cycleCount.id] = false;
    return {
      ...cycleCount,
      cycleCountItems: [
        ...cycleCount.cycleCountItems
          .map((item) => {
            const correspondingImportItem = cycleCounts[cycleCount.id]?.find(
              (cycleCountItem) => cycleCountItem.cycleCountItemId === item.id,
            );
            // Assign recounted by and date recounted only once to prevent performance issues
            // At this point, every item after being validated on the backend,
            // should have the same recountAssignee and dateRecounted set,
            // so we can make this operation only once
            // this is why we introduce the recountAssigneeImported boolean flag
            if (correspondingImportItem && !recountAssigneeImported[cycleCount.id]) {
              recountedByUpdates[cycleCount.id] = correspondingImportItem.recountAssignee;
              // Do not allow to clear the date recounted dropdown
              // if dateRecounted was not set in the sheet
              if (correspondingImportItem.dateRecounted) {
                dateRecountedUpdates[cycleCount.id] = correspondingImportItem.dateRecounted;
              }
              // Mark the flag as true, so that it's not triggered for each item
              recountAssigneeImported[cycleCount.id] = true;
            }

            if (correspondingImportItem) {
              // Remove items from the import that have a corresponding item
              // in the current cycle count. It allows us to treat items with
              // the wrong ID as new rows that do not already exist.
              cycleCounts = removeItemFromCycleCounts({
                cycleCounts,
                cycleCountId: cycleCount.id,
                itemId: correspondingImportItem.cycleCountItemId,
              });
            }
            return mergeImportItemsRecount(item, correspondingImportItem, reasonCodes);
          }),
        ...createCustomItemsRecountFromImport(cycleCounts[cycleCount.id], locale, reasonCodes),
      ],
    };
  });
  // eslint-disable-next-line no-param-reassign
  recountedBy.current = { ...recountedBy.current, ...recountedByUpdates };
  // eslint-disable-next-line no-param-reassign
  defaultRecountedBy.current = { ...defaultRecountedBy.current, ...recountedByUpdates };
  // eslint-disable-next-line no-param-reassign
  dateRecounted.current = { ...dateRecounted.current, ...dateRecountedUpdates };
};
