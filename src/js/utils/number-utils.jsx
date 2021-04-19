export default function accountingFormat(value) {
  if (!value) {
    return '';
  }

  const number = Number.parseFloat(value).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  if (number < 0) {
    return `(${number.replace('-', '')})`;
  }

  return number;
}
