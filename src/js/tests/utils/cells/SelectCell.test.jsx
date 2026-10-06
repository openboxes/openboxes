import React from 'react';

import { render, screen } from '@testing-library/react';

import SelectCell from 'utils/cells/SelectCell';

import '@testing-library/jest-dom';

jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);

const SelectFieldStub = jest.fn(() => <input data-testid="select" />);

const renderCell = (props = {}) => render(
  <SelectCell
    value={{ id: 'person-1', name: 'John Doe' }}
    label="react.receiving.recipient.label"
    defaultLabel="Recipient"
    selectFieldComponent={SelectFieldStub}
    {...props}
  />,
);

const lastSelectProps = () => SelectFieldStub.mock.calls[SelectFieldStub.mock.calls.length - 1][0];

describe('SelectCell', () => {
  beforeEach(() => {
    SelectFieldStub.mockClear();
  });

  it('should pass the error message to the select', () => {
    renderCell({ errorMessage: 'Duplicate rows for this inventory item' });

    expect(lastSelectProps().errorMessage).toBe('Duplicate rows for this inventory item');
  });

  it('should show the value tooltip when there is no error', () => {
    renderCell();

    expect(lastSelectProps()).toMatchObject({ errorMessage: null, showValueTooltip: true });
  });

  it('should hide the value tooltip while there is an error, so it does not cover the error tooltip', () => {
    renderCell({ errorMessage: 'Duplicate rows for this inventory item' });

    expect(lastSelectProps().showValueTooltip).toBe(false);
  });

  it('should stretch the tooltip wrapper over the cell, so the select keeps its full width', () => {
    renderCell();

    expect(screen.getByRole('tooltip')).toHaveClass('w-100');
  });

  it('should keep the same select element when an error appears, so it does not lose focus', () => {
    const { rerender } = renderCell();
    const select = screen.getByTestId('select');

    rerender(
      <SelectCell
        value={{ id: 'person-1', name: 'John Doe' }}
        label="react.receiving.recipient.label"
        defaultLabel="Recipient"
        selectFieldComponent={SelectFieldStub}
        errorMessage="Duplicate rows for this inventory item"
      />,
    );

    expect(screen.getByTestId('select')).toBe(select);
  });
});
