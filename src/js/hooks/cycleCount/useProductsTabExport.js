import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocationId, getCurrentLocationName } from 'selectors';

import { INVENTORY_AUDIT_SUMMARY_REPORT_CSV } from 'api/urls';
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
      url: INVENTORY_AUDIT_SUMMARY_REPORT_CSV,
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
      filename: `InventoryChangesReport-${currentLocationName}-${getFilenameDateString()}`,
      afterExporting: spinner.hide,
    });
  };

  const actions = [
    {
      label: 'react.cycleCount.reporting.exportFullReport.label',
      defaultLabel: 'Export Full Report',
      onClick: () => exportProductChangesReport({ startDate, endDate }),
    }, {
      label: 'react.cycleCount.reporting.exportFilteredReport.label',
      defaultLabel: 'Export Filtered Report',
      onClick: () => exportProductChangesReport({ startDate, endDate, products }),
    },
  ];
  return {
    actions,
  };
};

export default useProductsTabExport;
