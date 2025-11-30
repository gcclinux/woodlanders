# Testing the Tree Synchronization Fix

## Quick Test Guide

### Setup

1. **Build the game**:
   ```bash
   ./gradlew build
   ```

2. **Start the server**:
   ```bash
   ./start-server.sh
   ```

3. **Start two game clients** (in separate terminals):
   ```bash
   # Terminal 1 - Player 1
   ./gradlew run
   
   # Terminal 2 - Player 2  
   ./gradlew run
   ```

### Test Procedure

#### Test 1: Basic Tree Synchronization

1. **Player 1**: Connect to the server (or host)
2. **Player 2**: Connect to the same server
3. **Player 1**: Move far from spawn (at least 3000 pixels away)
   - Use WASD keys to move
   - Check coordinates in the UI
4. **Player 1**: Attack a tree in this area
   - Press SPACE near a tree
   - Observe the tree taking damage
5. **Player 2**: Move to the same location as Player 1
6. **Verify**: Player 2 should see the same tree that Player 1 attacked
7. **Player 2**: Attack the same tree
8. **Verify**: Both players see the tree's health decrease

**Expected Result**: ✅ Both players see the same tree and can interact with it

#### Test 2: Multiple Trees

1. **Player 1**: Attack multiple trees in a new area (beyond 2500 pixels from spawn)
2. **Player 2**: Move to the same area
3. **Verify**: Player 2 sees all the trees that Player 1 attacked

**Expected Result**: ✅ All trees are synchronized between players

#### Test 3: Stone Synchronization

1. **Player 1**: Move to a sand biome area (look for yellow/tan ground)
2. **Player 1**: Attack a stone (gray rock object)
3. **Player 2**: Move to the same location
4. **Verify**: Player 2 sees the same stone

**Expected Result**: ✅ Stones are also synchronized

### Server Log Verification

Check the server console output for these messages:

```
[TREE_SYNC] Generated tree 3000,3000 (type: SMALL) at (3000.0, 3000.0)
[TREE_SYNC] Broadcasting newly generated tree to all clients
[TREE_SYNC] Broadcast complete - all clients should now see tree 3000,3000
```

If you see these messages, the fix is working correctly!

### Common Issues

#### Issue: Player 2 doesn't see the tree

**Possible Causes**:
1. Network connection issue - check if both clients are connected
2. Player 2 is not in the exact same location - move closer
3. Server didn't generate the tree - check server logs

**Solution**: 
- Verify both clients show "Connected to server" in the UI
- Check server logs for `[TREE_SYNC]` messages
- Ensure Player 2 is within viewing distance of the tree

#### Issue: Tree appears but can't be attacked

**Possible Causes**:
1. Tree is out of attack range (100 pixels)
2. Client-side rendering issue

**Solution**:
- Move closer to the tree (within 100 pixels)
- Try restarting the client

### Success Criteria

The fix is working if:
- ✅ Both players see the same trees in all areas
- ✅ Trees generated on-demand are visible to all players
- ✅ Tree health updates are synchronized
- ✅ No "ghost trees" that only one player can see
- ✅ Server logs show `[TREE_SYNC]` broadcast messages

### Performance Check

Monitor the server console for:
- No excessive `[TREE_SYNC]` messages (should only appear when new areas are explored)
- No lag or delays when trees are generated
- Smooth gameplay for both clients

## Advanced Testing

### Stress Test: Many Players

1. Connect 3-4 clients to the same server
2. Have each player explore different areas
3. Have all players converge on one area
4. Verify all players see the same trees

### Edge Cases

1. **Very far distances**: Test at 10,000+ pixels from spawn
2. **Rapid exploration**: Move quickly through new areas
3. **Simultaneous attacks**: Have both players attack the same tree at the same time

## Debugging

If issues persist:

1. **Enable debug logging**:
   - Check server logs for `[TREE_SYNC]` messages
   - Check client logs for `[GameMessageHandler]` messages

2. **Verify message flow**:
   - Server should broadcast `WorldStateUpdateMessage`
   - Clients should receive and process the message
   - Clients should create trees locally

3. **Check network connectivity**:
   - Ensure both clients are connected to the server
   - Check for firewall issues
   - Verify server IP and port are correct

## Reporting Issues

If you find any issues with tree synchronization, please report:

1. **Steps to reproduce**
2. **Server logs** (especially `[TREE_SYNC]` messages)
3. **Client logs** (if available)
4. **Screenshots** showing the issue
5. **Number of players** connected
6. **Approximate coordinates** where the issue occurred

---

**Note**: This fix addresses the core tree synchronization issue. If you encounter other multiplayer issues (inventory sync, player movement, etc.), those are separate systems and may require additional fixes.
