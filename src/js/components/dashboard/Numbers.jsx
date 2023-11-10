import React from 'react';

import PropTypes from 'prop-types';

const Numbers = (props) => {
  const [data, options] = Object.values(props);
  return (
    <div className="gyr-indicator">
      {
      Object.keys(data).map((number) => {
        const { link, value, subtitle } = data[number];
        const redirect = link ? { href: link } : {};
        return (
          <a key={number} className="number-indicator" {...redirect}>
            <div className="value">
              <div className="circle" style={{ backgroundColor: options.colors[number] }} />
              {value}
            </div>
            <div className="subtitle">{subtitle}</div>
          </a>
        );
      })
    }
    </div>
  );
};

Numbers.propTypes = {
  data: PropTypes.shape({
    third: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
      link: PropTypes.string,
    }),
    second: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
      link: PropTypes.string,
    }),
    first: PropTypes.shape({
      subtitle: PropTypes.string,
      value: PropTypes.number,
      link: PropTypes.string,
    }),
  }),
  options: PropTypes.shape({
    colors: PropTypes.shape({
      first: PropTypes.string,
      second: PropTypes.string,
      third: PropTypes.string,
    }),
  }).isRequired,
};

Numbers.defaultProps = {
  data: null,
};

export default Numbers;
