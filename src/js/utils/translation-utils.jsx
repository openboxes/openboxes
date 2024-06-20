import _ from 'lodash';
import moment from 'moment';
import { getTranslate } from 'react-localize-redux';

const splitTranslation = (data, locale) => {
  const [en, fr] = _.split(data, '|fr:');
  return locale === 'fr' && fr ? fr : en;
};

export const formatDate = (localize) => (date, formatName) => {
  const localeCode = localize.languages.find(lang => lang.active)?.code;
  const dateFormat = getTranslate(localize)(formatName);
  return moment(date).locale(localeCode).format(dateFormat);
};

export default splitTranslation;
