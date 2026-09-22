import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';

import AutosaveQuantityInputCell from 'utils/cells/AutosaveQuantityInputCell';

import '@testing-library/jest-dom';

// InputWrapper resolves its translate function from the store; the cell test
// only cares about commit semantics, so translations resolve to the default message.
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);

describe('AutosaveQuantityInputCell', () => {
  let onCommit;

  const buildCell = (value) => (
    <AutosaveQuantityInputCell
      value={value}
      onCommit={onCommit}
      label="react.receiving.receivingNow.label"
      defaultLabel="Receiving Now"
    />
  );

  const renderCell = (value = null) => render(buildCell(value));

  const getInput = () => screen.getByTestId('table-cell').querySelector('input');

  beforeEach(() => {
    onCommit = jest.fn();
  });

  it('commits the truncated integer value on every change, without waiting for blur', () => {
    renderCell();
    const input = getInput();

    fireEvent.change(input, { target: { value: '1' } });
    fireEvent.change(input, { target: { value: '15' } });

    expect(onCommit).toHaveBeenCalledTimes(2);
    expect(onCommit).toHaveBeenNthCalledWith(1, 1);
    expect(onCommit).toHaveBeenNthCalledWith(2, 15);
  });

  it('commits null when the field is cleared', () => {
    renderCell(7);
    const input = getInput();

    fireEvent.change(input, { target: { value: '' } });

    expect(onCommit).toHaveBeenCalledTimes(1);
    expect(onCommit).toHaveBeenCalledWith(null);
  });

  it('does not commit again when blur re-emits the already committed value', () => {
    renderCell();
    const input = getInput();

    fireEvent.change(input, { target: { value: '15' } });
    // TextInput (type="number") re-fires onChange with the parsed value on blur.
    fireEvent.blur(input);

    expect(onCommit).toHaveBeenCalledTimes(1);
    expect(onCommit).toHaveBeenCalledWith(15);
  });

  it('does not commit when blur follows an untouched empty field', () => {
    renderCell();
    const input = getInput();

    fireEvent.blur(input);

    expect(onCommit).not.toHaveBeenCalled();
  });

  it('does not re-commit the stored value after a save reconciles the row', () => {
    const { rerender } = renderCell();
    const input = getInput();

    fireEvent.change(input, { target: { value: '15' } });
    // The saved quantity comes back through the value prop.
    rerender(buildCell(15));
    fireEvent.change(input, { target: { value: '15' } });

    expect(onCommit).toHaveBeenCalledTimes(1);
  });
});
