------------------------------------------------------
Version 2.3.4
------------------------------------------------------
- Fixed no one getting a cape unless impersonators got capes too

------------------------------------------------------
Version 2.3.3
------------------------------------------------------
- Added Illuminations compatibility
  - impersonators' cosmetics disappear during impersonation
  - if `impersonate:fakeCapes` is enabled, the impersonated player's cosmetics will appear instead

------------------------------------------------------
Version 2.3.2
------------------------------------------------------
- Fixed non-op players being kicked when in range of an impersonator

------------------------------------------------------
Version 2.3.1
------------------------------------------------------
- Fixed crash at launch due to an invalid refmap

------------------------------------------------------
Version 2.3.0
------------------------------------------------------
Updated to MC 1.17

------------------------------------------------------
Version 2.2.1
------------------------------------------------------
- Added compatibility with fabric permission mods such as LuckPerms

------------------------------------------------------
Version 2.2.0
------------------------------------------------------
- Improved skin reloading for modded clients that also have Impersonate installed:
    - No more weird rotation in first person
    - No more desynchronization issues
- Fixed crash with latest Fabric API due to old Cardinal Components dependency
- Fixed a desync when a player got its skin reloaded while riding
- Fixed skin changes sometimes just not applying

------------------------------------------------------
Version 2.1.1
------------------------------------------------------
- Fixed a bug where some skins would not load correctly
- Fixed a crash at launch with LuckPerms

------------------------------------------------------
Version 2.1.0
------------------------------------------------------
- Fixed a major bug that prevented players from being saved while impersonating someone
- Fixed capes not working despite `impersonate:fakeCapes` being enabled
- Removed invalid entries from mod metadata

------------------------------------------------------
Version 2.0.3
------------------------------------------------------
- Added a gamerule to disable server logs revealing impersonations

------------------------------------------------------
Version 2.0.2
------------------------------------------------------
- Updated to 1.16.3

------------------------------------------------------
Version 2.0.1
------------------------------------------------------
Changes
- Replaced use of Gamerules API proposal with actual merged Fabric API
- Updated Cardinal Components dependency

------------------------------------------------------
Version 2.0.0
------------------------------------------------------
- Updated to 1.16

Changes
- Impersonate now works as a serverside-only mod
- Operators' player list HUDs now reveal impersonations
- Nameplates now reveal impersonations to operators who have Impersonate installed on their client
- Impersonators sharing an identity with an online player will no longer get merged in the player list

Fixes
- Fixed impersonation breaking down in the chat when both 
  operators and regular players are online
- Fixed impersonations not always being revealed when they are a component of a chat message

------------------------------------------------------
Version 1.0.0
------------------------------------------------------
Initial release
