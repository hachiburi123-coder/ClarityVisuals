package ru.method.clarityvisuals.utils.render;

import java.util.Arrays;

/**
 * Примитивный open-addressing кэш "ключ чанка → высота поверхности" без боксинга:
 * вместо HashMap&lt;Long, Integer&gt;, который аллоцировал Long + Integer на каждый
 * вызов при спавне/рендере тысяч частиц за кадр.
 *
 * <p>Пустая ячейка определяется значением {@link #MISS} в массиве значений —
 * высоты мира никогда не бывают Integer.MIN_VALUE. При заполнении таблица
 * удваивается до {@link #MAX_CAPACITY}, затем полностью сбрасывается — то же
 * поведение, что у прежней HashMap с периодическим clear().</p>
 */
public final class GroundHeightCache {
    public static final int MISS = Integer.MIN_VALUE;
    private static final int INITIAL_CAPACITY = 1 << 12;
    private static final int MAX_CAPACITY = 1 << 15;

    private long[] keys = new long[INITIAL_CAPACITY];
    private int[] values = new int[INITIAL_CAPACITY];
    private int mask = INITIAL_CAPACITY - 1;
    private int limit = INITIAL_CAPACITY * 3 / 4;
    private int size;

    private static int spread(long key) {
        int h = (int)(key ^ key >>> 32);
        return h * 0x9E3779B9;
    }

    public int get(long key) {
        long[] k = this.keys;
        int[] v = this.values;
        int idx = GroundHeightCache.spread(key) & this.mask;
        while (true) {
            int value = v[idx];
            if (value == MISS) {
                return MISS;
            }
            if (k[idx] == key) {
                return value;
            }
            idx = idx + 1 & this.mask;
        }
    }

    public void put(long key, int value) {
        if (this.size >= this.limit) {
            if (this.values.length < MAX_CAPACITY) {
                this.grow();
            } else {
                this.reset();
            }
        }
        long[] k = this.keys;
        int[] v = this.values;
        int idx = GroundHeightCache.spread(key) & this.mask;
        while (true) {
            int current = v[idx];
            if (current == MISS) {
                k[idx] = key;
                v[idx] = value;
                this.size++;
                return;
            }
            if (k[idx] == key) {
                v[idx] = value;
                return;
            }
            idx = idx + 1 & this.mask;
        }
    }

    public void clear() {
        this.reset();
    }

    private void reset() {
        Arrays.fill(this.values, MISS);
        this.size = 0;
    }

    private void grow() {
        int newCapacity = this.values.length << 1;
        long[] oldKeys = this.keys;
        int[] oldValues = this.values;
        this.keys = new long[newCapacity];
        this.values = new int[newCapacity];
        this.mask = newCapacity - 1;
        this.limit = newCapacity * 3 / 4;
        this.size = 0;
        for (int i = 0; i < oldValues.length; i++) {
            int value = oldValues[i];
            if (value != MISS) {
                this.put(oldKeys[i], value);
            }
        }
    }
}
