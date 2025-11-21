import { useState } from 'react';

/**
 * Custom hook for handling modal actions that return a Promise.
 */
const useHandleModalAction = () => {
  const [modalState, setModalState] = useState({
    isOpen: false,
    data: null,
    resolver: null,
    type: null,
  });

  /**
   *  Shows the modal with provided data and returns a Promise
   *  @param {Array} data - Data to be shown in the modal.
   *  @param {string} type - Type of modal to be displayed.
   *  @returns {Promise} - Resolves to 'true' if user confirms the update, 'false' if not.
   */
  const openModal = ({ data, type }) =>
    new Promise((resolve) => {
      setModalState({
        isOpen: true,
        data,
        type,
        resolver: resolve,
      });
    });

  /**
   * Handles the response from the modal and resets the state.
   * @param {boolean} result - 'true' if the user confirmed, 'false' otherwise.
   */
  const handleResponse = (result) => {
    modalState.resolver?.(result);
    setModalState({
      isOpen: false,
      data: null,
      resolver: null,
      type: null,
    });
  };

  return {
    isOpen: modalState.isOpen,
    data: modalState.data,
    type: modalState.type,
    openModal,
    handleResponse,
  };
};

export default useHandleModalAction;
