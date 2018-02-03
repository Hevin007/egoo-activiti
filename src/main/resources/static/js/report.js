/**
 * Created by Administrator on 2018/1/17.
 */
var report = function() {
  // 配置
  var config = {
    router: {
      modelList: "/model-list.html",
    },
    //默认的data
    data : [
      {
        type: {name:"long"},
        name: "年份",
        id: "year"
      },
      {
        type: {name:"string"},
        name: "姓名",
        id: "name"
      },
      {
        type: {name:"boolean"},
        name: "是否为党员",
        id: "isPartyMember"
      },
      {
        type: {name:"date"},
        name: "出身日期",
        id: "birthDate"
      },
      {
        type: {name:"enum"},
        name: "专业技能",
        id: "skills",
        data: [
          {id: 1, name: "1"},
          {id: 2, name: "2"}
        ]
      }
    ],
  };



  var FormComponentFactory = function(formObj) {
    // 安全的构造方法，防止用户忘了使用new
    if(!(this instanceof FormComponentFactory)) {
      return new FormComponentFactory(formObj);
    }

    var type;
    this.name = formObj.name;
    this.id = formObj.id;
    this.data = formObj.data || {};

    switch (formObj.type.name) {
      case "enum":
        type = "checkbox";
        break;
      case "boolean":
        type = "radio";
        break;
      case "date":
        type = "date";
        break;
      default:
        type = "text";
    }

    this.type = type;
  };

  FormComponentFactory.prototype.getFormContentLi = function() {
    // 使用jquery字符串构造功能生成jquery对象，取第0个为js原生对象。
    var li = $(`<li class="list-group-item">
                  <label for="${this.id}" class="label-name">${this.name}</label>
                  <input id="${this.id}" type="${this.type}" name="${this.id}"  placeholder="${this.name}" class="myInput"/>
                </li>`)[0];

    switch (this.type) {
      case "radio":
        li.removeChild(li.lastElementChild);

        var label;
        var arr= ['true', 'false'];
        arr.forEach((ele) => {
          // 同上 取js原生对象
          label = $(`<label class="label-item">
                       <input type="radio" name="${this.id}" value="${ele}"/>${ele}
                     </label>`)[0];
          li.appendChild(label);
        });

        break;
      case "checkbox":
        li.removeChild(li.lastElementChild);
        var label;

        this.data.forEach((ele) => {
          // 同上 取js原生对象
          label = $(`<label class="label-item">
                         <input type="checkbox" name="${this.id}" value="${ele.id}"/>${ele.name}
                       </label>`)[0];
          li.appendChild(label);
        });

        break;
      default:


    }
    return li;
  }

  var loadFormData = function() {
    var formContent = document.getElementsByClassName("form-content")[0];

    report.data.forEach(function(ele) {
      var formComponent = new FormComponentFactory(ele);
      formContent.appendChild(formComponent.getFormContentLi());
    });
  };

  var submitForm = function(e) {
    e = e || window.event;
    e.preventDefault();
    var form = $("#form");
    var url = form.attr("action");
    var fileInput = document.getElementById("attachment");
    var reader = new FileReader();
    var formArray = form.serializeArray();
    var jsonForm = formatArray(formArray);


    reader.onload = function() {

      jsonForm[fileInput.name] = this.result;

      $.ajax(url, {
        type: "POST",
        data: JSON.stringify(jsonForm),
        contentType: "application/json; charset=utf-8",
        success: function() {
           window.location.href= config.router.modelList;
        },
        error: function(e) {
          console.log(e);
        },

      });
    }

    reader.readAsDataURL(fileInput.files[0]);
  };

  /* 数组转json
   * @param array 数组
   */
  function formatArray(array) {
    var dataArray = {};
    $.each(array, function () {
      if (dataArray[this.name]) {
        // 对应checkbox 和 select 值应该为数组
        if (!dataArray[this.name].push) {
          dataArray[this.name] = [dataArray[this.name]];
        }
        dataArray[this.name].push(this.value || '');
      } else {
        dataArray[this.name] = this.value || '';
      }
    });
    return dataArray;
  }

  var loadHistory = function() {
    var ul = document.getElementById("variable-list");
    var taskId = document.querySelector("input[name=taskId]").value;
    $.get(`/task/${taskId}/variables`,function(data) {
      var variables,tasks, key, li, p_variable, isEmpty = true, p_a;


      if(data && data["variables"]) {
        if(isEmpty) isEmpty=false;
        variables = data["variables"];
        li = $(`<li class="list-group-item" >
                </li>`)[0];
        for( key in variables) {

          p_variable = $(`<p>
                       <span class="variable-name">${key}</span>
                       <span class="variable-value">${variables[key]}</span>
                     </p>`)[0];
          li.appendChild(p_variable);
        }
        ul.appendChild(li);
      }

      if(data && data["tasks"]) {

        data["tasks"].forEach(function(task) {

          if(task["attachments"] && task["attachments"].length) {
            if(isEmpty) isEmpty=false;
            li = $(`<li class="list-group-item" >
                        <h4>${task["taskName"]}</h4>
                       <p>
                        <span class="variable-name">图片附件</span>
                       </p>
                    </li>`)[0];

            task["attachments"].forEach(function (attachment)  {
              p_a = $(`<p>
                            <span class="variable-name"></span>
                            <span class="variable-value">
                                <a href="/attachment/${attachment["attachmentId"]}" 
                                    target="_blank" 
                                    onclick="report.handleAttachment(event)"
                                    >${attachment["attachmentName"]}
                                 </a></span></p>`)[0];
              li.appendChild(p_a);
            });

            ul.appendChild(li);
          }
        });

      }
      // 删除默认节点
      if(!isEmpty) {
        ul.removeChild(ul.firstElementChild.nextElementSibling);
      }
    });

  }

  var handleAttachment = function (e) {
    e = e || window.event;
    e.preventDefault();
    var target = e.target || e.srcElement;
    var url =  target.href;
    var modal = $("#modal");
    var modal_body = $("#modal .modal-body");
    // todo 其他文件类型适配
    var img = new Image();
    img.src = url;
    modal_body.html(img);
    modal.modal("show");



  }


  var load = function () {
    loadFormData();
    loadHistory();
  }


  return {
    load: load,
    submitForm:  submitForm,
    data: window.data || config.data,
    handleAttachment: handleAttachment,
  }
}();

$(() => {
  report.load();
});