// eslint-disable-next-line import/prefer-default-export
export const convertToBase64 = (file) => {
  const reader = new FileReader();

  reader.readAsDataURL(file);

  return new Promise((resolve, reject) => {
    reader.onload = () => resolve(reader.result.split(',')[1]);
    reader.onerror = error => reject(error);
  });
};
