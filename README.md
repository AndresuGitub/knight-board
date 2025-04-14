# KnightPathJR
A Java project that simulates the movement of a knight on a board with obstacles, fully executable via Docker.
## How to Run

### 1. Build the Docker Image

```bash
docker build -t knight_board:latest .
```

### 2. Run the Docker Container

```bash
docker run -e BOARD_API=https://storage.googleapis.com/jobrapido-backend-test/board.json \
           -e COMMANDS_API=https://storage.googleapis.com/jobrapido-backend-test/commands.json \
           knight_board:latest
```

### Expected Output

```json
{
  "position": { "x": 7, "y": 3, "direction": "NORTH" },
  "status": "SUCCESS"
}
```

---

## Project Structure

```
.
├── src/
│   └── KnightPathJR.java
├── lib/
│   └── json-20210307.jar
├── build.sh
├── Dockerfile
├── .gitignore
└── README.md
```

---

## Requirements

- Java 17+
- Docker

---

## Notes

- The project uses [`json-20210307`](https://repo1.maven.org/maven2/org/json/json/20210307/json-20210307.jar).
- Place the JAR inside the `lib/` folder manually if not included.