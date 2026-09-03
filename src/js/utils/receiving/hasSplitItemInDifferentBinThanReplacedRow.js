/**
 * Whether the item has a split item in a bin other than the receiving bin its replaced row shows
 */
const hasSplitItemInDifferentBinThanReplacedRow = (item, entities, receivingBin) => {
  // Receiving bins can be turned off by config, leaving the replaced row with no bin to cross out.
  if (!receivingBin) {
    return false;
  }
  const splitItemIds = entities?.[item?.toggleRowId]?.splitItemIds ?? [];
  return splitItemIds.some(
    (splitItemId) => entities?.[splitItemId]?.binLocation?.id !== receivingBin.id,
  );
};

export default hasSplitItemInDifferentBinThanReplacedRow;
