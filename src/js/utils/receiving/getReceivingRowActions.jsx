import React from 'react';

import { RiChat1Line, RiPencilLine } from 'react-icons/ri';

/**
 * Builds the action descriptors for a receiving row, consumed by ActionsCell.
 */
const getReceivingRowActions = ({ itemId, onOpenCommentModal }) => [
  {
    key: 'edit',
    icon: <RiPencilLine size={22} />,
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

export default getReceivingRowActions;
