import { call_api, session_id, set_error_handler, set_success_handler, process_api_buttons } from './api.js';

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
    document.querySelector('#playertable tbody').appendChild(templateCopy);
}

document.addEventListener('DOMContentLoaded', async () => {
    if(await check_session()) {
        // Sesja nie wygasła
    }

    add_user({ playername: 'patryk3211', playerstatus: 'Żyje' })
})