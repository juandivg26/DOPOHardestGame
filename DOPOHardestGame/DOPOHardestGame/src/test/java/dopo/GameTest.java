package dopo;

import dopo.domain.board.LevelConfig;
import dopo.domain.board.SafeZone;
import dopo.domain.elements.*;
import dopo.domain.enemies.*;
import dopo.domain.game.GameMode;
import dopo.domain.game.GameState;
import dopo.domain.players.*;
import dopo.exceptions.GameException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias - The DOPO Hardest Game
 * Capa de Dominio - JUnit 4
 */
public class GameTest {

    private LevelConfig basicConfig() {
        LevelConfig cfg = new LevelConfig("test", 800, 500, 60);
        cfg.setStartZone(new SafeZone(0, 200, 80, 100, SafeZone.ZoneType.START));
        cfg.setFinalZone(new SafeZone(720, 200, 80, 100, SafeZone.ZoneType.FINAL));
        cfg.setP1Start(40, 240);
        cfg.setP2Start(740, 240);
        return cfg;
    }

    // ============================================================
    // JUGADORES - RED
    // ============================================================

    @Test
    public void testRedPlayerMuereAlPrimerGolpe() {
        RedPlayer p = new RedPlayer(100, 100);
        assertTrue("El jugador rojo debe morir al primer golpe", p.onHit());
    }

    @Test
    public void testRedPlayerNoTieneEscudo() {
        RedPlayer p = new RedPlayer(100, 100);
        assertFalse("Red no debe iniciar con escudo", p.isShieldActive());
    }

    @Test
    public void testRedPlayerVelocidadBase() {
        RedPlayer p = new RedPlayer(0, 0);
        assertEquals("Red debe tener velocidad base 3.0", 3.0, p.getBaseSpeed(), 0.01);
    }

    @Test
    public void testRedPlayerTamano() {
        RedPlayer p = new RedPlayer(0, 0);
        assertEquals("Red debe tener tamaño 20", 20, p.getSize());
    }

    @Test
    public void testRedPlayerTamanoConTempSize() {
        RedPlayer p = new RedPlayer(0, 0);
        p.setTempSize(35);
        assertEquals("Con tempSize activo debe usarse ese valor", 35, p.getSize());
    }

    // ============================================================
    // JUGADORES - GREEN
    // ============================================================

    @Test
    public void testGreenPlayerAbsorbePrimerGolpe() {
        GreenPlayer p = new GreenPlayer(100, 100);
        assertTrue("Green debe iniciar con escudo", p.isShieldActive());
        boolean muere = p.onHit();
        assertFalse("Green NO debe morir al primer golpe", muere);
        assertFalse("El escudo debe desaparecer tras el golpe", p.isShieldActive());
        assertEquals("La velocidad debe reducirse a 0.7x",
                p.getBaseSpeed() * 0.7, p.getCurrentSpeed(), 0.01);
    }

    @Test
    public void testGreenPlayerMuereEnSegundoGolpe() {
        GreenPlayer p = new GreenPlayer(100, 100);
        p.onHit();
        assertTrue("Green debe morir en el segundo golpe", p.onHit());
    }

    @Test
    public void testGreenPlayerRecuperaEscudoConRestoreAbility() {
        GreenPlayer p = new GreenPlayer(100, 100);
        p.onHit();
        assertFalse(p.isShieldActive());
        p.restoreAbility();
        assertTrue("El escudo debe restaurarse con restoreAbility", p.isShieldActive());
        assertEquals("La velocidad debe restaurarse", p.getBaseSpeed(), p.getCurrentSpeed(), 0.01);
    }

    @Test
    public void testGreenPlayerRespawnNoRestaurasEscudo() {
        GreenPlayer p = new GreenPlayer(100, 100);
        p.onHit();
        p.respawn();
        assertFalse("Respawn NO debe restaurar el escudo del verde", p.isShieldActive());
    }

    @Test
    public void testGreenPlayerRespawnRestauraPosicion() {
        GreenPlayer p = new GreenPlayer(50, 60);
        p.onHit();
        p.respawn();
        assertEquals("X debe restaurarse al respawnear", 50, p.getX());
        assertEquals("Y debe restaurarse al respawnear", 60, p.getY());
    }

    @Test
    public void testGreenPlayerTamano() {
        GreenPlayer p = new GreenPlayer(0, 0);
        assertEquals("Green debe tener tamaño 20", 20, p.getSize());
    }

