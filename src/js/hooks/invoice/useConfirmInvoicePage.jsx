import {
  useCallback, useEffect, useMemo, useState,
} from 'react';

import { useSelector } from 'react-redux';

import invoiceApi from 'api/services/InvoiceApi';
import { INVOICE_URL } from 'consts/applicationUrls';
import useSpinner from 'hooks/useSpinner';
import accountingFormat from 'utils/number-utils';

const useConfirmInvoicePage = ({ initialValues }) => {
  const spinner = useSpinner();

  const {
    pageSize,
    isSuperuser,
  } = useSelector((state) => ({
    pageSize: state.session.pageSize,
    isSuperuser: state.session.isSuperuser,
  }));

  const [stateValues, setStateValues] = useState({
    ...initialValues,
    invoiceItems: [],
  });

  /**
   * Fetches invoice values from API.
   * @public
   */
  const fetchInvoiceData = useCallback(() => {
    spinner.show();
    invoiceApi.getInvoice(stateValues.id)
      .then((response) => {
        setStateValues((state) => ({
          ...state,
          documents: response.data.data.documents,
          totalValue: response.data.data.totalValue,
        }));
      })
      .finally(() => spinner.hide());
  }, [stateValues.id]);

  useEffect(() => {
    if (stateValues.id) {
      fetchInvoiceData();
    }
  }, [stateValues.id]);

  const totalValue = useMemo(() =>
    accountingFormat(stateValues.totalValue.toFixed(2)), [stateValues.totalValue]);

  const submitInvoice = () => {
    invoiceApi.submitInvoice(stateValues.id)
      .then(() => {
        window.location = INVOICE_URL.show(stateValues.id);
      });
  };

  const postInvoice = () => {
    invoiceApi.postInvoice(stateValues.id)
      .then(() => {
        window.location = INVOICE_URL.show(stateValues.id);
      });
  };

  /**
   * Sets state of invoice items after fetch and calls method to fetch next items
   * @param response
   * @param {boolean} overrideInvoiceItems
   * @public
   */
  const setInvoiceItems = (response, overrideInvoiceItems = true) => {
    spinner.show();
    const { data, totalCount } = response.data;
    setStateValues((state) => ({
      ...state,
      invoiceItems: overrideInvoiceItems
        ? data
        : [
          ...state.invoiceItems,
          ...data,
        ],
      totalCount,
    }));

    spinner.hide();
  };

  /**
   * Loads more rows, needed for pagination
   * @param {index} startIndex
   * @public
   */
  const loadMoreRows = useCallback(
    ({ startIndex, overrideInvoiceItems = false }) => invoiceApi.getInvoiceItems(stateValues.id, {
      params: { offset: startIndex, max: pageSize },
    })
      .then((response) => {
        setInvoiceItems(response, overrideInvoiceItems);
      }),
    [stateValues.id, pageSize],
  );

  const updateInvoiceItemQuantity = (updateRowQuantity) => (invoiceItemId) => (quantity) => {
    updateRowQuantity?.(invoiceItemId, quantity);
    setStateValues((state) => ({
      ...state,
      invoiceItems: state.invoiceItems.map((item) => {
        if (item.id === invoiceItemId) {
          return { ...item, quantity };
        }

        return item;
      }),
    }));
  };

  const refetchData = async ({
    callback,
    overrideInvoiceItems = false,
  }) => {
    try {
      spinner.show();
      await fetchInvoiceData();
      await loadMoreRows({
        startIndex: 0,
        overrideInvoiceItems,
      });
    } finally {
      callback?.();
      spinner.hide();
    }
  };

  return {
    isSuperuser,
    stateValues,
    fetchInvoiceData,
    totalValue,
    submitInvoice,
    postInvoice,
    setInvoiceItems,
    updateInvoiceItemQuantity,
    refetchData,
    loadMoreRows,
  };
};

export default useConfirmInvoicePage;
