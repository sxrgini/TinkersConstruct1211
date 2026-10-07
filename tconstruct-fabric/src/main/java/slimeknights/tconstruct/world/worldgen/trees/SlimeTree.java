package slimeknights.tconstruct.world.worldgen.trees;

import net.minecraft.world.level.block.grower.TreeGrower;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.block.FoliageType;

import java.util.Optional;

/** Creates tree growers for slime trees */
public class SlimeTree {
  private SlimeTree() {}

  /** Creates the grower for the given foliage type */
  public static TreeGrower create(FoliageType foliageType) {
    String name = TConstruct.getResource(foliageType.getSerializedName() + "_slime_tree").toString();
    return switch (foliageType) {
      case EARTH -> new TreeGrower(name, Optional.empty(), Optional.of(TinkerStructures.earthSlimeTree), Optional.empty());
      case SKY -> new TreeGrower(name, Optional.empty(), Optional.of(TinkerStructures.skySlimeTree), Optional.empty());
      // 85% of ender trees are the tall variant
      case ENDER -> new TreeGrower(name, 0.85f, Optional.empty(), Optional.empty(), Optional.of(TinkerStructures.enderSlimeTree), Optional.of(TinkerStructures.enderSlimeTreeTall), Optional.empty(), Optional.empty());
      case BLOOD -> new TreeGrower(name, Optional.empty(), Optional.of(TinkerStructures.bloodSlimeFungus), Optional.empty());
      case ICHOR -> new TreeGrower(name, Optional.empty(), Optional.of(TinkerStructures.ichorSlimeFungus), Optional.empty());
    };
  }
}
