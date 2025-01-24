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
  updateInvoiceItemData,
  invoiceId,
  refetchData,
  invoiceItemsMap,
}) => {
  const spinner = useSpinner();
  const translate = useTranslate();
  const [editableRows, setEditableRows] = useState({});
  const [invalidRows, setInvalidRows] = useState([]);

  // Function reverting data to the version stored in editableRows state.
  // Used for reverting information after fetching new data.
  const revertQuantityToEdited = () => {
    Object.entries(editableRows)
      .forEach(([id, data]) => {
        updateInvoiceItemData()(id, 'quantity')(data?.quantity);
        updateInvoiceItemData()(id, 'unitPrice')(data?.unitPrice);
      });
  };

  // Triggering revertQuantityToEdited after delete (delete action needs refetch
  // because of the need for removing appropriate inverse item)
  useEffect(() => {
    revertQuantityToEdited();
  }, [invoiceItems.length]);

  const isEditable = (rowId) => rowId in editableRows;

  // Returns lines which should be sent for updating invoice items
  const getEditedInvoiceItems = () => invoiceItems
    .filter((item) => isEditable(item.id))
    .map((item) => (item?.orderAdjustment
      ? {
        id: item.id,
        unitPrice: item.unitPrice,
      } : {
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

  // Sending a request for updating invoice items (batch update)
  const updateInvoiceItem = async (callback) => {
    try {
      const invoiceItemsToUpdate = getEditedInvoiceItems();
      if (invoiceItemsToUpdate.length) {
        await prepaymentInvoiceApi.updateInvoiceItems(invoiceId, invoiceItemsToUpdate);
      }
      callback?.();
    } catch {
      spinner.hide();
    }
  };

  const markRowAsEditable = ({
    rowId,
    quantity,
    unitPrice,
  }) => {
    setEditableRows((rows) => ({
      ...rows,
      [rowId]: {
        quantity,
        unitPrice,
      },
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
      onClick: () => markRowAsEditable({
        rowId: row.id,
        quantity: row.quantity,
        unitPrice: row.unitPrice,
      }),
    };

    return row?.isCanceled || (row?.orderAdjustment && row?.unitPrice === 0)
      ? [removeAction]
      : [editAction, removeAction];
  };

  const isRowLoaded = useCallback(
    ({ index }) => !!invoiceItemsMap.get(index),
    [invoiceItems],
  );

  const validateQuantity = (row) => {
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

  // validation for order adjustments
  const validateUnitPrice = (row) => {
    const haveUnitPricesDifferentSigns = (row.amount > 0 && row.unitPrice < 0)
                                                || (row?.amount < 0 && row.unitPrice > 0);
    const { unitPriceAvailableToInvoice: unitPrice, amount } = row;
    const unitPriceAvailableToInvoice = Math.abs(unitPrice) + Math.abs(amount);
    if (!row.unitPrice
      || unitPriceAvailableToInvoice < Math.abs(row.unitPrice)
      || haveUnitPricesDifferentSigns) {
      setInvalidRows((rows) => ([...rows, row?.id]));
      return translate('react.invoice.errors.unitPrice.label', 'Wrong amount to invoice value');
    }

    setInvalidRows((rows) => rows.filter((rowId) => row.id !== rowId));
    return null;
  };

  const validate = (row) => {
    if (!isEditable(row?.id)) {
      return null;
    }

    if (row?.orderAdjustment) {
      return validateUnitPrice(row);
    }

    return validateQuantity(row);
  };

  const updateRow = (
    rowId,
    data,
  ) =>
    setEditableRows((rows) => ({
      ...rows,
      [rowId]: {
        ...rows[rowId],
        ...data,
      },
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
    updateRow,
    updateInvoiceItem,
    isActionMenuVisible,
    save,
    editableRows,
    isRowLoaded,
    isValid: !invalidRows.length,
  };
};

export default useInvoicePrepaidItemsTable;
