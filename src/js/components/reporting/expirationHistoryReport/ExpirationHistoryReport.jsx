import React from 'react';

import { RiDownload2Line } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import ExpirationHistoryReportFilters
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportFilters';
import ExpirationHistoryReportHeader
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportHeader';
import ExpirationHistoryReportTable
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportTable';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

const ExpirationHistoryReport = () => {
  useTranslation('reporting');

  const exportData = () => {
    console.log('Exporting expiration history report data...');
  };

  return (
    <PageWrapper>
      <ExpirationHistoryReportHeader />
      <div className="list-page-list-section">
        <ExpirationHistoryReportFilters />
        <div className="d-flex m-2 gap-8 justify-content-end">
          <Button
            onClick={exportData}
            defaultLabel="Export"
            label="react.default.button.export.label"
            variant="secondary"
            EndIcon={<RiDownload2Line />}
          />
        </div>
        <ExpirationHistoryReportTable />
      </div>
    </PageWrapper>
  );
};

export default ExpirationHistoryReport;
