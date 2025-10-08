/**
 * Filters documents by a specific step number
 * @param {Array} documents - Array of document objects to filter
 * @param {number} stepNumber - Step number to match against document.stepNumber
 * @returns {Array} Filtered array of documents matching the given step number
 */
const filterDocumentsByStepNumber = (documents, stepNumber) =>
  documents.filter((document) => document.stepNumber === stepNumber);

export default filterDocumentsByStepNumber;
