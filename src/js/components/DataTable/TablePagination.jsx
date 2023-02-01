import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { RiArrowLeftSLine, RiArrowRightSLine } from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Select from 'react-select';

import { translateWithDefaultMessage } from 'utils/Translate';

const TablePagination = (props) => {
  const [currentPage, setCurrentPage] = useState(props.page + 1);

  useEffect(() => {
    setCurrentPage(props.page + 1);
  }, [props.page]);

  const nextPage = () => {
    if (props.canNext) props.onPageChange(props.page + 1);
  };

  const previousPage = () => {
    if (props.canPrevious) props.onPageChange(props.page - 1);
  };

  const pageSizeChangeHandler = ({ value }) => {
    props.onPageSizeChange(value);
  };

  const changePageOnChangeHandler = (e) => {
    setCurrentPage(e.target.valueAsNumber);
  };
  const changePageOnKeyPressHandler = (event) => {
    if (event.key === 'Enter') {
      props.onPageChange(currentPage - 1);
    }
  };

  const totalDataSize = props.totalData || props.resolvedData.length;
  const rangeNumberFrom = (props.page * props.pageSize) + 1;
  let rangeNumberTo = (props.page * props.pageSize) + props.pageSize;
  if (totalDataSize < rangeNumberTo) {
    rangeNumberTo = totalDataSize;
  }

  const pageSizeSelectOptions = props.pageSizeOptions.map(size => ({
    label: `${size} ${props.translate('react.reactTable.pagination.rows.label', 'rows')}`,
    value: size,
  }));
  const selectedPageSizeOption = pageSizeSelectOptions.find(({ value }) =>
    value === props.pageSize);


  return (
    <div className="table-pagination d-flex flex-row align-items-center justify-content-between py-2 px-3">
      <div className="d-flex">
        <span>{`${rangeNumberFrom}-${rangeNumberTo}`}</span>
        <span className="mx-1">{props.translate('react.reactTable.pagination.of.label', 'of')}</span>
        <span>{totalDataSize}</span>
      </div>
      <div className="d-flex">
        <nav className="d-flex justify-content-center align-items-center">
          <button
            disabled={!props.canPrevious}
            className="table-pagination__btn-previous"
            onClick={previousPage}
          >
            <RiArrowLeftSLine />
          </button>
          <span className="table-pagination__current-page mx-2">
            <input
              type="number"
              min={1}
              max={props.pages}
              value={currentPage}
              onChange={changePageOnChangeHandler}
              onKeyPress={changePageOnKeyPressHandler}
            />
            <span className="mx-1">{props.translate('react.reactTable.pagination.of.label', 'of')}</span>
            <span>{props.pages}</span>
          </span>
          <button
            disabled={!props.canNext}
            className="table-pagination__btn-next"
            onClick={nextPage}
          >
            <RiArrowRightSLine />
          </button>
        </nav>
        <div className="d-flex ml-3">
          <Select
            className="table-pagination-select"
            classNamePrefix="table-pagination-select"
            menuPlacement="auto"
            options={pageSizeSelectOptions}
            value={selectedPageSizeOption}
            onChange={pageSizeChangeHandler}
          />
        </div>
      </div>
    </div>);
};

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

TablePagination.defaultProps = {
  totalData: undefined,
};

TablePagination.propTypes = {
  page: PropTypes.number.isRequired,
  pages: PropTypes.number.isRequired,
  pageSize: PropTypes.number.isRequired,
  resolvedData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  pageSizeOptions: PropTypes.arrayOf(PropTypes.number).isRequired,
  onPageSizeChange: PropTypes.func.isRequired,
  onPageChange: PropTypes.func.isRequired,
  canNext: PropTypes.bool.isRequired,
  canPrevious: PropTypes.bool.isRequired,
  totalData: PropTypes.number,
  translate: PropTypes.func.isRequired,
};


export default connect(mapStateToProps)(TablePagination);
