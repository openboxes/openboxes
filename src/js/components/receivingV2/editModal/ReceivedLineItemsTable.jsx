import React from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';
import Section from 'components/Layout/v2/Section';
import Subsection from 'components/Layout/v2/Subsection';
import useTranslate from 'hooks/useTranslate';
import Badge from 'utils/Badge';

const ReceivedLineItemsTable = ({ receivedItems, columns, totalReceived }) => {
  const translate = useTranslate();

  if (!receivedItems.length) {
    return null;
  }

  return (
    <Section showTitle={false} className="receiving-edit-modal__received mt-4">
      <Subsection
        title={(
          <div className="badge-container">
            <Badge
              label={translate('react.receiving.received.label', 'Received')}
              variant="badge--green text-uppercase rounded"
            />
          </div>
        )}
      >
        <div className="receiving-table">
          <DataTable
            columns={columns}
            data={receivedItems}
            totalCount={receivedItems.length}
            disablePagination
            disabled
            showFooter
            meta={{ totalReceived }}
            emptyTableMessage={{
              id: 'react.receiving.emptyReceivedTable.label',
              defaultMessage: 'No items received',
            }}
          />
        </div>
      </Subsection>
    </Section>
  );
};

ReceivedLineItemsTable.propTypes = {
  receivedItems: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  columns: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  totalReceived: PropTypes.number.isRequired,
};

export default ReceivedLineItemsTable;
