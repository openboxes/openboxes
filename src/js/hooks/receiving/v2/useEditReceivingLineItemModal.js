import { useCallback, useState } from 'react';

// TODO: for now this only opens/closes the modal. Real implementation will be done iteratively.
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
