# Design Review – KnightPathJR

## 1. Project and Structure

I developed a standalone Java application that can be fully executed via Docker.  
The main class (`KnightPathJR`) includes a `public static void main(String[] args)` method and a static nested class `Position` to represent the knight’s coordinates.

The goal is to ensure maximum portability and avoid local environment dependencies.

- Use of `Dockerfile` and `build.sh` for automated compilation
- Clean project structure: `src/`, `lib/`, `out/`
- Includes `README.md` and `.gitignore` for documentation and Git management

---
## 2. JSON Parsing and Configuration

- Used the `org.json` library (via JAR) for simplicity and lightness
- Board and command URLs are passed as environment variables
- Safe parsing and logging implemented to track the variables

---
## 3. Command Logic

### Board Parsing
- The board JSON contains an array of obstacles
- Every cell of the board is represented by a nested class Position which has only x,y coordinates as parameters.
- Position also handles movement with a dedicated method which updates the coordinates after every command and a method validating the position.
- The coordinates are centered in 0,0, (bottom left) so the position with coordinates 0,1 is corresponds to the cell where in the first column from left and the second row from bottom encounter. 
- Obstacles are stored in a `Set<Position>` to:
  - Avoid duplicates
  - Enable `O(1)` lookup
  - Handle unordered data
- `Position` overrides `equals()` and `hashCode()` to work properly within the `Set`

### Command Parsing
- Commands are read from a `JSONArray`, treated as an indexed list
- I chose not to use Streams to keep the `for` loop logic more readable and controllable

### Command Handling
- Parsing relies on `command.startsWith()` since there are only three command types: `START`, `ROTATE`, `MOVE`. No dedicated ENUM.
- Each command has a dedicated handler method:
  - `handleStart` : sets and validates the starting position. If the position is not valid execution ends with status: INVALID_START_POSITION**
  - `handleRotate` : updates the movement direction
  - `handleMove` : moves the knight step-by-step. This is necessary because a destination may be valid, but an obstacle could have been crossed.
    - If an obstacle is encountered, the knight stops before it
    - If the knight exits the board, execution ends with status OUT_OF_THE_BOARD  

---
## 4. Robustness and Logging

- Every step is logged to the console: URL reading, parsing, rotations, movements
- Logging is helpful even in non-interactive environments like Docker

---
## 5. Testing – Strategy and Priorities

Although full testing is not required, here is how I would approach it effectively:

###  Priority: **Command Logic**
1. `handleStart`
   -  Valid position : should start
   -  On obstacle or out-of-board : return `INVALID_START_POSITION`

2. `handleMove`
   -  Valid movement within board
   -  Movement that exits the board : `OUT_OF_THE_BOARD`
   -  Obstacle encountered mid-move : stop before

3. `handleRotate`
   - Should correctly update direction

---
###  Suggested Techniques

#### 1. **Unit Tests** with JUnit
- Isolate and test `handleStart`, `handleMove`, `Position.move()` with controlled input

#### 2. **Mocked JSON Input**
- Use hardcoded JSON data to avoid real HTTP calls

#### 3. **End-to-End (E2E) Testing via Docker**
- Verify the final output JSON through `docker run`
- Inspect `stdout` for result and logs

---
###  Minimal Test Cases

| Case                          | Expected Result                  |
|-------------------------------|----------------------------------|
| START on obstacle             | `INVALID_START_POSITION`         |
| MOVE off the board            | `OUT_OF_THE_BOARD`               |
| MOVE hitting obstacle         | Stops before, continues          |
| Successful command sequence   | `status: SUCCESS`                |
| Missing or invalid URL        | `GENERIC_ERROR`                  |

---
###  Conclusion

> I prioritize testing the core logic and edge cases,  
> avoiding over-testing well-defined static structures or parsing.


##### XX ## 
