/*
  Hook to manage export actions for the Products tab in Cycle Count reporting.
  startDate: The start date for the report.
  endDate: The end date for the report.
  products: The list of products to include in the report.
 */
import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocationId, getCurrentLocationName } from 'selectors';

import { INVENTORY_AUDIT_SUMMARY_REPORT } from 'api/urls';
import useSpinner from 'hooks/useSpinner';
import dateWithoutTimeZone from 'utils/dateUtils';
import exportFileFromAPI from 'utils/file-download-util';

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
    const date = new Date();
    const [month, day, year] = [date.getMonth(), date.getDate(), date.getFullYear()];
    const [hour, minutes, seconds] = [date.getHours(), date.getMinutes(), date.getSeconds()];
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
      filename: `InventoryChangesReport-${currentLocationName}-${year}${month}${day}-${hour}${minutes}${seconds}`,
      afterExporting: spinner.hide,
    });
  };

  // Export report: exports the same table as the Products Changes table in the UI,
  // for the time range selected, all records (without filters).
  // Export results: exports the same table as the Product Changes tables in the UI,
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
