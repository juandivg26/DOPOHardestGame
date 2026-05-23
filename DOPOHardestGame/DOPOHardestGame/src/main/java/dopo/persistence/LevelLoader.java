package dopo.persistence;

import dopo.domain.board.LevelConfig;
import dopo.domain.board.SafeZone;
import dopo.domain.elements.*;
import dopo.domain.enemies.*;
import dopo.domain.players.Player;
import dopo.exceptions.GameException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a .txt level configuration file into a LevelConfig object.
 *
 * FILE FORMAT:
 * Lines starting with # are comments and ignored.
 * Keywords are case-insensitive.
 *
 * LEVEL name
 * SIZE width height
 * TIME seconds
 * START_ZONE x y width height
 * FINAL_ZONE x y width height
 * [INTERMEDIATE_ZONE x y width height]
 * P1_START x y
 * P2_START x y
 * WALL x y width height
 * YELLOW_COIN x y
 * SKIN_COIN x y RED|BLUE|GREEN
 * BASIC_ENEMY x y H|V minBound maxBound speed
 * PATROL_ENEMY x y x1 y1 x2 y2 ... speed   (waypoints then speed at end)
 * FAST_ENEMY x y H|V minBound maxBound speed
 * LIFE_SOURCE x y
 * BOMB x y
 */
public class LevelLoader {

    public static LevelConfig load(String filePath) throws GameException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            LevelConfig config = null;
            String line;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                String keyword = parts[0].toUpperCase();

                switch (keyword) {
                    case "LEVEL":
                        // parsed lazily; name is parts[1..] joined
                        break;
                    case "SIZE":
                        // will set on config creation
                        break;
                    case "TIME":
                        // will set on config creation
                        break;
                    default:
                        break;
                }
            }

            // Second pass for simplicity
            return parseFile(filePath);

        } catch (IOException e) {
            throw new GameException("Cannot read config file: " + filePath, e);
        }
    }

    private static LevelConfig parseFile(String filePath) throws GameException {
        List<String[]> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#"))
                    lines.add(line.split("\\s+"));
            }
        } catch (IOException e) {
            throw new GameException("Cannot read config file: " + filePath, e);
        }

        String name = "Level";
        int w = 800, h = 500, time = 60;

        for (String[] p : lines) {
            switch (p[0].toUpperCase()) {
                case "LEVEL": name = p.length > 1 ? p[1] : "Level"; break;
                case "SIZE":  w = Integer.parseInt(p[1]); h = Integer.parseInt(p[2]); break;
                case "TIME":  time = Integer.parseInt(p[1]); break;
            }
        }

        LevelConfig config = new LevelConfig(name, w, h, time);

        for (String[] p : lines) {
            try {
                switch (p[0].toUpperCase()) {
                    case "START_ZONE":
                        config.setStartZone(new SafeZone(i(p,1),i(p,2),i(p,3),i(p,4), SafeZone.ZoneType.START));
                        break;
                    case "FINAL_ZONE":
                        config.setFinalZone(new SafeZone(i(p,1),i(p,2),i(p,3),i(p,4), SafeZone.ZoneType.FINAL));
                        break;
                    case "INTERMEDIATE_ZONE":
                        config.setIntermediateZone(new SafeZone(i(p,1),i(p,2),i(p,3),i(p,4), SafeZone.ZoneType.INTERMEDIATE));
                        break;
                    case "P1_START":
                        config.setP1Start(i(p,1), i(p,2)); break;
                    case "P2_START":
                        config.setP2Start(i(p,1), i(p,2)); break;
                    case "WALL":
                        config.addWall(new Wall(i(p,1),i(p,2),i(p,3),i(p,4))); break;
                    case "YELLOW_COIN":
                        config.addCoin(new YellowCoin(i(p,1),i(p,2))); break;
                    case "SKIN_COIN":
                        Player.PlayerType skin = Player.PlayerType.valueOf(p[3].toUpperCase());
                        config.addCoin(new SkinCoin(i(p,1),i(p,2),skin)); break;
                    case "BASIC_ENEMY":
                        boolean hB = p[3].equalsIgnoreCase("H");
                        config.addEnemy(new BasicBlueEnemy(i(p,1),i(p,2),hB,i(p,4),i(p,5),d(p,6))); break;
                    case "FAST_ENEMY":
                        boolean hF = p[3].equalsIgnoreCase("H");
                        config.addEnemy(new FastBlueEnemy(i(p,1),i(p,2),hF,i(p,4),i(p,5),d(p,6))); break;
                    case "PATROL_ENEMY":
                        // format: PATROL_ENEMY x y x1 y1 x2 y2 ... speed
                        double spd = Double.parseDouble(p[p.length - 1]);
                        int numWP = (p.length - 3 - 1) / 2;
                        int[][] wps = new int[numWP][2];
                        for (int k = 0; k < numWP; k++) {
                            wps[k][0] = Integer.parseInt(p[3 + k * 2]);
                            wps[k][1] = Integer.parseInt(p[4 + k * 2]);
                        }
                        config.addEnemy(new PatrolBlueEnemy(i(p,1),i(p,2),wps,spd)); break;
                    case "LIFE_SOURCE":
                        config.addLifeSource(new LifeSource(i(p,1),i(p,2))); break;
                    case "BOMB":
                        config.addBomb(new Bomb(i(p,1),i(p,2))); break;
                }
            } catch (Exception e) {
                throw new GameException("Error parsing line: " + String.join(" ", p) + " → " + e.getMessage(), e);
            }
        }

        // Default P1/P2 positions from zones if not set
        if (config.getStartZone() != null && config.getP1StartX() == 0 && config.getP1StartY() == 0) {
            config.setP1Start(config.getStartZone().getCenterX(), config.getStartZone().getCenterY());
        }
        if (config.getFinalZone() != null && config.getP2StartX() == 0 && config.getP2StartY() == 0) {
            config.setP2Start(config.getFinalZone().getCenterX(), config.getFinalZone().getCenterY());
        }

        return config;
    }

    private static int i(String[] p, int idx) { return Integer.parseInt(p[idx]); }
    private static double d(String[] p, int idx) { return Double.parseDouble(p[idx]); }
}
