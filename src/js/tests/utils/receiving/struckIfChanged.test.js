import ReceivingRowType from 'consts/receivingRowType';
import struckIfChanged from 'utils/receiving/struckIfChanged';

const replacedRow = (changes) => ({ rowType: ReceivingRowType.REPLACED, ...changes });

describe('struckIfChanged', () => {
  it('crosses out the field the split items override', () => {
    expect(struckIfChanged(replacedRow({ lotChanged: true }), 'lotChanged'))
      .toBe('receiving-table__struck');
  });

  it('leaves the fields the split items keep untouched', () => {
    const item = replacedRow({ lotChanged: true, expirationChanged: true });
    expect(struckIfChanged(item, 'productChanged')).toBe('');
    expect(struckIfChanged(item, 'recipientChanged')).toBe('');
  });

  it('crosses out nothing when a line is split without changing any field', () => {
    const item = replacedRow({
      productChanged: false,
      lotChanged: false,
      expirationChanged: false,
      recipientChanged: false,
    });
    expect(struckIfChanged(item, 'productChanged')).toBe('');
    expect(struckIfChanged(item, 'lotChanged')).toBe('');
    expect(struckIfChanged(item, 'expirationChanged')).toBe('');
    expect(struckIfChanged(item, 'recipientChanged')).toBe('');
  });

  it.each([
    ReceivingRowType.SPLIT_ITEM,
    ReceivingRowType.TOGGLE,
    null,
  ])('crosses out nothing on rows other than the replaced one (%s)', (rowType) => {
    expect(struckIfChanged({ rowType, productChanged: true }, 'productChanged')).toBe('');
  });

  it('crosses out nothing for missing entities (separator rows)', () => {
    expect(struckIfChanged(undefined, 'productChanged')).toBe('');
    expect(struckIfChanged(null, 'productChanged')).toBe('');
  });
});
