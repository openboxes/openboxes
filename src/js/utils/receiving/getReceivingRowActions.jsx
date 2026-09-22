import React from 'react';

import { RiChat1Line, RiDeleteBinLine, RiPencilLine } from 'react-icons/ri';

// The click event is forwarded so the popover can anchor itself under the icon that opened it.
const buildCommentAction = ({ itemId, hasComment, onOpenCommentModal }) => ({
  key: 'comment',
  icon: <RiChat1Line size={22} />,
  // A receipt item holds at most one comment, so the counter next to the icon only ever
  // marks that the row has one.
  badge: hasComment ? <span className="actions-cell__badge">1</span> : null,
  onClick: (event) => onOpenCommentModal?.(itemId, event),
  label: 'react.receiving.comment.label',
  defaultLabel: 'Comment',
});

/**
 * Builds the action descriptors for a receiving row, consumed by ActionsCell.
 */
const getReceivingRowActions = ({
  itemId, canComment, hasComment, onOpenCommentModal, onOpenEditModal,
}) => [
  {
    key: 'edit',
    icon: <RiPencilLine size={22} />,
    onClick: () => onOpenEditModal?.(itemId),
    label: 'react.default.button.edit.label',
    defaultLabel: 'Edit',
  },
  // The comment lives on a receipt item, so rows that don't back one (e.g. a line already
  // received in full) don't offer it.
  ...(canComment ? [buildCommentAction({ itemId, hasComment, onOpenCommentModal })] : []),
];

/**
 * Builds the action descriptors for a check step row, consumed by ActionsCell.
 */
export const getConfirmReceiptRowActions = ({ itemId, hasComment, onOpenCommentModal }) => [
  buildCommentAction({ itemId, hasComment, onOpenCommentModal }),
];

/**
 * Builds the action descriptors for a split item row (a single receipt item of a changed
 * shipment item), consumed by ActionsCell. The original line of the shipment item cannot be
 * removed (it backs the cancel-remaining flow on completion), so it carries the delete action
 * disabled, with a tooltip explaining why - the same treatment it gets in the edit modal.
 */
export const getReceivingSplitItemActions = ({ rowId, onRemove, isOriginalLine }) => [
  {
    key: 'delete',
    icon: (
      <RiDeleteBinLine
        size={22}
        className={`receiving-table__delete-icon ${isOriginalLine ? 'disabled-icon' : ''}`}
      />
    ),
    onClick: () => onRemove?.(rowId),
    disabled: isOriginalLine,
    // Only the line that cannot be deleted explains itself on hover.
    tooltipLabel: isOriginalLine ? 'react.receiving.deleteOriginalLine.tooltip.label' : null,
    defaultTooltipLabel: 'This line cannot be deleted because it represents the original product and lot entered by the shipper. If you did not receive this lot, enter zero in the receiving now field.',
    label: 'react.default.button.delete.label',
    defaultLabel: 'Delete',
  },
];

export default getReceivingRowActions;
