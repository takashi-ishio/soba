/**
 * This class is implemented based on SourceValue involved in ASM.
 */
/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package soba.core.method.asm;



import java.util.Arrays;

import gnu.trove.list.array.TIntArrayList;

import org.objectweb.asm.tree.analysis.Value;

public class FastSourceValue implements Value {

    /**
     * The number of words indicating the value.
     */
    private final int size;

    /**
     * Index of instructions that define the value.
     */
    private int[] instructions;
    
    private static int[] EMPTY_ARRAY = new int[0];

    public FastSourceValue(int size) {
    	this.size = size;
    	this.instructions = EMPTY_ARRAY;
    }

    public FastSourceValue(int size, int instructionIndex) {
        this.size = size;
        this.instructions = new int[] { instructionIndex };
    }

    public FastSourceValue(int size, int[] instructionIndices) {
        this.size = size;
        this.instructions = instructionIndices;
    }
    
    public int getSize() {
        return size;
    }
    
    public int[] getInstructions() {
    	return instructions;
    }
    
    public FastSourceValue(FastSourceValue base1, FastSourceValue base2) {
    	this.size = Math.min(base1.size, base2.size);
    	TIntArrayList list = new TIntArrayList(base1.instructions.length + base2.instructions.length);
    	int index1 = 0;
    	int index2 = 0;
    	while (index1 < base1.instructions.length && index2 < base2.instructions.length) {
			int v1 = base1.instructions[index1];
			int v2 = base2.instructions[index2];
			if (v1 == v2) {
				// Add only one element (behaves as "Set")
				list.add(v1);
				index1++;
				index2++;
			} else if (v1 < v2) {
				list.add(v1);
				index1++;
			} else { //v1 > v2
				list.add(v2);
				index2++;
			}
    	}
    	while (index1 < base1.instructions.length) {
    		list.add(base1.instructions[index1]);
    		index1++;
    	}
    	while (index2 < base2.instructions.length) {
    		list.add(base2.instructions[index2]);
    		index2++;
    	}
    	this.instructions = list.toArray();
    }
    
    public boolean containsAll(FastSourceValue another) {
    	int thisIndex = 0;
    	int anotherIndex = 0;
    	while (anotherIndex < another.instructions.length) {
    		if (thisIndex < this.instructions.length) {
    			int thisValue = instructions[thisIndex];
    			int anotherValue = another.instructions[anotherIndex];
    			if (thisValue > anotherValue) {
    				return false;
    			} else if (thisValue == anotherValue) {
    				thisIndex++;
    				anotherIndex++;
    			} else { // thisValue < anotherValue
    				thisIndex++;
    			}
    			
    		} else {
    			// End of this.instructions, but not the end of another.instruction
    			return false;
    		}
    	}
    	return true;
    }

    /**
     * Two FastSourceValues are the same if the value 
     * is defined by the same instructions.
     */
    public boolean equals(Object another) {
    	if (another instanceof FastSourceValue) {
            FastSourceValue v = (FastSourceValue)another;
            return size == v.size && Arrays.equals(instructions, v.instructions);
    	} else {
    		return false;
    	}
    }

    public int hashCode() {
    	return Arrays.hashCode(instructions);
    }

}
