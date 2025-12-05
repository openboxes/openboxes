import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocationId, getCurrentLocationName } from 'selectors';

import { INVENTORY_AUDIT_SUMMARY_REPORT } from 'api/urls';
import useSpinner from 'hooks/useSpinner';
import dateWithoutTimeZone, { getFilenameDateString } from 'utils/dateUtils';
import exportFileFromAPI from 'utils/file-download-util';

/**
 * @typedef {Object} Product
 * @property {number|string} id - Product identifier.
 *
 * @typedef {Object} UseProductsTabExportParams
 * @property {string} startDate - The start date for the report (ISO string).
 * @property {string} endDate - The end date for the report (ISO string).
 * @property {Product[]} [products] - Optional list of products to include.
 */

/**
 * Hook to manage export actions for the Products tab in Cycle Count reporting.
 * @param {UseProductsTabExportParams} params - The parameters for the hook.
 * @returns {{ actions: { label: string, defaultLabel: string, onClick: Function }[] }}
 */
const useProductsTabExport = ({
  startDate,
  endDate,
  products,
}) => {
  const spinner = useSpinner();

  const currentLocationId = useSelector(getCurrentLocationId);
  const currentLocationName = useSelector(getCurrentLocationName);

  const exportProductChangesReport = async (filters) => {
    spinner.show();
    await exportFileFromAPI({
      url: INVENTORY_AUDIT_SUMMARY_REPORT,
      params: _.omit({
        endDate: dateWithoutTimeZone({
          date: filters.endDate,
        }),
        startDate: dateWithoutTimeZone({
          date: filters.startDate,
        }),
        products: filters.products?.map((product) => product.id),
        facility: currentLocationId,
      }, (val) => {
        if (typeof val === 'boolean') {
          return !val;
        }
        return _.isEmpty(val);
      }),
      filename: `InventoryChangesReport-${currentLocationName}-${getFilenameDateString()}`,
      afterExporting: spinner.hide,
    });
  };

  // Export report: exports the same table as the Product Changes table in the UI,
  // for the time range selected, all records (without filters).
  // Export results: exports the same table as the Product Changes table in the UI,
  // for the time range selected, only the results currently visible in the
  // table (filters applied)
  const actions = [
    {
      label: 'react.cycleCount.reporting.exportReport.label',
      defaultLabel: 'Export report',
      onClick: () => exportProductChangesReport({ startDate, endDate }),
    }, {
      label: 'react.cycleCount.reporting.exportResults.label',
      defaultLabel: 'Export results',
      onClick: () => exportProductChangesReport({ startDate, endDate, products }),
    },
  ];
  return {
    actions,
  };
};

export default useProductsTabExport;
