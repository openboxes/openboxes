import React, { useCallback, useEffect, useState } from 'react';

import _ from 'lodash';
import { RiDeleteBinLine, RiPencilLine } from 'react-icons/ri';

import prepaymentInvoiceItemApi from 'api/services/PrepaymentInvoiceItemApi';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';

const useInvoicePrepaidItemsTable = ({ loadMoreRows, invoiceItems, updateInvoiceItemQuantity }) => {
  const spinner = useSpinner();
  const translate = useTranslate();
  const [editableRows, setEditableRows] = useState({});
  const [invalidRows, setInvalidRows] = useState([]);

  const revertQuantityToEdited = () => {
    Object.entries(editableRows).forEach(([id, quantity]) => {
      updateInvoiceItemQuantity()(id)(quantity);
    });
  };

  useEffect(() => {
    revertQuantityToEdited();
  }, [invoiceItems.length]);

  const deletePrepaidInvoiceItem = async (invoiceItemId) => {
    spinner.show();
    try {
      await prepaymentInvoiceItemApi.deletePrepaymentInvoiceItem(invoiceItemId);
      setEditableRows((rows) => _.omit(rows, invoiceItemId));
      setInvalidRows((rows) => rows.filter((rowId) => rowId !== invoiceItemId));
      loadMoreRows({ startIndex: 0, overrideInvoiceItems: true });
    } finally {
      spinner.hide();
    }
  };

  const markRowAsEditable = (rowId, quantity) => {
    setEditableRows((rows) => ({ ...rows, [rowId]: quantity }));
  };

  const isEditable = (rowId) => rowId in editableRows;

  const actions = (row) => {
    const removeAction = {
      defaultLabel: 'Remove',
      label: 'react.default.button.remove.label',
      leftIcon: <RiDeleteBinLine />,
      variant: 'danger',
      onClick: () => deletePrepaidInvoiceItem(row.id),
    };

    const editAction = {
      defaultLabel: 'Edit',
      label: 'react.default.button.edit.label',
      leftIcon: <RiPencilLine />,
      onClick: () => markRowAsEditable(row.id, row.quantity),
    };

    return row?.isCanceled || row?.orderAdjustment
      ? [removeAction]
      : [editAction, removeAction];
  };

  const isRowLoaded = useCallback(
    ({ index }) => !!invoiceItems[index],
    [invoiceItems],
  );

  const validate = (row) => {
    if (
      _.toInteger(row?.quantityAvailableToInvoice) < row?.quantity
      || _.toInteger(row?.quantity) <= 0
    ) {
      setInvalidRows((rows) => ([...rows, row?.id]));
      return translate('react.invoice.errors.quantityToInvoice.label', 'Wrong quantity to invoice value');
    }

    setInvalidRows((rows) => rows.filter((rowId) => row.id !== rowId));
    return null;
  };

  const getEditedInvoiceItems = () => invoiceItems
    .filter((item) => isEditable(item.id))
    .map((item) => ({ id: item.id, quantity: item.quantity }));

  const updateRowQuantity = (rowId, quantity) =>
    setEditableRows((rows) => ({ ...rows, [rowId]: quantity }));

  return {
    actions,
    isEditable,
    validate,
    updateRowQuantity,
    editableRows,
    isRowLoaded,
    isValid: !invalidRows.length,
  };
};

export default useInvoicePrepaidItemsTable;
