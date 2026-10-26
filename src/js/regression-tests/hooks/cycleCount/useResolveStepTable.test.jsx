import React from 'react';

import { flexRender, getCoreRowModel, useReactTable } from '@tanstack/react-table';
import { act, fireEvent, render } from '@testing-library/react';
import _ from 'lodash';
import { useSelector } from 'react-redux';

import useResolveStepTable from 'hooks/cycleCount/useResolveStepTable';

import '@testing-library/jest-dom';

const mockDispatch = jest.fn();
// Props of the last render of every input, keyed by "<column path>-<row index>"
const mockFields = {};

// Minimal stand-in for the form inputs: a plain input that exposes its props to the test
const mockField = (kind, props) => {
  const key = `${props.focusProps.fieldId}-${props.focusProps.fieldIndex}`;
  mockFields[key] = props;
  const display = typeof props.value === 'object' && props.value !== null
    ? (props.value.name ?? props.value.label)
    : props.value;
  return (
    <input
      aria-label={key}
      data-kind={kind}
      type={props.type}
      value={display ?? ''}
      disabled={props.disabled}
      onChange={(event) => props.onChange(event)}
      onBlur={props.onBlur}
      onKeyDown={props.onKeyDown}
    />
  );
};

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
  useDispatch: () => mockDispatch,
}));
jest.mock('selectors', () => ({
  getLotNumbersByProductId: (state, productId) => state.lotNumbers[productId] || [],
}));
jest.mock('actions', () => ({
  fetchReasonCodes: (activityCode, type) => ({ type, activityCode }),
}));
jest.mock('hooks/useTranslate');
jest.mock('utils/translation-utils', () => ({
  formatDate: () => (date, format) => `${date} as ${format}`,
}));
jest.mock('components/DataTable', () => ({
  TableCell: ({ children, tooltipLabel }) => <div data-tooltip={tooltipLabel}>{children}</div>,
}));
jest.mock('components/DataTable/TableHeaderCell', () => ({ children }) => <span>{children}</span>);
jest.mock('components/DataTable/v2/ValueIndicator', () => ({ value, variant }) => (
  <span data-testid="value-indicator" data-variant={variant}>{value}</span>
));
jest.mock('components/form-elements/v2/TextInput', () => (props) => mockField('text', props));
jest.mock('components/form-elements/v2/SelectField', () => (props) => mockField('select', props));
jest.mock('components/form-elements/v2/DateField', () => (props) => mockField('date', props));
jest.mock('react-tippy', () => ({
  Tooltip: ({ children, html }) => (
    <div>
      {html}
      {children}
    </div>
  ),
}));
jest.mock('wrappers/CustomTooltip', () => ({ content, className }) => (
  <span data-testid="tooltip-icon" className={className}>{content}</span>
));

// The hook uses String.prototype.replaceAll, which the Node 14 test runtime lacks
// (browsers have it)
if (!String.prototype.replaceAll) {
  // eslint-disable-next-line no-extend-native
  String.prototype.replaceAll = function replaceAll(search, replacement) {
    return this.split(search).join(replacement);
  };
}

const REASON_CODES = [{ id: 'DAMAGED', label: 'Damaged', value: 'DAMAGED' }];

const buildRows = () => [
  {
    id: 'item-1',
    binLocation: { id: 'bin-1', name: 'A1', zoneName: 'Zone A' },
    inventoryItem: { lotNumber: 'LOT-1', expirationDate: '2027-01-15' },
    quantityOnHand: 5,
    quantityCounted: 4,
    commentFromCount: 'shelf was messy',
    quantityRecounted: 6,
    rootCause: { id: 'DAMAGED', label: 'Damaged', value: 'DAMAGED' },
    comment: 'ok',
  },
  {
    id: 'newRow1',
    binLocation: null,
    inventoryItem: { lotNumber: null, expirationDate: null },
    quantityOnHand: null,
    quantityCounted: null,
    quantityRecounted: null,
    rootCause: null,
    comment: '',
  },
];

