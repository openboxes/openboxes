const preventFractionalValues = (event) => {
  if (event.key === '.' || event.key === ',') {
    event.preventDefault();
  }
};

export default preventFractionalValues;
