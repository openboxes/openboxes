export default function accountingFormat(value) {
  if (!value) {
    return '';
  }

  const number = Number.parseFloat(value).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  if (number < 0 || number.startsWith('-')) {
    return `(${number.replace('-', '')})`;
  }

  return number;
}
