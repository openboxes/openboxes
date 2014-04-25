var appControllers = angular.module('appControllers', []);

appControllers.controller('Ctrl', function ($scope, $translate) {
    $scope.changeLanguage = function (key) {
        $translate.use(key);
    };
});

appControllers.controller("ProductDetailController",['$scope', '$routeParams', '$http', function($scope, $routeParams, $http) {
    $scope.productId = $routeParams.productId;

}]);

appControllers.controller("ProductListController",['$scope', '$http', function($scope, $http) {
    $scope.results = [];
    $scope.availableStatuses = [];
    $scope.totalValue = 0.00;
    $scope.elapsedTime = 0.0;
    $scope.allStockCount = 0;
    $scope.inStockCount = 0;
    $scope.overStockCount = 0;
    $scope.lowStockCount = 0;
    $scope.reorderStockCount = 0;
    $scope.outOfStockCount = 0;
    $scope.sortBy = 'name';
    $scope.reverse = false;
    $scope.statusFilter = '';
    $scope.startFrom = 1;
    $scope.currentPage = 0;
    $scope.pageSize = 10;
    $scope.groups = [];
    $scope.hasProductGroupFilter = false;
    $scope.minPrice = 0;


    $scope.priceRangeFilter = function (product) {
        return product.pricePerUnit >= $scope.minPrice;
    };


    // Pagination in controller
    $scope.setCurrentPage = function(currentPage) {
        $scope.currentPage = currentPage;
    }

    $scope.getNumberAsArray = function (num) {
        return new Array(num);
    };

    $scope.numberOfPages = function() {
        return Math.ceil($scope.results.length/ $scope.pageSize);
    };

    //$scope.loading=false;

    function sortOn( collection, name ) {
        collection.sort(
            function( a, b ) {
                if ( a[ name ] <= b[ name ] ) {
                    return( -1 );
                }
                return( 1 );
            }
        );
    }

    // I group the results list on the given property.
    $scope.groupBy = function( attribute ) {

        // First, reset the groups.
        $scope.groups = [];

        // Now, sort the collection of result on the
        // grouping-property. This just makes it easier
        // to split the collection.
        sortOn( $scope.results, attribute );

        // I determine which group we are currently in.
        var groupValue = "_INVALID_GROUP_VALUE_";

        // As we loop over each friend, add it to the
        // current group - we'll create a NEW group every
        // time we come across a new attribute value.
        for ( var i = 0 ; i < $scope.results.length ; i++ ) {

            var result = $scope.results[ i ];

            // Should we create a new group?
            if ( result[ attribute ] !== groupValue ) {

                var group = {
                    label: result[ attribute ],
                    results: []
                };

                groupValue = group.label;

                $scope.groups.push( group );

            }

            // Add the friend to the currently active
            // grouping.
            group.results.push( result );

        }

    };


    $scope.export = function () {
        var blob = new Blob([document.getElementById('exportable').innerHTML], {
            type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8"
        });
        saveAs(blob, "inventory.xls");
    };

    $scope.refresh = function() {
        $scope.searchText.name = '';
        $scope.init();
    }


    $scope.init = function() {
        $scope.$emit('LOAD');

        $http.jsonp('/openboxes/json/calculateQuantityOnHandByProduct?callback=JSON_CALLBACK').success(function(data) {
            //console.log(data);
            $scope.results = data.items;
            $scope.elapsedTime = data.elapsedTime;
            $scope.allStockCount = data.allStockCount;
            $scope.inStockCount = data.inStockCount;
            $scope.overStockCount = data.overStockCount;
            $scope.lowStockCount = data.lowStockCount;
            $scope.reorderStockCount = data.reorderStockCount;
            $scope.outOfStockCount = data.outOfStockCount;
            $scope.totalValue = data.totalValue;

            angular.forEach(data.items, function(value, index){
                var exists = false;
                angular.forEach($scope.availableStatuses, function(availableStatus, index){
                    if (availableStatus == value.status) {
                        exists = true;
                    }
                });
                if (exists === false) {
                    $scope.availableStatuses.push(value.status);
                }

            });
            $scope.$emit('UNLOAD');


        }).error(function(error) {
            //console.log(error);
            alert("An error occurred that prevents us from populating this table " + error);
        });
    };


}]);

appControllers.controller('appController',['$scope',function($scope){
    $scope.$on('LOAD',function(){ $scope.loading=true; });
    $scope.$on('UNLOAD',function(){ $scope.loading=false; });
}]);
//We already have a limitTo filter built-in to angular,
//let's make a startFrom filter
appControllers.filter('startFrom', function() {
    return function(input, start) {
        start = +start; //parse to int
        return input.slice(start);
    }
});
