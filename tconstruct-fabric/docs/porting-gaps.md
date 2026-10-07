# Porting gaps and behavior changes

Notes recorded while porting to Fabric 1.21.1. Items here compile but behave differently or are not yet wired.

- `ModifiableArrow` / `ThrownTool`: vanilla knockback and pierce are enchantment based in 1.21, so `PunchModule` only affects `ModifiableArrow` and the "adder" overrides of `setPierceLevel`/`setKnockback` are gone.
