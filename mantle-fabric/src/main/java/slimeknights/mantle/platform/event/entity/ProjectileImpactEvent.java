package slimeknights.mantle.platform.event.entity;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import slimeknights.mantle.platform.event.EntityEvent;
import slimeknights.mantle.platform.event.Event.Cancelable;

/** Fired when a projectile hits something */
@Cancelable
public class ProjectileImpactEvent extends EntityEvent {
  /** What happens to the projectile after the impact */
  public enum ImpactResult {
    DEFAULT, SKIP_ENTITY, STOP_AT_CURRENT, STOP_AT_CURRENT_NO_DAMAGE
  }

  private final HitResult ray;
  private ImpactResult result = ImpactResult.DEFAULT;

  public ProjectileImpactEvent(Projectile projectile, HitResult ray) {
    super(projectile);
    this.ray = ray;
  }

  public HitResult getRayTraceResult() {
    return ray;
  }

  public Projectile getProjectile() {
    return (Projectile) getEntity();
  }

  @Override
  public Projectile getEntity() {
    return (Projectile) super.getEntity();
  }

  public ImpactResult getImpactResult() {
    return result;
  }

  public void setImpactResult(ImpactResult result) {
    this.result = result;
  }
}
