import useReceivingActions from 'hooks/receiving/v2/useReceivingActions';
import useReceivingColumns from 'hooks/receiving/v2/useReceivingColumns';

const useReceivingForm = () => {
  const { loading, lineItems } = useReceivingActions();
  const { columns } = useReceivingColumns();

  return {
    table: {
      lineItems,
      columns,
    },
    actions: {
      loading,
    },
  };
};

export default useReceivingForm;
