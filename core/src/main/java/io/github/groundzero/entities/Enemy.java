package io.github.groundzero.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import io.github.groundzero.items.MedKit;
import io.github.groundzero.items.ARAmmo;
import io.github.groundzero.level.Level;

/**
 * AI enemy that moves, chases the player, shoots bullets, takes damage,
 * and displays textures. When the enemy dies, it has a chance to drop a medkit
 * and/or an ammo pack at random offsets from its position
 */
public class Enemy {
    private float x, y;
    private float speed = 140f;

    // Spawn position and maximum chase distance
    private final float spawnX, spawnY;
    private final float maxChaseDistance = 500f;

    // Scaling factors for enemy textures
    private final float spriteScale = 4.0f;
    private final float gunScale = 3f;

    // Enemy health
    private int health = 100;

    // Textures and animation for enemy
    private Texture idleTexture;
    private Texture[] runTextures;
    private Animation<TextureRegion> runAnimation;
    private float stateTime = 0f;
    private boolean facingRight = true;

    // Indicates if enemy is idle
    private boolean isIdle = false;

    // Gun properties
    private Texture gunTexture;
    private float gunAngle = 0f;
    private Sound gunSound;
    private static final float GUNSHOT_VOLUME = 0.1f;

    // Textures for death and hit effects
    private Texture deadTexture;
    private Texture blinkingTexture;
    private float blinkTimer = 0f;
    private static final float BLINK_DURATION = 0.1f;

    // Shooting properties
    private float shootCooldown;
    private float shootTimer = 0f;

    // List of bullets fired by the enemy
    private ArrayList<Bullet> bullets;

    // Hitmarker sound for when enemy is hit
    private Sound hitmarkerSound;

    // Bobbing animation fields for the enemy's gun
    private float animationTimer = 0f;
    private final float bobbingAmplitude = 0.5f;
    private final float bobbingFrequency = 2 * MathUtils.PI;

    // Medkit dropped upon enemy death
    private MedKit medkit = null;
    // Medkit drop chance
    private float medkitDropChance = 0.25f;
    private boolean medkitDropped = false;

    // ARAmmo dropped upon enemy death
    private ARAmmo arAmmo = null;
    // ARAmmo drop chance
    private float arAmmoDropChance = 0.15f;
    private boolean arAmmoDropped = false;

    private boolean dropsHandled = false;

    // Reference to the level for collision checks
    private Level level;

    public Enemy(float x, float y, Level level) {
        this.x = x;
        this.y = y;
        this.spawnX = x;
        this.spawnY = y;
        this.level = level;

        // Load enemy textures
        idleTexture = new Texture("entities/enemy/enemy_idle_1.png");
        runTextures = new Texture[5];
        runTextures[0] = new Texture("entities/enemy/enemy_run_1.png");
        runTextures[1] = new Texture("entities/enemy/enemy_run_2.png");
        runTextures[2] = new Texture("entities/enemy/enemy_run_3.png");
        runTextures[3] = new Texture("entities/enemy/enemy_run_4.png");
        runTextures[4] = new Texture("entities/enemy/enemy_run_5.png");

        // Running animation
        TextureRegion[] runFrames = new TextureRegion[runTextures.length];
        for (int i = 0; i < runTextures.length; i++) {
            runFrames[i] = new TextureRegion(runTextures[i]);
        }
        runAnimation = new Animation<TextureRegion>(0.1f, runFrames);
        runAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Load gun texture
        gunTexture = new Texture("entities/enemy/gun/AK.png");

        // Load gunshot sound
        try {
            gunSound = Gdx.audio.newSound(Gdx.files.internal("entities/AR/sounds/gunshot.wav"));
        } catch (Exception e) {
            Gdx.app.error("Enemy", "Error loading gunshot sound", e);
        }

        // Load dead texture
        try {
            deadTexture = new Texture("entities/enemy/enemy_dead.png");
        } catch (Exception e) {
            Gdx.app.error("Enemy", "Error loading enemy dead texture", e);
        }

        // Load blinking texture
        try {
            blinkingTexture = new Texture("entities/enemy/enemy_blinking.png");
        } catch (Exception e) {
            Gdx.app.error("Enemy", "Error loading enemy blinking texture", e);
        }

        // Load hitmarker sound
        try {
            hitmarkerSound = Gdx.audio.newSound(Gdx.files.internal("entities/enemy/gun/hitmarker.wav"));
        } catch (Exception e) {
            Gdx.app.error("Enemy", "Error loading hitmarker sound", e);
        }

        bullets = new ArrayList<>();
    }

    /**
     * Returns the dimensions used for bullet/hit detection
     */
    public float getWidth() {
        return idleTexture.getWidth() * spriteScale * 2f;
    }
    public float getHeight() {
        return idleTexture.getHeight() * spriteScale * 2f;
    }

