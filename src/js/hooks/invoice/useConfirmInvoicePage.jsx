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
    // react-virtualize expects a map for checking if an item is already fetched. Using a list
    // results in fetching the same items range multiple times.
    invoiceItems: new Map(),
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

  const parseItemsToMap = (items, indexOffset = 0) =>
    new Map(items.map((item, index) => [index + indexOffset, item]));

  const addToLineItems = (stateData, newItems, firstIndex) => {
    const items = parseItemsToMap(newItems, firstIndex);
    return new Map([...Array.from(stateData), ...items]);
  };

  /**
   * Sets state of invoice items after fetch and calls method to fetch next items
   * @param response
   * @param {boolean} overrideInvoiceItems
   * @param startIndex
   * @public
   */
  const setInvoiceItems = (response, overrideInvoiceItems = true, startIndex = 0) => {
    spinner.show();
    const { data, totalCount } = response.data;
    setStateValues((state) => ({
      ...state,
      invoiceItems: overrideInvoiceItems
        ? parseItemsToMap(data)
        : addToLineItems(state.invoiceItems, data, startIndex),
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
    ({ startIndex, stopIndex, overrideInvoiceItems = false }) =>
      invoiceApi.getInvoiceItems(stateValues.id, {
        params: { offset: startIndex, max: stopIndex ? (stopIndex - startIndex + 1) : pageSize },
      })
        .then((response) => {
          setInvoiceItems(response, overrideInvoiceItems, startIndex);
        }),
    [stateValues.id, pageSize],
  );

  const updateInvoiceItemData = (updateRow) => (invoiceItemId, fieldName) => (value) => {
    updateRow?.(
      invoiceItemId,
      {
        [fieldName]: value,
      },
    );
    setStateValues((state) => ({
      ...state,
      invoiceItems: new Map(Array.from(state.invoiceItems).map(([idx, item]) => {
        if (item.id === invoiceItemId) {
          return [idx, { ...item, [fieldName]: value }];
        }

        return [idx, item];
      })),
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
        stopIndex: stateValues.totalCount,
        overrideInvoiceItems,
      });
    } finally {
      callback?.();
      spinner.hide();
    }
  };

  return {
    isSuperuser,
    stateValues: {
      ...stateValues,
      invoiceItems: Array.from(stateValues.invoiceItems.values()),
    },
    invoiceItemsMap: stateValues.invoiceItems,
    fetchInvoiceData,
    totalValue,
    submitInvoice,
    postInvoice,
    setInvoiceItems,
    updateInvoiceItemData,
    refetchData,
    loadMoreRows,
  };
};

export default useConfirmInvoicePage;
