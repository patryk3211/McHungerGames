import { call_api, SESSION_ID_KEY } from './api.js';

async function api_auth(username, password) {
    var response = await call_api('auth', { user: username, password: password });
    if(response.status == 400) {
        document.querySelector('#loginform-error').textContent = 'Nieprawidłowa nazwa użytkownika lub hasło';
    } else if(response.status == 200) {
        var json = JSON.parse(await response.text());
        sessionStorage.setItem(SESSION_ID_KEY, json.sid);
        location.replace('/panel/');
    } else {
        document.querySelector('#loginform-error').textContent = 'Status serwera: ' + response.status;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.querySelector('#loginform').addEventListener('submit', event => {
        event.preventDefault();
        var form = event.target;
        api_auth(form.username.value, form.password.value);
    });
});
