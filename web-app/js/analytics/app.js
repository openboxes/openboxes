var app = angular.module('APP',['googlechart','ngRoute','appControllers','pascalprecht.translate']);

app.config(function ($translateProvider) {
    $translateProvider.translations('en', {
        'default.title.label': 'Hello',
        'default.description.label': 'This is a paragraph.',
        'default.button.language.en.label': 'english',
        'default.button.language.de.label': 'german',
        'product.list.label': 'Product list',
        'product.details.label': 'Product details'
    });
    $translateProvider.translations('de', {
        'default.title.label': 'Hallo',
        'default.description.label': 'Dies ist ein Paragraph.',
        'default.button.language.en.label': 'englisch',
        'default.button.language.de.label': 'deutsch',
        'product.list.label': 'listo productos',
        'product.details.label': 'Details de producto'
    });
    $translateProvider.preferredLanguage('en');
});


app.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/products', {
                templateUrl: '/openboxes/js/analytics/product-list.html',
                controller: 'ProductListController'
            }).
            when('/products/:productId', {
                templateUrl: '/openboxes/js/analytics/product-detail.html',
                controller: 'ProductDetailController'
            }).
            otherwise({
                redirectTo: '/products'
            });
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