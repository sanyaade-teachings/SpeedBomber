/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbomber.model.units;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import speedbomber.Game;
import speedbomber.model.GameObjectGroup;
import speedbomber.model.GameWorld;
import speedbomber.model.Statistics;
import speedbomber.model.player.Player;

/**
 *
 * @author FalseCAM
 */
public class Haunter extends PlayerObject {

    float lifeTime = 0;
    Vector3f startPoint;
    Spatial spatial;
    BetterCharacterControl character;
    private Vector3f target = null;
    private Statistics statistics;

    public Haunter(GameWorld world, Player player, Vector3f startPoint) {
        this.world = world;
        this.player = player;
        this.startPoint = startPoint;
        player.setHaunter(this);
        node = new Node("Haunter");
        group = GameObjectGroup.HAUNTER;
        create();
        //node.attachChild(getHead());
        //node.attachChild(getBody());

        character = new BetterCharacterControl(0.5f, 2f, 100f);
        node.addControl(character);
        character.warp(startPoint);

    }

    private void create() {
        spatial = Game.getAssetManager().loadModel("Models/Haunter.j3o");
        spatial.scale(0.25f * Game.scale);
        Material mat = new Material(Game.getAssetManager(), "Materials/Normale.j3md");
        spatial.setMaterial(mat);
        spatial.setLocalTranslation(new Vector3f(0, 0.25f * Game.scale, 0));
        node.attachChild(spatial);
    }

    private Geometry getHead() {
        Mesh mesh = new Sphere(16, 16, 1f);
        Geometry geom = new Geometry("Head", mesh);
        Material mat = new Material(Game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        geom.setLocalTranslation(new Vector3f(0, 2, 0));
        return geom;
    }

    private Geometry getBody() {
        Mesh mesh = new Sphere(16, 16, 3f);
        Geometry geom = new Geometry("Body", mesh);
        Material mat = new Material(Game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        geom.setLocalTranslation(new Vector3f(0, 0.5f, 0));
        return geom;
    }

    public BetterCharacterControl getCharacterControl() {
        return character;
    }

    public void update(float tpf) {
        lifeTime += tpf;
        if (isAlive()) {
            if (target != null && node.getWorldTranslation().distance(target) > 1) {
                Vector3f dir = this.target.subtract(node.getWorldTranslation());
                character.setWalkDirection(dir.normalize().mult(20 * Game.scale));
                character.setViewDirection(new Vector3f(dir.normalize().x, 0, dir.normalize().z));
            } else {
                this.target = null;
                character.setWalkDirection(Vector3f.ZERO);
            }
        } else {
            if (lifeTime > 3f) {
                revive();
            }
        }
    }

    public void move(Vector3f target) {
        this.target = target;
    }

    @Override
    public void doDamage(PlayerObject origin) {
        if (this.lifeTime > 10f) {
            this.alive = false;
            this.lifeTime = 0;
            if (this.getOwner() != origin.getOwner()) {
                statistics.killFor(origin.getOwner());
            }
        }
    }

    public void revive() {
        this.target = null;
        this.alive = true;
        character.warp(startPoint);
        world.attachObject(this);
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }
}