    /**
     * Returns the dimensions used for collisions with the player
     */
    public float getCollisionWidth() {
        return idleTexture.getWidth() * spriteScale;
    }
    public float getCollisionHeight() {
        return idleTexture.getHeight() * spriteScale;
    }

    public void update(float deltaTime, Player player) {
        // Update timer for gun animation
        animationTimer += deltaTime;

        // Update enemy's bullets
        for (int i = 0; i < bullets.size(); i++) {
            Bullet bullet = bullets.get(i);
            bullet.update(deltaTime);
            Rectangle playerRect = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
            if (Intersector.intersectSegmentRectangle(
                new Vector2(bullet.getPrevX(), bullet.getPrevY()),
                new Vector2(bullet.getX(), bullet.getY()),
                playerRect)) {
                player.takeDamage(bullet.getDamage());
                bullet.dispose();
                bullets.remove(i);
                i--;
                continue;
            }
            if (bullet.getX() < -50 || bullet.getX() > Gdx.graphics.getWidth() + 50 ||
                bullet.getY() < -50 || bullet.getY() > Gdx.graphics.getHeight() + 50) {
                bullet.dispose();
                bullets.remove(i);
                i--;
            }
        }

        if (health > 0) {
            if (blinkTimer > 0) {
                blinkTimer -= deltaTime;
            }

            // Calculate direction toward the player
            float dx = player.getX() - x;
            float dy = player.getY() - y;
            float angleToPlayer = MathUtils.atan2(dy, dx);

            // Movement
            float proposedX = x + MathUtils.cos(angleToPlayer) * speed * deltaTime;
            float proposedY = y + MathUtils.sin(angleToPlayer) * speed * deltaTime;
            float dist = (float)Math.hypot(proposedX - spawnX, proposedY - spawnY);

            if (dist <= maxChaseDistance) {
                // horizontal collision check
                if (!level.isWallAt(proposedX, y) &&
                    !level.isWallAt(proposedX + getCollisionWidth(), y) &&
                    !level.isWallAt(proposedX, y + getCollisionHeight()) &&
                    !level.isWallAt(proposedX + getCollisionWidth(), y + getCollisionHeight())) {
                    x = proposedX;
                }
                // vertical collision check
                if (!level.isWallAt(x, proposedY) &&
                    !level.isWallAt(x + getCollisionWidth(), proposedY) &&
                    !level.isWallAt(x, proposedY + getCollisionHeight()) &&
                    !level.isWallAt(x + getCollisionWidth(), proposedY + getCollisionHeight())) {
                    y = proposedY;
                }
                isIdle = false;
            } else {
                isIdle = true;
            }

            // Aiming
            float rawAngle = angleToPlayer * MathUtils.radiansToDegrees;
            if (rawAngle < 0) rawAngle += 360;
            facingRight = (dx >= 0);
            gunAngle = facingRight ? rawAngle : (180 + rawAngle) % 360;

            if (!isIdle) stateTime += deltaTime;

            // Shooting logic
            shootTimer += deltaTime;
            if (shootTimer >= shootCooldown) {
                float pcx = player.getCenterX(), pcy = player.getCenterY();
                float left = pcx - Gdx.graphics.getWidth()/2f, right = pcx + Gdx.graphics.getWidth()/2f;
                float bottom = pcy - Gdx.graphics.getHeight()/2f, top = pcy + Gdx.graphics.getHeight()/2f;
                if (x >= left && x <= right && y >= bottom && y <= top && MathUtils.randomBoolean(0.8f)) {
                    shoot(player);
                }
                shootTimer = 0f;
                shootCooldown = MathUtils.random(1f, 3f);
            }

            // Bounce off collision with player
            Rectangle eR = new Rectangle(x, y, getCollisionWidth(), getCollisionHeight());
            Rectangle pR = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
            if (eR.overlaps(pR)) {
                float eCX = x + getCollisionWidth()/2f, eCY = y + getCollisionHeight()/2f;
                float pCX = player.getX() + player.getWidth()/2f, pCY = player.getY() + player.getHeight()/2f;
                float dX = eCX - pCX, dY = eCY - pCY;
                if (dX==0 && dY==0) dX=1;
                float oX = Math.min(eR.x+eR.width, pR.x+pR.width) - Math.max(eR.x, pR.x);
                float oY = Math.min(eR.y+eR.height, pR.y+pR.height) - Math.max(eR.y, pR.y);
                float m = 1.5f;
                if (oX < oY) {
                    float push = (oX/2f)*m, dir = dX/Math.abs(dX);
                    x += push*dir; player.pushBy(-push*dir, 0);
                } else {
                    float push = (oY/2f)*m, dir = dY/Math.abs(dY);
                    y += push*dir; player.pushBy(0, -push*dir);
                }
            }
        } else {
            if (!dropsHandled) {
                if (!medkitDropped && MathUtils.randomBoolean(medkitDropChance)) dropMedKit();
                if (!arAmmoDropped && MathUtils.randomBoolean(arAmmoDropChance)) dropARAmmo();
                dropsHandled = true;
            }
            if (medkit != null) medkit.update(deltaTime, player);
            if (arAmmo  != null) arAmmo.update(deltaTime, player);
        }
    }

