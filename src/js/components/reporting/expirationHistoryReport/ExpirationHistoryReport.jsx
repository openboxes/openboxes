import React from 'react';

import { RiDownload2Line } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import expirationHistoryReportFilterFields
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportFilterFields';
import ExpirationHistoryReportFilters
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportFilters';
import ExpirationHistoryReportHeader
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportHeader';
import ExpirationHistoryReportTable
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportTable';
import useExpirationHistoryReport from 'hooks/reporting/useExpirationHistoryReport';
import useExpirationHistoryReportFilters from 'hooks/reporting/useExpirationHistoryReportFilters';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

const ExpirationHistoryReport = () => {
  useTranslation('reporting');

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
        <div className="d-flex m-2 gap-8 justify-content-end">
          <Button
            onClick={exportData}
            defaultLabel="Export"
            label="react.default.button.export.label"
            variant="secondary"
            EndIcon={<RiDownload2Line />}
          />
        </div>
        <ExpirationHistoryReportTable
          columns={columns}
          emptyTableMessage={emptyTableMessage}
          filterParams={filterParams}
          tableData={tableData}
          paginationProps={paginationProps}
          loading={loading}
        />
      </div>
    </PageWrapper>
  );
};

export default ExpirationHistoryReport;
