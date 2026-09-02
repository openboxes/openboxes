import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowGoBackLine } from 'react-icons/ri';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import useTranslate from 'hooks/useTranslate';
import Badge from 'utils/Badge';

const ReceivingLineItemsTable = ({
  fields, columns, receivingNow, revertToOriginal,
}) => {
  const translate = useTranslate();

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mt-4">
        <div className="badge-container">
          <Badge
            label={translate('react.receiving.receivingNow.label', 'Receiving Now')}
            variant="badge--primary text-uppercase rounded"
            clickable={false}
          />
        </div>
        {/* TODO: This button is hidden (instead of removed) since we are revising its behaviour.
                  The revision is scheduled for 0.9.10, but if we decide the button isn't needed,
                  it and its existing (broken) functionality should be removed entirely. */}
        {false && (
        <Button
          label="react.receiving.revertToOriginal.label"
          defaultLabel="Revert to original"
          variant="secondary"
          EndIcon={<RiArrowGoBackLine size={18} />}
          onClick={revertToOriginal}
        />
        )}
      </div>
      <form className="receiving-table receiving-edit-modal__receiving-table mt-2">
        <DataTable
          columns={columns}
          data={fields}
          totalCount={fields.length}
          disablePagination
          showFooter
          meta={{ totalReceivingNow: receivingNow }}
          emptyTableMessage={{
            id: 'react.receiving.emptyTable.label',
            defaultMessage: 'No items to receive',
          }}
        />
      </form>
    </>
  );
};

ReceivingLineItemsTable.propTypes = {
  fields: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  columns: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  receivingNow: PropTypes.number.isRequired,
  revertToOriginal: PropTypes.func.isRequired,
};

export default ReceivingLineItemsTable;
