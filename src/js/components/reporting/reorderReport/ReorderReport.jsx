import React from 'react';

import ReorderReportFilters from 'components/reporting/reorderReport/ReorderReportFilters';
import ReorderReportHeader from 'components/reporting/reorderReport/ReorderReportHeader';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const ReorderReport = () => {
  useTranslation('report');

  return (
    <PageWrapper>
      <ReorderReportHeader />
      <div className="list-page-list-section">
        <ReorderReportFilters />
      </div>
    </PageWrapper>
  );
};

export default ReorderReport;
