export default function accountingFormat(number) {
  if (number && number < 0) {
    return `(${number.toString().replace('-', '')})`;
  }

  return number.toString();
}
