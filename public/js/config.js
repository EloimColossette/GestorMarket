const API_BASE_URL = "http://192.168.0.20:8080";

(function () {
    const originalFetch = window.fetch;

    window.fetch = async function (...args) {
        const response = await originalFetch(...args);

        const refreshedToken = response.headers.get("X-Refreshed-Token");
        if (refreshedToken) {
            localStorage.setItem("token", refreshedToken);
        }

        return response;
    };
})();