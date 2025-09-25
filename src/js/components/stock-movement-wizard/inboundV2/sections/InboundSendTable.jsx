import React from 'react';

import DataTable from 'components/DataTable/v2/DataTable';
import useInboundSendTable from 'hooks/inboundV2/send/useInboundSendTable';

const InboundSendTable = () => {
  const { columns, tableData, loading } = useInboundSendTable();

  return (
    <DataTable
      columns={columns}
      data={tableData}
      loading={loading}
      disablePagination
      overflowVisible
    />
  );
};

export default InboundSendTable;
