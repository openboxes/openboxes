import useReceivingActions from 'hooks/receiving/v2/useReceivingActions';
import useReceivingColumns from 'hooks/receiving/v2/useReceivingColumns';

const useReceivingForm = () => {
  const { loading, lineItemsState, updateLineItem } = useReceivingActions();
  const { columns } = useReceivingColumns();

  return {
    table: {
      lineItemsState,
      columns,
    },
    actions: {
      loading,
      updateLineItem,
    },
  };
};

export default useReceivingForm;
