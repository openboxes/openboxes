import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import ReactTable from 'react-table';

import 'react-table/react-table.css';

import Select from '../../utils/Select';
import Input from '../../utils/Input';
import { debouncedUsersFetch } from '../../utils/option-utils';

const StocklistTable = ({
  data, availableStocklists, parentIndex, addItem, editItem,
  updateItemField, saveItem, deleteItem, printItem, mailItem,
}) => {
  const COLUMNS = [
    {
      Header: 'Stocklist Name',
      accessor: 'name',
      // eslint-disable-next-line react/prop-types
      Cell: ({ index, original }) => {
        if (!original.new) {
          return original.name || '';
        }

        return (
          <Select
            value={original.stocklistId}
            onChange={value => updateItemField(parentIndex, index, 'stocklistId', value)}
            options={_.map(availableStocklists, val => ({ value: val.id, label: val.name }))}
            className="select-xs"
          />
        );
      },
    },
    {
      Header: 'Manager',
      accessor: 'manager.name',
      // eslint-disable-next-line react/prop-types
      Cell: ({ index, original }) => {
        if (!original.new && !original.edit) {
          return _.get(original, 'manager.name') || '';
        }

        return (
          <Select
            value={original.manager}
            onChange={value => updateItemField(parentIndex, index, 'manager', value)}
            async
            openOnClick={false}
            autoload={false}
            loadOptions={debouncedUsersFetch}
            cache={false}
            options={[]}
            labelKey="name"
            className="select-xs"
          />
        );
      },
    },
    {
      Header: 'Replenishment period',
      accessor: 'replenishmentPeriod',
      // eslint-disable-next-line react/prop-types
      Cell: ({ index, original }) => {
        if (!original.new && !original.edit) {
          return _.isNil(original.replenishmentPeriod) ? '' : original.replenishmentPeriod;
        }

        return (
          <Input
            value={original.replenishmentPeriod || ''}
            onChange={value => updateItemField(parentIndex, index, 'replenishmentPeriod', value)}
          />
        );
      },
      className: 'text-center',
    },
    {
      Header: 'Maximum  Quantity',
      accessor: 'maxQuantity',
      // eslint-disable-next-line react/prop-types
      Cell: ({ index, original }) => {
        if (!original.new && !original.edit) {
          return _.isNil(original.maxQuantity) ? '' : original.maxQuantity;
        }

        return (
          <Input
            value={original.maxQuantity || ''}
            onChange={value => updateItemField(parentIndex, index, 'maxQuantity', value)}
          />
        );
      },
      className: 'text-center',
    },
    {
      Header: 'Unit of measure',
      accessor: 'uom',
      className: 'text-center',
    },
    {
      Header: 'Actions',
      accessor: 'edit',
      // eslint-disable-next-line react/prop-types
      Cell: ({ index, original }) => (
        <div>
          <button
            className="btn btn-outline-primary btn-xs mx-1"
            disabled={original.edit || original.new}
            onClick={() => editItem(parentIndex, index)}
          >Edit
          </button>
          <button
            className="btn btn-outline-primary btn-xs mx-1"
            disabled={(!original.edit && !original.new) || !original.stocklistId
              || _.isNil(original.maxQuantity) || original.maxQuantity === ''}
            onClick={() => saveItem(parentIndex, index, original)}
          >Save
          </button>
          <button
            className="btn btn-outline-danger btn-xs mx-1"
            onClick={() => deleteItem(parentIndex, index)}
          >Delete
          </button>
          <button
            className="btn btn-outline-secondary btn-xs mx-1"
            disabled={original.edit || original.new}
            onClick={() => printItem(parentIndex, index)}
          >Print
          </button>
          <button
            className="btn btn-outline-secondary btn-xs mx-1"
            disabled={original.edit || original.new}
            onClick={() => mailItem(parentIndex, index)}
          >Email
          </button>
        </div>
      ),
    },
  ];

  return (
    <div>
      {
        data && !!data.length &&
        <ReactTable
          data={data}
          columns={COLUMNS}
          showPagination={false}
          minRows={0}
        />
      }
      <button
        className="btn btn-outline-success btn-xs my-1"
        onClick={() => addItem(parentIndex)}
      >Add Stocklist
      </button>
    </div>
  );
};

export default StocklistTable;

StocklistTable.propTypes = {
  data: PropTypes.arrayOf(PropTypes.shape({})),
  availableStocklists: PropTypes.arrayOf(PropTypes.shape({})),
  parentIndex: PropTypes.number.isRequired,
  addItem: PropTypes.func.isRequired,
  editItem: PropTypes.func.isRequired,
  updateItemField: PropTypes.func.isRequired,
  saveItem: PropTypes.func.isRequired,
  deleteItem: PropTypes.func.isRequired,
  printItem: PropTypes.func.isRequired,
  mailItem: PropTypes.func.isRequired,
};

StocklistTable.defaultProps = {
  data: [],
  availableStocklists: [],
};
