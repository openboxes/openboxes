import _ from 'lodash';

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
