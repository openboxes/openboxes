import React from 'react';

import {
  fireEvent, render, screen,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { cycleCountItem, cycleCountStore, storedItem } from 'regression-tests/cycle-count/cycleCountStore';

import { FETCH_BIN_LOCATIONS, FETCH_LOT_NUMBERS_BY_PRODUCT_IDS, SET_ERRORS } from 'actions/types';
import ActionsCell from 'components/cycleCount/tableCell/ActionsCell';
import BinLocationCell from 'components/cycleCount/tableCell/BinLocationCell';
import CommentCell from 'components/cycleCount/tableCell/CommentCell';
import ExpirationDateCell from 'components/cycleCount/tableCell/ExpirationDateCell';
import LotNumberCell from 'components/cycleCount/tableCell/LotNumberCell';
import QuantityCell from 'components/cycleCount/tableCell/QuantityCell';

import '@testing-library/jest-dom';

// The API client is never reached by these cells; mocking it keeps the real one (which loads the
// app's store) out of the import graph.
jest.mock('utils/apiClient', () => ({ __esModule: true, default: { get: jest.fn() } }));

// One cycle count with a counted row and a row added during the count (ids of added rows
// start with "newRow").
const NEW_ROW_ID = 'newRow7';
const cycleCounts = () => [{
  id: 'cc1',
  cycleCountItems: [
    cycleCountItem(),
    cycleCountItem({
      id: NEW_ROW_ID,
      custom: true,
      quantityCounted: null,
      comment: null,
      binLocation: null,
      inventoryItem: { lotNumber: null, expirationDate: null },
    }),
  ],
}];

const renderCell = (cell, store = cycleCountStore({ cycleCounts: cycleCounts() })) => ({
  store,
  ...render(<Provider store={store}>{cell}</Provider>),
});

// An error the validation put on a field of the first row.
const withError = (store, field, message) => {
  store.dispatch({
    type: SET_ERRORS,
    payload: { cc1: { cycleCountItems: { 0: { [field]: { _errors: [message] } } } } },
  });
  return store;
};

describe('QuantityCell', () => {
  it('shows the counted quantity as text when the step is read only', () => {
    renderCell(<QuantityCell id="item1" index="0" cycleCountId="cc1" isStepEditable={false} />);

    expect(screen.getByText('7')).toBeInTheDocument();
    expect(screen.queryByRole('spinbutton')).not.toBeInTheDocument();
  });

  it('stores the typed quantity when the field loses focus', () => {
    const { store } = renderCell(
      <QuantityCell id="item1" index="0" cycleCountId="cc1" isStepEditable />,
    );
    const input = screen.getByRole('spinbutton');
    expect(input).toHaveValue(7);

    fireEvent.change(input, { target: { value: '12' } });
    fireEvent.blur(input);

    expect(storedItem(store, 'cc1', 'item1').quantityCounted).toBe(12);
  });

  it('starts empty for a row that has not been counted yet', () => {
    renderCell(<QuantityCell id={NEW_ROW_ID} index="1" cycleCountId="cc1" isStepEditable />);

    expect(screen.getByRole('spinbutton')).toHaveValue(null);
  });

  it('marks the field when the validation found an error in a filled-in row', () => {
    const store = withError(cycleCountStore({ cycleCounts: cycleCounts() }), 'quantityCounted', 'Too many');
    const { container } = renderCell(
      <QuantityCell id="item1" index="0" cycleCountId="cc1" isStepEditable />,
      store,
    );

    expect(screen.getByRole('spinbutton')).toHaveClass('has-errors');
    expect(container.querySelector('.tooltip-icon--error')).toBeInTheDocument();
  });

  it('hides the error while the quantity is being edited', () => {
    const store = withError(cycleCountStore({ cycleCounts: cycleCounts() }), 'quantityCounted', 'Too many');
    const { container } = renderCell(
      <QuantityCell id="item1" index="0" cycleCountId="cc1" isStepEditable />,
      store,
    );

    fireEvent.change(screen.getByRole('spinbutton'), { target: { value: '3' } });

    expect(container.querySelector('.tooltip-icon--error')).not.toBeInTheDocument();
  });
});

describe('CommentCell', () => {
  it('shows the comment as text when the step is read only', () => {
    renderCell(<CommentCell id="item1" cycleCountId="cc1" isStepEditable={false} />);

    expect(screen.getByText('Shelf A')).toBeInTheDocument();
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
  });

  it('stores the edited comment when the field loses focus', () => {
    const { store } = renderCell(<CommentCell id="item1" cycleCountId="cc1" isStepEditable />);
    const input = screen.getByRole('textbox');
    expect(input).toHaveValue('Shelf A');

    fireEvent.change(input, { target: { value: 'Moved to shelf B' } });
    expect(storedItem(store, 'cc1', 'item1').comment).toBe('Shelf A');
    fireEvent.blur(input);

    expect(storedItem(store, 'cc1', 'item1').comment).toBe('Moved to shelf B');
  });

  it('starts with an empty field when the row has no comment', () => {
    renderCell(<CommentCell id={NEW_ROW_ID} cycleCountId="cc1" isStepEditable />);

    expect(screen.getByRole('textbox')).toHaveValue('');
  });
});

describe('ActionsCell', () => {
  const renderActions = (props) => {
    const removeRow = jest.fn();
    const utils = renderCell(
      <ActionsCell cycleCountId="cc1" isFormDisabled={false} isStepEditable removeRow={removeRow} {...props} />,
    );
    return { removeRow, ...utils };
  };

  it('offers to delete a row added during the count', () => {
    const { container, removeRow } = renderActions({ id: NEW_ROW_ID });
    const icon = container.querySelector('svg');

    expect(icon).toHaveClass('cursor-pointer');
    fireEvent.click(icon);

    expect(removeRow).toHaveBeenCalledWith(NEW_ROW_ID);
  });

  it('greys the delete icon out while the form is disabled', () => {
    const { container } = renderActions({ id: NEW_ROW_ID, isFormDisabled: true });

    expect(container.querySelector('svg')).toHaveClass('disabled-icon');
  });

  it('does not offer to delete a row that came with the count', () => {
    const { container } = renderActions({ id: 'item1' });

    expect(container.querySelector('svg')).not.toBeInTheDocument();
  });

  it('does not offer to delete anything when the step is read only', () => {
    const { container } = renderActions({ id: NEW_ROW_ID, isStepEditable: false });

    expect(container.querySelector('svg')).not.toBeInTheDocument();
  });
});

describe('BinLocationCell', () => {
  it('renders nothing when the location does not use bin locations', () => {
    const { container } = renderCell(
      <BinLocationCell id="item1" index="0" cycleCountId="cc1" showBinLocation={false} isStepEditable />,
    );

    expect(container).toBeEmptyDOMElement();
  });

  it('shows the bin as text when the step is read only', () => {
    renderCell(
      <BinLocationCell id="item1" index="0" cycleCountId="cc1" showBinLocation isStepEditable={false} />,
    );

    expect(screen.getAllByText('A-01').length).toBeGreaterThan(0);
  });

  it('keeps the bin of a counted row fixed', () => {
    const { container } = renderCell(
      <BinLocationCell id="item1" index="0" cycleCountId="cc1" showBinLocation isStepEditable />,
    );

    expect(container.querySelector('.react-select--is-disabled')).toBeInTheDocument();
    expect(screen.getAllByText('A-01').length).toBeGreaterThan(0);
  });

  it('stores the bin chosen for an added row', () => {
    const store = cycleCountStore({ cycleCounts: cycleCounts() });
    store.dispatch({
      type: FETCH_BIN_LOCATIONS,
      payload: [{ id: 'bin2', name: 'B-07' }, { id: 'bin3', name: 'C-02' }],
    });
    const { container } = renderCell(
      <BinLocationCell id={NEW_ROW_ID} index="1" cycleCountId="cc1" showBinLocation isStepEditable />,
      store,
    );

    const input = container.querySelector('input');
    fireEvent.focus(input);
    fireEvent.keyDown(input, { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(screen.getByText('B-07'));

    expect(storedItem(store, 'cc1', NEW_ROW_ID).binLocation).toEqual({ id: 'bin2', name: 'B-07' });
  });
});

describe('LotNumberCell', () => {
  it('shows the lot as text when the step is read only', () => {
    renderCell(
      <LotNumberCell
        id="item1"
        index="0"
        cycleCountId="cc1"
        setDisabledExpirationDateFields={jest.fn()}
        isStepEditable={false}
      />,
    );

    expect(screen.getAllByText('LOT-1').length).toBeGreaterThan(0);
  });

  it('keeps the lot of a counted row fixed', () => {
    const { container } = renderCell(
      <LotNumberCell
        id="item1"
        index="0"
        cycleCountId="cc1"
        setDisabledExpirationDateFields={jest.fn()}
        isStepEditable
      />,
    );

    expect(container.querySelector('.react-select--is-disabled')).toBeInTheDocument();
  });

  it('stores an existing lot and fills its expiration date in for an added row', () => {
    const store = cycleCountStore({ cycleCounts: cycleCounts() });
    store.dispatch({
      type: FETCH_LOT_NUMBERS_BY_PRODUCT_IDS,
      payload: {
        p1: [
          { lotNumber: 'LOT-1', expirationDate: '10/31/2027' },
          { lotNumber: 'LOT-2', expirationDate: '01/31/2028' },
        ],
      },
    });
    const setDisabledExpirationDateFields = jest.fn();
    const { container } = renderCell(
      <LotNumberCell
        id={NEW_ROW_ID}
        index="1"
        cycleCountId="cc1"
        setDisabledExpirationDateFields={setDisabledExpirationDateFields}
        isStepEditable
      />,
      store,
    );

    const input = container.querySelector('input');
    fireEvent.focus(input);
    fireEvent.keyDown(input, { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(screen.getByText('LOT-2'));

    expect(storedItem(store, 'cc1', NEW_ROW_ID).inventoryItem.lotNumber).toBe('LOT-2');
    // The expiration date of a known lot is filled in (and locked) for that row.
    const update = setDisabledExpirationDateFields.mock.calls[0][0];
    expect(update({ other: 'x' })).toEqual({ other: 'x', [NEW_ROW_ID]: '01/31/2028' });
  });
});

describe('ExpirationDateCell', () => {
  // The calendar opens in a portal on the app's root element.
  beforeAll(() => {
    const root = document.createElement('div');
    root.id = 'root';
    document.body.appendChild(root);
  });

  const renderDate = (props) => renderCell(
    <ExpirationDateCell index="1" cycleCountId="cc1" disabledExpirationDateFields={{}} isStepEditable {...props} />,
  );

  it('shows the formatted date when the step is read only', () => {
    renderDate({ id: 'item1', index: '0', isStepEditable: false });

    expect(screen.getByText('31/Oct/2027')).toBeInTheDocument();
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it('keeps the date of a counted row fixed', () => {
    const { container } = renderDate({ id: 'item1', index: '0' });

    expect(screen.getByText('31/Oct/2027')).toBeInTheDocument();
    expect(container.querySelector('.date-field-input')).toHaveClass('disabled');
  });

  it('copies the expiration date of a chosen existing lot into an added row and locks it', () => {
    const { store, container } = renderDate({
      id: NEW_ROW_ID,
      disabledExpirationDateFields: { [NEW_ROW_ID]: '01/31/2028' },
    });

    expect(storedItem(store, 'cc1', NEW_ROW_ID).inventoryItem.expirationDate).toBe('01/31/2028');
    expect(screen.getByText('31/Jan/2028')).toBeInTheDocument();
    expect(container.querySelector('.date-field-input')).toHaveClass('disabled');
  });

  it('stores the date picked for an added row with a new lot', () => {
    const store = cycleCountStore({ cycleCounts: cycleCounts() });
    store.dispatch({
      type: 'UPDATE_FIELD_VALUE',
      payload: {
        id: 'cc1', rowId: NEW_ROW_ID, field: 'inventoryItem.expirationDate', value: '03/01/2028',
      },
    });
    const { container } = renderCell(
      <ExpirationDateCell
        id={NEW_ROW_ID}
        index="1"
        cycleCountId="cc1"
        disabledExpirationDateFields={{}}
        isStepEditable
      />,
      store,
    );
    expect(container.querySelector('.date-field-input')).not.toHaveClass('disabled');

    fireEvent.click(container.querySelector('.date-field-input'));
    fireEvent.click(screen.getByRole('option', { name: 'day-15' }));

    expect(storedItem(store, 'cc1', NEW_ROW_ID).inventoryItem.expirationDate).toBe('Mar 15, 2028');
    expect(screen.getByText('15/Mar/2028')).toBeInTheDocument();
  });

  it('clears the date of an added row', () => {
    const store = cycleCountStore({ cycleCounts: cycleCounts() });
    store.dispatch({
      type: 'UPDATE_FIELD_VALUE',
      payload: {
        id: 'cc1', rowId: NEW_ROW_ID, field: 'inventoryItem.expirationDate', value: '03/01/2028',
      },
    });
    renderCell(
      <ExpirationDateCell
        id={NEW_ROW_ID}
        index="1"
        cycleCountId="cc1"
        disabledExpirationDateFields={{}}
        isStepEditable
      />,
      store,
    );

    fireEvent.click(screen.getByRole('button', { name: 'clear' }));

    expect(storedItem(store, 'cc1', NEW_ROW_ID).inventoryItem.expirationDate).toBeNull();
    expect(screen.queryByText('01/Mar/2028')).not.toBeInTheDocument();
  });
});
