#!/usr/bin/env python3
"""Keep the six placed-bath models and animation/texture lookups intact on GeckoLib 5."""

from pathlib import Path
import re


model = Path("src/main/java/EdDYON/guaniao/client/bath/BirdBathModel.java").read_text()
item_model = Path("src/main/java/EdDYON/guaniao/client/bath/BirdBathItemModel.java").read_text()
variant = Path("src/main/java/EdDYON/guaniao/content/bath/BirdBathVariant.java").read_text()

assert "DataTicket<BirdBathVariant> VARIANT" in model
assert 'DataTicket.create("guaniao_bird_bath_variant"' in model
assert "renderState.getOrDefaultGeckolibData(VARIANT, BirdBathVariant.BIRD_BATH)" in model
assert 'normalizedId(variant(renderState).model(), ".geo.json")' in model
assert "return variant(renderState).texture();" in model
assert 'normalizedId(animatable.variant().animation(), ".animation.json")' in model
assert "static Identifier normalizedId(Identifier authored, String suffix)" in item_model
assert "path.substring(0, path.length() - suffix.length())" in item_model

# Original variant definitions remain the sole source of all six block and
# inventory model paths, not a parallel set of replacement assets.
model_ids = re.findall(r'\("([a-z0-9_]+)", "geo/([^\"]+)\.geo\.json"', variant)
assert len(model_ids) == 6 and len({id for id, _ in model_ids}) == 6
assert len({path for _, path in model_ids}) == 6
assert "variant.texture()" in item_model and "variant(renderState).texture()" in model
print("PASS: six authored bath model/texture/animation variants remain the only assets")
print("PASS: GeckoLib 5 render-state ticket selects each block model without asset renaming")
