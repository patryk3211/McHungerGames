import {
    call_api,
    session_id,
    set_error_handler,
    set_success_handler,
    process_api_buttons,
    start_websocket,
    send_websocket,
    add_websocket_handler
} from './api.js';

async function check_session() {
    var sid = session_id();
    if(sid === null) {
        location.replace('/login/');
        return false;
    }
    var response = await call_api('check', { sid: sid });
    if(response.status != 200) {
        location.replace('/login/');
        return false;
    }
    var json = JSON.parse(await response.text());
    if(!json.status) {
        location.replace('/login/');
        return false;
    }
    return true;
}

function add_message(type, msg) {
    var listRoot = document.querySelector('#messagelist');
    var templateCopy = listRoot.querySelector('.item-' + type).content.cloneNode(true);
    templateCopy.querySelector('.msg').textContent = msg;
    templateCopy.querySelector('.close').addEventListener('click', event => {
        event.target.parentElement.remove();
    });
    listRoot.querySelector('ul').appendChild(templateCopy);
}

set_error_handler(msg => add_message('error', msg));
set_success_handler(msg => add_message('success', msg));

function add_user(userdata) {
    var templateCopy = document.querySelector('#playertable-entry').content.cloneNode(true);
    for(var i = 0; i < templateCopy.children.length; ++i) {
        var el = templateCopy.children.item(i);
        el.innerHTML = el.innerHTML.replaceAll(/@(\w*)@/g, (m, key) => userdata.hasOwnProperty(key) ? userdata[key] : '');
    }
    process_api_buttons(templateCopy);
    var ret = templateCopy.children[0];
    document.querySelector('#playertable tbody').appendChild(templateCopy);
    return ret;
}

var user_list = [];
add_websocket_handler('players', json => {
    var tbody = document.querySelector('#playertable tbody');
    tbody.replaceChildren([]);
    user_list = [];

    var players = json.players.sort((a, b) => a.name > b.name);
    players.forEach(player => {
        var element = add_user({ playername: player.name, playerstatus: player.state });
        user_list.push({ name: player.name, state: player.state, element: element });
    });
});

add_websocket_handler('tracked', json => {
    var handled = false;
    var userdata = { playername: json.name, playerstatus: json.state };
    for(var i = 0; i < user_list.length; ++i) {
        if(user_list[i].name == json.name) {
            // Found the user
            user_list[i].state = json.state;
            user_list[i].element.querySelector('.status').textContent = json.state;
            handled = true;
            break;
        } else if(user_list[i].name < json.name) {
            continue;
        } else {
            // Names are bigger but not equal, insert a new user here
            var templateCopy = document.querySelector('#playertable-entry').content.cloneNode(true);
            for(var i = 0; i < templateCopy.children.length; ++i) {
                var el = templateCopy.children.item(i);
                el.innerHTML = el.innerHTML.replaceAll(/@(\w*)@/g, (m, key) => userdata.hasOwnProperty(key) ? userdata[key] : '');
            }
            process_api_buttons(templateCopy);
            var element = templateCopy.children[0];
            user_list[i].element.before(templateCopy);
            user_list.splice(i, 0, { name: json.name, state: json.state, element: element });
            handled = true;
            break;
        }
    }
    if(!handled) {
        var templateCopy = document.querySelector('#playertable-entry').content.cloneNode(true);
        for(var i = 0; i < templateCopy.children.length; ++i) {
            var el = templateCopy.children.item(i);
            el.innerHTML = el.innerHTML.replaceAll(/@(\w*)@/g, (m, key) => userdata.hasOwnProperty(key) ? userdata[key] : '');
        }
        process_api_buttons(templateCopy);
        var element = templateCopy.children[0];
        document.querySelector('#playertable tbody').appendChild(templateCopy);
        user_list.push({ name: json.name, state: json.state, element: element });
    }
});

add_websocket_handler('count', json => {
    document.querySelector('#playerleftcount').textContent = json.remaining;
    document.querySelector('#playertotalcount').textContent = json.online;
});

document.addEventListener('DOMContentLoaded', async () => {
    if(await check_session()) {
        // Sesja nie wygasła
        await start_websocket();
        send_websocket('players');
        send_websocket('subscribe', { channel: 'tracked' });
        send_websocket('subscribe', { channel: 'count' });
    }
})