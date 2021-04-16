export default function accountingFormat(value) {
  if (!value) {
    return '';
  }

  const number = Number.parseFloat(value).toFixed(2);

  if (number < 0) {
    return `(${number.replace('-', '')})`;
  }

  return number;
}
