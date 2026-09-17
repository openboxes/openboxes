import React, { useState } from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { legacy_configureStore as configureStore } from 'redux-mock-store';

import DateFieldDateFns from 'components/form-elements/v2/DateFieldDateFns';
import SelectField from 'components/form-elements/v2/SelectField';
import useTableArrowNavigation from 'hooks/useTableArrowNavigation';

import '@testing-library/jest-dom';

jest.mock('hooks/useTranslate');

const COLUMN = 'quantityReceiving';
const OTHER_COLUMN = 'recipient';
// The date field reads the locale from the session, the select reads translations.
const store = configureStore()({ session: {}, localize: { languages: [] } });

describe('useTableArrowNavigation', () => {
  // A stand-in for a table, with a row per field.
  const Table = ({ rows, onNavigatePastLastField, verticalOnly }) => {
    const [addedRows, setAddedRows] = useState([]);

    const tableRef = useTableArrowNavigation({
      verticalOnly,
      onNavigatePastLastField: onNavigatePastLastField
        ? (column) => {
          onNavigatePastLastField(column);
          setAddedRows((currentRows) => [...currentRows, `added-${currentRows.length}`]);
        }
        : undefined,
    });

    // DataTable stamps the column and the mark on every cell, so the stand-in does the same.
    const renderField = ({
      id, column, disabled, unmarked, field,
    }) => (
      <div key={id} data-column-id={column} data-arrow-navigation={!unmarked || undefined}>
        {field === 'date' && <DateFieldDateFns onChange={() => {}} showCustomInput={false} />}
        {field === 'select' && <SelectField options={[{ id: 'a', label: 'A' }]} />}
        {field === 'hidden' && <input type="hidden" data-testid={id} />}
        {!field && <input type="number" data-testid={id} disabled={disabled} />}
      </div>
    );

    return (
      <div ref={tableRef}>
        {rows.map((row) => (row.column ? renderField(row) : <div key={row.id} />))}
        {addedRows.map((id) => renderField({ id, column: COLUMN }))}
      </div>
    );
  };

  const renderTable = (rows, options = {}) => render(
    <Provider store={store}><Table rows={rows} {...options} /></Provider>,
  );

  const arrowDown = (element) => fireEvent.keyDown(element, { key: 'ArrowDown' });
  const arrowUp = (element) => fireEvent.keyDown(element, { key: 'ArrowUp' });
  const arrowRight = (element) => fireEvent.keyDown(element, { key: 'ArrowRight' });
  const arrowLeft = (element) => fireEvent.keyDown(element, { key: 'ArrowLeft' });

  // Testing Library wraps a render in act(), and the added field has to be there right away, so
  // the event goes in the way the browser sends it. React warns about that, hence the muted log.
  const arrowDownNatively = (element) => {
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
    element.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'ArrowDown', bubbles: true, cancelable: true }),
    );
    consoleSpy.mockRestore();
  };

  // Two columns, so left and right have somewhere to go too, in render order.
  const gridRows = [
    { id: 'recipient-first', column: OTHER_COLUMN },
    { id: 'quantity-first', column: COLUMN },
    { id: 'recipient-second', column: OTHER_COLUMN },
    { id: 'quantity-second', column: COLUMN },
  ];

  // One column, so only up and down have anywhere to go.
  const columnRows = [
    { id: 'first', column: COLUMN },
    { id: 'second', column: COLUMN },
    { id: 'third', column: COLUMN },
  ];

  it('moves down and up the column, stays at its edges and selects the value it enters', () => {
    renderTable(columnRows);
    const selectValue = jest.spyOn(screen.getByTestId('second'), 'select');

    arrowDown(screen.getByTestId('first'));
    expect(screen.getByTestId('second')).toHaveFocus();
    expect(selectValue).toHaveBeenCalled();

    arrowUp(screen.getByTestId('second'));
    expect(screen.getByTestId('first')).toHaveFocus();

    arrowUp(screen.getByTestId('first'));
    expect(screen.getByTestId('first')).toHaveFocus();

    screen.getByTestId('third').focus();
    arrowDown(screen.getByTestId('third'));
    expect(screen.getByTestId('third')).toHaveFocus();
  });

  it('skips rows without a field of the column, and hidden or disabled fields', () => {
    renderTable([
      { id: 'first', column: COLUMN },
      { id: 'rollup' },
      { id: 'hidden-only', column: COLUMN, field: 'hidden' },
      { id: 'completed', column: COLUMN, disabled: true },
      { id: 'last', column: COLUMN },
    ]);

    arrowDown(screen.getByTestId('first'));
    expect(screen.getByTestId('last')).toHaveFocus();

    arrowUp(screen.getByTestId('last'));
    expect(screen.getByTestId('first')).toHaveFocus();
  });

  it('navigates each column on its own, and continues in the neighbouring row sideways', () => {
    renderTable(gridRows);

    arrowDown(screen.getByTestId('recipient-first'));
    expect(screen.getByTestId('recipient-second')).toHaveFocus();

    arrowRight(screen.getByTestId('quantity-first'));
    expect(screen.getByTestId('recipient-second')).toHaveFocus();

    arrowLeft(screen.getByTestId('recipient-second'));
    expect(screen.getByTestId('quantity-first')).toHaveFocus();
  });

  it('leaves left and right to the field on a vertical only table, and still moves up and down', () => {
    renderTable(gridRows, { verticalOnly: true });

    const isNotPrevented = arrowRight(screen.getByTestId('recipient-first'));
    expect(screen.getByTestId('quantity-first')).not.toHaveFocus();
    expect(isNotPrevented).toBe(true);

    arrowDown(screen.getByTestId('quantity-first'));
    expect(screen.getByTestId('quantity-second')).toHaveFocus();
  });

  it('leaves alone the fields of a column the navigation is off for', () => {
    renderTable([
      { id: 'first', column: COLUMN },
      { id: 'comment', column: OTHER_COLUMN, unmarked: true },
      { id: 'second', column: COLUMN },
    ]);

    const isNotPrevented = arrowDown(screen.getByTestId('comment'));

    expect(screen.getByTestId('second')).not.toHaveFocus();
    expect(isNotPrevented).toBe(true);
  });

  it('keeps the arrows it uses from the field and from handlers above the table', () => {
    renderTable(columnRows);
    const keysAboveTheTable = [];
    const listener = (event) => keysAboveTheTable.push(event.key);
    document.body.addEventListener('keydown', listener);

    const isNotPrevented = arrowDown(screen.getByTestId('first'));
    document.body.removeEventListener('keydown', listener);

    expect(isNotPrevented).toBe(false);
    expect(keysAboveTheTable).toEqual([]);
  });

  it('closes the calendar of the date field it leaves', () => {
    // react-datepicker puts its calendar in a portal into the app root.
    document.body.appendChild(Object.assign(document.createElement('div'), { id: 'root' }));
    renderTable([
      { id: 'expiry', column: COLUMN, field: 'date' },
      { id: 'below', column: COLUMN },
    ]);
    // The only text input among the number ones the stand-in renders.
    const dateField = screen.getByRole('textbox');
    fireEvent.click(dateField);
    expect(document.querySelector('.react-datepicker')).toBeInTheDocument();

    arrowDown(dateField);

    expect(screen.getByTestId('below')).toHaveFocus();
    expect(document.querySelector('.react-datepicker')).not.toBeInTheDocument();
  });

  it('lands on the input of a select and leaves it without opening the menu', () => {
    renderTable([
      { id: 'above', column: COLUMN },
      { id: 'recipient', column: COLUMN, field: 'select' },
      { id: 'below', column: COLUMN },
    ]);
    // What react-select gives the focus to. The value lives in a hidden input next to it.
    const selectInput = document.querySelector('.react-select__input input');

    arrowDown(screen.getByTestId('above'));
    expect(selectInput).toHaveFocus();

    arrowDown(selectInput);

    expect(screen.getByTestId('below')).toHaveFocus();
    expect(document.querySelector('.react-select__menu')).not.toBeInTheDocument();
  });

  it('adds a row from the last field of the table and focuses the field it brings', () => {
    const onNavigatePastLastField = jest.fn();
    renderTable(gridRows, { onNavigatePastLastField });

    arrowDownNatively(screen.getByTestId('recipient-second'));
    expect(onNavigatePastLastField).not.toHaveBeenCalled();

    arrowDownNatively(screen.getByTestId('quantity-second'));
    expect(onNavigatePastLastField).toHaveBeenCalledWith(COLUMN);
    expect(screen.getByTestId('added-0')).toHaveFocus();
  });
});
