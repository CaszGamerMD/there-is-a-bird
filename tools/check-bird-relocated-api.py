#!/usr/bin/env python3
"""Guard mechanical Minecraft 26.2 class/annotation relocations; not gameplay certification."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/java/EdDYON/guaniao"
NULLABLE = (
    'content/bird/kingfisher/KingfisherFishingArc.java',
    'content/bird/hummingbird/HummingbirdGardenBehavior.java',
    'content/bird/hummingbird/HummingbirdSites.java',
    'content/bird/hummingbird/FlowerHabitatIndex.java',
    'content/bird/kestrel/KestrelFlightMotor.java',
    'client/hummingbird/HummingbirdSoundManager.java',
    'content/bird/hummingbird/HummingbirdPollinationService.java',
    'content/bird/CleanBirdTemptGoal.java',
    'content/bird/kingfisher/KingfisherHabitat.java',
    'content/bird/crow/CrowScareSmallBirdsGoal.java',
    'content/bird/brain/BirdHabitatCache.java',
    'content/bird/brain/BirdMigrationGoal.java',
    'content/bird/nightheron/NightHeronShorelineCache.java',
    'content/bird/umbrellacockatoo/UmbrellaCockatooSocialGoal.java',
    'content/bird/umbrellacockatoo/UmbrellaCockatooObserveGoal.java',
    'content/bird/umbrellacockatoo/UmbrellaCockatooInspectGoal.java',
    'content/bird/umbrellacockatoo/UmbrellaCockatooMimicry.java',
)
MOVED_TYPES = (
    'event/BirdServerTickManager.java',
    'content/dropping/BirdDroppingProjectileEntity.java',
    'content/dropping/BirdDroppingPrankHandler.java',
    'mixin/VillagerTradePriceMixin.java',
    'content/bird/nightheron/NightHeronFishCaptureEvent.java',
    'content/bird/nightheron/NightHeronFishing.java',
)
OLD_IMPORTS = (
    "net.minecraft.world.entity.npc.Villager",
    "net.minecraft.world.entity.animal.IronGolem",
    "net.minecraft.world.entity.animal.AbstractFish",
)
for path in NULLABLE:
    source = (ROOT / path).read_text(encoding="utf-8")
    assert "import javax.annotation.Nullable;" not in source, path
    assert source.count("import org.jetbrains.annotations.Nullable;") == 1, path
    assert "@Nullable" in source, path
    print("PASS 26.2 nullable:", path)
for path in MOVED_TYPES:
    source = (ROOT / path).read_text(encoding="utf-8")
    for old in OLD_IMPORTS:
        assert "import " + old + ";" not in source, (path, old)
    print("PASS 26.2 moved vanilla type:", path)
assert len(NULLABLE) == 17
assert len(MOVED_TYPES) == 6
