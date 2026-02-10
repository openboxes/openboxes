import React, { memo } from 'react';

import DataTable from 'components/DataTable/v2/DataTable';
import useInboundSendTable from 'hooks/inboundV2/send/useInboundSendTable';

const InboundSendTable = memo(() => {
  const { columns, tableData, loading } = useInboundSendTable();

  return (
    <div data-testid="items-table">
      <DataTable
        columns={columns}
        data={tableData}
        totalCount={tableData.length}
        loading={loading}
        disablePagination
        overflowVisible
      />
    </div>
  );
});

export default InboundSendTable;
