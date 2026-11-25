import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { RiArrowLeftSLine, RiArrowRightSLine } from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Select from 'react-select';

import { translateWithDefaultMessage } from 'utils/Translate';

const TablePagination = ({
  page,
  pages,
  pageSize,
  resolvedData,
  pageSizeOptions,
  onPageSizeChange,
  onPageChange,
  canNext,
  canPrevious,
  totalData = undefined,
  translate,
  className = '',
}) => {
  const [currentPage, setCurrentPage] = useState(page + 1);

  useEffect(() => {
    setCurrentPage(page + 1);
  }, [page]);

  const nextPage = () => {
    if (canNext) onPageChange(page + 1);
  };

  const previousPage = () => {
    if (canPrevious) onPageChange(page - 1);
  };

  const pageSizeChangeHandler = ({ value }) => {
    onPageSizeChange(value);
  };

  const changePageOnChangeHandler = (e) => {
    setCurrentPage(e.target.valueAsNumber);
  };
  const changePageOnKeyPressHandler = (event) => {
    if (event.key === 'Enter') {
      onPageChange(currentPage - 1);
    }
  };

  const totalDataSize = totalData || resolvedData.length;
  const rangeNumberFrom = (page * pageSize) + 1;
  let rangeNumberTo = (page * pageSize) + pageSize;
  if (totalDataSize < rangeNumberTo) {
    rangeNumberTo = totalDataSize;
  }

  const pageSizeSelectOptions = pageSizeOptions.map((size) => ({
    label: `${size} ${translate('react.reactTable.pagination.rows.label', 'rows')}`,
    value: size,
  }));
  const selectedPageSizeOption = pageSizeSelectOptions.find(({ value }) =>
    value === pageSize);

  return (
    <div className={`table-pagination d-flex flex-row align-items-center justify-content-between py-2 px-3 ${className}`}>
      <div className="d-flex">
        <span>{`${rangeNumberFrom}-${rangeNumberTo}`}</span>
        <span className="mx-1">{translate('react.reactTable.pagination.of.label', 'of')}</span>
        <span>{totalDataSize}</span>
      </div>
      <div className="d-flex pages-selector">
        <nav className="d-flex justify-content-center align-items-center">
          <button
            type="button"
            disabled={!canPrevious}
            className="table-pagination__btn-previous"
            onClick={previousPage}
          >
            <RiArrowLeftSLine />
          </button>
          <span className="table-pagination__current-page mx-2">
            <input
              type="number"
              min={1}
              max={pages}
              value={currentPage}
              onChange={changePageOnChangeHandler}
              onKeyPress={changePageOnKeyPressHandler}
            />
            <span className="mx-1">{translate('react.reactTable.pagination.of.label', 'of')}</span>
            <span>{pages}</span>
          </span>
          <button
            type="button"
            disabled={!canNext}
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
    </div>
  );
};

const mapStateToProps = (state) => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

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
  className: PropTypes.string,
};

export default connect(mapStateToProps)(TablePagination);
