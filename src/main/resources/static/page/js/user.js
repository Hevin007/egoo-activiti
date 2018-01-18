/**
 * Created by three on 2017/4/1.
 */
$(document).ready(function(){


    $("#userSubmit").click(function () {
        var userName = $("#inputUsername").val();
        var password = $("#inputPassword").val();
        var role = $("#inputRole").val();

        $.ajax({
            url:'/api/saveUser',
            type:'GET', //GET
            data:{
                user:userName,
                password:password,
                role:role
            },

            dataType:'json',    //返回的数据格式：json/xml/html/script/jsonp/text
            success:function(data,textStatus,jqXHR){
                alert(data.msg);
            },
            error:function(xhr,textStatus){
                alert("保存失败，请稍后重试！");
            }
        });
    });

    $("#roleSubmit").click(function () {
        var roleName=$("#roleName").val();
        var roleURL="",roleMethod="";
        $("[name='roleURL']:checked").each(function(i){
            if(i==0){
                roleURL+=$(this).val();
            }else {
                roleURL+=","+$(this).val();
            }
        });
        $("[name='roleMethod']:checked").each(function(i){
            if(i==0){
                roleMethod+=$(this).val();
            }else {
                roleMethod+=","+$(this).val();
            }
        });
        $.ajax({
            url:'/api/saveRole',
            type:'GET', //GET
            data:{
                role:roleName,
                authUrl:roleURL,
                authMethod:roleMethod
            },

            dataType:'json',    //返回的数据格式：json/xml/html/script/jsonp/text
            success:function(data,textStatus,jqXHR){
                if(data==1){
                    alert("角色保存成功！");
                }else {
                    alert("保存失败，请稍后重试！");
                }
            },
            error:function(xhr,textStatus){
                alert("保存失败，请稍后重试！");
            }
        });
    });
});
