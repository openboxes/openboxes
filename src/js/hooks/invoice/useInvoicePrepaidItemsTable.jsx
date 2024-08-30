import React, { useCallback, useEffect, useState } from 'react';

import _ from 'lodash';
import { RiDeleteBinLine, RiPencilLine } from 'react-icons/ri';

import prepaymentInvoiceApi from 'api/services/PrepaymentInvoiceApi';
import prepaymentInvoiceItemApi from 'api/services/PrepaymentInvoiceItemApi';
import InvoiceStatus from 'consts/invoiceStatus';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';

const useInvoicePrepaidItemsTable = ({
  invoiceItems,
  updateInvoiceItemQuantity,
  invoiceId,
  refetchData,
}) => {
  const spinner = useSpinner();
  const translate = useTranslate();
  const [editableRows, setEditableRows] = useState({});
  const [invalidRows, setInvalidRows] = useState([]);

  // Function reverting quantity to the version stored in editableRows state.
  // Used for reverting changed quantity after fetching new data.
  const revertQuantityToEdited = () => {
    Object.entries(editableRows)
      .forEach(([id, quantity]) => {
        updateInvoiceItemQuantity()(id)(quantity);
      });
  };

  // Triggering revertQuantityToEdited after delete (delete action needs refetch
  // because of the need for removing appropriate inverse item)
  useEffect(() => {
    revertQuantityToEdited();
  }, [invoiceItems.length]);

  const isEditable = (rowId) => rowId in editableRows;

  // Returns quantity which should be sent for updating invoice items
  const getEditedInvoiceItems = () => invoiceItems
    .filter((item) => isEditable(item.id))
    .map((item) => ({
      id: item.id,
      quantity: item.quantity,
    }));

  const deletePrepaidInvoiceItem = async (invoiceItemId) => {
    spinner.show();
    try {
      await prepaymentInvoiceItemApi.deletePrepaymentInvoiceItem(invoiceItemId);
      setEditableRows((rows) => _.omit(rows, invoiceItemId));
      setInvalidRows((rows) => rows.filter((rowId) => rowId !== invoiceItemId));
      refetchData({
        overrideInvoiceItems: true,
      });
    } finally {
      spinner.hide();
    }
  };

  // Sending a request for updating invoice items quantity (batch update)
  const updateInvoiceItem = async (callback) => {
    const invoiceItemsToUpdate = getEditedInvoiceItems();
    spinner.show();
    try {
      if (invoiceItemsToUpdate.length) {
        await prepaymentInvoiceApi.updateInvoiceItems(invoiceId, invoiceItemsToUpdate);
      }
      callback?.();
    } finally {
      spinner.hide();
    }
  };

  const markRowAsEditable = (rowId, quantity) => {
    setEditableRows((rows) => ({
      ...rows,
      [rowId]: quantity,
    }));
  };

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
    if (!isEditable(row?.id)) {
      return null;
    }

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

  const updateRowQuantity = (rowId, quantity) =>
    setEditableRows((rows) => ({
      ...rows,
      [rowId]: quantity,
    }));

  const isActionMenuVisible = (invoiceStatus, isPrepaymentInvoice, isInverseItem) =>
    !isInverseItem && !isPrepaymentInvoice && (
      invoiceStatus === InvoiceStatus.PENDING || invoiceStatus === InvoiceStatus.SUBMITTED
    );

  // Removing all information about edited lines
  const clearEditedState = () => {
    setEditableRows({});
    setInvalidRows([]);
  };

  // Saving edited lines and refetching all invoice data
  const save = async () => {
    await updateInvoiceItem();
    refetchData({
      overrideInvoiceItems: true,
      callback: clearEditedState,
    });
  };

  return {
    actions,
    isEditable,
    validate,
    updateRowQuantity,
    updateInvoiceItem,
    isActionMenuVisible,
    save,
    editableRows,
    isRowLoaded,
    isValid: !invalidRows.length,
  };
};

export default useInvoicePrepaidItemsTable;
