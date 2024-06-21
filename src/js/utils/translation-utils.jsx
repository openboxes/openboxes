import _ from 'lodash';
import moment from 'moment';
import { getTranslate } from 'react-localize-redux';

const splitTranslation = (data, locale) => {
  const [en, fr] = _.split(data, '|fr:');
  return locale === 'fr' && fr ? fr : en;
};

export const getDateFormat = (localize, formatName) =>
  getTranslate(localize)(formatName);

export const getLocaleCode = (localize) =>
  localize.languages.find(lang => lang.active)?.code;

export const formatDate = (localize) => (date, formatName) => {
  const localeCode = getLocaleCode(localize);
  const dateFormat = getTranslate(localize)(formatName);
  return moment(date).locale(localeCode).format(dateFormat);
};

export default splitTranslation;
