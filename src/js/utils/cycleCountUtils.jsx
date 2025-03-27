/**
 * Util method to remove lot number spaces from a cycle count item
 * @param cycleCountItem
 */
const trimLotNumberSpaces = (cycleCountItem) => ({
  ...cycleCountItem,
  inventoryItem: {
    ...cycleCountItem.inventoryItem,
    lotNumber: cycleCountItem.inventoryItem?.lotNumber?.trim(),
  },
});

export default trimLotNumberSpaces;
