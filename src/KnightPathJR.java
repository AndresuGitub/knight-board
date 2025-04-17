import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.stream.*;
import org.json.*;

public class KnightPathJR {

    static class Position {
        int x, y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Position move(String direction) {
            return switch (direction) {
                case "NORTH" -> new Position(x, y + 1);
                case "SOUTH" -> new Position(x, y - 1);
                case "EAST" -> new Position(x + 1, y);
                case "WEST" -> new Position(x - 1, y);
                default -> throw new IllegalArgumentException("Invalid direction: " + direction);
            };
        }

        boolean isOutOfBounds(int width, int height) {
            return x < 0 || x >= width || y < 0 || y >= height;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Position p)) return false;
            return this.x == p.x && this.y == p.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    static JSONObject output = new JSONObject();
    static Position pos = null;
    static String direction = "";
    static boolean isStartPosValid = false;
    static int width, height;
    static Set<Position> obstacles = new HashSet<>();

    public static void main(String[] args) {
        try {
            String boardUrl = System.getenv("BOARD_API");
            String commandsUrl = System.getenv("COMMANDS_API");

            System.out.println("Fetching board from: " + boardUrl);
            System.out.println("Fetching commands from: " + commandsUrl);

            JSONObject board = fetchJson(boardUrl);
            JSONObject commandsObj = fetchJson(commandsUrl);
            JSONArray commandsArray = commandsObj.getJSONArray("commands");
            JSONArray obstaclesArray = commandsObj.getJSONArray("obstacles");

            width = board.getInt("width");
            height = board.getInt("height");

            IntStream.range(0, obstaclesArray.length())
                .mapToObj(obstaclesArray::getJSONObject)
                .map(ob -> new Position(ob.getInt("x"), ob.getInt("y")))
                .forEach(obstacles::add);

            for (int i = 0; i < commandsArray.length(); i++) {
                String command = obstaclesArray.getString(i);
                System.out.println("COMMAND: " + command);
                if (command.startsWith("START")) {
                    if (!handleStart(command)) return;
                } else if (command.startsWith("ROTATE")) {
                    handleRotate(command);
                } else if (command.startsWith("MOVE")) {
                    if (!handleMove(command)) return;
                }
            }

            if (!isStartPosValid) {
                output.put("status", "INVALID_START_POSITION");
            } else {
                JSONObject position = new JSONObject();
                position.put("x", pos.x);
                position.put("y", pos.y);
                position.put("direction", direction);
                output.put("position", position);
                output.put("status", "SUCCESS");
                System.out.printf("Final position: (%d,%d) facing %s%n", pos.x, pos.y, direction);
            }

        } catch (Exception e) {
            output = new JSONObject();
            output.put("status", "GENERIC_ERROR");
            System.out.println("Exception IN MAIN METHOD: " + e.getMessage());
        }

        System.out.println(output.toString());
    }

    private static boolean handleStart(String command) {
        String[] parts = command.split(" ");
        String[] coordinates = parts[1].split(",");
        int x = Integer.parseInt(coordinates[0]);
        int y = Integer.parseInt(coordinates[1]);
        direction = coordinates[2];
        pos = new Position(x, y);
        isStartPosValid = true;
        System.out.printf("Starting at (%d,%d) facing %s%n", x, y, direction);
        if (pos.isOutOfBounds(width, height) || obstacles.contains(pos)) {
            output.put("status", "INVALID_START_POSITION");
            System.out.println("Start position is invalid.");
            System.out.println(output.toString());
            return false;
        }
        return true;
    }

    private static void handleRotate(String command) {
        direction = command.split(" ")[1];
        System.out.println("Rotated to face " + direction);
    }

    private static boolean handleMove(String command) {
        int steps = Integer.parseInt(command.split(" ")[1]);
        for (int s = 0; s < steps; s++) {
            Position next = pos.move(direction);
            System.out.printf("Trying step to (%d,%d)%n", next.x, next.y);
            if (next.isOutOfBounds(width, height)) {
                output.put("status", "OUT_OF_THE_BOARD");
                System.out.println("OUT_OF_THE_BOARD at: (" + next.x + "," + next.y + ")");
                System.out.println(output.toString());
                return false;
            }
            if (obstacles.contains(next)) {
                System.out.printf("Obstacle at (%d,%d), stopping before entering%n", next.x, next.y);
                break;
            }
            pos = next;
            System.out.printf("Moved to (%d,%d)%n", pos.x, pos.y);
        }
        return true;
    }

    private static URL safeParseUrl(String urlString) {
        try {
            return new URI(urlString).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException("Invalid URL: " + urlString, e);
        }
    }

    private static JSONObject fetchJson(String urlString) throws Exception {
        URL url = safeParseUrl(urlString);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            content.append(inputLine);
        in.close();
        return new JSONObject(content.toString());
    }
}
