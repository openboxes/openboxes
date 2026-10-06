/**
 * Receiving-now quantities of the lines below a replaced row, summed.
 */
const sumSplitItemsQuantityReceiving = (item, entities) => {
  const splitItemIds = entities?.[item?.toggleRowId]?.splitItemIds ?? [];
  return splitItemIds.reduce(
    (sum, splitItemId) => sum + (Number(entities?.[splitItemId]?.quantityReceiving) || 0),
    0,
  );
};

export default sumSplitItemsQuantityReceiving;