    private void dropMedKit() {
        float ox = MathUtils.random(-40f,40f), oy = MathUtils.random(-40f,40f);
        medkit = new MedKit(x+ox, y+oy);
        medkitDropped = true;
    }

    private void dropARAmmo() {
        float ox = MathUtils.random(-40f,40f), oy = MathUtils.random(-40f,40f);
        arAmmo = new ARAmmo(x+ox, y+oy);
        arAmmoDropped = true;
    }

    private void shoot(Player player) {
        if (gunSound!=null) gunSound.play(GUNSHOT_VOLUME);
        float md=60f, vo = MathUtils.sin(animationTimer*bobbingFrequency)*bobbingAmplitude;
        float gx = x+8f, gy = y+15f+vo;
        if (!facingRight) gx-=32f;
        float dx = player.getCenterX()-gx, dy = player.getCenterY()-gy;
        float ra = (float)Math.atan2(dy,dx)*MathUtils.radiansToDegrees;
        if (ra<0) ra+=360;
        float arad = (float)Math.toRadians(ra);
        float bx = gx+MathUtils.cos(arad)*md, by = gy+MathUtils.sin(arad)*md;
        bullets.add(new Bullet(bx,by,MathUtils.cos(arad),MathUtils.sin(arad),ra));
    }

    public void render(SpriteBatch batch) {
        if (health>0) {
            if (blinkTimer>0) {
                if (facingRight) {
                    batch.draw(blinkingTexture, x,y,
                        blinkingTexture.getWidth()*spriteScale,
                        blinkingTexture.getHeight()*spriteScale);
                } else {
                    batch.draw(blinkingTexture, x+blinkingTexture.getWidth()*spriteScale,y,
                        -blinkingTexture.getWidth()*spriteScale,
                        blinkingTexture.getHeight()*spriteScale);
                }
            } else {
                if (isIdle) {
                    if (facingRight) {
                        batch.draw(idleTexture, x,y,
                            idleTexture.getWidth()*spriteScale,
                            idleTexture.getHeight()*spriteScale);
                    } else {
                        batch.draw(idleTexture, x+idleTexture.getWidth()*spriteScale,y,
                            -idleTexture.getWidth()*spriteScale,
                            idleTexture.getHeight()*spriteScale);
                    }
                } else {
                    TextureRegion f = runAnimation.getKeyFrame(stateTime,true);
                    if (facingRight) {
                        batch.draw(f, x,y,
                            f.getRegionWidth()*spriteScale,
                            f.getRegionHeight()*spriteScale);
                    } else {
                        batch.draw(f, x+f.getRegionWidth()*spriteScale,y,
                            -f.getRegionWidth()*spriteScale,
                            f.getRegionHeight()*spriteScale);
                    }
                }
            }
            float gx = x+8f, vo = MathUtils.sin(animationTimer*bobbingFrequency)*bobbingAmplitude;
            float gy = y+15f+vo;
            if (!facingRight) gx-=32f;
            batch.draw(gunTexture,
                gx,gy,
                gunTexture.getWidth()*gunScale/2f,
                gunTexture.getHeight()*gunScale/2f,
                gunTexture.getWidth()*gunScale,
                gunTexture.getHeight()*gunScale,
                1,1,gunAngle,
                0,0,
                gunTexture.getWidth(),gunTexture.getHeight(),
                !facingRight,false);
        } else {
            batch.draw(deadTexture, x,y,
                deadTexture.getWidth()*spriteScale,
                deadTexture.getHeight()*spriteScale);
            if (medkit!=null) medkit.render(batch);
            if (arAmmo!=null) arAmmo.render(batch);
        }
        for (Bullet b: bullets) b.render(batch);
    }

    public void takeDamage(int amount) {
        if (health<=0) return;
        health-=amount;
        blinkTimer=BLINK_DURATION;
        if (hitmarkerSound!=null) hitmarkerSound.play(0.1f);
    }

    public void dispose() {
        idleTexture.dispose();
        for (Texture t: runTextures) t.dispose();
        gunTexture.dispose();
        deadTexture.dispose();
        blinkingTexture.dispose();
        if (gunSound!=null) gunSound.dispose();
        if (hitmarkerSound!=null) hitmarkerSound.dispose();
        for (Bullet b: bullets) b.dispose();
        if (medkit!=null) medkit.dispose();
        if (arAmmo!=null) arAmmo.dispose();
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getHealth() { return health; }

    /**
     * Sets the chance for a medkit to drop upon death
     * @param chance A float value between 0 and 1 for the drop chance
     */
    public void setMedkitDropChance(float chance) {
        this.medkitDropChance = chance;
    }
}
