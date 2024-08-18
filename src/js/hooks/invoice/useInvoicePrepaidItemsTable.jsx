import React, { useCallback } from 'react';

import { RiDeleteBinLine, RiPencilLine } from 'react-icons/ri';

import prepaymentInvoiceItemApi from 'api/services/PrepaymentInvoiceItemApi';
import useSpinner from 'hooks/useSpinner';

const useInvoicePrepaidItemsTable = ({ loadMoreRows, invoiceItems }) => {
  const spinner = useSpinner();

  const deletePrepaidInvoiceItem = async (invoiceItemId) => {
    spinner.show();
    try {
      await prepaymentInvoiceItemApi.deletePrepaymentInvoiceItem(invoiceItemId);
      loadMoreRows({ startIndex: 0, overrideInvoiceItems: true });
    } finally {
      spinner.hide();
    }
  };

  const actions = (row) => [
    {
      defaultLabel: 'Edit',
      label: 'react.default.button.edit.label',
      leftIcon: <RiPencilLine />,
      onClick: () => {},
    },
    {
      defaultLabel: 'Remove',
      label: 'react.default.button.remove.label',
      leftIcon: <RiDeleteBinLine />,
      variant: 'danger',
      onClick: () => deletePrepaidInvoiceItem(row.id),
    },
  ];

  const isRowLoaded = useCallback(
    ({ index }) => !!invoiceItems[index],
    [invoiceItems],
  );

  return {
    actions,
    isRowLoaded,
  };
};

export default useInvoicePrepaidItemsTable;