    @Test
    public void testGreenPlayerTamanoConTempSize() {
        GreenPlayer p = new GreenPlayer(0, 0);
        p.setTempSize(30);
        assertEquals("Con tempSize activo debe usarse ese valor", 30, p.getSize());
    }

    // ============================================================
    // JUGADORES - BLUE
    // ============================================================

    @Test
    public void testBluePlayerEsMasRapidoQueRed() {
        BluePlayer b = new BluePlayer(0, 0);
        RedPlayer r = new RedPlayer(0, 0);
        assertTrue("Blue debe ser más rápido que Red", b.getBaseSpeed() > r.getBaseSpeed());
    }

    @Test
    public void testBluePlayerEsMasGrandeQueRed() {
        BluePlayer b = new BluePlayer(0, 0);
        RedPlayer r = new RedPlayer(0, 0);
        assertTrue("Blue debe ser más grande que Red", b.getSize() > r.getSize());
    }

    @Test
    public void testBluePlayerMuereAlPrimerGolpe() {
        BluePlayer b = new BluePlayer(0, 0);
        assertTrue("Blue debe morir al primer golpe", b.onHit());
    }

    @Test
    public void testBluePlayerVelocidad() {
        BluePlayer b = new BluePlayer(0, 0);
        assertEquals("Blue debe tener velocidad 4.5", 4.5, b.getBaseSpeed(), 0.01);
    }

    @Test
    public void testBluePlayerTamano() {
        BluePlayer b = new BluePlayer(0, 0);
        assertEquals("Blue debe tener tamaño 30", 30, b.getSize());
    }

    @Test
    public void testBluePlayerNoTieneEscudo() {
        BluePlayer b = new BluePlayer(0, 0);
        assertFalse("Blue no debe iniciar con escudo", b.isShieldActive());
    }

    @Test
    public void testBluePlayerTamanoConTempSize() {
        BluePlayer b = new BluePlayer(0, 0);
        b.setTempSize(40);
        assertEquals("Con tempSize activo debe usarse ese valor", 40, b.getSize());
    }

    // ============================================================
    // PLAYER (base)
    // ============================================================

    @Test
    public void testContadorDeMuertes() {
        RedPlayer p = new RedPlayer(100, 100);
        p.die();
        p.die();
        assertEquals("Deben registrarse 2 muertes", 2, p.getDeaths());
    }

    @Test
    public void testRecolectarMonedaIncrementaContador() {
        RedPlayer p = new RedPlayer(100, 100);
        p.collectCoin();
        p.collectCoin();
        assertEquals("Deben registrarse 2 monedas", 2, p.getCoinsCollected());
    }

    @Test
    public void testZonaIntermediaCambiaSpawnPoint() {
        RedPlayer p = new RedPlayer(40, 240);
        p.setSpawnPoint(400, 250);
        p.die();
        assertEquals("El jugador debe respawnear en X=400", 400, p.getX());
        assertEquals("El jugador debe respawnear en Y=250", 250, p.getY());
    }

    @Test
    public void testJugadorRestableceVelocidadAlRespawnear() {
        RedPlayer p = new RedPlayer(100, 100);
        p.setCurrentSpeed(1.0);
        p.respawn();
        assertEquals("La velocidad debe restaurarse al respawnear",
                p.getBaseSpeed(), p.getCurrentSpeed(), 0.01);
    }

    @Test
    public void testPlayerBorderColor() {
        RedPlayer p = new RedPlayer(0, 0);
        p.setBorderColor(java.awt.Color.CYAN);
        assertEquals("El color de borde debe guardarse", java.awt.Color.CYAN, p.getBorderColor());
    }

    @Test
    public void testPlayerInvulnerabilidad() {
        RedPlayer p = new RedPlayer(0, 0);
        assertFalse("No debe ser invulnerable al inicio", p.isInvulnerable());
        p.setInvulnerable(10);
        assertTrue("Debe ser invulnerable tras setInvulnerable", p.isInvulnerable());
        for (int i = 0; i < 10; i++) p.tickInvulnerability();
        assertFalse("No debe ser invulnerable después de que pasen los ticks", p.isInvulnerable());
    }

    @Test
    public void testPlayerResetSpawnToStart() {
        RedPlayer p = new RedPlayer(50, 50);
        p.setSpawnPoint(200, 200);
        p.resetSpawnToStart();
        assertEquals("SpawnX debe regresar al inicio", 50, p.getSpawnX());
        assertEquals("SpawnY debe regresar al inicio", 50, p.getSpawnY());
    }

