import useInboundAddItemsActions from 'hooks/inboundV2/addItems/useInboundAddItemsActions';
import useInboundAddItemsColumns from 'hooks/inboundV2/addItems/useInboundAddItemsColumns';
import useInboundAddItemsFormState from 'hooks/inboundV2/addItems/useInboundAddItemsFormState';
import useInboundAddItemsImportExport from 'hooks/inboundV2/addItems/useInboundAddItemsImportExport';
import useHandleModalAction from 'hooks/useHandleModalAction';
import useTranslation from 'hooks/useTranslation';

const useInboundAddItemsForm = ({ next, previous }) => {
  useTranslation('stockMovement');

  const {
    isOpen: isModalOpen,
    data: modalData,
    type: modalType,
    openModal,
    handleResponse: handleModalResponse,
  } = useHandleModalAction();

  const {
    control,
    getValues,
    handleSubmit,
    errors,
    trigger,
    setValue,
    defaultTableRow,
  } = useInboundAddItemsFormState();

  const {
    loading,
    lineItemsArrayFields,
    addNewLine,
    removeRow,
    removeAllRows,
    saveRequisitionItemsInCurrentStep,
    fetchLineItems,
    removeSavedRow,
    nextPage,
    previousPage,
    save,
    saveAndExit,
    refresh,
  } = useInboundAddItemsActions({
    control,
    getValues,
    setValue,
    trigger,
    defaultTableRow,
    next,
    previous,
    openModal,
  });

  const {
    importTemplate,
    exportTemplate,
  } = useInboundAddItemsImportExport({
    getValues,
    setValue,
    fetchLineItems,
    saveRequisitionItemsInCurrentStep,
    defaultTableRow,
  });

  const { columns } = useInboundAddItemsColumns({
    errors,
    control,
    removeSavedRow,
    trigger,
    getValues,
    setValue,
    removeRow,
    addNewLine,
  });

  return {
    form: {
      control,
      handleSubmit,
      errors,
    },
    table: {
      lineItemsArrayFields,
      columns,
    },
    actions: {
      loading,
      addNewLine,
      removeAllRows,
      nextPage,
      previousPage,
      save,
      saveAndExit,
      refresh,
    },
    modal: {
      isModalOpen,
      modalData,
      modalType,
      handleModalResponse,
    },
    importExport: {
      importTemplate,
      exportTemplate,
    },
  };
};

export default useInboundAddItemsForm;
