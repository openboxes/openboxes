import { useCallback, useRef, useState } from 'react';

import useSaveComment from 'hooks/receiving/v2/useSaveComment';

// Vertical gap between the clicked comment icon and the popover opened under it.
const COMMENT_MODAL_GAP = 4;

const useCommentModal = ({ updateLineItemComment }) => {
  const [isOpen, setIsOpen] = useState(false);
  const itemId = useRef(null);
  const anchor = useRef(null);

  const openModal = useCallback((id, anchorPosition) => {
    itemId.current = id;
    anchor.current = anchorPosition ?? null;
    setIsOpen(true);
  }, []);

  const closeModal = () => setIsOpen(false);

  // Opens the comment popover anchored under the clicked icon. Measured here rather than in the
  // hook because it needs the DOM node of the button that fired the event.
  const onOpenCommentModal = useCallback((rowId, event) => {
    const { bottom, right } = event.currentTarget.getBoundingClientRect();
    openModal(rowId, {
      top: bottom + COMMENT_MODAL_GAP,
      right: Math.max(window.innerWidth - right, 0),
    });
  }, [openModal]);

  const { saveComment } = useSaveComment({
    updateLineItemComment,
    onClose: closeModal,
  });

  return {
    isOpen,
    itemId: itemId.current,
    anchor: anchor.current,
    openModal,
    closeModal,
    onOpenCommentModal,
    saveComment,
  };
};

export default useCommentModal;