    @Test
    public void testPlayerSetXY() {
        RedPlayer p = new RedPlayer(0, 0);
        p.setX(150);
        p.setY(250);
        assertEquals(150, p.getX());
        assertEquals(250, p.getY());
    }

    @Test
    public void testPlayerTipoRed() {
        RedPlayer p = new RedPlayer(0, 0);
        assertEquals(Player.PlayerType.RED, p.getType());
    }

    @Test
    public void testPlayerTipoBlue() {
        BluePlayer b = new BluePlayer(0, 0);
        assertEquals(Player.PlayerType.BLUE, b.getType());
    }

    @Test
    public void testPlayerTipoGreen() {
        GreenPlayer g = new GreenPlayer(0, 0);
        assertEquals(Player.PlayerType.GREEN, g.getType());
    }

    @Test
    public void testPlayerToString() {
        RedPlayer p = new RedPlayer(10, 20);
        String str = p.toString();
        assertTrue("toString debe incluir el tipo", str.contains("RED"));
    }

    @Test
    public void testPlayerShieldActivoSetGet() {
        RedPlayer p = new RedPlayer(0, 0);
        p.setShieldActive(true);
        assertTrue(p.isShieldActive());
        p.setShieldActive(false);
        assertFalse(p.isShieldActive());
    }

    // ============================================================
    // PLAYER FACTORY
    // ============================================================

    @Test
    public void testPlayerFactoryCreaJugadoresCorrectamente() throws GameException {
        Player r = PlayerFactory.create(Player.PlayerType.RED, 0, 0);
        Player b = PlayerFactory.create(Player.PlayerType.BLUE, 0, 0);
        Player g = PlayerFactory.create(Player.PlayerType.GREEN, 0, 0);
        assertTrue("Debe crear RedPlayer", r instanceof RedPlayer);
        assertTrue("Debe crear BluePlayer", b instanceof BluePlayer);
        assertTrue("Debe crear GreenPlayer", g instanceof GreenPlayer);
    }

    @Test(expected = GameException.class)
    public void testPlayerFactoryLanzaExcepcionConTipoInvalido() throws GameException {
        PlayerFactory.create(null, 0, 0);
    }

    // ============================================================
    // WALL
    // ============================================================

    @Test
    public void testWallBlockeaColision() {
        Wall w = new Wall(100, 100, 50, 50);
        assertTrue("Debe detectar colisión con el jugador dentro", w.blocks(100, 100, 20));
    }

    @Test
    public void testWallNoBlockeaFuera() {
        Wall w = new Wall(100, 100, 50, 50);
        assertFalse("No debe detectar colisión lejos del muro", w.blocks(300, 300, 20));
    }

    @Test
    public void testWallDimensiones() {
        Wall w = new Wall(10, 20, 60, 40);
        assertEquals(10, w.getX());
        assertEquals(20, w.getY());
        assertEquals(60, w.getWidth());
        assertEquals(40, w.getHeight());
    }

    @Test
    public void testWallGetSize() {
        Wall w = new Wall(0, 0, 60, 40);
        assertEquals("getSize debe retornar el máximo entre ancho y alto", 60, w.getSize());
    }

    @Test
    public void testWallSetXY() {
        Wall w = new Wall(0, 0, 50, 50);
        w.setX(100);
        w.setY(200);
        assertEquals(100, w.getX());
        assertEquals(200, w.getY());
    }

    @Test
    public void testWallBordeIzquierdo() {
        Wall w = new Wall(100, 100, 50, 50);
        // Jugador justo a la izquierda → no colisiona
        assertFalse(w.blocks(70, 110, 20)); // 70+20=90 < 100
    }

    @Test
    public void testWallBordeDerecho() {
        Wall w = new Wall(100, 100, 50, 50);
        // Jugador justo a la derecha → no colisiona
        assertFalse(w.blocks(155, 110, 20)); // 155 > 150
    }

    // ============================================================
    // MONEDAS - YELLOW
    // ============================================================

    @Test
    public void testYellowCoinSeRecolecta() {
        YellowCoin c = new YellowCoin(100, 100);
        assertFalse("La moneda no debe estar recogida al inicio", c.isCollected());
        c.collect();
        assertTrue("La moneda debe marcarse como recogida", c.isCollected());
    }

    @Test
    public void testYellowCoinTipoEsYELLOW() {
        YellowCoin c = new YellowCoin(0, 0);
        assertEquals("El tipo debe ser YELLOW", Coin.CoinType.YELLOW, c.getType());
    }

