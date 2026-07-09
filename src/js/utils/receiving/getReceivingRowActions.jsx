import React from 'react';

import { RiChat1Line, RiDeleteBinLine, RiPencilLine } from 'react-icons/ri';

/**
 * Builds the action descriptors for a receiving row, consumed by ActionsCell.
 */
const getReceivingRowActions = ({ itemId, onOpenCommentModal, onOpenEditModal }) => [
  {
    key: 'edit',
    icon: <RiPencilLine size={22} />,
    onClick: () => onOpenEditModal?.(itemId),
    label: 'react.default.button.edit.label',
    defaultLabel: 'Edit',
  },
  {
    key: 'comment',
    icon: <RiChat1Line size={22} />,
    onClick: () => onOpenCommentModal?.(itemId),
    label: 'react.receiving.comment.label',
    defaultLabel: 'Comment',
  },
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
