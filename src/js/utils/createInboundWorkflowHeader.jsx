import PropTypes from 'prop-types';

const createInboundWorkflowHeader = (data) => [
  {
    text: data.identifier,
    color: '#000000',
    delimeter: ' - ',
    isDate: false,
  },
  {
    text: data.origin.name,
    color: '#004d40',
    delimeter: ' to ',
    isDate: false,
  },
  {
    text: data.destination.name,
    color: '#01579b',
    delimeter: ', ',
    isDate: false,
  },
  {
    text: data.dateRequested,
    color: '#4a148c',
    delimeter: ', ',
    isDate: true,
  },
  {
    text: data.description,
    color: '#770838',
    delimeter: '',
    isDate: false,
  },
];

createInboundWorkflowHeader.propTypes = {
  data: PropTypes.shape({
    identifier: PropTypes.string.isRequired,
    origin: PropTypes.shape({
      name: PropTypes.string.isRequired,
    }).isRequired,
    destination: PropTypes.shape({
      name: PropTypes.string.isRequired,
    }).isRequired,
    dateRequested: PropTypes.string.isRequired,
    description: PropTypes.string.isRequired,
  }).isRequired,
};

export default createInboundWorkflowHeader;
