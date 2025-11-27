# Character Sprite Synchronization Test

## What Was Fixed

1. **PlayerConfig Persistence**: Character selection is now saved to PlayerConfig (woodlanders.json) so it persists across game sessions
2. **Initial Connection**: When a new player joins, they now receive PlayerJoinMessages for all existing players with their correct character sprites
3. **Character Updates**: When a player changes their character and saves, the update is broadcast to all other players
4. **Remote Player Updates**: RemotePlayers are now properly updated (disposed and recreated) when character sprites change
5. **World State Sync**: After sending world state, server sends individual character sprite updates for all existing players

## Test Scenarios

### Scenario 1: Both Players Join Fresh
1. Player A selects "girl_walnut_start.png" in character selection
2. Player A starts/joins server
3. Player B selects "boy_green_start.png" in character selection  
4. Player B joins server
5. **Expected**: Player A sees Player B as green boy, Player B sees Player A as walnut girl

### Scenario 2: Player Joins Existing Game
1. Player A (walnut girl) is already in game
2. Player B (green boy) joins
3. **Expected**: Both players see each other with correct sprites

### Scenario 3: Player Changes Character Mid-Game
1. Both players in game
2. Player A opens menu → Player Profile → Choose Character
3. Player A selects "girl_red_start.png"
4. Player A clicks "Save Player"
5. **Expected**: 
   - Player A sees their own sprite change to red girl
   - Player B sees Player A change to red girl

## Debug Output to Look For

When testing, check console for these messages:

**On Client Connect:**
```
[CLIENT] My selected character sprite: girl_walnut_start.png
[CLIENT] Sent player info to server: Player_abc123, sprite: girl_walnut_start.png
```

**On Server:**
```
[SERVER] Updated character sprite for abc123: girl_walnut_start.png
[SERVER] Broadcasting character update to other clients: girl_walnut_start.png
[SERVER] Sending character sprite to new client: Player_xyz789 -> boy_green_start.png
```

**On Other Clients:**
```
[CLIENT] Creating new remote player: Player_abc123 with sprite: girl_walnut_start.png
[CLIENT] Updating character sprite for Player_abc123 to girl_walnut_start.png
```

## Troubleshooting

If sprites still show as navy:

1. **Check PlayerConfig**: Make sure character selection is being saved
   - Look for `woodlanders.json` in config directory
   - Should contain: `"selectedCharacter": "girl_walnut_start.png"`

2. **Check Console Output**: Look for the debug messages above
   - If you don't see "[CLIENT] My selected character sprite:", the character isn't being loaded from config
   - If you don't see "[SERVER] Updated character sprite:", the server isn't receiving the message

3. **Check Sprite Files**: Verify the sprite files exist:
   - `assets/sprites/player/girl_walnut_start.png`
   - `assets/sprites/player/boy_green_start.png`
   - etc.

4. **Timing Issue**: If sprites are wrong initially but correct after character change:
   - This is expected - the initial world state uses defaults
   - The PlayerInfoMessage updates should arrive within 1 second
   - If they don't, there may be a network delay issue
