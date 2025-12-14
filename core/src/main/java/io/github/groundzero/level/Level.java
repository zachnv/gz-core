package io.github.groundzero.level;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * Level loads and renders a Tiled .tmx map and collision/trees/other layer
 */
public class Level {
    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer renderer;
    private final TiledMapTileLayer collisionLayer;
    private final TiledMapTileLayer treesLayer;
    private final TiledMapTileLayer otherLayer;
    private final float fadeRadius;

    /**
     * @param tmxFilePath
     */
    public Level(String tmxFilePath) {
        tiledMap = new TmxMapLoader().load(tmxFilePath);
        renderer = new OrthogonalTiledMapRenderer(tiledMap);

        MapLayer collLayer = tiledMap.getLayers().get("collision");
        MapLayer treeLayer = tiledMap.getLayers().get("trees");
        MapLayer othrLayer = tiledMap.getLayers().get("other");
        if (!(collLayer instanceof TiledMapTileLayer)
            || !(treeLayer instanceof TiledMapTileLayer)
            || !(othrLayer instanceof TiledMapTileLayer)) {
            throw new IllegalArgumentException(
                "Tiled map must contain a collision, trees, and an other layer");
        }
        collisionLayer = (TiledMapTileLayer) collLayer;
        treesLayer     = (TiledMapTileLayer) treeLayer;
        otherLayer     = (TiledMapTileLayer) othrLayer;
        this.fadeRadius = treesLayer.getTileWidth() * 4f;
    }

    /**
     * Render the base layers
     */
    public void renderBase(OrthographicCamera camera) {
        renderer.setView(camera);
        treesLayer.setVisible(false);
        renderer.render();
        treesLayer.setVisible(true);
    }

    /**
     * Draws the trees layer, fading to 50% if player walks into tree
     */
    public void renderTrees(OrthographicCamera camera,
                            SpriteBatch batch,
                            Rectangle playerBounds) {
        float tw = treesLayer.getTileWidth();
        float th = treesLayer.getTileHeight();

        float pcx = playerBounds.x + playerBounds.width * 0.5f;
        float pcy = playerBounds.y + playerBounds.height * 0.5f;

        int startX = (int)((camera.position.x - camera.viewportWidth/2)  / tw) - 1;
        int startY = (int)((camera.position.y - camera.viewportHeight/2) / th) - 1;
        int endX   = (int)((camera.position.x + camera.viewportWidth/2)  / tw) + 1;
        int endY   = (int)((camera.position.y + camera.viewportHeight/2) / th) + 1;

        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX   = Math.min(treesLayer.getWidth(),  endX);
        endY   = Math.min(treesLayer.getHeight(), endY);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                TiledMapTileLayer.Cell cell = treesLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) continue;

                float wx = x * tw + tw/2f;
                float wy = y * th + th/2f;
                float dx = wx - pcx;
                float dy = wy - pcy;
                float dist = (float)Math.hypot(dx, dy);

                // alpha
                float alpha = dist < fadeRadius
                    ? 0.5f + 0.5f * (dist / fadeRadius)
                    : 1f;

                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(
                    cell.getTile().getTextureRegion(),
                    x * tw, y * th, tw, th
                );
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);  // reset
    }

    /**
     * @return true if the world‐coordinate (x,y) hits a tile in the collision layer only
     */
    public boolean isWallAt(float worldX, float worldY) {
        float tw = collisionLayer.getTileWidth();
        float th = collisionLayer.getTileHeight();
        int cellX = (int)Math.floor(worldX / tw);
        int cellY = (int)Math.floor(worldY / th);

        if (cellX < 0 || cellX >= collisionLayer.getWidth()
            || cellY < 0 || cellY >= collisionLayer.getHeight()) {
            return false;
        }

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(cellX, cellY);
        return cell != null && cell.getTile() != null;
    }

    public void dispose() {
        renderer.dispose();
        tiledMap.dispose();
    }
}
