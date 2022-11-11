;(function(window) {
    if (window.WebViewJavascriptBridge) {
        return;
    }
    window.WebViewJavascriptBridge = {
        registerHandler: registerHandler,
        callHandler: callHandler,
        handleMessageFromNative: handleMessageFromNative
    };
    let messageHandlers = {};
    let responseCallbacks = {};
    let uniqueId = 1;
    function registerHandler(handlerName, handler) {
        messageHandlers[handlerName] = handler;
    }
    function callHandler(handlerName, data, responseCallback) {
        if (arguments.length === 2 && typeof data == 'function') {
            responseCallback = data;
            data = null;
        }
        doSend({ handlerName:handlerName, data:data }, responseCallback);
    }
    function doSend(message, responseCallback) {
        if (responseCallback) {
            const callbackId = 'cb_'+(uniqueId++)+'_'+new Date().getTime();
            responseCallbacks[callbackId] = responseCallback;
            message['callbackId'] = callbackId;
        }
        window.normalPipe.postMessage(JSON.stringify(message));
    }
    function handleMessageFromNative(messageJSON) {
        const message = JSON.parse(messageJSON);
        let responseCallback;
        if (message.responseId) {
            /* web  - >  app */
            responseCallback = responseCallbacks[message.responseId];
            if (!responseCallback) {
                return;
            }
            responseCallback(JSON.stringify(message.responseData));
            delete responseCallbacks[message.responseId];
        } else {
           /* app  - >  web */
            if (message.callbackId) {
                const callbackResponseId = message.callbackId;
                responseCallback = function(responseData) {
                   /* 接收 web - > call - > app   */
                    doSend({ handlerName:message.handlerName, responseId:callbackResponseId, responseData:responseData });
                };
            }
            const handler = messageHandlers[message.handlerName];
            if (!handler) {
                console.log("WebViewJavascriptBridge: WARNING: no handler for message from Kotlin:", message);
            } else {
                /*将 app data String - > web */
                handler(JSON.stringify(message.data), responseCallback);
            }
        }
    }
})(window);