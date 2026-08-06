/**
 * Checks whether any line of the receipt sits somewhere other than its default location: the
 * receiving bin, which every line is assigned when the receipt is started, or no bin at all when
 * the location generates no receiving bin.
 */
const isAnyBinLocationOtherThanDefault = (lineItemsState, receivingBin) =>
  Object.values(lineItemsState.entities)
    .some((item) => item?.binLocation && item.binLocation.id !== receivingBin?.id);

export default isAnyBinLocationOtherThanDefault;
