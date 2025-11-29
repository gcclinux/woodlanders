# Apple Sapling Drop - Manual Testing Guide

This document provides step-by-step instructions for manually testing the Apple Sapling dual-drop functionality.

## Test Environment Setup

1. Build the project:
   ```bash
   ./gradlew build
   ```

2. Run the game:
   ```bash
   ./gradlew run
   ```

## Test Case 1: Single-Player Dual-Drop Spawn

**Objective:** Verify that destroying an AppleTree spawns both Apple and AppleSapling items

**Steps:**
1. Start a new single-player game
2. Locate an AppleTree (large tree with apples)
3. Position your character near the tree
4. Press SPACEBAR repeatedly to attack the tree
5. Continue attacking until the tree is destroyed

**Expected Results:**
- ✓ Console log shows: "Apple tree destroyed! Apple dropped at: X, Y"
- ✓ Console log shows: "AppleSapling dropped at: X+8, Y"
- ✓ Two items appear on the ground at the tree's base position
- ✓ Items are positioned approximately 8 pixels apart horizontally
- ✓ Both items are visible and rendered at 32x32 pixels

**Requirements Validated:** 1.1, 1.2, 1.5, 2.1, 2.2

---

## Test Case 2: Apple Pickup

**Objective:** Verify that walking over an Apple picks it up

**Steps:**
1. After destroying a tree (from Test Case 1), walk over the Apple item
2. Observe the console output

**Expected Results:**
- ✓ Apple disappears from the game world
- ✓ Console shows pickup message (if implemented)
- ✓ No errors or crashes occur

**Requirements Validated:** 3.1

---

## Test Case 3: AppleSapling Pickup and Inventory

**Objective:** Verify that walking over an AppleSapling picks it up and adds it to inventory

**Steps:**
1. After destroying a tree, walk over the AppleSapling item
2. Observe the console output
3. Press 'I' to open inventory navigation mode
4. Check the inventory UI at the bottom of the screen

**Expected Results:**
- ✓ AppleSapling disappears from the game world
- ✓ Console shows: "AppleSapling removed from game"
- ✓ Inventory UI shows AppleSapling icon in slot 8 (rightmost slot)
- ✓ AppleSapling count displays next to the icon (should show "1")
- ✓ No errors or crashes occur

**Requirements Validated:** 3.2, 4.1, 4.3

---

## Test Case 4: Multiple AppleSapling Pickups

**Objective:** Verify that multiple AppleSaplings can be collected and counted

**Steps:**
1. Destroy 3 different AppleTrees
2. Pick up all 3 AppleSaplings
3. Check the inventory UI

**Expected Results:**
- ✓ Inventory shows AppleSapling count of 3
- ✓ Count increments correctly with each pickup
- ✓ All items are removed from the game world

**Requirements Validated:** 4.1

---

## Test Case 5: Save Game Persistence

**Objective:** Verify that AppleSapling count persists when saving the game

**Steps:**
1. Collect several AppleSaplings (e.g., 5)
2. Note the current AppleSapling count in inventory
3. Open the game menu (ESC key)
4. Select "Save World"
5. Enter a save name (e.g., "apple_test")
6. Save the game

**Expected Results:**
- ✓ Game saves successfully without errors
- ✓ Console shows save confirmation
- ✓ No crashes or errors occur

**Requirements Validated:** 4.5

---

## Test Case 6: Load Game Restoration

**Objective:** Verify that AppleSapling count is restored when loading a saved game

**Steps:**
1. After saving (from Test Case 5), exit to main menu or close the game
2. Start the game again
3. Open the game menu (ESC key)
4. Select "Load World"
5. Select the saved game from Test Case 5
6. Load the game
7. Press 'I' to check inventory

**Expected Results:**
- ✓ Game loads successfully
- ✓ AppleSapling count matches the saved value (e.g., 5)
- ✓ All other inventory items are also restored
- ✓ No errors or crashes occur

**Requirements Validated:** 4.6

---

## Test Case 7: Visual Verification

**Objective:** Verify that items render correctly with proper sprites and sizes

**Steps:**
1. Destroy an AppleTree
2. Observe the spawned items closely
3. Compare the sprites to the expected appearance

