import React from 'react';

import { RiDownload2Line } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { getCurrencyCode, getCurrentLocale } from 'selectors';

import Button from 'components/form-elements/Button';
import expirationHistoryReportFilterFields
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportFilterFields';
import ExpirationHistoryReportFilters
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportFilters';
import ExpirationHistoryReportHeader
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportHeader';
import ExpirationHistoryReportTable
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportTable';
import { Locale, LocaleConverter } from 'consts/locale';
import useExpirationHistoryReport from 'hooks/reporting/useExpirationHistoryReport';
import useExpirationHistoryReportFilters from 'hooks/reporting/useExpirationHistoryReportFilters';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

const ExpirationHistoryReport = () => {
  useTranslation('reporting');

  const translate = useTranslate();

  const locale = useSelector(getCurrentLocale);
  const currencyCode = useSelector(getCurrencyCode);

  const {
    defaultFilterValues,
    filterParams,
    filtersInitialized,
    setFilterValues,
    shouldFetch,
    setShouldFetch,
  } = useExpirationHistoryReportFilters({ filterFields: expirationHistoryReportFilterFields });

  const {
    columns,
    loading,
    emptyTableMessage,
    tableData,
    paginationProps,
    setSerializedParams,
    exportData,
  } = useExpirationHistoryReport({
    filterParams,
    defaultFilterValues,
    shouldFetch,
    setShouldFetch,
    filtersInitialized,
  });

  const shouldDisableExportButton =
    !filterParams.startDate && !filterParams.endDate && !tableData?.data?.length > 0;

  const totalAmount = `${translate('react.report.expirationHistory.totalAmount.label', 'Total amount')}: ${tableData?.totalValueLostToExpiry?.toLocaleString([LocaleConverter[locale] || Locale.EN]) ?? 0} ${currencyCode}`;

  return (
    <PageWrapper>
      <ExpirationHistoryReportHeader />
      <div className="list-page-list-section">
        <ExpirationHistoryReportFilters
          defaultFilterValues={defaultFilterValues}
          setSerializedParams={setSerializedParams}
          setFilterValues={setFilterValues}
          setShouldFetch={setShouldFetch}
        />
        <div className="title-text px-3 py-2 d-flex justify-content-between align-items-center">
          <span>
            {`${tableData?.totalQuantityLostToExpiry ?? 0} ${translate('react.report.expirationHistory.quantityLostToExpiry.label', 'Quantity Lost to Expiry')} (${totalAmount})`}
          </span>
          <Button
            onClick={exportData}
            defaultLabel="Export"
            label="react.default.button.export.label"
            variant="secondary"
            EndIcon={<RiDownload2Line />}
            disabled={shouldDisableExportButton}
          />
        </div>
        <ExpirationHistoryReportTable
          columns={columns}
          emptyTableMessage={emptyTableMessage}
          filterParams={filterParams}
          tableData={tableData}
          paginationProps={paginationProps}
          loading={loading}
          footerComponent={() => (
            <span className="title-text p-1 d-flex flex-1 justify-content-end">
              {totalAmount}
            </span>
          )}
        />
      </div>
    </PageWrapper>
  );
};

export default ExpirationHistoryReport;
