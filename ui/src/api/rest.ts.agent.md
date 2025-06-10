Wraps the browser `fetch` API for the UI. Functions `get` and `post` prefix
requests with the `VITE_NODE_URL` environment variable so the front-end can
access the node REST API.
