import { call_api, session_id } from './api.js';

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

document.addEventListener('DOMContentLoaded', async () => {
    if(await check_session()) {
        // Sesja nie wygasła
    }
})