    @Test
    public void testYellowCoinPosicion() {
        YellowCoin c = new YellowCoin(150, 250);
        assertEquals(150, c.getX());
        assertEquals(250, c.getY());
    }

    @Test
    public void testYellowCoinTamano() {
        YellowCoin c = new YellowCoin(0, 0);
        assertEquals("Tamaño de la moneda debe ser 14", 14, c.getSize());
    }

    @Test
    public void testYellowCoinSetXY() {
        YellowCoin c = new YellowCoin(0, 0);
        c.setX(50);
        c.setY(75);
        assertEquals(50, c.getX());
        assertEquals(75, c.getY());
    }

    // ============================================================
    // MONEDAS - SKIN
    // ============================================================

    @Test
    public void testSkinCoinTieneAsociacionCorrectaRed() {
        SkinCoin sc = new SkinCoin(0, 0, Player.PlayerType.RED);
        assertEquals(Player.PlayerType.RED, sc.getAssociatedSkin());
        assertEquals(Coin.CoinType.SKIN_RED, sc.getType());
    }

    @Test
    public void testSkinCoinBlue() {
        SkinCoin sc = new SkinCoin(0, 0, Player.PlayerType.BLUE);
        assertEquals(Coin.CoinType.SKIN_BLUE, sc.getType());
        assertEquals(Player.PlayerType.BLUE, sc.getAssociatedSkin());
    }

    @Test
    public void testSkinCoinGreen() {
        SkinCoin sc = new SkinCoin(0, 0, Player.PlayerType.GREEN);
        assertEquals(Coin.CoinType.SKIN_GREEN, sc.getType());
        assertEquals(Player.PlayerType.GREEN, sc.getAssociatedSkin());
    }

    @Test
    public void testSkinCoinSeRecolecta() {
        SkinCoin sc = new SkinCoin(0, 0, Player.PlayerType.RED);
        assertFalse(sc.isCollected());
        sc.collect();
        assertTrue(sc.isCollected());
    }

    // ============================================================
    // BOMBA
    // ============================================================

    @Test
    public void testBombaNoExplotadaAlInicio() {
        Bomb b = new Bomb(100, 100);
        assertFalse("La bomba no debe estar explotada al inicio", b.isExploded());
    }

    @Test
    public void testBombaExplotaAlActivarse() {
        Bomb b = new Bomb(100, 100);
        b.explode();
        assertTrue("La bomba debe estar explotada", b.isExploded());
    }

    @Test
    public void testBombaDetectaColision() {
        Bomb b = new Bomb(100, 100);
        assertTrue("Debe detectar colisión en la misma posición", b.collidesWith(100, 100, 20));
        assertFalse("No debe detectar colisión a distancia", b.collidesWith(300, 300, 20));
    }

    @Test
    public void testBombaExplotadaNoColisiona() {
        Bomb b = new Bomb(100, 100);
        b.explode();
        assertFalse("Una bomba explotada no debe detectar más colisiones", b.collidesWith(100, 100, 20));
    }

    @Test
    public void testBombaPosicion() {
        Bomb b = new Bomb(200, 300);
        assertEquals(200, b.getX());
        assertEquals(300, b.getY());
    }

    @Test
    public void testBombaTamano() {
        Bomb b = new Bomb(0, 0);
        assertEquals(16, b.getSize());
    }

    @Test
    public void testBombaSetXY() {
        Bomb b = new Bomb(0, 0);
        b.setX(50);
        b.setY(60);
        assertEquals(50, b.getX());
        assertEquals(60, b.getY());
    }

    // ============================================================
    // FUENTE DE VIDA
    // ============================================================

    @Test
    public void testLifeSourceNoUsadaAlInicio() {
        LifeSource ls = new LifeSource(100, 100);
        assertFalse("La fuente de vida no debe estar usada al inicio", ls.isUsed());
    }

    @Test
    public void testLifeSourceSeUsaCorrectamente() {
        LifeSource ls = new LifeSource(100, 100);
        ls.use();
        assertTrue("La fuente de vida debe marcarse como usada", ls.isUsed());
    }

    @Test
    public void testLifeSourceDetectaColision() {
        LifeSource ls = new LifeSource(100, 100);
        assertTrue("Debe detectar colisión con el jugador", ls.collidesWith(100, 100, 20));
        assertFalse("No debe detectar colisión a distancia", ls.collidesWith(300, 300, 20));
    }

