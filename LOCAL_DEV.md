# Local Development

Use these commands from the project root:

```bash
cd /Users/mac/jshERP
scripts/start-local.sh
```

The script starts:

- backend: `http://localhost:9999/jshERP-boot`
- frontend: `http://localhost:3000/`
- mini program default API: `http://localhost:9999/jshERP-boot`

To stop both services:

```bash
cd /Users/mac/jshERP
scripts/stop-local.sh
```

Logs are written to:

- `backend.log`
- `frontend.log`

If the backend jar is missing, build it first:

```bash
cd /Users/mac/jshERP/jshERP-boot
mvn package -DskipTests
```

The backend expects local MySQL on port `3307` and Redis on port `6379`, as configured in `jshERP-boot/src/main/resources/application.yml`.

For WeChat mini program真机调试, change the login page service URL from `localhost` to the computer LAN IP, for example:

```text
http://192.168.3.42:9999/jshERP-boot
```
