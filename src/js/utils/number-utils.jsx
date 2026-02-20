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

// 1 MB = 1024 * 1024 = 1,048,576 bytes
export const bytesToMB = (bytes) => (bytes / (1024 * 1024)).toFixed(2);