    @Test
    public void testLifeSourceUsadaNoColisiona() {
        LifeSource ls = new LifeSource(100, 100);
        ls.use();
        assertFalse("Una fuente usada no debe detectar más colisiones", ls.collidesWith(100, 100, 20));
    }

    @Test
    public void testLifeSourcePosicion() {
        LifeSource ls = new LifeSource(150, 250);
        assertEquals(150, ls.getX());
        assertEquals(250, ls.getY());
    }

    @Test
    public void testLifeSourceTamano() {
        LifeSource ls = new LifeSource(0, 0);
        assertEquals(16, ls.getSize());
    }

    @Test
    public void testLifeSourceSetXY() {
        LifeSource ls = new LifeSource(0, 0);
        ls.setX(80);
        ls.setY(90);
        assertEquals(80, ls.getX());
        assertEquals(90, ls.getY());
    }

    // ============================================================
    // ENEMIGOS - BASIC
    // ============================================================

    @Test
    public void testBasicEnemyRebotaEnParedIzquierda() {
        BasicBlueEnemy e = new BasicBlueEnemy(100, 100, true, 100, 500, -3);
        e.update();
        assertTrue("El enemigo no debe salir del límite izquierdo", e.getX() >= 100);
    }

    @Test
    public void testBasicEnemyColisionaConJugador() {
        BasicBlueEnemy e = new BasicBlueEnemy(100, 100, true, 0, 800, 3);
        assertTrue("Debe detectar colisión en la misma posición", e.collidesWith(100, 100, 20));
        assertFalse("No debe detectar colisión a distancia", e.collidesWith(200, 200, 20));
    }

    @Test
    public void testBasicEnemyMueveHorizontalmente() {
        BasicBlueEnemy e = new BasicBlueEnemy(200, 100, true, 100, 500, 3);
        int xInicial = e.getX();
        e.update();
        assertNotEquals("El enemigo debe haberse movido", xInicial, e.getX());
    }

    @Test
    public void testBasicEnemyMueveVerticalmente() {
        BasicBlueEnemy e = new BasicBlueEnemy(200, 200, false, 100, 400, 3);
        int yInicial = e.getY();
        e.update();
        assertNotEquals("El enemigo vertical debe haberse movido", yInicial, e.getY());
    }

    @Test
    public void testBasicEnemyRebotaEnParedDerecha() {
        // Posicionar al borde derecho para forzar rebote
        BasicBlueEnemy e = new BasicBlueEnemy(490, 100, true, 100, 500, 20);
        e.update(); // debe rebotar
        assertTrue("Tras rebotar, X debe ser <= maxBound - size", e.getX() <= 484);
    }

    @Test
    public void testBasicEnemyRebotaVerticalAbajo() {
        BasicBlueEnemy e = new BasicBlueEnemy(200, 388, false, 100, 400, 20);
        e.update();
        assertTrue("Tras rebotar abajo, Y debe ser <= maxBound - size", e.getY() <= 384);
    }

    @Test
    public void testBasicEnemyEsActivo() {
        BasicBlueEnemy e = new BasicBlueEnemy(100, 100, true, 0, 800, 3);
        assertTrue("El enemigo debe estar activo al inicio", e.isActive());
    }

    @Test
    public void testBasicEnemySeDesactiva() {
        BasicBlueEnemy e = new BasicBlueEnemy(100, 100, true, 0, 800, 3);
        e.setActive(false);
        assertFalse("El enemigo debe estar inactivo", e.isActive());
    }

    @Test
    public void testBasicEnemyTipoEsBasic() {
        BasicBlueEnemy e = new BasicBlueEnemy(0, 0, true, 0, 800, 3);
        assertEquals(Enemy.EnemyType.BASIC_BLUE, e.getType());
    }

    @Test
    public void testBasicEnemyGetSize() {
        BasicBlueEnemy e = new BasicBlueEnemy(0, 0, true, 0, 800, 3);
        assertEquals(16, e.getSize());
    }

    @Test
    public void testBasicEnemySetXY() {
        BasicBlueEnemy e = new BasicBlueEnemy(0, 0, true, 0, 800, 3);
        e.setX(100);
        e.setY(200);
        assertEquals(100, e.getX());
        assertEquals(200, e.getY());
    }

    // ============================================================
    // ENEMIGOS - FAST
    // ============================================================

    @Test
    public void testFastEnemyTieneDoblesVelocidad() {
        FastBlueEnemy fast = new FastBlueEnemy(200, 200, true, 100, 500, 2.0);
        assertEquals("FastEnemy debe tener el doble de velocidad base",
                4.0, fast.getSpeed(), 0.01);
    }

