import { useState } from 'react';

import { ReceivingView } from 'consts/receivingViewOptions';
import useReceivingActions from 'hooks/receiving/v2/useReceivingActions';
import useReceivingColumns from 'hooks/receiving/v2/useReceivingColumns';

const useReceivingForm = () => {
  const [view, setView] = useState(ReceivingView.TABLE);
  const { loading, lineItemsState, updateLineItem } = useReceivingActions(view);
  const { columns } = useReceivingColumns(view);

  return {
    view,
    setView,
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
