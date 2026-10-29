import React from 'react';

import {
  fireEvent, render, screen, within,
} from '@testing-library/react';
import { useForm } from 'react-hook-form';
import { ColumnsTable } from 'regression-tests/hooks/inboundV2/columnTestUtils';

import requisitionStatus from 'consts/requisitionStatus';
import useInboundAddItemsColumns from 'hooks/inboundV2/addItems/useInboundAddItemsColumns';
import useArrowsNavigation from 'hooks/useArrowsNavigation';

import '@testing-library/jest-dom';

jest.mock('selectors', () => ({
  getCurrentLocale: () => 'en',
  getCurrentLocationId: () => 'loc-1',
  getDebounceTime: () => 500,
  getMinSearchLength: () => 3,
  getUsers: () => [],
}));
jest.mock('react-redux', () => ({
  useSelector: (selector) => selector(),
}));
jest.mock('utils/option-utils', () => ({
  debouncePeopleFetch: jest.fn(() => 'people-fetch'),
}));
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));
const mockHandleKeyDown = jest.fn();
jest.mock('hooks/useArrowsNavigation', () => jest.fn(() => ({ handleKeyDown: mockHandleKeyDown })));
jest.mock('components/DataTable', () => ({
  TableCell: jest.requireActual('regression-tests/hooks/inboundV2/columnTestUtils').MockTableCell,
}));
jest.mock('components/DataTable/TableHeaderCell', () => jest.requireActual('regression-tests/hooks/inboundV2/columnTestUtils').MockTableHeaderCell);

// Form element stand-ins: a plain input per field, exposing what the column passes in.
const mockFocusAttrs = (focusProps) => ({
  'data-row-index': focusProps?.rowIndex ?? '',
  'data-column-id': focusProps?.columnId ?? '',
});
jest.mock('components/form-elements/v2/TextInput', () => ({
  onChange, onBlur, onFocus, onKeyDown, value, ariaLabel, hasErrors, focusProps, type,
}) => (
  <input
    aria-label={ariaLabel.defaultMessage}
    type={type || 'text'}
    value={value ?? ''}
    data-errors={hasErrors ? 'true' : 'false'}
    {...mockFocusAttrs(focusProps)}
    onChange={(e) => onChange(e.target.value)}
    onBlur={onBlur}
    onFocus={onFocus}
    onKeyDown={onKeyDown}
  />
));
jest.mock('components/form-elements/v2/DateFieldDateFns', () => ({
  onChange, onBlur, value, ariaLabel, onKeyDown,
}) => (
  <input
    aria-label={ariaLabel.defaultMessage}
    value={value ?? ''}
    onChange={(e) => onChange(e.target.value)}
    onBlur={onBlur}
    onKeyDown={onKeyDown}
  />
));
const mockProduct = { id: 'p-9', label: 'P9 - Gauze', productCode: 'P9' };
const mockRecipient = { id: 'u-2', label: 'John', name: 'John' };
// The product cell is the v2 ProductSelectField: its stand-in exposes the product controls.
jest.mock('components/form-elements/v2/ProductSelectField', () => ({
  onChange, value, ariaLabel, onBlur, onExactProductSelected, locationId,
}) => (
  <div>
    <input aria-label={ariaLabel.defaultMessage} readOnly value={value?.label ?? ''} data-location={locationId} onBlur={onBlur} />
    <button type="button" onClick={() => onChange(mockProduct)}>pick product</button>
    <button type="button" onClick={() => onExactProductSelected({ product: mockProduct })}>scan product</button>
  </div>
));
jest.mock('components/form-elements/v2/SelectField', () => ({
  onChange, value, ariaLabel, placeholder, loadOptions, onBlur,
}) => {
  const name = ariaLabel?.defaultMessage ?? `header ${placeholder}`;
  return (
    <div>
      <input aria-label={name} readOnly value={value?.label ?? ''} data-load-options={loadOptions} onBlur={onBlur} />
      <button type="button" onClick={() => onChange(mockRecipient)}>{`pick ${name}`}</button>
      <button type="button" onClick={() => onChange(null)}>{`clear ${name}`}</button>
    </div>
  );
});

