# octoprint-proxy-dashboard

Proxy server and UI for multiple Octoprints, with webcam snapshots.

Made for MakeHaven, allowing users to keep watch on their 3d prints from outside the network without the risk of exposing individual Octaprint servers to the internet.

Written in Clojure and Javascript.

Three services:
* **api (clojure)** for each configured Octoprint, polls `cam` server on interval if available (for webcam snapshots), and subscribes to the Octoprint Push Update servers, broadcasting all relevant changes to clients via its own websocket server.
* **cam (clojure)** simple webcam service to be installed on 3d printer pis alongside octoprint. On command, it requests a frame from the webcam (using FFmpeg), and returns a base64-encoded representation of the image.
* **front-end (javascript)** simple react front-end that connects to `api` via websocket and receives updates on individual octoprint/`cam` servers, reflecting changes immediately to the ui.

## Running With Docker

All of the Dockerfiles are compatible with `arm32/v7` (Raspberry Pi).

### `api`

Bind the directory containing your config file as a Docker volume, then point the api server to that config file (in the container). Run with `--help` to see options.

```sh
cd api
docker build -t api .
docker run --rm -it --network host -v $HOME:/configs api --port 8080 --config /configs/octoprint-api-config.json
```

### `cam`

Bind host DEVICE to container DEVICE, then supply DEVICE name to server. Run with `--help` to see options.

```sh
cd cam
docker build -t cam .
DEVICE=/dev/video0 eval 'docker run --rm -it -p 8020:8020 --device $DEVICE:$DEVICE cam --device $DEVICE'
```

### `front-end`

You must supply the Docker build argument `api_base_url`, which is inlined into the source and points to `api`. Nginx configuration is avaiable in `nginx.conf`.

```sh
cd front-end
docker build --build-arg api_base_url=http://192.168.1.10:8080 -t front-end .
docker run --rm -it -p 80:80 front-end
```

## Screenshot

![octoprint proxy screenshot](screenshot.png)

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request

## Credits

* Andrew Suzuki - @andrewsuzuki - [andrewsuzuki.com](http://andrewsuzuki.com)

## License

MIT
