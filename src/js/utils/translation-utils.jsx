import _ from 'lodash';

const splitTranslation = (data, locale) => {
  const [en, fr] = _.split(data, '|fr:');
  return locale === 'fr' && fr ? fr : en;
};

export default splitTranslation;