    @Test
    public void testFastEnemySeActualiza() {
        FastBlueEnemy e = new FastBlueEnemy(200, 200, true, 100, 500, 2.0);
        int xInicial = e.getX();
        e.update();
        assertNotEquals("FastEnemy debe haberse movido", xInicial, e.getX());
    }

    @Test
    public void testFastEnemyRebotaEnBorde() {
        FastBlueEnemy e = new FastBlueEnemy(490, 200, true, 100, 500, 2.0);
        e.update();
        assertTrue("Tras rebotar, debe estar dentro del límite", e.getX() <= 484);
    }

    @Test
    public void testFastEnemyVerticalSeActualiza() {
        FastBlueEnemy e = new FastBlueEnemy(200, 200, false, 100, 400, 2.0);
        int yInicial = e.getY();
        e.update();
        assertNotEquals("FastEnemy vertical debe moverse", yInicial, e.getY());
    }

    @Test
    public void testFastEnemyRebotaVertical() {
        FastBlueEnemy e = new FastBlueEnemy(200, 388, false, 100, 400, 2.0);
        e.update();
        assertTrue("Tras rebotar abajo, Y debe ser <= maxBound - size", e.getY() <= 384);
    }

    @Test
    public void testFastEnemyTipoEsFast() {
        FastBlueEnemy e = new FastBlueEnemy(0, 0, true, 0, 800, 2.0);
        assertEquals(Enemy.EnemyType.FAST_BLUE, e.getType());
    }

    // ============================================================
    // ENEMIGOS - PATROL
    // ============================================================

    @Test
    public void testPatrolEnemySigueSusWaypoints() {
        int[][] waypoints = {{300, 200}, {500, 200}, {500, 300}, {300, 300}};
        PatrolBlueEnemy e = new PatrolBlueEnemy(300, 200, waypoints, 2.0);
        e.update();
        assertTrue("El patrullero debe moverse hacia su waypoint",
                e.getX() >= 300 && e.getY() >= 200);
    }

    @Test
    public void testPatrolEnemyAvanzaHaciaWaypoint() {
        int[][] waypoints = {{300, 200}, {400, 200}};
        PatrolBlueEnemy e = new PatrolBlueEnemy(300, 200, waypoints, 5.0);
        e.update();
        // Debe avanzar hacia X=400
        assertTrue("Debe haberse movido hacia el waypoint", e.getX() > 300);
    }

    @Test
    public void testPatrolEnemySinWaypointsNoCrash() {
        PatrolBlueEnemy e = new PatrolBlueEnemy(100, 100, new int[0][0], 2.0);
        e.update(); // no debe lanzar excepción
        assertEquals("Sin waypoints no debe moverse", 100, e.getX());
    }

    @Test
    public void testPatrolEnemyTipoEsPatrol() {
        int[][] wp = {{100, 100}, {200, 100}};
        PatrolBlueEnemy e = new PatrolBlueEnemy(100, 100, wp, 2.0);
        assertEquals(Enemy.EnemyType.PATROL_BLUE, e.getType());
    }

    @Test
    public void testPatrolEnemyColisionaConJugador() {
        int[][] wp = {{100, 100}, {200, 100}};
        PatrolBlueEnemy e = new PatrolBlueEnemy(100, 100, wp, 2.0);
        assertTrue(e.collidesWith(100, 100, 20));
        assertFalse(e.collidesWith(300, 300, 20));
    }

    // ============================================================
    // SAFE ZONE
    // ============================================================

    @Test
    public void testSafeZoneDetectaJugadorAdentro() {
        SafeZone z = new SafeZone(0, 200, 80, 100, SafeZone.ZoneType.START);
        assertTrue("El jugador debe estar dentro de la zona", z.contains(20, 220, 20));
        assertFalse("El jugador no debe estar fuera de la zona", z.contains(200, 220, 20));
    }

    @Test
    public void testSafeZoneCentroCalculadoCorrectamente() {
        SafeZone z = new SafeZone(0, 0, 100, 200, SafeZone.ZoneType.START);
        assertEquals("El centro X debe ser 50", 50, z.getCenterX());
        assertEquals("El centro Y debe ser 100", 100, z.getCenterY());
    }

