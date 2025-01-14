import React from 'react';

import PropTypes from 'prop-types';
import { RiDownload2Line } from 'react-icons/ri';

import AllProductsTabFooter from 'components/cycleCount/allProductsTab/AllProductsTabFooter';
import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import useAllProductsTab from 'hooks/cycleCount/useAllProductsTab';

const CycleCountAllProducts = ({
  filterParams,
}) => {
  const {
    columns,
    tableData,
    loading,
    emptyTableMessage,
    exportTableData,
    setOffset,
    setPageSize,
  } = useAllProductsTab({
    filterParams,
  });

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
        setOffset={setOffset}
        setPageSize={setPageSize}
      />
    </div>
  );
};

export default CycleCountAllProducts;

CycleCountAllProducts.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
};
