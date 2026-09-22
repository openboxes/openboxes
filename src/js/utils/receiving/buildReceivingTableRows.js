import ReceivingRowType from 'consts/receivingRowType';

// Maps the normalized line items state into the rows consumed by DataTable, shared by
// the receiving and check step tables. Separators pass through without meta. Meta is
// only used to disable (grey out) fully received rows, and separators don't need disabling.
const buildReceivingTableRows = (lineItemsState) => {
  const { entities, ids } = lineItemsState;

  const buildRow = (rowId) => ({
    id: rowId,
    meta: {
      isRowDisabled: entities[rowId]?.isCompleted,
      label: 'react.receiving.fullyReceived.label',
      defaultMessage: 'This line has been fully received',
    },
  });

  // Split item rows, rendered by TanStack as subRows of their toggle row.
  // Split items of the same product merge into one visual block.
  const buildSubRow = (splitItemId, splitItemIndex, splitItemIds) => {
    const nextSplitItem = entities[splitItemIds[splitItemIndex + 1]];
    return {
      id: splitItemId,
      mergeWithNextRow: Boolean(nextSplitItem && !nextSplitItem.isFirstSplitItem),
      isLastSubRow: splitItemIndex === splitItemIds.length - 1,
    };
  };

  const isReplaced = (id) => entities[id]?.rowType === ReceivingRowType.REPLACED;

  // A "block" = shipment item row + its toggle row + split-item sub-rows. All rows in a
  // block share a class so they get the same alternating background. REPLACED merges into
  // the next row (its toggle), so both stay in the same block.
  const { rows } = ids
    // Split item rows render as subRows of their toggle row, not at the top level.
    .filter((entry) => entities[entry]?.rowType !== ReceivingRowType.SPLIT_ITEM)
    .reduce((acc, entry, index, entries) => {
      // Separators (pack-level headers) don't belong to any block - blockIndex stays.
      if (entry.isSeparator) {
        acc.rows.push(entry);
        return acc;
      }
      // A row starts a new block unless the previous top-level row is a REPLACED row
      // (which merges with its next row - this one - forming one block). First row,
      // row after a separator, or row after a non-REPLACED all start a new block.
      const previousEntry = entries[index - 1];
      const startsNewBlock = !previousEntry
        || previousEntry.isSeparator
        || !isReplaced(previousEntry);
      if (startsNewBlock) {
        acc.blockIndex += 1;
      }
      const className = acc.blockIndex % 2 === 0
        ? 'receiving-table__block--even'
        : '';
      acc.rows.push({
        ...buildRow(entry),
        className,
        // Sub-rows inherit the parent's class so the whole block shares one background.
        subRows: entities[entry]?.splitItemIds?.map((splitItemId, i, splitItemIds) => ({
          ...buildSubRow(splitItemId, i, splitItemIds),
          className,
        })),
        // A replaced row is always followed by its toggle row and merges with it.
        mergeWithNextRow: isReplaced(entry),
      });
      return acc;
    }, { rows: [], blockIndex: 0 });

  return rows;
};

export default buildReceivingTableRows;
