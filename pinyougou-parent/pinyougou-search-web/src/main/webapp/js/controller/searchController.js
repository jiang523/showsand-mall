app.controller('searchController', function ($scope,searchService) {


    //定义搜索对象的结构
    $scope.searchMap ={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':10}
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                //$scope.searchMap.pageNo = 1;
                $scope.buildPageLabel();
            }
        )
    }

    //构建分页栏
    $scope.buildPageLabel = function(){
        //构建分页标签
        $scope.pageLabel = [];
        let firstPage = 1;
        let lastPage = $scope.resultMap.totalPages;
        $scope.firstDot = true;
        $scope.lastDot = true;

        if (lastPage>5){
            if ($scope.searchMap.pageNo<=3){
                lastPage = 5;
                $scope.firstDot = false;
            }else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-1){
                firstPage = $scope.resultMap.totalPages-4;
                $scope.lastDot = false;
            }else{
                firstPage = $scope.searchMap.pageNo-2;
                lastPage = $scope.searchMap.pageNo+2;
            }
        }else{
            $scope.firstDot = false;
            $scope.lastDot = false;
        }

        for (var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
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

    $scope.queryByPage = function (pageNo) {
        if (pageNo<1 || pageNo>$scope.searchMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1){
            return true;
        }else {
            return false;
        }
    }

    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages){
            return true;
        }else {
            return false;
        }
    }
});
