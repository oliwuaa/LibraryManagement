const API_URL = 'http://localhost:8080';

export const fetchWithAuth = async (url, options = {}, retry = true) => {
    let accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');

    const headers = {
        ...options.headers,
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
    };

    const response = await fetch(`${API_URL}${url}`, {
        ...options,
        headers,
    });

    if (response.status === 403 && retry && refreshToken) {
        const refreshRes = await fetch(`${API_URL}/auth/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken }),
        });

        if (refreshRes.ok) {
            const data = await refreshRes.json();
            localStorage.setItem('accessToken', data.accessToken);
            return fetchWithAuth(url, options, false);
        } else {
            return { error: true, status: 403, message: 'Session expired, please log in again.' };
        }
    }

    if (!response.ok) {
        return { error: true, status: response.status, message: await response.text() };
    }
    return response;
};