var app=angular.module('pinyougou',[]);

//定义过滤器
app.filter('trustHtml',['$sce',function ($sce) {
    return function (data) {
        //返回过滤后的内容
        return $sce.trustAsHtml(data);
    }
}]);
