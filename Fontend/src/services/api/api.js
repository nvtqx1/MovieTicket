const API_URL = 'http://localhost:8080/api/v1'; // Thay đổi theo URL Backend

const request = async (path, options = {}) => {
    const token = localStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
    };

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(`${API_URL}${path}`, {
        ...options,
        headers,
    });

    if (!response.ok) {
        const errorBody = await response.json().catch(() => null);
        throw new Error(errorBody?.message || `Request failed with status ${response.status}`);
    }

    if (response.status === 204) {
        return { data: null };
    }

    return { data: await response.json() };
};

const api = {
    get: (path, options) => request(path, { ...options, method: 'GET' }),
    post: (path, body, options) => request(path, {
        ...options,
        method: 'POST',
        body: JSON.stringify(body),
    }),
};

export default api;
