import React from 'react';

import { RiDownload2Line } from 'react-icons/ri';

import AllProductsTabFooter from 'components/cycleCount/allProductsTab/AllProductsTabFooter';
import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import useAllProductsTab from 'hooks/cycleCount/useAllProductsTab';

const CycleCountAllProducts = () => {
  const {
    columns,
    tableData,
    loading,
    emptyTableMessage,
    exportTableData,
  } = useAllProductsTab();

  return (
    <div>
      <div className="d-flex justify-content-end">
        <Button
          onClick={exportTableData}
          className="m-2"
          defaultLabel="Export"
          label="react.default.button.export.label"
          variant="secondary"
          EndIcon={<RiDownload2Line />}
        />
      </div>
      <DataTable
        columns={columns}
        data={tableData.data}
        footerComponent={AllProductsTabFooter}
        emptyTableMessage={emptyTableMessage}
        loading={loading}
      />
    </div>
  );
};

export default CycleCountAllProducts;
