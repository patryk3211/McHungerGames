const SESSION_ID_KEY = 'MCHG_SESSION_ID';

function call_api(endpoint, data) {
    var headers = {};
    if(data !== undefined) {
        headers = { "Content-Type": "application/json" };
    }
    return fetch(`/api/${endpoint}`, {
        method: "POST",
        headers: headers,
        body: data !== undefined ? JSON.stringify(data) : undefined
    })
}

function session_id() {
    return sessionStorage.getItem(SESSION_ID_KEY);
}

export { call_api, session_id, SESSION_ID_KEY };
