package org.gridman.encoding.ndr;

/**
 * This class represents an NDR pointer and its referenced value.
 *
 * An embedded reference pointer is represented in two parts, a 4 octet value in place
 * and a possibly deferred representation of the referent.
 *
 * The reference pointer itself is represented by 4 octets of unspecified value. The four
 * octets are aligned as if they were a long integer.
 *
 * The special case of an toByteArray of reference pointers embedded in a structure has no NDR
 * representation, that is; there is no 4-byte unspecified value transmitted.
 *
 * The representation of the referent of the reference pointer may be deferred to a later position
 * in the octet stream.
 *
 * If a pointer is embedded in an toByteArray, structure or union, the representation of its referent is deferred
 * to a position in the octet stream that follows the representation of the construction that embeds the pointer.
 * Representations of pointer referents are ordered according to a left-to-right, depth-first traversal of the
 * embedding construction. Following is an elaboration of the deferral algorithm in detail:
 * If an toByteArray, structure, or union embeds a pointer, the representation of the referent of the
 * pointer is deferred to a position in the octet stream that follows the representation of the embedding
 * construction.
 * If an toByteArray or structure embeds more than one pointer, all pointer referent representations are deferred,
 * and the order in which referents are represented is the order in which their pointers appear in place in the toByteArray
 * or structure.
 * If an toByteArray, structure or union embeds another toByteArray, structure or union, referent representations for the embedded
 * construction are further deferred to a position in the octet stream that follows the representation of the embedding
 * construction. The set of referent representations for the embedded construction is inserted among the referent
 * representations for any pointers in the embedding construction, according to the order of elements or members in the
 * embedding construction.
 * The deferral of referent representations iterates through all successive embedding arrays, structures, and unions
 * to the outermost toByteArray, structure or union.
 * 
 * @author Jonathan Knight
 */
public class NDREmbeddedPointer<T> {
    private int pointer;
    private T value;

    public NDREmbeddedPointer(int pointer) {
        this.pointer = pointer;
    }

    public int getPointer() {
        return pointer;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
