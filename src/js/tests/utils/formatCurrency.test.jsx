import formatCurrency from 'utils/formatCurrency';

describe('formatCurrency', () => {
  it('should format number as USD by default', () => {
    const result = formatCurrency(1234.5);
    expect(result).toBe('$1,234.50');
  });

  it('should format number as EUR in German locale', () => {
    const result = formatCurrency(9876.54, 'de-DE', 'EUR');
    expect(result).toBe('9.876,54 €');
  });

  it('should format number with Japanese Yen (JPY)', () => {
    const result = formatCurrency(5000, 'ja-JP', 'JPY');
    expect(result).toBe('￥5,000.00');
  });

  it('should handle zero value correctly', () => {
    const result = formatCurrency(0);
    expect(result).toBe('$0.00');
  });

  it('should handle negative value', () => {
    const result = formatCurrency(-99.99);
    expect(result).toBe('-$99.99');
  });

  it('should round to two decimal places', () => {
    const result = formatCurrency(12.3456);
    expect(result).toBe('$12.35');
  });

  it('should return NaN for non-numeric string', () => {
    const result = formatCurrency('not-a-number');
    expect(result).toBe('$NaN');
  });

  it('should handle NaN input', () => {
    const result = formatCurrency(NaN);
    expect(result).toBe('$NaN');
  });
});
