import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocationId } from 'selectors';

import { INVENTORY_TRANSACTIONS_SUMMARY_CSV } from 'api/urls';
import useSpinner from 'hooks/useSpinner';
import dateWithoutTimeZone from 'utils/dateUtils';
import exportFileFromAPI from 'utils/file-download-util';

/**
 * Hook to manage export actions for the Inventory Transactions tab in Cycle Count reporting.
 *
 * @param {string} startDate - The start date for the report (ISO string).
 * @param {string} endDate - The end date for the report (ISO string).
 * @param {Product[]} [products] - Optional list of products to include.
 * @returns {{ actions: { label: string, defaultLabel: string, onClick: Function }[] }}
 */
const useInventoryTransactionsTabExport = ({
  startDate,
  endDate,
  products,
}) => {
  const spinner = useSpinner();

  const currentLocationId = useSelector(getCurrentLocationId);

  const exportInventoryTransactionsReport = async (filters) => {
    spinner.show();
    await exportFileFromAPI({
      url: INVENTORY_TRANSACTIONS_SUMMARY_CSV,
      params: _.omit({
        endDate: dateWithoutTimeZone({
          date: filters.endDate,
        }),
        startDate: dateWithoutTimeZone({
          date: filters.startDate,
        }),
        products: filters.products ? filters.products?.map((product) => product.id) : undefined,
        facility: currentLocationId,
      }, (val) => {
        if (typeof val === 'boolean') {
          return !val;
        }
        return _.isEmpty(val);
      }),
      afterExporting: spinner.hide,
    });
  };

  const actions = [
    {
      label: 'react.cycleCount.reporting.exportReport.label',
      defaultLabel: 'Export report',
      onClick: () => exportInventoryTransactionsReport({ startDate, endDate }),
    }, {
      label: 'react.cycleCount.reporting.exportResults.label',
      defaultLabel: 'Export results',
      onClick: () => exportInventoryTransactionsReport({ startDate, endDate, products }),
    },
  ];
  return {
    actions,
  };
};

export default useInventoryTransactionsTabExport;
