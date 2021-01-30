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
