/**
 * Created by Administrator on 2018/1/17.
 */
var report = function() {

  var data = [
    {
      type: "long",
      name: "年份",
      id: "year"
    },
    {
      type: "string",
      name: "姓名",
      id: "name"
    },
    {
      type: "boolean",
      name: "是否为党员",
      id: "isPartyMember"
    },
    {
      type: "date",
      name: "出身日期",
      id: "birthDate"
    },
    {
      type: "enum",
      name: "专业技能",
      id: "skills",
      data: [
        {id: 1, name: "1"},
        {id: 2, name: "2"}
      ]
    }
  ];

  var FormComponentFactory = function(formObj) {
    // 安全的构造方法，防止用户忘了使用new
    if(!(this instanceof FormComponentFactory)) {
      return new FormComponentFactory(formObj);
    }

    var type;
    this.name = formObj.name;
    this.id = formObj.id;
    this.data = formObj.data;

    switch (formObj.type) {
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
  }

  FormComponentFactory.prototype.getFormContentLi = function() {
    // 使用jquery字符串构造功能生成jquery对象，取第0个为js原生对象。
    var li = $(`<li class="list-group-item">
                  <label for="${this.id}" class="label-name">${this.name}</label>
                  <input id="${this.id}" type="${this.type}" name="${this.id}"  placeholder="${this.name}" class="myInput">
                </li>`)[0];

    switch (this.type) {
      case "radio":
        li.removeChild(li.lastElementChild);

        var label;
        var arr= ['true', 'false']
        arr.forEach((ele) => {
          // 同上 取js原生对象
          label = $(`<label class="label-item">
                       <input type="radio" name="${this.id}" value="${ele}">${ele}
                     </label>`)[0];
          li.appendChild(label);
        });

        break;
      case "checkbox":
        li.removeChild(li.lastElementChild);
        var label;

        this.data.forEach((ele) => {
          // 同上 去js原生对象
          label = $(`<label class="label-item">
                         <input type="checkbox" name="${this.id}" value="${ele.id}">${ele.name}
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

    this.data.forEach(function(ele) {
      var formComponent = new FormComponentFactory(ele);
      formContent.appendChild(formComponent.getFormContentLi());
    });
  }

  return {
    loadFormData: loadFormData,
    data:data,
  }
}();

$(() => {
  report.loadFormData();
});