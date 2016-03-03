package es.uvigo.darwin.jmodeltest.utilities;

import java.util.Arrays;

/**
 * A bit-set of fixed size. Size is determined on creation.
 *
 * @author Joseph Heled
 * @version $Id: FixedBitSet.java 591 2006-12-21 02:39:18Z pepster $
 */
public class FixedBitSet implements Comparable<FixedBitSet> {
    int[] bits;
    int size;
    //private int intSize = Integer.SIZE;

    private final static int ADDRESS_BITS_PER_UNIT = 5;
    private final static int BITS_PER_UNIT = 1 << ADDRESS_BITS_PER_UNIT;
    private final static int BIT_INDEX_MASK = BITS_PER_UNIT - 1;


    private static int unitIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_UNIT;
    }

    private int countBits(int b) {
        int sum = 0;

        while (b != 0) {
            // remove most significant bit
            b = b & (b - 1);
            ++sum;
        }
        return sum;
    }

    /**
     * Given a bit index, return a unit that masks that bit in its unit.
     * @return the mask
     */
    private static int bit(int bitIndex) {
        return 1 << (bitIndex & BIT_INDEX_MASK);
    }

    public FixedBitSet(int size) {
        this.size = size;
        bits = new int[(unitIndex(size - 1) + 1)];
    }

    public FixedBitSet(FixedBitSet bs) {
        bits = bs.bits.clone();
        size = bs.size;
    }


    public void set(int position) {
        int unitIndex = unitIndex(position);
        bits[unitIndex] |= bit(position);
    }

    public void clear(int position) {
        int unitIndex = unitIndex(position);
        bits[unitIndex] &= ~bit(position);
    }

    /**
     * @param bitset
     * @return true if bitset contains this set (this <= bitset)
     */
    public boolean setInclusion(final FixedBitSet bitset) {
        for (int k = 0; k < bits.length; ++k) {
            if (bits[k] != (bits[k] & bitset.bits[k])) {
                return false;
            }
        }
        return true;
    }

    public void union(FixedBitSet b) {
        for (int k = 0; k < Math.min(bits.length, b.bits.length); ++k) {
            bits[k] |= b.bits[k];
        }
    }

    public void intersect(FixedBitSet b) {
        for (int k = 0; k < Math.min(bits.length, b.bits.length); ++k) {
            bits[k] &= b.bits[k];
        }
    }

    public void setMinus(FixedBitSet b) {
        for (int k = 0; k < Math.min(bits.length, b.bits.length); ++k) {
            bits[k] &= ~b.bits[k];
        }
    }

    public int intersectCardinality(FixedBitSet b) {
        int c = 0;
        for (int k = 0; k < Math.min(bits.length, b.bits.length); ++k) {
            c += countBits(bits[k] & b.bits[k]);
        }
        return c;
    }

    public static FixedBitSet complement(FixedBitSet b) {
        FixedBitSet t = new FixedBitSet(b);
        t.complement();
        return t;
    }

    public void complement() {
        int k;
        for (k = 0; k < bits.length - 1; ++k) {
            bits[k] = ~ bits[k];
        }

        bits[k] = ~bits[k];
        // reset all higher order bits
        final int mask = bit(size) - 1;
        if( mask != 0 ) {
            bits[k] &= mask;
        }
    }

    private final static byte firstBitLocation[] = {
            -1, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
            4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0};

    private int firstOnBit(int i) {
        for (int k = 0; k < 4; ++k) {
            char b = (char) (i & 0xff);
            if (b != 0) {
                return 8 * k + firstBitLocation[b];
            }
            i = i >> 8;
        }
        return -1;
    }

    /**
     * Iteration helper. A typical iteration on set bits might be
     * FixedBitSet b;
     * for(int i = b.nextOnBit(0); i >= 0; i = b.nextOnBit(i+1)) ...
     *
     * @param fromIndex
     * @return Next set member whose index is >= fromIndex. -1 if none.
     */
    public int nextOnBit(int fromIndex) {
        int u = unitIndex(fromIndex);
        int testIndex = (fromIndex & BIT_INDEX_MASK);
        int unit = bits[u] >> testIndex;

        if (unit == 0) {
            testIndex = 0;

            while ((unit == 0) && (u < bits.length - 1))
                unit = bits[++u];
        }

        if (unit == 0)
            return -1;

        testIndex += firstOnBit(unit);
        return ((u * BITS_PER_UNIT) + testIndex);
    }

    public int cardinality() {
        int sum = 0;
        for (int b : bits) {
            sum += countBits(b);
        }
        return sum;
    }

    public boolean contains(final int i) {
        final int unitIndex = unitIndex(i);
        return (bits[unitIndex] & bit(i)) != 0;
    }

    @Override
    public int hashCode() {
        int code = 0;

        for (int bit : bits) {
            code = code ^ bit;
        }
        return code;
    }

    @Override
    public boolean equals(Object x) {
        if (x instanceof FixedBitSet) {
            final FixedBitSet b = (FixedBitSet) x;

            return b.size == size && Arrays.equals(bits, b.bits);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder rep = new StringBuilder();
        rep.append("{");
        for (int b = 0; b < size; ++b) {
            if (contains(b)) {
                if (rep.length() > 0) {
                    rep.append("," + b);
                } else {
                    rep.append("" + b);
                }
            }
        }
        rep.append("}");
        return rep.toString();
    }
    
    public String splitRepresentation() {
        StringBuilder rep = new StringBuilder();
        for (int b = 0; b < size; ++b) {
            if (contains(b)) {
                rep.append("*");
            }
            else {
                rep.append("-");
            }
        }
        return rep.toString();
    }

    public int compareTo(FixedBitSet fbs) {
        int minSize;
        int defaultValue;
        if (size < fbs.size) {
            minSize = size;
            defaultValue = -1;
        } else {
            minSize = fbs.size;
            if (size == fbs.size)
                defaultValue = 0;
            else
                defaultValue = 1;
        }
        
        for (int i = 0; i < minSize; i++) {
            if (this.contains(i) && !fbs.contains(i)) {
                return -1;
            }
            if (!this.contains(i) && fbs.contains(i)) {
                return 1;
            }
        }
        return defaultValue;
    }

}