    @Test
    public void testSafeZoneTipos() {
        SafeZone start = new SafeZone(0, 0, 50, 50, SafeZone.ZoneType.START);
        SafeZone inter = new SafeZone(0, 0, 50, 50, SafeZone.ZoneType.INTERMEDIATE);
        SafeZone fin   = new SafeZone(0, 0, 50, 50, SafeZone.ZoneType.FINAL);
        assertEquals(SafeZone.ZoneType.START, start.getZoneType());
        assertEquals(SafeZone.ZoneType.INTERMEDIATE, inter.getZoneType());
        assertEquals(SafeZone.ZoneType.FINAL, fin.getZoneType());
    }

    @Test
    public void testSafeZoneDimensiones() {
        SafeZone z = new SafeZone(10, 20, 80, 100, SafeZone.ZoneType.FINAL);
        assertEquals(10, z.getX());
        assertEquals(20, z.getY());
        assertEquals(80, z.getWidth());
        assertEquals(100, z.getHeight());
    }

    @Test
    public void testSafeZoneToString() {
        SafeZone z = new SafeZone(0, 0, 50, 50, SafeZone.ZoneType.START);
        assertNotNull(z.toString());
        assertTrue(z.toString().contains("START"));
    }

    // ============================================================
    // LEVEL CONFIG
    // ============================================================

    @Test
    public void testLevelConfigGetters() {
        LevelConfig cfg = new LevelConfig("nivel1", 800, 500, 90);
        assertEquals("nivel1", cfg.getName());
        assertEquals(800, cfg.getBoardWidth());
        assertEquals(500, cfg.getBoardHeight());
        assertEquals(90, cfg.getTimeLimitSeconds());
    }

    @Test
    public void testLevelConfigAgregarYObtenerElementos() {
        LevelConfig cfg = new LevelConfig("test", 800, 500, 60);
        cfg.addCoin(new YellowCoin(100, 100));
        cfg.addEnemy(new BasicBlueEnemy(200, 200, true, 0, 800, 3));
        cfg.addWall(new Wall(50, 50, 100, 20));
        cfg.addBomb(new Bomb(300, 300));
        cfg.addLifeSource(new LifeSource(400, 400));

        assertEquals(1, cfg.getCoins().size());
        assertEquals(1, cfg.getEnemies().size());
        assertEquals(1, cfg.getWalls().size());
        assertEquals(1, cfg.getBombs().size());
        assertEquals(1, cfg.getLifeSources().size());
        assertEquals(1, cfg.getTotalCoins());
    }

    @Test
    public void testLevelConfigStartYFinalZone() {
        LevelConfig cfg = basicConfig();
        assertNotNull(cfg.getStartZone());
        assertNotNull(cfg.getFinalZone());
        assertNull(cfg.getIntermediateZone());
    }

    @Test
    public void testLevelConfigIntermediateZone() {
        LevelConfig cfg = basicConfig();
        SafeZone iz = new SafeZone(400, 200, 50, 100, SafeZone.ZoneType.INTERMEDIATE);
        cfg.setIntermediateZone(iz);
        assertNotNull(cfg.getIntermediateZone());
        assertEquals(SafeZone.ZoneType.INTERMEDIATE, cfg.getIntermediateZone().getZoneType());
    }

    @Test
    public void testLevelConfigPosicionesJugadores() {
        LevelConfig cfg = basicConfig();
        assertEquals(40, cfg.getP1StartX());
        assertEquals(240, cfg.getP1StartY());
        assertEquals(740, cfg.getP2StartX());
        assertEquals(240, cfg.getP2StartY());
    }

    // ============================================================
    // GAME STATE
    // ============================================================

    @Test
    public void testGameStateIniciaEnPlaying() throws GameException {
        LevelConfig cfg = basicConfig();
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        assertEquals(GameState.Status.PLAYING, state.getStatus());
    }

    @Test
    public void testPausarYReanudarJuego() throws GameException {
        LevelConfig cfg = basicConfig();
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        state.pause();
        assertTrue(state.isPaused());
        state.resume();
        assertFalse(state.isPaused());
    }

    @Test(expected = GameException.class)
    public void testGameStateLanzaExcepcionSiConfigEsNull() throws GameException {
        new GameState(null, GameMode.PLAYER, new RedPlayer(0, 0), null);
    }

    @Test(expected = GameException.class)
    public void testGameStateLanzaExcepcionSiJugadorEsNull() throws GameException {
        new GameState(basicConfig(), GameMode.PLAYER, null, null);
    }

    @Test
    public void testTiempoSeAgotaYJuegoPasaALost() throws GameException {
        LevelConfig cfg = basicConfig();
        cfg.addCoin(new YellowCoin(600, 240));
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        state.update(61_000);
        assertEquals(GameState.Status.LOST, state.getStatus());
    }

