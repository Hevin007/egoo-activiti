/**
 * 构造第一条消息，显示在最近联系人昵称的下面(移到UI组件去了)
 * @param msg：消息对象
 */
function buildSessionMsg(msg) {
    var text = (msg.scene!='p2p'?((msg.from ===userUID)?"你":getNick(msg.from))+":":""), type = msg.type;
    if (!/text|image|file|audio|video|geo|custom|tip|notification/i.test(type)) return '';
    switch (type) {
        case 'text':
            text += _$escape(msg.text);
            text = buildEmoji(text);
            break;
        case 'image':
            text += '[图片]';
            break;
        case 'file':
            if (!/exe|bat/i.test(msg.file.ext)) {
                text += '[文件]';
            } else {
                text += '[非法文件，已被本站拦截]';
            }
            break;
        case 'audio':
            text += '[语音]';
            break;
        case 'video':
            text += '[视频]';
            break;
        case 'geo':
            text += '[位置]';
            break;
        case 'tip':
            text = '[提醒消息]';
            break;
        case 'custom':
            var content = JSON.parse(msg.content);
            if(content.type===1){
                text += '[猜拳]';
            }else if(content.type===2){
                text +='[阅后即焚]';
            }else if(content.type===3){
                text +='[贴图]';
            }else if(content.type===4){
                text +='[白板]';
            }else{
                text += '[自定义消息]';
            }
            break;
        case 'notification':
            text = '['+transNotification(msg)+']';
            break;
        default:
            text += '[未知消息类型]';
            break;
    }
    if(msg.status=== "fail"){
        text = '<i class="icon icon-error"></i>'+text;
    }
    return text;
}

/**
 * 会话列表消息
 * @param  {object} msg 消息
 * @return {string} str
 */
function getMessage(msg) {
    var str = '',
        url = msg.file ? _$escape(msg.file.url) : '',
        sentStr = (msg.from!==userUID)?"收到":"发送";
    switch (msg.type) {
        case 'text':
            var re = /(http:\/\/[\w.\/]+)(?![^<]+>)/gi; // 识别链接
            str = _$escape(msg.text);
            str = str.replace(re, "<a href='$1' target='_blank'>$1</a>");

            str = buildEmoji(str);
            str ="<div class='f-maxWid'>"+str+"</div>"
            break;
        case 'image':
            if(msg.status === -1){
                str = '<p>['+msg.message.message+']</p>';
            }else{
                msg.file.url = _$escape(msg.file.url);
                str = '<a href="' + msg.file.url + '?imageView" target="_blank"><img onload="loadImg()" data-src="' + msg.file.url + '" src="' + msg.file.url + '?imageView&thumbnail=200x0&quality=85"/></a>';
            }
            break;
        case 'file':
            if(msg.status === -1){
                str = '<p>['+msg.message.message+']</p>';
            }else{
                if (/png|jpg|bmp|jpeg|gif/i.test(msg.file.ext)) {
                    msg.file.url = _$escape(msg.file.url);
                    str = '<a class="f-maxWid" href="' + msg.file.url + '?imageView" target="_blank"><img data-src="' + msg.file.url + '" src="' + msg.file.url + '?imageView&thumbnail=200x0&quality=85"/></a>';
                } else if (!/exe|bat/i.test(msg.file.ext)) {
                    url += msg.file ? '?download=' + encodeURI(_$escape(msg.file.name)): '';
                    str = '<a href="' + url + '" target="_blank" class="download-file f-maxWid"><span class="icon icon-file2"></span>' +_$escape(msg.file.name) + '</a>';
                } else {
                    str = '<p>[非法文件，已被本站拦截]</p>';
                }
            }
            break;
        case 'tip':
            str = msg.tip;
            break;
        case 'video':
            // str = '<a href="' + url + '" target="_blank" class="download-file"><span class="icon icon-file2"></span>[你收到了一条视频消息]</a>';
            str= '<video src= "'+url+'" controls>您的浏览器不支持 video 标签。</video>';

            break;
        case 'audio':
            if(!!window.Audio){
                if(msg.from === userUID&&msg.from!==msg.to){
                    str ='<div class="u-audio j-mbox right"> <a href="javascript:;" class="j-play playAudio" data-dur="'+msg.file.dur+'"  data-src="'+ url+'">点击播放</a><b class="j-duration">'+Math.floor((msg.file.dur)/1000)+'"</b><span class="u-icn u-icn-play" title="播放音频"></span></div>'
                }else{
                    str ='<div class="u-audio j-mbox left"> <a href="javascript:;" class="j-play playAudio" data-dur="'+msg.file.dur+'"  data-src="'+ url+'">点击播放</a><b class="j-duration">'+Math.floor((msg.file.dur)/1000)+'"</b><span class="u-icn u-icn-play" title="播放音频"></span></div>'
                }
            }else{
                str = '<a href="' + url + '" target="_blank" class="download-file"><span class="icon icon-file2"></span>['+sentStr+'一条语音消息]</a>';
            }
            break;
        case 'geo':
            str = sentStr+'一条[地理位置]消息';
            break;
        case 'custom':
            var content = JSON.parse(msg.content);
            if(content.type===1){
                str = sentStr+'一条[猜拳]消息,请到手机或电脑客户端查看';
            }else if(content.type===2){
                str = sentStr+'一条[阅后即焚]消息,请到手机或电脑客户端查看';
            }else if(content.type===3){
                var catalog = _$escape(content.data.catalog),
                    chartlet = _$escape(content.data.chartlet);
                str = '<img class="chartlet" onload="loadImg()" src="./images/'+catalog+'/' +chartlet+'.png">';
            }else if(content.type==4){
                str = sentStr+'一条[白板]消息,请到手机或电脑客户端查看';
            }else{
                str = sentStr+'一条[自定义]消息，请到手机或电脑客户端查看';
            }
            break;
        default:
            str = sentStr+'一条[未知消息类型]消息';
            break;
    }
    return str;
}
/**
 * 时间戳转化为日期（用于消息列表）
 * @return {string} 转化后的日期
 */