const lines = [
  {
    itemId: 'saved-1',
    palletName: 'Pallet A',
    boxName: '',
    product: { id: 'p-1', label: 'P1 - Aspirin' },
    lotNumber: 'L1',
    expirationDate: new Date(2027, 0, 15),
    quantityRequested: 4,
    recipient: { id: 'u-1', label: 'Jane' },
  },
  {
    palletName: '',
    boxName: '',
    product: null,
    lotNumber: '',
    expirationDate: null,
    quantityRequested: '',
    recipient: null,
    statusCode: requisitionStatus.SUBSTITUTED,
  },
];

describe('useInboundAddItemsColumns', () => {
  let formRef;
  let props;

  const Harness = ({ errors }) => {
    const form = useForm({
      defaultValues: { values: { lineItems: lines }, currentLineItems: [{ id: 'saved-1' }] },
    });
    formRef.current = form;
    const { columns } = useInboundAddItemsColumns({
      errors,
      control: form.control,
      getValues: form.getValues,
      setValue: form.setValue,
      trigger: props.trigger,
      removeRow: props.removeRow,
      removeSavedRow: props.removeSavedRow,
      addNewLine: props.addNewLine,
    });
    return <ColumnsTable columns={columns} data={form.getValues('values.lineItems')} />;
  };

  const renderColumns = (errors = {}) => render(<Harness errors={errors} />);
  const row = (index) => within(screen.getByTestId(`row-${index}`));
  const cellTooltip = (index, columnId) => within(screen.getByTestId(`cell-${index}-${columnId}`))
    .getByTestId('table-cell')
    .getAttribute('data-tooltip');

  beforeEach(() => {
    jest.clearAllMocks();
    formRef = { current: null };
    props = {
      trigger: jest.fn(),
      removeRow: jest.fn(),
      removeSavedRow: jest.fn(() => Promise.resolve()),
      addNewLine: jest.fn(),
    };
  });

  it('renders the headers with the required product and quantity columns', () => {
    renderColumns();
    const headers = screen.getAllByTestId('header-cell');
    expect(headers.map((h) => h.textContent))
      .toEqual(['Pack Level 1', 'Pack Level 2', 'Product', 'Lot', 'Expiry', 'Quantity', 'pick header Recipientclear header Recipient', 'Delete']);
    expect(headers.filter((h) => h.getAttribute('data-required')).map((h) => h.textContent))
      .toEqual(['Product', 'Quantity']);
    expect(screen.getByLabelText('header Recipient')).toHaveAttribute('data-load-options', 'people-fetch');
  });

  it('wires arrow navigation over the editable cells', () => {
    renderColumns();
    expect(useArrowsNavigation).toHaveBeenCalledWith(expect.objectContaining({
      newRowFocusableCells: [
        'palletName', 'boxName', 'product', 'lotNumber', 'expirationDate', 'quantityRequested', 'recipient',
      ],
      tableData: lines,
      addNewRow: props.addNewLine,
      onBlur: props.trigger,
    }));
    fireEvent.keyDown(row(1).getByLabelText('Lot'), { key: 'ArrowDown' });
    expect(mockHandleKeyDown).toHaveBeenCalledWith(expect.anything(), 1, 'lotNumber');
  });

  it('shows each value in the cell tooltip, or the error message when there is one', () => {
    renderColumns({
      values: { lineItems: [undefined, { quantityRequested: { message: 'Required' }, product: { message: 'Pick one' } }] },
    });
    expect(cellTooltip(0, 'palletName')).toBe('Pallet A');
    expect(cellTooltip(0, 'product')).toBe('P1 - Aspirin');
    expect(cellTooltip(0, 'lotNumber')).toBe('L1');
    expect(cellTooltip(0, 'expirationDate')).toBe('15/Jan/2027');
    expect(cellTooltip(0, 'quantityRequested')).toBe('4');
    expect(cellTooltip(0, 'recipient')).toBe('Jane');
    expect(cellTooltip(1, 'quantityRequested')).toBe('Required');
    expect(cellTooltip(1, 'product')).toBe('Pick one');
    expect(row(1).getByLabelText('Quantity')).toHaveAttribute('data-errors', 'true');
    expect(row(0).getByLabelText('Quantity')).toHaveAttribute('data-errors', 'false');
  });

  it('writes typed values into the form', () => {
    renderColumns();
    fireEvent.change(row(1).getByLabelText('Pack Level 1'), { target: { value: 'Pallet B' } });
    fireEvent.change(row(1).getByLabelText('Pack Level 2'), { target: { value: 'Box 7' } });
    fireEvent.change(row(1).getByLabelText('Lot'), { target: { value: 'LOT-9' } });
    fireEvent.change(row(1).getByLabelText('Quantity'), { target: { value: '12' } });
    expect(formRef.current.getValues('values.lineItems.1')).toMatchObject({
      palletName: 'Pallet B',
      boxName: 'Box 7',
      lotNumber: 'LOT-9',
      quantityRequested: '12',
    });
  });

  it('marks the paired field as touched when leaving a cell', () => {
    renderColumns();
    fireEvent.blur(row(0).getByLabelText('Pack Level 1'));
    expect(formRef.current.getFieldState('values.lineItems.0.palletName').isTouched).toBe(true);
    expect(formRef.current.getFieldState('values.lineItems.0.boxName').isTouched).toBe(true);

    fireEvent.blur(row(1).getByLabelText('Expiry'));
    expect(formRef.current.getFieldState('values.lineItems.1.lotNumber').isTouched).toBe(true);

    fireEvent.blur(row(1).getByLabelText('Product'));
    expect(formRef.current.getFieldState('values.lineItems.1.quantityRequested').isTouched).toBe(true);
  });

  it('selects the input text on focus once it has rendered', () => {
    jest.useFakeTimers();
    renderColumns();
    const input = row(0).getByLabelText('Lot');
    const select = jest.spyOn(input, 'select');
    fireEvent.focus(input);
    expect(select).not.toHaveBeenCalled();
    jest.runAllTimers();
    expect(select).toHaveBeenCalled();
    jest.useRealTimers();
  });

  it('stores the picked product and moves focus to quantity after an exact match', () => {
    renderColumns();
    fireEvent.click(row(1).getByText('pick product'));
    expect(formRef.current.getValues('values.lineItems.1.product')).toEqual(mockProduct);
    expect(row(1).getByLabelText('Product')).toHaveAttribute('data-location', 'loc-1');

    fireEvent.click(row(1).getByText('scan product'));
    const quantity = row(1).getByLabelText('Quantity');
    expect(quantity).toHaveAttribute('data-row-index', '1');
    expect(quantity).toHaveAttribute('data-column-id', 'quantityRequested');

    // Leaving any cell clears the stored focus target.
    fireEvent.blur(quantity);
    expect(row(1).getByLabelText('Quantity')).toHaveAttribute('data-row-index', '');
  });

  it('applies the header recipient to every line, and ignores clearing it', () => {
    renderColumns();
    fireEvent.click(screen.getByText('pick header Recipient'));
    expect(formRef.current.getValues('values.lineItems').map((line) => line.recipient))
      .toEqual([mockRecipient, mockRecipient]);
    expect(screen.getByLabelText('header Recipient')).toHaveValue('John');

    fireEvent.click(screen.getByText('clear header Recipient'));
    expect(screen.getByLabelText('header Recipient')).toHaveValue('');
    expect(formRef.current.getValues('values.lineItems.0.recipient')).toEqual(mockRecipient);
  });

  it('sets a single line recipient', () => {
    renderColumns();
    fireEvent.click(row(1).getByText('pick Recipient'));
    expect(formRef.current.getValues('values.lineItems.1.recipient')).toEqual(mockRecipient);
    expect(formRef.current.getValues('values.lineItems.0.recipient')).toEqual(lines[0].recipient);
  });

  it('deletes a saved line on the server before removing the row', async () => {
    renderColumns();
    const bins = screen.getAllByRole('button', { name: 'Delete' });
    fireEvent.click(bins[0].firstChild);
    await Promise.resolve();
    await Promise.resolve();
    expect(props.removeSavedRow).toHaveBeenCalledWith('saved-1');
    expect(props.removeRow).toHaveBeenCalledWith(0);
  });

  it('removes an unsaved line locally only', async () => {
    renderColumns();
    const bins = screen.getAllByRole('button', { name: 'Delete' });
    fireEvent.click(bins[1].firstChild);
    await Promise.resolve();
    expect(props.removeSavedRow).not.toHaveBeenCalled();
    expect(props.removeRow).toHaveBeenCalledWith(1);
  });
});
