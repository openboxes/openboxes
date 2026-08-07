/**
 * Checks whether any line of the receipt sits in a bin other than the given one, treating a line
 * with no bin at all as a line that has not been moved.
 */
const anyLineHasOtherBin = (lineItemsState, bin) =>
  Object.values(lineItemsState.entities)
    .some((item) => item?.binLocation && item.binLocation.id !== bin?.id);

export default anyLineHasOtherBin;