var transTime = (function(){
    var getDayPoint = function(time){
        time.setMinutes(0);
        time.setSeconds(0);
        time.setMilliseconds(0);
        time.setHours(0);
        var today = time.getTime();
        time.setMonth(1);
        time.setDate(1);
        var yearDay = time.getTime();
        return [today,yearDay];
    }
    return function(time){
        var check = getDayPoint(new Date());
        if (time>=check[0]){
            return dateFormat(time,"HH:mm")
        }else if(time<check[0]&&time>=check[1]){
            return dateFormat(time,"MM-dd HH:mm")
        }else{
            return dateFormat(time,"yyyy-MM-dd HH:mm")
        }
    }
})();
/**
 * 时间戳转化为日期(用于左边会话面板)
 * @return {string} 转化后的日期
 */
var transTime2 = (function(){
    var getDayPoint = function(time){
        time.setMinutes(0);
        time.setSeconds(0);
        time.setMilliseconds(0);
        time.setHours(0);
        var today = time.getTime();
        time.setMonth(1);
        time.setDate(1);
        var yearDay = time.getTime();
        return [today,yearDay];
    }
    return function(time){
        var check = getDayPoint(new Date());
        if (time>=check[0]){
            return dateFormat(time,"HH:mm")
        }else if(time>=check[0]-60*1000*60*24){
            return "昨天";
        }else if(time>=(check[0]-2*60*1000*60*24)){
            return "前天";
        }else if(time>=(check[0]-7*60*1000*60*24)){
            return "星期"+dateFormat(time,"w");
        }else if(time>=check[1]){
            return dateFormat(time,"MM-dd")
        }else{
            return dateFormat(time,"yyyy-MM-dd")
        }
    }
})();

/**
 * 根据消息的发送人，构造发送方，注意：发送人有可能是自己
 * @param msg：消息对象
 */
function buildSender(msg) {
    var sender = '';
    if (msg.from === msg.to) {
        if (msg.fromClientType==="Web") {
            sender = 'me';
        } else {
            sender= 'you';
        }
    } else {
        if (msg.from === userUID && !msg.fromClientType) {
            sender = 'me';
        } else {
            sender = 'you';
        }
        if (msg.from === userUID && msg.to != userUID) {
            sender = 'me';
        }
    }
    return sender;
}


