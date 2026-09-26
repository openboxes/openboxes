import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocationId } from 'selectors';

import { AUTO_ISSUANCE_TRANSACTIONS_REPORT_CSV } from 'api/urls';
import useSpinner from 'hooks/useSpinner';
import dateWithoutTimeZone from 'utils/dateUtils';
import exportFileFromAPI from 'utils/file-download-util';

/**
 * Hook to manage export actions for the Auto-Issuance Transactions tab in Cycle Count reporting.
 */
const useAutoIssuanceTransactionsTabExport = ({
  startDate,
  endDate,
  products,
  binLocations,
}) => {
  const spinner = useSpinner();

  const currentLocationId = useSelector(getCurrentLocationId);

  const exportAutoIssuanceTransactionsReport = async (filters) => {
    spinner.show();
    await exportFileFromAPI({
      url: AUTO_ISSUANCE_TRANSACTIONS_REPORT_CSV,
      params: _.omitBy({
        endDate: dateWithoutTimeZone({
          date: filters.endDate,
        }),
        startDate: dateWithoutTimeZone({
          date: filters.startDate,
        }),
        products: filters.products?.map?.((product) => product.id),
        binLocations: filters.binLocations?.map?.((binLocation) => binLocation.id),
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
      label: 'react.cycleCount.reporting.exportFullReport.label',
      defaultLabel: 'Export Full Report',
      onClick: () => exportAutoIssuanceTransactionsReport({ startDate, endDate }),
    }, {
      label: 'react.cycleCount.reporting.exportFilteredReport.label',
      defaultLabel: 'Export Filtered Report',
      onClick: () => exportAutoIssuanceTransactionsReport({
        startDate, endDate, products, binLocations,
      }),
    },
  ];
  return {
    actions,
  };
};

export default useAutoIssuanceTransactionsTabExport;
