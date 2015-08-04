/**
 * Note: this class is implemented based on SourceInterpreter involved in ASM.
 * 
 * This class is different from SourceInterpreter:
 * SourceInterpreter uses an empty set to represent a data-flow from a method parameter.
 * Therefore, the class fails to merge a data-flow path from a method parameter 
 * and aother path that overwrites the value.
 * An example code is soba.testdata.DefUseTestData.overwriteParam().
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

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

import soba.util.ObjectIdMap;

public class FastSourceInterpreter extends Interpreter<Value> implements Opcodes {
	
	public static final int METHOD_ENTRY = -1;
	private ObjectIdMap<AbstractInsnNode> instructions;
	
	public FastSourceInterpreter(ObjectIdMap<AbstractInsnNode> instructions) {
		super(ASM5);
		this.instructions = instructions;
	}

	/**
	 * This implementation is different from SourceInterpreter.
	 * We distinguish a method parameter with an "un-initialized" 
	 * entry for double-word data.
	 */
    public Value newValue(final Type type) {
    	if (type == null) {
    		// an anonymous value that fills the second entry for double-word data.
    		return new FastSourceValue(1);
    	} else if (type == Type.VOID_TYPE) {
            return null;
        } else {
            return new FastSourceValue(type.getSize(), METHOD_ENTRY);
        }
    }

    public Value newOperation(final AbstractInsnNode insn) {
        int size;
        switch (insn.getOpcode()) {
            case LCONST_0:
            case LCONST_1:
            case DCONST_0:
            case DCONST_1:
                size = 2;
                break;
            case LDC:
                Object cst = ((LdcInsnNode) insn).cst;
                size = cst instanceof Long || cst instanceof Double ? 2 : 1;
                break;
            case GETSTATIC:
                size = Type.getType(((FieldInsnNode) insn).desc).getSize();
                break;
            default:
                size = 1;
        }
        return new FastSourceValue(size, instructions.getId(insn));
    }

    public Value copyOperation(final AbstractInsnNode insn, final Value value) {
        return new FastSourceValue(value.getSize(), instructions.getId(insn));
    }

    public Value unaryOperation(final AbstractInsnNode insn, final Value value)
    {
        int size;
        switch (insn.getOpcode()) {
            case LNEG:
            case DNEG:
            case I2L:
            case I2D:
            case L2D:
            case F2L:
            case F2D:
            case D2L:
                size = 2;
                break;
            case GETFIELD:
                size = Type.getType(((FieldInsnNode) insn).desc).getSize();
                break;
            default:
                size = 1;
        }
        return new FastSourceValue(size, instructions.getId(insn));
    }

    public Value binaryOperation(
        final AbstractInsnNode insn,
        final Value value1,
        final Value value2)
    {
        int size;
        switch (insn.getOpcode()) {
            case LALOAD:
            case DALOAD:
            case LADD:
            case DADD:
            case LSUB:
            case DSUB:
            case LMUL:
            case DMUL:
            case LDIV:
            case DDIV:
            case LREM:
            case DREM:
            case LSHL:
            case LSHR:
            case LUSHR:
            case LAND:
            case LOR:
            case LXOR:
                size = 2;
                break;
            default:
                size = 1;
        }
        return new FastSourceValue(size, instructions.getId(insn));
    }

    public Value ternaryOperation(
        final AbstractInsnNode insn,
        final Value value1,
        final Value value2,
        final Value value3)
    {
        return new FastSourceValue(1, instructions.getId(insn));
    }

    public Value naryOperation(final AbstractInsnNode insn, final List<? extends Value> values) {
        int size;
        if (insn.getOpcode() == MULTIANEWARRAY) {
            size = 1;
        } else if (insn.getOpcode() == INVOKEDYNAMIC) {
        	 size = Type.getReturnType(((InvokeDynamicInsnNode) insn).desc).getSize();
        } else {
            size = Type.getReturnType(((MethodInsnNode) insn).desc).getSize();
        }
        return new FastSourceValue(size, instructions.getId(insn));
    }

    public void returnOperation(
        final AbstractInsnNode insn,
        final Value value,
        final Value expected)
    {
    }

    /**
     * Implementation Note: This method must return v 
     * if v contains all elements in w.
     */
    public Value merge(final Value v, final Value w) {
        if (v == w) return v;
        FastSourceValue dv = (FastSourceValue)v;
        FastSourceValue dw = (FastSourceValue)w;

    	if (dv.getSize() == dw.getSize() && dv.containsAll(dw)) {
        	return v;
        } else {
            return new FastSourceValue(dv, dw);
        }
    }

}
