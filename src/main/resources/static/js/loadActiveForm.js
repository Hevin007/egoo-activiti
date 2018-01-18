$(function () {

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
        {id: 1, name: "值1"},
        {id: 2, name: "值2"}
      ]
    }
  ];



  function loadFormData(data) {
    var formContent = document.getElementsByClassName("form-content")[0];

    data.forEach(function(ele) {

      var formComponent = new FormComponent(ele);
      formContent.appendChild(formComponent.getFormContentDiv());
    });
  }

  class FormComponent {
    constructor(formObj) {


      var type;
      this.name = formObj.name;
      this.id = formObj.id;
      this.data = formObj.data;

      switch (formObj.type) {
        case "enum":
          type = "checkbox";
          break;
        case "boolean":
          type = "select";
          break;
        case "date":
          type = "date";
          break;
        default:
          type = "text";
      }

      this.type = type;
    }

    getFormContentDiv () {

      var div = document.createElement("div");
      var label = document.createElement("label");
      var span = document.createElement("span");
      div.classList.add("form-group");
      span.innerHTML = this.name + "：";
      label.appendChild(span);
      div.appendChild(label);

      switch (this.type) {
        case "select":
          var select = document.createElement("select");
          select.dataset["id"] = this.type;
          ["真", "假"].forEach(function (ele) {
            var option = document.createElement("option");
            option.innerHTML = ele;
            select.appendChild(option);
          });
          div.appendChild(select);
          break;
        case "checkbox":

          this.data.forEach(function (ele) {
            var newLabel = document.createElement("label");
            newLabel.value = ele.name;

            newLabel.innerHTML = ele.name;
            var checkbox = document.createElement("input");
            checkbox.type = "checkbox";
            checkbox.dataset["id"] = ele.id;
            checkbox.value = ele.name;
            newLabel.appendChild(checkbox);
            div.appendChild(newLabel);
          });
          break;
        default:
          var input = document.createElement("input");

          input.type = this.type;
          input.dataset["id"] = this.id;
          label.appendChild(input);

      }

      return div;

    }


  }

  loadFormData(data);

})