import { useCallback, useState } from 'react';

// TODO: for now this only opens the modal. Real implementation will be done in OBPIH-7849.
const useCommentModal = () => {
  const [isOpen, setIsOpen] = useState(false);
  const openModal = useCallback(() => setIsOpen(true), []);
  const closeModal = useCallback(() => setIsOpen(false), []);

  return { isOpen, openModal, closeModal };
};

export default useCommentModal;
