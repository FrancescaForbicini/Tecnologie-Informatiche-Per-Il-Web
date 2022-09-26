
function makeCall(method, url, formElement, cback, reset = true) {
    var req = new XMLHttpRequest(); // visible by closure
    req.onreadystatechange = function() {
        cback(req)
    }; // closure
    req.open(method, url);
    if (formElement == null) {
        req.send();
    } else {
        let formData = new FormData(formElement);
        req.send(formData);
    }
    if (formElement !== null && reset === true) {
        // formElement.reset();
    }
}

function makeCallJSON(method, url, json, cback, reset = true) {
    var req = new XMLHttpRequest(); // visible by closure
    req.onreadystatechange = function() {
        cback(req)
    }; // closure
    req.open(method, url);
    if (json == null) {
        req.send();
    } else {
        req.send(json);
    }
    if (json !== null && reset === true) {
        // formElement.reset();
    }
}