**Expected Results:**
- ✓ Apple sprite appears as a red apple (from sprite sheet coordinates 0, 128)
- ✓ AppleSapling sprite appears as a small tree sapling (from sprite sheet coordinates 192, 254)
- ✓ Both items render at 32x32 pixels (approximately half a tile)
- ✓ Items are clearly visible and distinguishable
- ✓ Items are positioned 8 pixels apart horizontally

**Requirements Validated:** 2.1, 2.2

---

## Test Case 8: Multiple Tree Destructions

**Objective:** Verify that multiple trees can be destroyed without issues

**Steps:**
1. Destroy 5 different AppleTrees in sequence
2. Observe item spawns for each tree
3. Pick up all items

**Expected Results:**
- ✓ Each tree spawns both Apple and AppleSapling
- ✓ All items are pickable
- ✓ Inventory counts correctly (5 apples, 5 saplings)
- ✓ No memory leaks or performance issues
- ✓ Console logs show correct positions for all items

**Requirements Validated:** 1.1, 1.2, 3.1, 3.2, 4.1

---

## Test Case 9: Pickup Range Verification

**Objective:** Verify that items are picked up within the correct range

**Steps:**
1. Destroy an AppleTree
2. Approach the items slowly from different directions
3. Note when the pickup occurs

**Expected Results:**
- ✓ Items are picked up when player is within 32 pixels of item center
- ✓ Items are NOT picked up when player is far away
- ✓ Pickup works from all directions (north, south, east, west)

**Requirements Validated:** 3.6 (from design document)

---

## Test Case 10: Edge Case - Rapid Pickup

**Objective:** Verify that picking up both items quickly doesn't cause issues

**Steps:**
1. Destroy an AppleTree
2. Walk quickly through both items to pick them up in rapid succession

**Expected Results:**
- ✓ Both items are picked up successfully
- ✓ No crashes or errors occur
- ✓ Inventory counts are correct (1 apple, 1 sapling)
- ✓ No duplicate pickups occur

**Requirements Validated:** 3.1, 3.2, 4.1

---

## Automated Test Results

The following automated tests have been run and passed:

✓ testDualItemSpawnWithCorrectPositioning - Verifies position calculations
✓ testItemKeyNamingConvention - Verifies key format
✓ testInventoryIntegration - Verifies inventory addition
✓ testMultiplePickups - Verifies multiple pickups
✓ testSaveLoadRoundTrip - Verifies save/load persistence
✓ testSaveLoadWithZeroCount - Verifies edge case
✓ testSaveLoadWithLargeCount - Verifies edge case
✓ testTextureDisposal - Verifies dispose pattern
✓ testMultipleTreeDestructions - Verifies multiple spawns
✓ testItemTypeEnumSupport - Verifies enum support

All automated tests passed successfully.

---

## Test Completion Checklist

Mark each test case as you complete it:

- [ ] Test Case 1: Single-Player Dual-Drop Spawn
- [ ] Test Case 2: Apple Pickup
- [ ] Test Case 3: AppleSapling Pickup and Inventory
- [ ] Test Case 4: Multiple AppleSapling Pickups
- [ ] Test Case 5: Save Game Persistence
- [ ] Test Case 6: Load Game Restoration
- [ ] Test Case 7: Visual Verification
- [ ] Test Case 8: Multiple Tree Destructions
- [ ] Test Case 9: Pickup Range Verification
- [ ] Test Case 10: Edge Case - Rapid Pickup

---

## Known Issues

Document any issues found during testing:

1. [Issue description]
2. [Issue description]

---

## Notes

- The AppleSapling is rendered at 32x32 pixels (as specified in requirements)
- The Apple is rendered at 24x24 pixels (existing implementation)
- Items spawn with an 8-pixel horizontal offset
- Pickup range is 32 pixels from player center to item center
- AppleSapling appears in inventory slot 8 (rightmost slot)

---

## Conclusion

After completing all test cases, verify that:
- All requirements (1.1, 1.2, 2.1, 2.2, 3.1, 3.2, 4.1, 4.3, 4.5, 4.6) are validated
- No crashes or errors occurred
- All functionality works as expected
- Save/load persistence is working correctly

If all tests pass, the Apple Sapling Drop feature is ready for production.
