const validate = (values) => {
  const errors = {};
  if (!values.description) {
    errors.description = 'This field is required';
  }
  if (!values.origin) {
    errors.origin = 'This field is required';
  }
  if (!values.destination) {
    errors.destination = 'This field is required';
  }
  if (!values.requestedBy) {
    errors.requestedBy = 'This field is required';
  }
  if (!values.dateRequested) {
    errors.dateRequested = 'This field is required';
  }
  return errors;
};

export default validate;
