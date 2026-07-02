import { useCallback, useState } from 'react';

// TODO: only opens/closes the modal for now.
const useEditReceivingLineItemModal = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [itemId, setItemId] = useState(null);
  const openModal = useCallback((id) => {
    setItemId(id);
    setIsOpen(true);
  }, []);
  const closeModal = useCallback(() => setIsOpen(false), []);

  return {
    isOpen, itemId, openModal, closeModal,
  };
};

export default useEditReceivingLineItemModal;
