# 0.2.2
- Fixed an issue with data saving 
- Added interdimensional travel for the home command
- Added a  menu to move the beacon
- Removed permissions temporarily

# 0.2.1

- Fixed an issue with luckperms compat
- Fixed an issue with faction names
- Fixed a casting issue with update faction setting packet
- Renamed ``FactionSavedData#markDirty(ServerLevel level)`` to ``FactionSavedData#save(ServerLevel level)``
- Removed duplicate ``FactionSavedData#save`` calls
# 0.2.0

- Added a new subcommand: Manage
  - This command consists of 4 menus:
    - Rename Faction → Renames the faction to the value provided in the edit box. Color codes supported!
    - Transfer Ownership → The way to retire as a leader
    - View members → This menu views all members, and here you can promote/demote members in bulk, unlike the /faction promote/demote commands
    - Settings → These are per faction settings, for example whether friendly fire is enabled
    

- Changes to the way data is saved
  - Instead of every 5 minutes, every change is now saved instantly
  - Moved global data to be faction specific


- Luckperms (0.2.0) and TAB (0.1.2) compatiblity for server hosts
- Wrote networking in kotlin
- Expanded packet and setting API's
- Actually put common code in a common folder
- Added a debug subcommand for development / server owners
- Renamed ``resources/assets/scaredsfactions/gui/container/rename_faction.png`` to ``resources/assets/scaredsfactions/gui/container/edit_string_value.png`` to better add a GUI with an EditBox 
- Changes some return messages.
- Split ``FactionSavedData`` from ``Faction`` because the file was getting too big for me to read



# 0.1.2
- Added a placeholder for the mod: TAB by NEZNAMY
- Added support for color coded faction names (only with vanilla color coding using &. Gradients and hex aren't supported yet.)

# 0.1.1
- Added a cooldown to the /faction home command
- Added config values related to the cooldown for /faction home
- Added a feature that prevents a faction from being knocked out when there is no player of said faction  online

# 0.1.0
First version: No documented changes