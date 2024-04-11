function call_api(endpoint, data) {
    var headers = {};
    if(data !== undefined) {
        headers = { "Content-Type": "application/json" };
    }
    return fetch("/api/${endpoint}", {
        method: "POST",
        headers: headers,
        body: data !== undefined ? JSON.stringify(data) : undefined
    })
}

export { call_api };
