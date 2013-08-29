/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbomber.model;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import speedbomber.ClientMain;
import speedbomber.controller.GameController;
import speedbomber.controller.PlayerController;
import speedbomber.model.network.GameClient;
import speedbomber.model.player.Player;
import speedbomber.model.units.Bomb;
import speedbomber.model.units.Grenade;
import speedbomber.model.units.Haunter;
import speedbomber.model.world.map.AbstractMap;
import speedbomber.model.world.map.Map;

/**
 *
 * @author FalseCAM
 */
public class LevelAppState extends AbstractAppState {
    
    public static final String mapFile = "Maps/Map.map";
    ClientMain app;
    Node rootNode;
    BulletAppState bulletAppState;
    DirectionalLight sun = new DirectionalLight();
    AmbientLight al = new AmbientLight();
    List<GameObject> gameObjects = new LinkedList<GameObject>();
    AbstractMap abstractMap;
    Map map;
    List<Player> players = new LinkedList<Player>();
    HashMap<Player, Haunter> haunters = new HashMap<Player, Haunter>();
    GameController gameController;
    PlayerController playerController = new PlayerController();
    ChaseCamera chaseCam;
    int userId = 0;
    boolean enabled = false;
    
    public LevelAppState(Integer userId) {
        super();
        this.userId = userId;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (ClientMain) app;
        this.rootNode = new Node();
        gameController = new GameController(this);
        GameClient.getClientListener().setGameController(this.gameController);
        this.app.getInputController().setPlayerController(this.playerController);
        initPhysics();
        initLights();
        initMap();
        initPlayer();
        initHaunter();
        initCamera();
        
        this.app.getRootNode().attachChild(this.rootNode);
    }
    
    private void initPhysics() {
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        app.getStateManager().attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -9.81f, 0));
        // Debug Physics
        //bulletAppState.getPhysicsSpace().enableDebug(app.getAssetManager());
    }
    
    private void initLights() {
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal());
        rootNode.addLight(sun);
        al.setColor(ColorRGBA.White.mult(0.4f));
        rootNode.addLight(al);
    }
    
    private void initMap() {
        abstractMap = AbstractMap.loadMapFile(mapFile);
        map = new Map(abstractMap);
        attachObject(map);
    }
    
    private void initPlayer() {
        players.clear();
        for (int i = 0; i < map.getSpawnPoints().size(); i++) {
            players.add(Player.getPlayer(i));
        }
    }
    
    private void initHaunter() {
        for (int i = 0; i < players.size(); i++) {
            Vector3f charLocation = new Vector3f(map.getSpawnPoint(i).getWorldTranslation().getX(), 0, map.getSpawnPoint(i).getWorldTranslation().getZ());
            Haunter haunter = new Haunter(charLocation);
            haunters.put(players.get(i), haunter);
            attachObject(haunter);
        }
    }
    
    private void initCamera() {
        // Enable a chase cam for this target (typically the player).
        chaseCam = new ChaseCamera(app.getCamera(), getHaunter(players.get(userId)).getNode(), app.getInputManager());
        chaseCam.setDefaultDistance(85f);
        chaseCam.setMaxDistance(90f);
        chaseCam.setSmoothMotion(true);
    }
    
    public void attachObject(GameObject gameObject) {
        gameObjects.add(gameObject);
        rootNode.attachChild(gameObject.getNode());
        bulletAppState.getPhysicsSpace().addAll(gameObject.getNode());
    }
    
    public void detachObject(GameObject gameObject) {
        rootNode.detachChild(gameObject.getNode());
        bulletAppState.getPhysicsSpace().removeAll(gameObject.getNode());
        gameObjects.remove(gameObject);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        this.bulletAppState.cleanup();
        app.getStateManager().detach(this.bulletAppState);
        app.getRootNode().detachChild(this.rootNode);
        this.app.getInputController().setPlayerController(null);
        GameClient.getClientListener().setGameController(null);
        chaseCam.setEnabled(false);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        // Pause and unpause
        super.setEnabled(enabled);
        if (enabled && isInitialized()) {
        } else {
        }
        chaseCam.setEnabled(enabled);
        bulletAppState.setEnabled(enabled);
    }

    // Note that update is only called while the state is both attached and enabled.
    @Override
    public void update(float tpf) {
        if (!super.isEnabled()) {
            return;
        }
        for (GameObject gameObject : gameObjects) {
            gameObject.update(tpf);
        }
        playerController.update(tpf);
    }
    
    public void throwGrenade(Haunter haunter, Vector3f target) {
        
        Grenade grenade = new Grenade(haunter, target);
        attachObject(grenade);
        
    }
    
    public void placeBomb(Haunter haunter) {
        Bomb bomb = new Bomb();
        bomb.getPhysics().setPhysicsLocation(haunter.getNode().getWorldTranslation());
        attachObject(bomb);
        
    }
    
    public Haunter getHaunter(Player player) {
        return haunters.get(player);
    }
    
    public List<Player> getPlayers(){
        return players;
    }
}