    @Test
    public void testJuegoNoPasaAWonSinMonedas() throws GameException {
        LevelConfig cfg = basicConfig();
        cfg.addCoin(new YellowCoin(600, 240));
        RedPlayer p = new RedPlayer(730, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        state.update(100);
        assertNotEquals(GameState.Status.WON, state.getStatus());
    }

    @Test
    public void testJuegoGanaCuandoRecogeTodoYLlegaAlFinal() throws GameException {
        LevelConfig cfg = basicConfig();
        YellowCoin coin = new YellowCoin(600, 240);
        cfg.addCoin(coin);
        coin.collect();
        RedPlayer p = new RedPlayer(730, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        state.update(100);
        assertEquals(GameState.Status.WON, state.getStatus());
    }

    @Test
    public void testCalcularPuntajeBasico() throws GameException {
        LevelConfig cfg = basicConfig();
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        p.collectCoin();
        p.collectCoin();
        int score = state.calculateScore(p);
        assertTrue("El puntaje debe ser positivo", score > 0);
        assertEquals("Puntaje debe ser (2×10) + (60×1) - (0×5) = 80", 80, score);
    }

    @Test
    public void testPuntajeNoEsNegativo() throws GameException {
        LevelConfig cfg = basicConfig();
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        for (int i = 0; i < 20; i++) p.die();
        int score = state.calculateScore(p);
        assertTrue("El puntaje no debe ser negativo", score >= 0);
    }

    @Test
    public void testModoJuegoPVP() throws GameException {
        LevelConfig cfg = basicConfig();
        RedPlayer p1 = new RedPlayer(40, 240);
        BluePlayer p2 = new BluePlayer(740, 240);
        GameState state = new GameState(cfg, GameMode.PVP, p1, p2);
        assertNotNull(state.getPlayer2());
        assertEquals(GameMode.PVP, state.getMode());
    }

    @Test
    public void testTiempoRestanteDisminuye() throws GameException {
        LevelConfig cfg = basicConfig();
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        int tiempoInicial = state.getTimeRemainingMs();
        state.update(1000);
        assertTrue(state.getTimeRemainingMs() < tiempoInicial);
    }

    @Test
    public void testUpdateNoProcesaSiNoEstaPlaying() throws GameException {
        LevelConfig cfg = basicConfig();
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        state.pause();
        int tiempoAntes = state.getTimeRemainingMs();
        state.update(5000); // pausado, no debe cambiar
        assertEquals("Pausado no debe consumir tiempo", tiempoAntes, state.getTimeRemainingMs());
    }

    @Test
    public void testGetTimeRemainingSeconds() throws GameException {
        LevelConfig cfg = basicConfig();
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        assertEquals("Tiempo inicial en segundos debe ser 60", 60, state.getTimeRemainingSeconds());
    }

    @Test
    public void testGetCollectedCoins() throws GameException {
        LevelConfig cfg = basicConfig();
        YellowCoin c1 = new YellowCoin(100, 100);
        YellowCoin c2 = new YellowCoin(200, 200);
        cfg.addCoin(c1);
        cfg.addCoin(c2);
        c1.collect();
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        assertEquals("Debe contar 1 moneda recolectada", 1, state.getCollectedCoins());
    }

    @Test
    public void testGameStateTempSkinsInicianNull() throws GameException {
        LevelConfig cfg = basicConfig();
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        assertNull("P1TempSkin debe iniciar null", state.getP1TempSkin());
        assertNull("P2TempSkin debe iniciar null", state.getP2TempSkin());
    }

    @Test
    public void testGameStatePausadoResumido() throws GameException {
        LevelConfig cfg = basicConfig();
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        // Doble pause no cambia estado
        state.pause();
        state.pause();
        assertEquals(GameState.Status.PAUSED, state.getStatus());
        state.resume();
        assertEquals(GameState.Status.PLAYING, state.getStatus());
        // Resume en playing no hace nada raro
        state.resume();
        assertEquals(GameState.Status.PLAYING, state.getStatus());
    }

    @Test
    public void testGameStateGetConfig() throws GameException {
        LevelConfig cfg = basicConfig();
        RedPlayer p = new RedPlayer(40, 240);
        GameState state = new GameState(cfg, GameMode.PLAYER, p, null);
        assertNotNull(state.getConfig());
        assertEquals("test", state.getConfig().getName());
    }
}