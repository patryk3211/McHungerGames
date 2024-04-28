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

var WEBSOCKET_HANDLERS = {
    error: json => error_handler(json.msg),
};

var WEBSOCKET = null;
function start_websocket() {
    return new Promise((then, error) => {
        WEBSOCKET = new WebSocket("/api/ws");
        WEBSOCKET.addEventListener('open', () => {
            WEBSOCKET.send(JSON.stringify({ type: 'auth', sid: session_id() }));
            setInterval(() => {
                WEBSOCKET.send(JSON.stringify({ type: 'keep-alive' }));
            }, 9000);
            then();
        });
        WEBSOCKET.addEventListener('error', event => console.log(event));
        WEBSOCKET.addEventListener('message', event => {
            var json = JSON.parse(event.data);
            var handler = WEBSOCKET_HANDLERS[json.type];
            if(handler)
                handler(json);
        });
    });
}

function send_websocket(type, data) {
    var request = { type: type };
    if(data !== undefined && data !== null) {
        for(var prop in data) {
            request[prop] = data[prop];
        }
    }
    WEBSOCKET.send(JSON.stringify(request));
}

function websocket_subscribe(channel) {
    send_websocket('subscribe', { channel: channel });
}

function add_websocket_handler(type, handler) {
    WEBSOCKET_HANDLERS[type] = handler;
}

var error_handler = default_error_handler;
function default_error_handler(message) {
    console.error(message);
}

function set_error_handler(handler) {
    error_handler = handler;
}

var success_handler = default_success_handler;
function default_success_handler(message) {
    console.log(message);
}

function set_success_handler(handler) {
    success_handler = handler;
}

async function button_api_call(event) {
    var btn = event.target;
    var api_endpoint = btn.dataset['apicall'];
    var args = { sid: session_id() };
    for(var prop in btn.dataset) {
        var matched = prop.match(/apicall(\w+)/);
        if(!matched)
            continue;
        args[matched[1].toLocaleLowerCase()] = btn.dataset[prop];
    }

    var response = await call_api(api_endpoint, args);
    if(response.status == 200) {
        if(btn.dataset['success'])
            success_handler(btn.dataset['success']);
    } else {
        if(response.headers.get('content-type').split(';')[0] == 'application/json') {
            var json = JSON.parse(await response.text());
            if(json !== undefined && json.msg !== undefined) {
                error_handler(json.msg);
                return;
            }
        }
        error_handler("Błąd serwera: " + response.status);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    process_api_buttons(document);
});

function process_api_buttons(element) {
    element.querySelectorAll('button').forEach(btn => {
        var call = btn.dataset['apicall'];
        if(call !== undefined) {
            btn.addEventListener('click', button_api_call);
        }
    });
}

export {
    call_api,
    session_id,
    set_error_handler,
    set_success_handler,
    process_api_buttons,
    start_websocket,
    send_websocket,
    add_websocket_handler,
    websocket_subscribe,
    SESSION_ID_KEY
};
