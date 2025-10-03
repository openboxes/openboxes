const filterDocumentsByStepNumber = (documents, stepNumber) =>
  documents.filter((document) => document.stepNumber === stepNumber);

export default filterDocumentsByStepNumber;
