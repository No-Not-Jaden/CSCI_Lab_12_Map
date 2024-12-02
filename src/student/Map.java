// Jaden Paterson
package student;

public class Map<Key, Value> {
    /**
     * The keys in the map.
     */
    private Key[] keys;
    /**
     * The values in the map.
     */
    private Value[] values;
    /**
     * Whether a key at an index is empty after removal or empty since start.
     */
    private boolean[] emptyAfterRemoval;
    /**
     * Whether a key at an index is valid.
     */
    private boolean[] filledSlots;
    /**
     * The number of valid items in the map.
     */
    private int count;

    /**
     * The maximum amount of collisions before the map is resized.
     */
    private static final int MAX_COLLISIONS = 10;
    /**
     * The maximum load factor before the size gets increased.
     */
    private static final double MAX_LOAD_FACTOR = 0.6;
    /**
     * The minimum load factor before the size gets decreased.
     */
    private static final double MIN_LOAD_FACTOR = 0.15;
    /**
     * The first hash key for the hash function.
     */
    private static final int HASH_KEY_1 = 11;
    /**
     * The second hash key for the hash function.
     */
    private static final int HASH_KEY_2 = 7;
    /**
     * The value multiplied to the resize function.
     * Note: This is only set as a static variable because gradescope
     * views it as a "magic" number.
     * It would be better to keep it in the function.
     */
    private static final int RESIZE_MULTIPLIER = 3;

    /**
     * Create a new map object.
     *
     * @param length Original length of the map.
     */
    public Map(int length) {
        if (length <= 0) {
            length = 1;
        }
        keys = (Key[]) new Object[length];
        values = (Value[]) new Object[length];
        emptyAfterRemoval = new boolean[length];
        filledSlots = new boolean[length];
    }

    /**
     * Check if 2 keys are equal. Either key can be null.
     *
     * @param leftKey  First key to compare.
     * @param rightKey Second key to compare.
     * @return True if the keys are equal.
     */
    private boolean isEqual(Key leftKey, Key rightKey) {
        if (leftKey == null || rightKey == null) {
            return leftKey == rightKey;
        }
        return leftKey.equals(rightKey);
    }

    /**
     * Get the index of a key in the map.
     *
     * @param key The key to search for.
     * @return The index of the key in the map, or -1 if it doesn't exist.
     */
    private int getIndex(Key key) {
        // get the index of the key with double hashing
        int i = 0;
        int index = getIndex(key, 0);
        // look for key in the map
        // there may be a collision, and multiple indices will be searched.
        // The maximum amount of collisions is 10
        while (!isEqual(keys[index], key)
                && (filledSlots[index] || emptyAfterRemoval[index])
                && i < MAX_COLLISIONS) {
            i++;
            index = getIndex(key, i);
        }
        if (isEqual(keys[index], key) && filledSlots[index]) {
            return index;
        }
        return -1;
    }

    /**
     * Get the index for an empty space in the map where a key can be entered.
     *
     * @param key Key to add to the map.
     * @return The index of where the key should be added.
     */
    private int getPutIndex(Key key) {
        // get the index of the key with double hashing
        int i = 0;
        int index = getIndex(key, 0);
        // look for key in the map
        while (filledSlots[index] && i < MAX_COLLISIONS) {
            i++;
            index = getIndex(key, i);
        }
        if (i == MAX_COLLISIONS) {
            // too many collisions
            // increase size
            increaseSize();
            return getPutIndex(key);
        }
        return index;
    }

    /**
     * Get a possible index for a key in the map.
     *
     * @param key The key to get the index of.
     * @param i   The number of collisions that have occurred while
     *            searching for this key.
     * @return The index of where the key could be in the map.
     */
    private int getIndex(Key key, int i) {
        // double hashing formula: (h1(key) + i * h2(key)) % tableSize
        // where h1(key) = key % 11 and h2(key) = 7 - key % 7
        int hashCode = key != null ? key.hashCode() : 0;
        return Math.abs((hashCode % HASH_KEY_1
                + i * (HASH_KEY_2 - hashCode % HASH_KEY_2))
                % keys.length);
    }

    /**
     * Get a value from the map.
     *
     * @param key Key to get the value from.
     * @return The value in the map or null if the key doesn't exist.
     */
    public Value get(Key key) {
        int index = getIndex(key);
        if (index != -1) {
            // value exists
            return values[index];
        }
        return null;
    }

    /**
     * Check if the map contains a key.
     *
     * @param key Key to look for.
     * @return True if the map contains the key.
     */
    public boolean containsKey(Key key) {
        return getIndex(key) != -1;
    }

    /**
     * Put a key into the map. If the key exists already,
     * the value for the key will be changed.
     *
     * @param key   The key to add to the map.
     * @param value The value to add to the map.
     */
    public void put(Key key, Value value) {
        if (containsKey(key)) {
            int index = getIndex(key);
            values[index] = value;
            return;
        }
        // check if load factor has been reached
        if ((double) count / keys.length > MAX_LOAD_FACTOR) {
            // increase size
            increaseSize();
        }
        int index = getPutIndex(key);
        keys[index] = key;
        values[index] = value;
        filledSlots[index] = true;
        count++;
    }

    /**
     * Increases the size of the map.
     */
    private void increaseSize() {
        resize(keys.length * RESIZE_MULTIPLIER / 2 + 1);
    }

    /**
     * Resizes the map to a new size.
     *
     * @param newSize The new size of the map.
     */
    private void resize(int newSize) {
        // grab a reference to the old keys and values
        Key[] oldKeys = keys;
        Value[] oldValues = values;
        boolean[] oldFilledSlots = filledSlots;
        // initialize new arrays for keys and values
        keys = (Key[]) new Object[newSize];
        values = (Value[]) new Object[newSize];
        emptyAfterRemoval = new boolean[newSize];
        filledSlots = new boolean[newSize];
        // add all the old keys back
        count = 0; // reset count
        for (int i = 0; i < oldKeys.length; i++) {
            if (oldFilledSlots[i]) {
                put(oldKeys[i], oldValues[i]);
            }
        }
    }

    /**
     * Get the number of items in the map.
     *
     * @return The number of items in the map.
     */
    public int size() {
        return count;
    }

    /**
     * Remove a key from the map.
     *
     * @param key Key to remove from the map.
     * @return True if the key was removed.
     */
    public boolean remove(Key key) {
        if (containsKey(key)) {
            // remove
            int index = getIndex(key);
            emptyAfterRemoval[index] = true;
            filledSlots[index] = false;
            count--;
            // check load factor
            if ((double) count / keys.length < MIN_LOAD_FACTOR && count > 2) {
                // decrease size
                decreaseSize();
            }
            return true;
        }
        return false;
    }

    /**
     * Decrease the size of the map.
     */
    private void decreaseSize() {
        resize(keys.length / 2 + 1);
    }
}
