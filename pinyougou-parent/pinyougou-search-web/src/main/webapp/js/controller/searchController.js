app.controller('searchController', function ($scope,searchService) {


    //定义搜索对象的结构
    $scope.searchMap ={'keywords':'','category':'','brand':'','spec':{},'price':''}
    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
            }
        )
    }

    //添加搜索项
    $scope.addSearchItem = function (key,value) {
        if (key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchMap[key] = value;
        }else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    }

    $scope.removeSearchItem = function (key) {
        if (key == 'category' || key == 'brand' || 'price'){
            $scope.searchMap[key] = '';
        }else {
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }
});
