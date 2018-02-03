/**
 * Created by Administrator on 2018/1/19.
 */
var modelList = {
  downloadXML: function (event) {
    event = event || window.event;
    var target = event.target || event.srcElement;

    var url = target.dataset.url;
    $.ajax({
      url: url,
      type: "GET",
      dataType: 'xml',
      success: function (data) {
        const a = document.createElement('a');
        a.setAttribute('href', url);
        a.click();
      },
      error: function () {
        alert("模型数据为空，请先设计流程并成功保存，再进行导出。")
      }
    })
  },
  delDeployment: function (event) {

    event = event || window.event;
    var target = event.target || event.srcElement;
    $.ajax({
      url: '/deployments/' + target.dataset.id,
      type: "delete",
      success: function (result) {
        if (result.status == "000") {
          var tr = target.parentNode.parentNode;
          var table = tr.parentNode;
          table.removeChild(tr);
        } else {
          console.log("delete deployment error");
        }
      },

    })
  },

  loadDeployments: function() {
    AJAX.GET("/deployments", null
      , function (result) {
        result.rows.list.forEach(function(ele) {
          ele.deploymentTime = new Date(ele.deploymentTime).toLocaleString();
        });
        var html = template("deployments", result);
        $("#deployment-list").html(html);
      });
  },
  loadInstances: function() {
    $.ajax("/instances",{
      type: "GET",
      success: function (result) {

        if(!result || !result.success) {
          return;
        }
        var html = template("process_instances",result);
        $("#process_instance-list").html(html);
      }
    });
  },
  loadTasks: function() {
    $.ajax("/tasks",{
      type: "GET",
      success: function (result) {

        if(!result || !result.success) {
          return;
        }
        var html = template("tasks",result);
        $("#task-list").html(html);
      }
    });
  },

  initData: function() {
    modelList.initUser();
    // model使用模板加载了，首页不会闪
    // modelList.loadModels();
    modelList.loadDeployments();
    modelList.loadInstances();
    modelList.loadTasks();
  },

  getCookie: function(name) {
    var arr;
    var reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
    if( arr = document.cookie.match(reg) ) {
      return arr[2];
    }else {
      return null;
    }
  },

  initUser: function() {
    var userId = modelList.getCookie("userId") || '请登录';
    $("#userId").html(userId);
  },

  claimTask: function(event) {
    event = event || window.event;
    var target = event.target || event.srcElement;
    $.ajax({
      url: '/claimTask/' + target.dataset.id,
      type: "GET",
      success: function (result) {
        // TODO 优化只渲染更改部分
        modelList.loadTasks();
      },

    })
  },



}


$(function () {
  modelList.initData();
})