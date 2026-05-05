const API_URL = import.meta.env.VITE_API_URL
    ? `${import.meta.env.VITE_API_URL}/api/v1`
    : 'http://localhost:8080/api/v1';

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

    // Auto-logout on 401
    if (response.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('tokenType');
        localStorage.removeItem('roles');
        window.dispatchEvent(new Event('auth:logout'));
    }

    if (!response.ok) {
        const errorBody = await response.json().catch(() => null);
        const error = new Error(errorBody?.message || `Request failed with status ${response.status}`);
        error.status = response.status;
        error.body = errorBody;
        throw error;
    }

    // Handle 204 No Content
    if (response.status === 204) {
        return { data: null };
    }

    // Handle text responses (e.g. "User registered successfully!")
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return { data: await response.json() };
    }

    return { data: await response.text() };
};

const api = {
    get: (path, options) => request(path, { ...options, method: 'GET' }),
    post: (path, body, options) => request(path, {
        ...options,
        method: 'POST',
        body: JSON.stringify(body),
    }),
    put: (path, body, options) => request(path, {
        ...options,
        method: 'PUT',
        body: JSON.stringify(body),
    }),
    patch: (path, body, options) => request(path, {
        ...options,
        method: 'PATCH',
        body: JSON.stringify(body),
    }),
    delete: (path, options) => request(path, { ...options, method: 'DELETE' }),
};

export default api;
