import accountingFormat from 'utils/number-utils';

describe('accountingFormat()', () => {
  it('should return empty string if no value is given', () => {
    const formattedNumber = accountingFormat('');
    expect(formattedNumber).toBe('');
  });
  it('should return numbers with two decimal places after the dot', () => {
    expect(accountingFormat('1')).toBe('1.00');
    expect(accountingFormat('3.99')).toBe('3.99');
    expect(accountingFormat('1.421')).toBe('1.42');
    expect(accountingFormat('1.7899999')).toBe('1.79');
    expect(accountingFormat('0')).toBe('0.00');
    expect(accountingFormat('1.9999')).toBe('2.00');
  });
  it('should convert negative numbers to positive', () => {
    expect(accountingFormat('-1')).toBe('(1.00)');
    expect(accountingFormat('-11.99')).toBe('(11.99)');
    expect(accountingFormat('-1.421')).toBe('(1.42)');
  });
});
