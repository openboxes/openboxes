var app = angular.module('APP',['googlechart']).controller("mainController",['$scope', '$http', function($scope, $http) {
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
        $scope.limitTo = 25;
        $scope.chart = {
            "type": "ColumnChart",
            "cssStyle": "height:200px; width:300px;",
            "data": {
                "cols": [
                    {
                        "id": "month",
                        "label": "Month",
                        "type": "string",
                        "p": {}
                    },
                    {
                        "id": "laptop-id",
                        "label": "Laptop",
                        "type": "number",
                        "p": {}
                    },
                    {
                        "id": "desktop-id",
                        "label": "Desktop",
                        "type": "number",
                        "p": {}
                    },
                    {
                        "id": "server-id",
                        "label": "Server",
                        "type": "number",
                        "p": {}
                    },
                    {
                        "id": "cost-id",
                        "label": "Shipping",
                        "type": "number"
                    }
                ],
                "rows": [
                    {
                        "c": [
                            {
                                "v": "January"
                            },
                            {
                                "v": 19,
                                "f": "42 items"
                            },
                            {
                                "v": 12,
                                "f": "Ony 12 items"
                            },
                            {
                                "v": 7,
                                "f": "7 servers"
                            },
                            {
                                "v": 4
                            }
                        ]
                    },
                    {
                        "c": [
                            {
                                "v": "February"
                            },
                            {
                                "v": 13
                            },
                            {
                                "v": 1,
                                "f": "1 unit (Out of stock this month)"
                            },
                            {
                                "v": 12
                            },
                            {
                                "v": 2
                            }
                        ]
                    },
                    {
                        "c": [
                            {
                                "v": "March"
                            },
                            {
                                "v": 24
                            },
                            {
                                "v": 0
                            },
                            {
                                "v": 11
                            },
                            {
                                "v": 6
                            }
                        ]
                    }
                ]
            },
            "options": {
                "title": "Sales per month",
                "isStacked": "true",
                "fill": 20,
                "displayExactValues": true,
                "vAxis": {
                    "title": "Sales unit",
                    "gridlines": {
                        "count": 6
                    }
                },
                "hAxis": {
                    "title": "Date"
                }
            },
            "formatters": {},
            "displayed": true
        };

        //$scope.loading=false;


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

        $http.jsonp('/openboxes/inventory/calculateQuantityOnHandByProduct?callback=JSON_CALLBACK').success(function(data) {
            console.log(data);
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
            console.log(error);
            alert("An error occurred that prevents us from populating this table" + error);
        });
    };


}]).controller('appController',['$scope',function($scope){
    $scope.$on('LOAD',function(){ $scope.loading=true; });
    $scope.$on('UNLOAD',function(){ $scope.loading=false; });
}]);


//angular.module('APP',[]).controller('myController',['$scope','$http',function($scope,$http){
//        $scope.$emit('LOAD')
//        $http.jsonp('http://filltext.com/?rows=10&delay=5&fname={firstName}&callback=JSON_CALLBACK')
//            .success(function(data){
//                $scope.people=data
//                $scope.$emit('UNLOAD')
//            })
//    }]).
//    controller('appController',['$scope',function($scope){
//        $scope.$on('LOAD',function(){$scope.loading=true});
//        $scope.$on('UNLOAD',function(){$scope.loading=false});
//    }])