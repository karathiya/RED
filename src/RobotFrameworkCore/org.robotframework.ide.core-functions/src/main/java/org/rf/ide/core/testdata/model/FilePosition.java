/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.rf.ide.core.testdata.model.table.ECompareResult;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;

import com.google.common.base.Objects;

public class FilePosition implements Serializable {

    private static final long serialVersionUID = 5481992321406778250L;

    public static final int NOT_SET = IRobotLineElement.NOT_SET;

    private final int line;

    private final int column;

    private final int offset;

    public FilePosition(final int line, final int column) {
        this(line, column, NOT_SET);
    }

    public FilePosition(final int line, final int column, final int offset) {
        this.line = line;
        this.column = column;
        this.offset = offset;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return String.format("FilePosition [line=%s, column=%s, offset=%s]", line, column, offset);
    }

    public static FilePosition createNotSet() {
        return new FilePosition(NOT_SET, NOT_SET, NOT_SET);
    }

    public boolean isNotSet() {
        return getLine() == NOT_SET && getColumn() == NOT_SET && getOffset() == NOT_SET;
    }

    public boolean isSet() {
        return !isNotSet();
    }

    public boolean isBefore(final FilePosition other) {
        return compare(other) == ECompareResult.LESS_THAN.getValue();
    }

    public boolean isAfter(final FilePosition other) {
        return compare(other) == ECompareResult.GREATER_THAN.getValue();
    }

    public boolean isSamePlace(final FilePosition other) {
        return compare(other) == ECompareResult.EQUAL_TO.getValue();
    }

    public int compare(final FilePosition other) {
        return compare(other, true);
    }

    public int compare(final FilePosition other, final boolean shouldCheckOffset) {
        ECompareResult result;
        if (other != null) {
            final int otherOffset = other.getOffset();
            final int otherLine = other.getLine();
            final int otherColumn = other.getColumn();

            if (shouldCheckOffset) {
                result = compare(offset, otherOffset);
            } else {
                result = ECompareResult.COMPARE_NOT_SET;
            }

            if (result == ECompareResult.COMPARE_NOT_SET || result == ECompareResult.EQUAL_TO) {
                result = compare(line, otherLine);
            }
            if (result == ECompareResult.COMPARE_NOT_SET || result == ECompareResult.EQUAL_TO) {
                result = compare(column, otherColumn);
            }

            if (result == ECompareResult.COMPARE_NOT_SET) {
                result = ECompareResult.EQUAL_TO;
            }
        } else {
            result = ECompareResult.GREATER_THAN;
        }

        return result.getValue();
    }

    private ECompareResult compare(final int valuePosO1, final int valuePosO2) {
        ECompareResult result;
        if (valuePosO1 != NOT_SET && valuePosO2 != NOT_SET) {
            result = ECompareResult.map(Integer.compare(valuePosO1, valuePosO2));
        } else if (valuePosO1 != NOT_SET) {
            result = ECompareResult.LESS_THAN;
        } else if (valuePosO2 != NOT_SET) {
            result = ECompareResult.GREATER_THAN;
        } else {
            result = ECompareResult.COMPARE_NOT_SET;
        }

        return result;
    }

    private Object readResolve() throws ObjectStreamException {
        // we wan't file position object to be NOT_SET when deserializing
        return createNotSet();
    }

    public FilePosition copy() {
        return new FilePosition(getLine(), getColumn(), getOffset());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(column, line, offset);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == getClass()) {
            final FilePosition that = (FilePosition) obj;
            return this.line == that.line && this.column == that.column && this.offset == that.offset;
        }
        return false;
    }
}
