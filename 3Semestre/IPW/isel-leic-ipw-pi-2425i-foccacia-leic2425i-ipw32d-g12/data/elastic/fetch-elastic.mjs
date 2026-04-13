const URI_PREFIX='http://localhost:9200';

export async function fetchElastic(method, path, body=undefined){
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    };

    const response = await fetch(URI_PREFIX + path, options);
    return await response.json();
}

