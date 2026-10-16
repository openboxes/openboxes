import { renderHook } from '@testing-library/react-hooks';

import navigationKey from 'consts/navigationKey';
import useArrowsNavigation from 'hooks/useArrowsNavigation';

// A new row can edit every column; an existing row only the lot and the quantity.
const NEW_ROW_CELLS = ['product', 'lot', 'expiry', 'quantity'];
const EXISTING_ROW_CELLS = ['lot', 'quantity'];

const keyEvent = (key, ctrlKey = false) => ({
  key,
  ctrlKey,
  preventDefault: jest.fn(),
  stopPropagation: jest.fn(),
});

describe('useArrowsNavigation', () => {
  let props;

  const press = (key, rowIndex, columnId, ctrlKey) => {
    const { result } = renderHook(() => useArrowsNavigation(props));
    const event = keyEvent(key, ctrlKey);
    result.current.handleKeyDown(event, rowIndex, columnId);
    return event;
  };

  const expectFocus = (columnId, rowIndex) => {
    expect(props.setColumnId).toHaveBeenLastCalledWith(columnId);
    expect(props.setRowIndex).toHaveBeenLastCalledWith(rowIndex);
    expect(props.onBlur).toHaveBeenCalledTimes(1);
  };

  beforeEach(() => {
    props = {
      newRowFocusableCells: NEW_ROW_CELLS,
      existingRowFocusableCells: EXISTING_ROW_CELLS,
      // Rows 0 and 1 already exist, row 2 was added in the form
      tableData: [{ id: 'a' }, { id: 'b' }, { isNew: true }],
      setColumnId: jest.fn(),
      setRowIndex: jest.fn(),
      addNewRow: jest.fn(),
      isNewRow: (row) => Boolean(row?.isNew),
      getValues: jest.fn(),
      setValue: jest.fn(),
      onBlur: jest.fn(),
    };
  });

  describe('arrow up', () => {
    it('moves to the same column of the row above', () => {
      const event = press(navigationKey.ARROW_UP, 1, 'lot');

      expectFocus('lot', 0);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
    });

    it('stays on the first row', () => {
      const event = press(navigationKey.ARROW_UP, 0, 'lot');

      expect(props.setRowIndex).not.toHaveBeenCalled();
      expect(props.onBlur).not.toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('does not move into a column the existing row above cannot edit', () => {
      press(navigationKey.ARROW_UP, 2, 'product');

      expect(props.setRowIndex).not.toHaveBeenCalled();
    });

    it('moves into any column when the row above is a new row', () => {
      props.tableData = [{ id: 'a' }, { isNew: true }, { isNew: true }];

      press(navigationKey.ARROW_UP, 2, 'product');

      expectFocus('product', 1);
    });
  });

  describe('arrow down', () => {
    it('moves to the same column of the next row', () => {
      const event = press(navigationKey.ARROW_DOWN, 0, 'lot');

      expectFocus('lot', 1);
      expect(props.addNewRow).not.toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('copies the value down to the next row with ctrl', () => {
      props.getValues.mockReturnValue(12);

      press(navigationKey.ARROW_DOWN, 0, 'quantity', true);

      expect(props.getValues).toHaveBeenCalledWith('values.lineItems.0.quantity');
      expect(props.setValue).toHaveBeenCalledWith('values.lineItems.1.quantity', 12);
      expectFocus('quantity', 1);
    });

    it.each(['', undefined, null])('copies nothing with ctrl when the value is %p, but still moves', (value) => {
      props.getValues.mockReturnValue(value);

      press(navigationKey.ARROW_DOWN, 0, 'quantity', true);

      expect(props.setValue).not.toHaveBeenCalled();
      expectFocus('quantity', 1);
    });

    it('copies 0, which is a value', () => {
      props.getValues.mockReturnValue(0);

      press(navigationKey.ARROW_DOWN, 1, 'quantity', true);

      expect(props.setValue).toHaveBeenCalledWith('values.lineItems.2.quantity', 0);
    });

    it('moves without copying with ctrl when the form accessors are not given', () => {
      props.getValues = undefined;
      props.setValue = undefined;

      press(navigationKey.ARROW_DOWN, 0, 'quantity', true);

      expectFocus('quantity', 1);
    });

    it('adds a row from the last column of the last row and focuses its first cell', () => {
      const event = press(navigationKey.ARROW_DOWN, 2, 'quantity');

      expect(props.addNewRow).toHaveBeenCalledTimes(1);
      expectFocus('product', 3);
      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('adds a row without copying when ctrl is held on the last row', () => {
      press(navigationKey.ARROW_DOWN, 2, 'quantity', true);

      expect(props.setValue).not.toHaveBeenCalled();
      expect(props.addNewRow).toHaveBeenCalledTimes(1);
    });

    it('does nothing from another column of the last row', () => {
      const event = press(navigationKey.ARROW_DOWN, 2, 'lot');

      expect(props.addNewRow).not.toHaveBeenCalled();
      expect(props.setRowIndex).not.toHaveBeenCalled();
      expect(props.onBlur).not.toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
    });
  });

  describe('arrow right and tab', () => {
    it('skips the columns an existing row cannot edit', () => {
      const event = press(navigationKey.ARROW_RIGHT, 0, 'lot');

      expectFocus('quantity', 0);
      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('moves to the next column of a new row', () => {
      press(navigationKey.TAB, 2, 'product');

      expectFocus('lot', 2);
    });

    it('wraps from the last column to the first editable column of the next existing row', () => {
      press(navigationKey.TAB, 0, 'quantity');

      expectFocus('lot', 1);
      expect(props.addNewRow).not.toHaveBeenCalled();
    });

    it('wraps to the first column when the next row is new', () => {
      press(navigationKey.ARROW_RIGHT, 1, 'quantity');

      expectFocus('product', 2);
    });

    it('adds a row when wrapping past the last row', () => {
      press(navigationKey.TAB, 2, 'quantity');

      expect(props.addNewRow).toHaveBeenCalledTimes(1);
      expectFocus('product', 3);
    });
  });

  describe('arrow left', () => {
    it('stays put at the first editable cell of the first row', () => {
      press(navigationKey.ARROW_LEFT, 0, 'lot');

      expectFocus('lot', 0);
    });

    it('wraps to the last editable column of the existing row above', () => {
      press(navigationKey.ARROW_LEFT, 1, 'lot');

      expectFocus('quantity', 0);
    });

    it('skips the columns an existing row cannot edit', () => {
      press(navigationKey.ARROW_LEFT, 1, 'quantity');

      expectFocus('lot', 1);
    });

    it('moves to the previous column of a new row', () => {
      press(navigationKey.ARROW_LEFT, 2, 'expiry');

      expectFocus('lot', 2);
    });

    it('wraps from the first column of a new row to the row above', () => {
      press(navigationKey.ARROW_LEFT, 2, 'product');

      expectFocus('quantity', 1);
    });

    it('wraps to the last column when the row above is new', () => {
      props.tableData = [{ isNew: true }, { isNew: true }];

      press(navigationKey.ARROW_LEFT, 1, 'product');

      expectFocus('quantity', 0);
    });
  });

  it('ignores other keys', () => {
    const event = press('Enter', 0, 'lot');

    expect(props.setColumnId).not.toHaveBeenCalled();
    expect(props.setRowIndex).not.toHaveBeenCalled();
    expect(event.preventDefault).not.toHaveBeenCalled();
  });
});
