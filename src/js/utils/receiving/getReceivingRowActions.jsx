import React from 'react';

import { RiChat1Line, RiDeleteBinLine, RiPencilLine } from 'react-icons/ri';

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
  // received in full) don't offer it. The click event is forwarded so the popover can
  // anchor itself under the icon that opened it.
  ...(canComment ? [{
    key: 'comment',
    icon: <RiChat1Line size={22} />,
    // A receipt item holds at most one comment, so the counter next to the icon only ever
    // marks that the row has one.
    badge: hasComment ? <span className="actions-cell__badge">1</span> : null,
    onClick: (event) => onOpenCommentModal?.(itemId, event),
    label: 'react.receiving.comment.label',
    defaultLabel: 'Comment',
  }] : []),
];

/**
 * Builds the action descriptors for a split item row (a single receipt item of a changed
 * shipment item), consumed by ActionsCell.
 */
export const getReceivingSplitItemActions = ({ rowId, onRemove }) => [
  {
    key: 'delete',
    icon: <RiDeleteBinLine size={22} className="receiving-table__delete-icon" />,
    onClick: () => onRemove?.(rowId),
    label: 'react.default.button.delete.label',
    defaultLabel: 'Delete',
  },
];

export default getReceivingRowActions;