// Renders the columns of the hook the same way the resolve step table does
const Harness = ({ hookProps, updateData, onHookResult }) => {
  const hookResult = useResolveStepTable(hookProps);
  onHookResult(hookResult);
  const table = useReactTable({
    data: hookProps.tableData,
    columns: hookResult.columns,
    defaultColumn: hookResult.defaultColumn,
    getCoreRowModel: getCoreRowModel(),
    meta: { updateData },
  });
  return (
    <div>
      {table.getRowModel().rows.map((row) => (
        <div key={row.id}>
          {row.getVisibleCells()
            .filter((cell) => !cell.column.columnDef.meta?.hide)
            .map((cell) => (
              <div key={cell.id} data-testid={`${cell.column.id}-${row.index}`}>
                {flexRender(cell.column.columnDef.cell, cell.getContext())}
              </div>
            ))}
        </div>
      ))}
    </div>
  );
};

describe('useResolveStepTable', () => {
  let state;
  let hookResult;

  const renderTable = (props = {}) => {
    const tableData = props.tableData || buildRows();
    // Mirrors the resolve step: updateData writes into the shared table data
    const updateData = jest.fn((cycleCountId, rowId, columnId, value) => {
      const row = tableData.find((r) => r.id === rowId);
      _.set(row, columnId.replaceAll('_', '.'), value);
    });
    const hookProps = {
      cycleCountId: 'cc-1',
      productId: 'prod-1',
      removeRow: jest.fn(),
      triggerValidation: jest.fn(),
      validationErrors: {},
      shouldHaveRootCause: jest.fn(() => false),
      isStepEditable: true,
      addEmptyRow: jest.fn(),
      isFormDisabled: false,
      forceRerender: jest.fn(),
      ...props,
      tableData,
    };
    const utils = render(
      <Harness
        hookProps={hookProps}
        updateData={updateData}
        onHookResult={(result) => { hookResult = result; }}
      />,
    );
    return {
      ...utils, hookProps, updateData, tableData,
    };
  };

  const cell = (getByTestId, column, index) => getByTestId(`${column}-${index}`);

  beforeEach(() => {
    jest.clearAllMocks();
    Object.keys(mockFields).forEach((key) => delete mockFields[key]);
    state = {
      users: { data: [{ id: 'user-1' }] },
      session: { currentLocation: { id: 'loc-1', supportedActivities: ['PUTAWAY_STOCK', 'PICK_STOCK'] } },
      localize: {},
      cycleCount: {
        reasonCodes: REASON_CODES,
        binLocations: [
          {
            id: 'bin-1', name: 'A1', zoneId: 'zone-a', zoneName: 'Zone A',
          },
          { id: 'bin-2', name: 'B1' },
        ],
      },
      lotNumbers: {
        'prod-1': [
          { lotNumber: 'LOT-1', expirationDate: '2027-01-15' },
          { lotNumber: 'LOT-9', expirationDate: '2028-03-01' },
        ],
      },
    };
    useSelector.mockImplementation((selector) => selector(state));
  });

  it('fetches cycle count reason codes and exposes the users', () => {
    renderTable();

    expect(mockDispatch).toHaveBeenCalledWith({ type: 'FETCH_CYCLE_COUNT_REASON_CODES', activityCode: 'CYCLE_COUNT' });
    expect(hookResult.users).toEqual([{ id: 'user-1' }]);
  });

  describe('editable step', () => {
    it('only lets existing rows edit the recount, root cause and comment', () => {
      const { getByLabelText } = renderTable();

      ['binLocation', 'inventoryItem.lotNumber', 'inventoryItem.expirationDate'].forEach((field) => {
        expect(getByLabelText(`${field}-0`)).toBeDisabled();
        expect(getByLabelText(`${field}-1`)).toBeEnabled();
      });
      ['quantityRecounted', 'rootCause', 'comment'].forEach((field) => {
        expect(getByLabelText(`${field}-0`)).toBeEnabled();
      });
      expect(getByLabelText('quantityRecounted-0')).toHaveAttribute('type', 'number');
      expect(getByLabelText('comment-0')).toHaveAttribute('type', 'text');
    });

    it('disables every field when the form is disabled', () => {
      const { getByLabelText } = renderTable({ isFormDisabled: true });

      expect(getByLabelText('quantityRecounted-0')).toBeDisabled();
      expect(getByLabelText('comment-1')).toBeDisabled();
    });

    it('uses the matching input and options for every column', () => {
      const { getByLabelText } = renderTable();

      expect(getByLabelText('binLocation-0')).toHaveAttribute('data-kind', 'select');
      expect(getByLabelText('binLocation-0')).toHaveValue('Zone A: A1');
      expect(mockFields['binLocation-0'].options.map((group) => [group.name, group.options.map((o) => o.name)]))
        .toEqual([['Zone A', ['Zone A: A1']], ['No Zone', ['B1']]]);

      expect(getByLabelText('inventoryItem.lotNumber-0')).toHaveValue('LOT-1');
      expect(mockFields['inventoryItem.lotNumber-0']).toEqual(expect.objectContaining({
        creatable: true,
        placeholder: 'NO LOT',
        options: [
          {
            id: 'LOT-1', name: 'LOT-1', label: 'LOT-1', value: 'LOT-1',
          },
          {
            id: 'LOT-9', name: 'LOT-9', label: 'LOT-9', value: 'LOT-9',
          },
        ],
      }));
      expect(mockFields['inventoryItem.lotNumber-1'].placeholder).toBe(false);

      expect(getByLabelText('inventoryItem.expirationDate-0')).toHaveAttribute('data-kind', 'date');
      expect(mockFields['inventoryItem.expirationDate-0'].customDateFormat).toBe('DD/MMM/yyyy');
      expect(mockFields['rootCause-0'].options).toBe(REASON_CODES);
    });

    it('shows the counted quantities and the count and recount differences', () => {
      const { getByTestId } = renderTable();

      expect(cell(getByTestId, 'quantityCounted', 0)).toHaveTextContent('4');
      expect(cell(getByTestId, 'quantityCounted', 0)).toHaveTextContent('shelf was messy');
      expect(cell(getByTestId, 'quantityCounted', 1).querySelector('[data-variant="EMPTY"]')).not.toBeNull();

      const countDifference = cell(getByTestId, 'countDifference', 0).firstChild.firstChild;
      expect(countDifference).toHaveTextContent('-1');
      expect(countDifference).toHaveAttribute('data-variant', 'NEGATIVE');
      expect(cell(getByTestId, 'countDifference', 1).firstChild.firstChild).toHaveAttribute('data-variant', 'EMPTY');

      const recountDifference = cell(getByTestId, 'recountDifference', 0).firstChild.firstChild;
      expect(recountDifference).toHaveTextContent('1');
      expect(recountDifference).toHaveAttribute('data-variant', 'POSITIVE');
      expect(cell(getByTestId, 'recountDifference', 1).firstChild.firstChild).not.toHaveAttribute('data-variant');
    });

    it('saves a recounted quantity on blur and refreshes the recount difference', () => {
      const {
        getByLabelText, getByTestId, updateData, hookProps,
      } = renderTable();
      updateData.mockClear();
      const input = getByLabelText('quantityRecounted-0');

      fireEvent.change(input, { target: { value: '8' } });
      fireEvent.blur(input);

      expect(updateData).toHaveBeenCalledWith('cc-1', 'item-1', 'quantityRecounted', '8');
      expect(updateData).toHaveBeenLastCalledWith('cc-1', 'item-1', 'quantityRecounted', 8);
      expect(hookProps.triggerValidation).toHaveBeenCalledTimes(1);
      expect(input).toHaveValue(8);
      const recountDifference = cell(getByTestId, 'recountDifference', 0).firstChild.firstChild;
      expect(recountDifference).toHaveTextContent('3');
    });

    it('does nothing on blur when the value did not change', () => {
      const { getByLabelText, updateData, hookProps } = renderTable();
      updateData.mockClear();

      fireEvent.blur(getByLabelText('comment-0'));

      expect(updateData).not.toHaveBeenCalled();
      expect(hookProps.triggerValidation).not.toHaveBeenCalled();
    });

    it('saves a text field on blur', () => {
      const { getByLabelText, updateData, hookProps } = renderTable();
      const input = getByLabelText('comment-1');

      fireEvent.change(input, { target: { value: 'found on the floor' } });
      fireEvent.blur(input);

      expect(updateData).toHaveBeenCalledWith('cc-1', 'newRow1', 'comment', 'found on the floor');
      expect(hookProps.triggerValidation).toHaveBeenCalled();
    });

    it('picking an existing lot copies its expiration date and locks the date', () => {
      const { getByLabelText, updateData } = renderTable();
      updateData.mockClear();

      act(() => mockFields['inventoryItem.lotNumber-1'].onChange({ value: 'LOT-9' }));

      expect(updateData).toHaveBeenCalledWith('cc-1', 'newRow1', 'inventoryItem.lotNumber', 'LOT-9');
      expect(updateData).toHaveBeenCalledWith('cc-1', 'newRow1', 'inventoryItem.expirationDate', '2028-03-01');
      expect(getByLabelText('inventoryItem.lotNumber-1')).toHaveValue('LOT-9');
      expect(getByLabelText('inventoryItem.expirationDate-1')).toBeDisabled();
    });

    it('creating a new lot clears the expiration date and keeps it editable', () => {
      const { getByLabelText, updateData } = renderTable();
      act(() => mockFields['inventoryItem.lotNumber-1'].onChange({ value: 'LOT-9' }));
      updateData.mockClear();

      act(() => mockFields['inventoryItem.lotNumber-1'].onChange({ value: 'LOT-NEW' }));

      expect(updateData).toHaveBeenCalledWith('cc-1', 'newRow1', 'inventoryItem.expirationDate', undefined);
      expect(getByLabelText('inventoryItem.expirationDate-1')).toBeEnabled();
    });

    it('selecting a root cause revalidates and stores the option', () => {
      const { getByLabelText, updateData, hookProps } = renderTable();
      updateData.mockClear();

      act(() => mockFields['rootCause-1'].onChange(REASON_CODES[0]));

      expect(hookProps.triggerValidation).toHaveBeenCalledTimes(1);
      expect(updateData).toHaveBeenCalledWith('cc-1', 'newRow1', 'rootCause', REASON_CODES[0]);
      expect(getByLabelText('rootCause-1')).toHaveValue('Damaged');
    });
  });

  describe('errors and warnings', () => {
    it('shows the validation error of a cell', () => {
      const { getByTestId } = renderTable({
        validationErrors: {
          'cc-1': { errors: { 1: { quantityRecounted: { _errors: ['Quantity recounted is required'] } } } },
        },
      });

      const icon = cell(getByTestId, 'quantityRecounted', 1).querySelector('[data-testid="tooltip-icon"]');
      expect(icon).toHaveTextContent('Quantity recounted is required');
      expect(icon).toHaveClass('tooltip-icon--error');
      expect(mockFields['quantityRecounted-1'].showErrorBorder).toEqual(['Quantity recounted is required']);
      expect(cell(getByTestId, 'quantityRecounted', 0).querySelector('[data-testid="tooltip-icon"]')).toBeNull();
    });

    it('warns about a missing root cause on the root cause cell only', () => {
      const { getByTestId } = renderTable({ shouldHaveRootCause: (id) => id === 'newRow1' });

      const icon = cell(getByTestId, 'rootCause', 1).querySelector('[data-testid="tooltip-icon"]');
      expect(icon).toHaveTextContent('Specify result of investigation if applicable.');
      expect(icon).toHaveClass('tooltip-icon--warning');
      expect(cell(getByTestId, 'comment', 1).querySelector('[data-testid="tooltip-icon"]')).toBeNull();
      expect(cell(getByTestId, 'rootCause', 0).querySelector('[data-testid="tooltip-icon"]')).toBeNull();
    });

    it('clears the warning once a root cause is picked', () => {
      const { getByTestId } = renderTable({ shouldHaveRootCause: () => true });

      act(() => mockFields['rootCause-1'].onChange(REASON_CODES[0]));

      expect(cell(getByTestId, 'rootCause', 1).querySelector('[data-testid="tooltip-icon"]')).toBeNull();
    });
  });

  describe('actions column', () => {
    it('is hidden when there is no new or custom row', () => {
      const rows = buildRows().slice(0, 1);
      const { queryByTestId } = renderTable({ tableData: rows });

      expect(queryByTestId('actions-0')).toBeNull();
    });

    it('deletes new and custom rows only', () => {
      const rows = buildRows();
      rows.push({ ...rows[0], id: 'item-custom', custom: true });
      const { getByTestId, hookProps } = renderTable({ tableData: rows });

      expect(cell(getByTestId, 'actions', 0).querySelector('svg')).toBeNull();
      fireEvent.click(cell(getByTestId, 'actions', 1).querySelector('svg'));
      fireEvent.click(cell(getByTestId, 'actions', 2).querySelector('svg'));

      expect(hookProps.removeRow.mock.calls).toEqual([['cc-1', 'newRow1'], ['cc-1', 'item-custom']]);
    });
  });

  describe('keyboard navigation', () => {
    it('moves the focus down and resets it on a click outside the inputs', () => {
      const { getByLabelText, hookProps } = renderTable();

      fireEvent.keyDown(getByLabelText('quantityRecounted-0'), { key: 'ArrowDown' });

      expect(mockFields['quantityRecounted-1'].focusProps).toEqual(expect.objectContaining({
        rowIndex: 1, columnId: 'quantityRecounted',
      }));

      fireEvent.click(document.body);

      expect(mockFields['quantityRecounted-1'].focusProps.rowIndex).toBeNull();
      expect(hookProps.forceRerender).toHaveBeenCalled();
    });

    it('keeps the focus on clicks inside inputs and resets it after picking an option', () => {
      const { getByLabelText, hookProps, container } = renderTable();
      fireEvent.keyDown(getByLabelText('quantityRecounted-0'), { key: 'ArrowDown' });

      fireEvent.click(getByLabelText('comment-0'));
      expect(mockFields['quantityRecounted-1'].focusProps.rowIndex).toBe(1);

      const option = document.createElement('div');
      option.className = 'react-select__option';
      container.appendChild(option);
      fireEvent.click(option);

      expect(mockFields['quantityRecounted-1'].focusProps.rowIndex).toBeNull();
      expect(hookProps.forceRerender).not.toHaveBeenCalled();
    });

    it('adds a row when moving down from the last cell of the last row', () => {
      const { getByLabelText, hookProps } = renderTable();

      fireEvent.keyDown(getByLabelText('comment-1'), { key: 'ArrowDown' });

      expect(hookProps.addEmptyRow).toHaveBeenCalledWith('prod-1', 'cc-1', false);
    });

    it('does not move into a locked expiration date', () => {
      const { getByLabelText } = renderTable();
      act(() => mockFields['inventoryItem.lotNumber-1'].onChange({ value: 'LOT-9' }));

      fireEvent.keyDown(getByLabelText('inventoryItem.expirationDate-0'), { key: 'ArrowDown' });

      expect(mockFields['inventoryItem.expirationDate-0'].focusProps.rowIndex).toBeNull();
    });
  });

  describe('confirmation step', () => {
    it('shows read-only values', () => {
      const {
        getByTestId, queryByLabelText, queryByTestId,
      } = renderTable({ isStepEditable: false });

      expect(queryByLabelText('quantityRecounted-0')).toBeNull();
      expect(cell(getByTestId, 'binLocation', 0)).toHaveTextContent('Zone A: A1');
      expect(cell(getByTestId, 'inventoryItem_lotNumber', 0)).toHaveTextContent('LOT-1');
      expect(cell(getByTestId, 'inventoryItem_expirationDate', 0)).toHaveTextContent('2027-01-15 as DD/MMM/yyyy');
      expect(cell(getByTestId, 'rootCause', 0)).toHaveTextContent('Damaged');
      expect(cell(getByTestId, 'quantityRecounted', 0)).toHaveTextContent('6');
      expect(cell(getByTestId, 'comment', 0)).toHaveTextContent('ok');
      expect(queryByTestId('actions-1')).toBeNull();
    });

    it('labels empty cells with their column name', () => {
      const { getByTestId } = renderTable({ isStepEditable: false });

      expect(cell(getByTestId, 'binLocation', 1).firstChild).toHaveAttribute('data-tooltip', 'Bin Location');
      expect(cell(getByTestId, 'inventoryItem_lotNumber', 1).firstChild).toHaveAttribute('data-tooltip', 'Serial / Lot Number');
      expect(cell(getByTestId, 'rootCause', 1).firstChild).toHaveAttribute('data-tooltip', 'Root Cause');
    });
  });

  it('hides the bin location column when the location has no bins', () => {
    state.session.currentLocation.supportedActivities = [];

    const { queryByTestId } = renderTable();

    expect(queryByTestId('binLocation-0')).toBeNull();
    expect(hookResult.columns[0].meta.hide).toBe(true);
  });
});