function transNotification(item) {
    var type = item.attach.type,
        from = (item.from === userUID?true:false),
        str,
        tName,
        accounts,
        member = [];

    //从消息item拿得到team信息就从那边拿,msg那拿不到就本地拿
    //这冗余代码就是为了处理群通知的文案高级群叫群，讨论组叫讨论组
    var team = item.attach && item.attach.team;
    if (!team) {
        team = yunXin.cache.getTeamMapById(item.target);
    }else{
        if(!team.type){
            team = yunXin.cache.getTeamMapById(item.target);
        }
    }
    if(team&&team.type&&team.type==="normal"){
        tName="讨论组";
    }else{
        tName="群";
    }
    /**--------------------正剧在下面------------------------*/
    switch (type) {
        case 'addTeamMembers':
            accounts = item.attach.accounts;
            for(var i = 0;i<accounts.length;i++){
                if(accounts[i]===userUID){
                    member.push("你");
                }else{
                    member.push(getNick(accounts[i]));
                }

            }
            member =  member.join(",");
            str = from?"你将"+member+"加入"+tName:member+"加入"+tName;
            return str;
            break;
        case 'removeTeamMembers':
            accounts = item.attach.accounts;
            for(var i = 0;i<accounts.length;i++){
                if(accounts[i]===userUID){
                    member.push("你");
                }else{
                    member.push(getNick(accounts[i]));
                }
            }
            member =  member.join(",");
            str = from?("你将"+member+"移除"+tName):(member+"被移除"+tName);
            return str;
            break;
        case 'leaveTeam':
            var member =  (item.from ===userUID)?"你":getNick(item.from);
            str = member+"退出了"+tName;
            return str;
            break;
        case 'updateTeam':
            if(item.attach.team.joinMode){
                switch (item.attach.team.joinMode){
                    case "noVerify":
                        str = "群身份验证模式更新为允许任何人加入";
                        break;
                    case "needVerify":
                        str = "群身份验证模式更新为需要验证消息";
                        break;
                    case "rejectAll":
                        str = "群身份验证模式更新为不允许任何人申请加入";
                        break;
                    default:
                        str = '更新群消息';
                        break;
                }
            }else if(item.attach.team.name){
                var user =  (item.from ===userUID)?"你":getNick(item.from);
                str = user+"更新"+tName+"名称为"+ item.attach.team.name;
            }else if(item.attach.team.intro){
                var user =  (item.from ===userUID)?"你":getNick(item.from);
                str = user+"更新群介绍为"+ item.attach.team.intro;
            }else if(item.attach.team.inviteMode){
                str = item.attach.team.inviteMode==='manager'?'邀请他人权限为管理员':'邀请他人权限为所有人';
            }else if(item.attach.team.beInviteMode){
                str = item.attach.team.beInviteMode==='noVerify'?'被邀请他人权限为不需要验证':'被邀请他人权限为需要验证';
            }else if(item.attach.team.updateTeamMode){
                str = item.attach.team.updateTeamMode==='manager'?'群资料修改权限为管理员':'群资料修改权限为所有人';
            }else if(item.attach.team.avatar){
                var user =  (item.from ===userUID)?"你":getNick(item.from);
                str = user+"更改了群头像";
            }else{
                str = '更新群消息';
            }
            return str;
            break;
        case 'acceptTeamInvite':
            var member,
                admin;
            if(item.from === item.attach.account){
                member = (item.from ===userUID)?"你":getNick(item.from);
                str = member?member:item.from+"加入了群";
            }else{
                admin = (item.attach.account===userUID)?"你":getNick(item.attach.account);
                member =  (item.from ===userUID)?"你":getNick(item.from);
                str = member +'接受了'+admin+"的入群邀请";
            }
            return str;
            break;
        case 'passTeamApply':
            var member,
                admin;
            if(item.from === item.attach.account){
                member = (item.from ===userUID)?"你":getNick(item.from);
                str = member+"加入了群";
            }else{
                member = (item.attach.account===userUID)?"你":getNick(item.attach.account);
                admin =  (item.from ===userUID)?"你":getNick(item.from);
                str = admin +'通过了'+member +"的入群申请";
            }
            return str;
            break;
        case 'dismissTeam':
            var member =  (item.from ===userUID)?"你":getNick(item.from);
            str = member+"解散了群";
            return str;
            break;
        case 'updateTeamMute':
            var account = item.attach.account,
                name;
            if(account===userUID){
                name = '你';
            }else{
                name = getNick(account);
            }
            str = name+'被'+((item.from ===userUID)?'你':'管理员')+(item.attach.mute?'禁言':'解除禁言');
            return str;
            break;
        default:
            return '通知消息';
            break;

    }
}

/**
 * 移除定位会话圆点
 */

function removeChatVernier(account) {
    if (account == $('li.active').attr('data-account')) {
        $('#chatVernier span').css('top', '-20px');
    }
}

function loadImg() {
    $('#chatContent').scrollTop(99999);
}

function getAvatar(url){
    var re=/^((http|https|ftp):\/\/)?(\w(\:\w)?@)?([0-9a-z_-]+\.)*?([a-z0-9-]+\.[a-z]{2,6}(\.[a-z]{2})?(\:[0-9]{2,6})?)((\/[^?#<>\/\\*":]*)+(\?[^#]*)?(#.*)?)?$/i;
    if(re.test(url)){
        return url+"?imageView&thumbnail=80x80&quality=85";
    }else{
        return "images/default-icon.png"
    }
}

//或者备注名或者昵称
function getNick(account,cache){
    cache = cache||yunXin.cache;
    return cache.getFriendAlias(account)||(cache.getUserById(account)?cache.getUserById(account).nick:account);
}
//拿所有消息中涉及到的账号（为了正确显示昵称=。=）
function getAllAccount(obj) {
    if(!obj) {
        return;
    }
    if(!Array.isArray(obj)){
        obj = [obj]
    }
    var array = [];
    for (var i = obj.length - 1; i >= 0; i--) {
        array.push(obj[i].from);
        if(obj[i].attach){
            if(obj[i].attach.accounts){
                for (var j = obj[i].attach.accounts.length - 1; j >= 0; j--) {
                    array.push(obj[i].attach.accounts[j]);
                };
            }
        }
    };
    return array;
}