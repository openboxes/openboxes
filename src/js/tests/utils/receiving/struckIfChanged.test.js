import ReceivingRowType from 'consts/receivingRowType';
import struckIfChanged from 'utils/receiving/struckIfChanged';

describe('struckIfChanged', () => {
  it('crosses out the field the split items override', () => {
    expect(struckIfChanged(ReceivingRowType.REPLACED, true)).toBe('receiving-table__struck');
  });

  it('leaves the fields the split items keep untouched', () => {
    expect(struckIfChanged(ReceivingRowType.REPLACED, false)).toBe('');
  });

  it('crosses out nothing when a line is split without changing any field', () => {
    expect(struckIfChanged(ReceivingRowType.REPLACED, undefined)).toBe('');
  });

  it.each([
    ReceivingRowType.SPLIT_ITEM,
    ReceivingRowType.TOGGLE,
    null,
  ])('crosses out nothing on rows other than the replaced one (%s)', (rowType) => {
    expect(struckIfChanged(rowType, true)).toBe('');
  });

  it('crosses out nothing for missing entities (separator rows)', () => {
    expect(struckIfChanged(undefined, true)).toBe('');
  });
});
