var login = {

    init: function() {
        this.initNode();
        this.addEvent();
    },

    initNode: function() {	// 初始化节点
        this.$userId = $('#userId');
        this.$username = $('#username');
        this.$pwd = $('#password');
        this.$tenant = $('#tenant');
        this.$errorMsg = $('#errorMsg');
        this.$submit = $('#submitBtn');
        this.$loginForm = $('#loginForm');
    },

    addEvent: function() {	// 绑定事件
        var that = this;
        this.$submit.on('click', this.validate.bind(this));
        $(document).on('keydown', function(e) {
            var ev = e || window.event;
            if (ev.keyCode === 13) {
                that.validate();
            }
        });
    },

    userIdEncode: function(username, tenant) {
        var result = "";
        if(username != null && tenant != null && username.length > 0 && tenant.length > 0) {
            userIdLen = username.length;
            result = userIdLen + ":" + username;
            result = result + tenant;
        }
        return result;
    },

    validate: function() {
        this.$errorMsg.addClass('hide');
        var that = this,
            username = $.trim(this.$username.val()),
            tenant = this.$tenant.val(),
            pwd = this.$pwd.val(),
            errorMsg = '';
        if(tenant.length === 0) {
            errorMsg = '租户不能为空';
        } else if (username.length === 0) {
            errorMsg = '帐号不能为空';
        } else if (pwd.length === 0) {
            errorMsg = '密码不能为空';
        } else {
            this.$userId.val(this.userIdEncode(username, tenant));
            console.log("提交表单!!");
            // 密码提交到数据库之前先进行加密（加盐）
            // bcrypt.genSalt(10, function (err, salt){
            //     bcrypt.hash("B4c0/\/", salt, function(err,hash){
            //         pwd = hash;
            //         next();
            //     })
            // });
            this.$loginForm.submit();
            return;
        }
        this.$errorMsg.html("");  //reset
        this.$errorMsg.html(errorMsg).removeClass('hide');  // 显示错误信息
        return false;
    }
};
login.init();