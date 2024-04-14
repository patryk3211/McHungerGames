import { call_api, session_id } from './api.js';

function redirect(to) {
    location.replace(to);
    var el = document.createElement('a');
    el.href = to;
    el.textContent = "Problem z przekierowaniem?";
    document.body.appendChild(el);
}

window.addEventListener('load', async () => {
    var sid = session_id();
    if(sid === null) {
        redirect('/login/');
        return;
    }
    var response = await call_api('check', { sid: sid });
    if(response.status != 200) {
        redirect('/login/');
        return;
    }
    var json = JSON.parse(await response.text());
    if(!json.status) {
        redirect('/login/');
        return;
    }
    redirect('/panel/');
});
