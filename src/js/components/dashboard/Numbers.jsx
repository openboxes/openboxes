import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

const Numbers = (props) => {
  const [data, options] = Object.values(props);
  const values = _.sortBy(Object.entries(data), '[1].order');
  return (
    <div className="gyr-indicator">
      {
        values.map(([key, indicator]) => {
          if (indicator) {
            const {
              link,
              value,
              subtitle,
            } = indicator;
            const redirect = link ? { href: link } : {};
            return (
              <a key={key} className="number-indicator" {...redirect}>
                <div className="value">
                  <div className="circle" style={{ backgroundColor: options.colors[key] }} />
                  {value}
                </div>
                <div className="subtitle">{subtitle}</div>
              </a>
            );
          }

          return null;
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
