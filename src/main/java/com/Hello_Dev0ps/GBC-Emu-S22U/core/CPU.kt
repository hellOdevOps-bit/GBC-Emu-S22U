package com.Hello_Dev0ps.gbc_emu_s22u

class CPU(private val memory: Memory) {
    // Registres principaux
    var af: UShort = 0u
    var bc: UShort = 0u
    var de: UShort = 0u
    var hl: UShort = 0u
    
    // Registres de pile et compteur de programme
    var sp: UShort = 0u
    var pc: UShort = 0u
    
    // Flags du registre F
    private var zeroFlag: Boolean = false
    private var subtractFlag: Boolean = false
    private var halfCarryFlag: Boolean = false
    private var carryFlag: Boolean = false
    
    // Interruptions
    private var ime: Boolean = false // Interrupt Master Enable
    
    // Cycles d'horloge
    private var cycles: Int = 0
    
    private val cbHandler = CBOpcodeHandler(this, memory)
    
    // Méthodes d'accès aux flags
    fun getZeroFlag(): Boolean = zeroFlag
    fun getSubtractFlag(): Boolean = subtractFlag
    fun getHalfCarryFlag(): Boolean = halfCarryFlag
    fun getCarryFlag(): Boolean = carryFlag
    
    fun setZeroFlag(value: Boolean) { zeroFlag = value }
    fun setSubtractFlag(value: Boolean) { subtractFlag = value }
    fun setHalfCarryFlag(value: Boolean) { halfCarryFlag = value }
    fun setCarryFlag(value: Boolean) { carryFlag = value }
    
    // Accès aux registres 8 bits
    fun getA(): UByte = (af shr 8).toUByte()
    fun getF(): UByte = (af and 0xFFu).toUByte()
    fun getB(): UByte = (bc shr 8).toUByte()
    fun getC(): UByte = (bc and 0xFFu).toUByte()
    fun getD(): UByte = (de shr 8).toUByte()
    fun getE(): UByte = (de and 0xFFu).toUByte()
    fun getH(): UByte = (hl shr 8).toUByte()
    fun getL(): UByte = (hl and 0xFFu).toUByte()

    fun setA(value: UByte) { af = ((af and 0x00FFu) or (value.toInt() shl 8).toUShort()) }
    fun setF(value: UByte) { af = ((af and 0xFF00u) or value.toUShort()) }
    fun setB(value: UByte) { bc = ((bc and 0x00FFu) or (value.toInt() shl 8).toUShort()) }
    fun setC(value: UByte) { bc = ((bc and 0xFF00u) or value.toUShort()) }
    fun setD(value: UByte) { de = ((de and 0x00FFu) or (value.toInt() shl 8).toUShort()) }
    fun setE(value: UByte) { de = ((de and 0xFF00u) or value.toUShort()) }
    fun setH(value: UByte) { hl = ((hl and 0x00FFu) or (value.toInt() shl 8).toUShort()) }
    fun setL(value: UByte) { hl = ((hl and 0xFF00u) or value.toUShort()) }
    
    // Méthode pour exécuter un cycle d'instruction
    fun step() {
        val opcode = memory.readByte(pc).toInt()
        pc = (pc + 1u).toUShort()
        
        when (opcode) {
            // NOP
            0x00 -> {
                cycles += 4
            }
            // LD BC, nn
            0x01 -> {
                val low = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                val high = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                bc = ((high.toInt() shl 8) or low.toInt()).toUShort()
                cycles += 12
            }
            // LD (BC), A
            0x02 -> {
                memory.writeByte(bc, getA())
                cycles += 8
            }
            // INC BC
            0x03 -> {
                bc = (bc + 1u).toUShort()
                cycles += 8
            }
            // INC B
            0x04 -> {
                val b = getB()
                val result = b + 1u
                setB(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((b and 0x0Fu) == 0x0Fu.toUByte())
                cycles += 4
            }
            // DEC B
            0x05 -> {
                val b = getB()
                val result = b - 1u
                setB(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((b and 0x0Fu) == 0x00u.toUByte())
                cycles += 4
            }
            // LD B, n
            0x06 -> {
                val value = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                setB(value)
                cycles += 8
            }
            // RLCA
            0x07 -> {
                val a = getA()
                val newCarry = (a and 0x80u.toUByte()) != 0u.toUByte()
                val result = ((a shl 1) or (if (newCarry) 0x01u else 0x00u)).toUByte()
                setA(result)
                setZeroFlag(false)
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(newCarry)
                cycles += 4
            }
            // LD (nn), SP
            0x08 -> {
                val low = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                val high = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                val address = ((high.toInt() shl 8) or low.toInt()).toUShort()
                memory.writeByte(address, (sp shr 8).toUByte())
                memory.writeByte((address + 1u).toUShort(), (sp and 0xFFu).toUByte())
                cycles += 20
            }
            // ADD HL, BC
            0x09 -> {
                val result = hl.toInt() + bc.toInt()
                val halfCarry = ((hl and 0x0FFFu) + (bc and 0x0FFFu)) > 0x0FFF
                val carry = result > 0xFFFF
                hl = result.toUShort()
                setSubtractFlag(false)
                setHalfCarryFlag(halfCarry)
                setCarryFlag(carry)
                cycles += 8
            }
            // LD A, (BC)
            0x0A -> {
                setA(memory.readByte(bc))
                cycles += 8
            }
            // DEC BC
            0x0B -> {
                bc = (bc - 1u).toUShort()
                cycles += 8
            }
            // INC C
            0x0C -> {
                val c = getC()
                val result = c + 1u
                setC(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((c and 0x0Fu) == 0x0Fu.toUByte())
                cycles += 4
            }
            // DEC C
            0x0D -> {
                val c = getC()
                val result = c - 1u
                setC(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((c and 0x0Fu) == 0x00u.toUByte())
                cycles += 4
            }
            // LD C, n
            0x0E -> {
                val value = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                setC(value)
                cycles += 8
            }
            // RRCA
            0x0F -> {
                val a = getA()
                val newCarry = (a and 0x01u.toUByte()) != 0u.toUByte()
                val result = ((a shr 1) or (if (newCarry) 0x80u else 0x00u)).toUByte()
                setA(result)
                setZeroFlag(false)
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(newCarry)
                cycles += 4
            }
            // STOP
            0x10 -> {
                // Arrêt du CPU jusqu'à la prochaine interruption
                cycles += 4
                // TODO: Implémenter la logique d'arrêt
            }
            // LD DE, nn
            0x11 -> {
                val low = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                val high = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                de = ((high.toInt() shl 8) or low.toInt()).toUShort()
                cycles += 12
            }
            // LD (DE), A
            0x12 -> {
                memory.writeByte(de, getA())
                cycles += 8
            }
            // INC DE
            0x13 -> {
                de = (de + 1u).toUShort()
                cycles += 8
            }
            // INC D
            0x14 -> {
                val d = getD()
                val result = d + 1u
                setD(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((d and 0x0Fu) == 0x0Fu.toUByte())
                cycles += 4
            }
            // DEC D
            0x15 -> {
                val d = getD()
                val result = d - 1u
                setD(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((d and 0x0Fu) == 0x00u.toUByte())
                cycles += 4
            }
            // LD D, n
            0x16 -> {
                val value = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                setD(value)
                cycles += 8
            }
            // RLA
            0x17 -> {
                val a = getA()
                val oldCarry = if (getCarryFlag()) 0x01u else 0x00u
                val newCarry = (a and 0x80u.toUByte()) != 0u.toUByte()
                val result = ((a shl 1) or oldCarry).toUByte()
                setA(result)
                setZeroFlag(false)
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(newCarry)
                cycles += 4
            }
            // JR n
            0x18 -> {
                val offset = memory.readByte(pc).toByte()
                pc = (pc + 1u).toUShort()
                pc = (pc.toInt() + offset).toUShort()
                cycles += 12
            }
            // ADD HL, DE
            0x19 -> {
                val result = hl.toInt() + de.toInt()
                val halfCarry = ((hl and 0x0FFFu) + (de and 0x0FFFu)) > 0x0FFF
                val carry = result > 0xFFFF
                hl = result.toUShort()
                setSubtractFlag(false)
                setHalfCarryFlag(halfCarry)
                setCarryFlag(carry)
                cycles += 8
            }
            // LD A, (DE)
            0x1A -> {
                setA(memory.readByte(de))
                cycles += 8
            }
            // DEC DE
            0x1B -> {
                de = (de - 1u).toUShort()
                cycles += 8
            }
            // INC E
            0x1C -> {
                val e = getE()
                val result = e + 1u
                setE(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((e and 0x0Fu) == 0x0Fu.toUByte())
                cycles += 4
            }
            // DEC E
            0x1D -> {
                val e = getE()
                val result = e - 1u
                setE(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((e and 0x0Fu) == 0x00u.toUByte())
                cycles += 4
            }
            // LD E, n
            0x1E -> {
                val value = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                setE(value)
                cycles += 8
            }
            // RRA
            0x1F -> {
                val a = getA()
                val oldCarry = if (getCarryFlag()) 0x80u else 0x00u
                val newCarry = (a and 0x01u.toUByte()) != 0u.toUByte()
                val result = ((a shr 1) or oldCarry).toUByte()
                setA(result)
                setZeroFlag(false)
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(newCarry)
                cycles += 4
            }
            // JR NZ, n
            0x20 -> {
                val offset = memory.readByte(pc).toByte()
                pc = (pc + 1u).toUShort()
                if (!getZeroFlag()) {
                    pc = (pc.toInt() + offset).toUShort()
                    cycles += 12
                } else {
                    cycles += 8
                }
            }
            // LD HL, nn
            0x21 -> {
                val low = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                val high = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                hl = ((high.toInt() shl 8) or low.toInt()).toUShort()
                cycles += 12
            }
            // LD (HL+), A
            0x22 -> {
                memory.writeByte(hl, getA())
                hl = (hl + 1u).toUShort()
                cycles += 8
            }
            // INC HL
            0x23 -> {
                hl = (hl + 1u).toUShort()
                cycles += 8
            }
            // INC H
            0x24 -> {
                val h = getH()
                val result = h + 1u
                setH(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((h and 0x0Fu) == 0x0Fu.toUByte())
                cycles += 4
            }
            // DEC H
            0x25 -> {
                val h = getH()
                val result = h - 1u
                setH(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((h and 0x0Fu) == 0x00u.toUByte())
                cycles += 4
            }
            // LD H, n
            0x26 -> {
                val value = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                setH(value)
                cycles += 8
            }
            // DAA
            0x27 -> {
                var a = getA().toInt()
                var adjust = 0
                
                if (getHalfCarryFlag() || (!getSubtractFlag() && (a and 0x0F) > 9)) {
                    adjust += 0x06
                }
                
                if (getCarryFlag() || (!getSubtractFlag() && a > 0x99)) {
                    adjust += 0x60
                    setCarryFlag(true)
                }
                
                if (getSubtractFlag()) {
                    a -= adjust
                } else {
                    a += adjust
                }
                
                setA(a.toUByte())
                setZeroFlag(a == 0)
                setHalfCarryFlag(false)
                cycles += 4
            }
            // JR Z, n
            0x28 -> {
                val offset = memory.readByte(pc).toByte()
                pc = (pc + 1u).toUShort()
                if (getZeroFlag()) {
                    pc = (pc.toInt() + offset).toUShort()
                    cycles += 12
                } else {
                    cycles += 8
                }
            }
            // ADD HL, HL
            0x29 -> {
                val result = hl.toInt() + hl.toInt()
                val halfCarry = ((hl and 0x0FFFu) + (hl and 0x0FFFu)) > 0x0FFF
                val carry = result > 0xFFFF
                hl = result.toUShort()
                setSubtractFlag(false)
                setHalfCarryFlag(halfCarry)
                setCarryFlag(carry)
                cycles += 8
            }
            // LD A, (HL+)
            0x2A -> {
                setA(memory.readByte(hl))
                hl = (hl + 1u).toUShort()
                cycles += 8
            }
            // DEC HL
            0x2B -> {
                hl = (hl - 1u).toUShort()
                cycles += 8
            }
            // INC L
            0x2C -> {
                val l = getL()
                val result = l + 1u
                setL(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((l and 0x0Fu) == 0x0Fu.toUByte())
                cycles += 4
            }
            // DEC L
            0x2D -> {
                val l = getL()
                val result = l - 1u
                setL(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((l and 0x0Fu) == 0x00u.toUByte())
                cycles += 4
            }
            // LD L, n
            0x2E -> {
                val value = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                setL(value)
                cycles += 8
            }
            // CPL
            0x2F -> {
                setA(getA().inv())
                setSubtractFlag(true)
                setHalfCarryFlag(true)
                cycles += 4
            }
            // JR NC, n
            0x30 -> {
                val offset = memory.readByte(pc).toByte()
                pc = (pc + 1u).toUShort()
                if (!getCarryFlag()) {
                    pc = (pc.toInt() + offset).toUShort()
                    cycles += 12
                } else {
                    cycles += 8
                }
            }
            // LD SP, nn
            0x31 -> {
                val low = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                val high = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                sp = ((high.toInt() shl 8) or low.toInt()).toUShort()
                cycles += 12
            }
            // LD (HL-), A
            0x32 -> {
                memory.writeByte(hl, getA())
                hl = (hl - 1u).toUShort()
                cycles += 8
            }
            // INC SP
            0x33 -> {
                sp = (sp + 1u).toUShort()
                cycles += 8
            }
            // INC (HL)
            0x34 -> {
                val value = memory.readByte(hl)
                val result = value + 1u
                memory.writeByte(hl, result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((value and 0x0Fu) == 0x0Fu.toUByte())
                cycles += 12
            }
            // DEC (HL)
            0x35 -> {
                val value = memory.readByte(hl)
                val result = value - 1u
                memory.writeByte(hl, result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((value and 0x0Fu) == 0x00u.toUByte())
                cycles += 12
            }
            // LD (HL), n
            0x36 -> {
                val value = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                memory.writeByte(hl, value)
                cycles += 12
            }
            // SCF
            0x37 -> {
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(true)
                cycles += 4
            }
            // JR C, n
            0x38 -> {
                val offset = memory.readByte(pc).toByte()
                pc = (pc + 1u).toUShort()
                if (getCarryFlag()) {
                    pc = (pc.toInt() + offset).toUShort()
                    cycles += 12
                } else {
                    cycles += 8
                }
            }
            // ADD HL, SP
            0x39 -> {
                val result = hl.toInt() + sp.toInt()
                val halfCarry = ((hl and 0x0FFFu) + (sp and 0x0FFFu)) > 0x0FFF
                val carry = result > 0xFFFF
                hl = result.toUShort()
                setSubtractFlag(false)
                setHalfCarryFlag(halfCarry)
                setCarryFlag(carry)
                cycles += 8
            }
            // LD A, (HL-)
            0x3A -> {
                setA(memory.readByte(hl))
                hl = (hl - 1u).toUShort()
                cycles += 8
            }
            // DEC SP
            0x3B -> {
                sp = (sp - 1u).toUShort()
                cycles += 8
            }
            // INC A
            0x3C -> {
                val a = getA()
                val result = a + 1u
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) == 0x0Fu.toUByte())
                cycles += 4
            }
            // DEC A
            0x3D -> {
                val a = getA()
                val result = a - 1u
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) == 0x00u.toUByte())
                cycles += 4
            }
            // LD A, n
            0x3E -> {
                val value = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                setA(value)
                cycles += 8
            }
            // CCF
            0x3F -> {
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(!getCarryFlag())
                cycles += 4
            }
            // LD B, B
            0x40 -> {
                // NOP effectif
                cycles += 4
            }
            // LD B, C
            0x41 -> {
                setB(getC())
                cycles += 4
            }
            // LD B, D
            0x42 -> {
                setB(getD())
                cycles += 4
            }
            // LD B, E
            0x43 -> {
                setB(getE())
                cycles += 4
            }
            // LD B, H
            0x44 -> {
                setB(getH())
                cycles += 4
            }
            // LD B, L
            0x45 -> {
                setB(getL())
                cycles += 4
            }
            // LD B, (HL)
            0x46 -> {
                setB(memory.readByte(hl))
                cycles += 8
            }
            // LD B, A
            0x47 -> {
                setB(getA())
                cycles += 4
            }
            // LD C, B
            0x48 -> {
                setC(getB())
                cycles += 4
            }
            // LD C, C
            0x49 -> {
                // NOP effectif
                cycles += 4
            }
            // LD C, D
            0x4A -> {
                setC(getD())
                cycles += 4
            }
            // LD C, E
            0x4B -> {
                setC(getE())
                cycles += 4
            }
            // LD C, H
            0x4C -> {
                setC(getH())
                cycles += 4
            }
            // LD C, L
            0x4D -> {
                setC(getL())
                cycles += 4
            }
            // LD C, (HL)
            0x4E -> {
                setC(memory.readByte(hl))
                cycles += 8
            }
            // LD C, A
            0x4F -> {
                setC(getA())
                cycles += 4
            }
            // LD D, B
            0x50 -> {
                setD(getB())
                cycles += 4
            }
            // LD D, C
            0x51 -> {
                setD(getC())
                cycles += 4
            }
            // LD D, D
            0x52 -> {
                // NOP effectif
                cycles += 4
            }
            // LD D, E
            0x53 -> {
                setD(getE())
                cycles += 4
            }
            // LD D, H
            0x54 -> {
                setD(getH())
                cycles += 4
            }
            // LD D, L
            0x55 -> {
                setD(getL())
                cycles += 4
            }
            // LD D, (HL)
            0x56 -> {
                setD(memory.readByte(hl))
                cycles += 8
            }
            // LD D, A
            0x57 -> {
                setD(getA())
                cycles += 4
            }
            // LD E, B
            0x58 -> {
                setE(getB())
                cycles += 4
            }
            // LD E, C
            0x59 -> {
                setE(getC())
                cycles += 4
            }
            // LD E, D
            0x5A -> {
                setE(getD())
                cycles += 4
            }
            // LD E, E
            0x5B -> {
                // NOP effectif
                cycles += 4
            }
            // LD E, H
            0x5C -> {
                setE(getH())
                cycles += 4
            }
            // LD E, L
            0x5D -> {
                setE(getL())
                cycles += 4
            }
            // LD E, (HL)
            0x5E -> {
                setE(memory.readByte(hl))
                cycles += 8
            }
            // LD E, A
            0x5F -> {
                setE(getA())
                cycles += 4
            }
            // LD H, B
            0x60 -> {
                setH(getB())
                cycles += 4
            }
            // LD H, C
            0x61 -> {
                setH(getC())
                cycles += 4
            }
            // LD H, D
            0x62 -> {
                setH(getD())
                cycles += 4
            }
            // LD H, E
            0x63 -> {
                setH(getE())
                cycles += 4
            }
            // LD H, H
            0x64 -> {
                // NOP effectif
                cycles += 4
            }
            // LD H, L
            0x65 -> {
                setH(getL())
                cycles += 4
            }
            // LD H, (HL)
            0x66 -> {
                setH(memory.readByte(hl))
                cycles += 8
            }
            // LD H, A
            0x67 -> {
                setH(getA())
                cycles += 4
            }
            // LD L, B
            0x68 -> {
                setL(getB())
                cycles += 4
            }
            // LD L, C
            0x69 -> {
                setL(getC())
                cycles += 4
            }
            // LD L, D
            0x6A -> {
                setL(getD())
                cycles += 4
            }
            // LD L, E
            0x6B -> {
                setL(getE())
                cycles += 4
            }
            // LD L, H
            0x6C -> {
                setL(getH())
                cycles += 4
            }
            // LD L, L
            0x6D -> {
                // NOP effectif
                cycles += 4
            }
            // LD L, (HL)
            0x6E -> {
                setL(memory.readByte(hl))
                cycles += 8
            }
            // LD L, A
            0x6F -> {
                setL(getA())
                cycles += 4
            }
            // LD (HL), B
            0x70 -> {
                memory.writeByte(hl, getB())
                cycles += 8
            }
            // LD (HL), C
            0x71 -> {
                memory.writeByte(hl, getC())
                cycles += 8
            }
            // LD (HL), D
            0x72 -> {
                memory.writeByte(hl, getD())
                cycles += 8
            }
            // LD (HL), E
            0x73 -> {
                memory.writeByte(hl, getE())
                cycles += 8
            }
            // LD (HL), H
            0x74 -> {
                memory.writeByte(hl, getH())
                cycles += 8
            }
            // LD (HL), L
            0x75 -> {
                memory.writeByte(hl, getL())
                cycles += 8
            }
            // HALT
            0x76 -> {
                // Arrêt du CPU jusqu'à la prochaine interruption
                cycles += 4
                // TODO: Implémenter la logique d'arrêt
            }
            // LD (HL), A
            0x77 -> {
                memory.writeByte(hl, getA())
                cycles += 8
            }
            // LD A, B
            0x78 -> {
                setA(getB())
                cycles += 4
            }
            // LD A, C
            0x79 -> {
                setA(getC())
                cycles += 4
            }
            // LD A, D
            0x7A -> {
                setA(getD())
                cycles += 4
            }
            // LD A, E
            0x7B -> {
                setA(getE())
                cycles += 4
            }
            // LD A, H
            0x7C -> {
                setA(getH())
                cycles += 4
            }
            // LD A, L
            0x7D -> {
                setA(getL())
                cycles += 4
            }
            // LD A, (HL)
            0x7E -> {
                setA(memory.readByte(hl))
                cycles += 8
            }
            // LD A, A
            0x7F -> {
                // NOP effectif
                cycles += 4
            }
            // ADD A, B
            0x80 -> {
                val a = getA()
                val b = getB()
                val result = a + b
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (b and 0x0Fu) > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADD A, C
            0x81 -> {
                val a = getA()
                val c = getC()
                val result = a + c
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (c and 0x0Fu) > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADD A, D
            0x82 -> {
                val a = getA()
                val d = getD()
                val result = a + d
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (d and 0x0Fu) > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADD A, E
            0x83 -> {
                val a = getA()
                val e = getE()
                val result = a + e
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (e and 0x0Fu) > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADD A, H
            0x84 -> {
                val a = getA()
                val h = getH()
                val result = a + h
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (h and 0x0Fu) > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADD A, L
            0x85 -> {
                val a = getA()
                val l = getL()
                val result = a + l
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (l and 0x0Fu) > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADD A, (HL)
            0x86 -> {
                val a = getA()
                val value = memory.readByte(hl)
                val result = a + value
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (value and 0x0Fu) > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 8
            }
            // ADD A, A
            0x87 -> {
                val a = getA()
                val result = a + a
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (a and 0x0Fu) > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADC A, B
            0x88 -> {
                val a = getA()
                val b = getB()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a + b + carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (b and 0x0Fu) + carry > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADC A, C
            0x89 -> {
                val a = getA()
                val c = getC()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a + c + carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (c and 0x0Fu) + carry > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADC A, D
            0x8A -> {
                val a = getA()
                val d = getD()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a + d + carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (d and 0x0Fu) + carry > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADC A, E
            0x8B -> {
                val a = getA()
                val e = getE()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a + e + carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (e and 0x0Fu) + carry > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADC A, H
            0x8C -> {
                val a = getA()
                val h = getH()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a + h + carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (h and 0x0Fu) + carry > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADC A, L
            0x8D -> {
                val a = getA()
                val l = getL()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a + l + carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (l and 0x0Fu) + carry > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // ADC A, (HL)
            0x8E -> {
                val a = getA()
                val value = memory.readByte(hl)
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a + value + carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (value and 0x0Fu) + carry > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 8
            }
            // ADC A, A
            0x8F -> {
                val a = getA()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a + a + carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (a and 0x0Fu) + carry > 0x0Fu)
                setCarryFlag(result > 0xFFu)
                cycles += 4
            }
            // SUB B
            0x90 -> {
                val a = getA()
                val b = getB()
                val result = a - b
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (b and 0x0Fu))
                setCarryFlag(a < b)
                cycles += 4
            }
            // SUB C
            0x91 -> {
                val a = getA()
                val c = getC()
                val result = a - c
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (c and 0x0Fu))
                setCarryFlag(a < c)
                cycles += 4
            }
            // SUB D
            0x92 -> {
                val a = getA()
                val d = getD()
                val result = a - d
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (d and 0x0Fu))
                setCarryFlag(a < d)
                cycles += 4
            }
            // SUB E
            0x93 -> {
                val a = getA()
                val e = getE()
                val result = a - e
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (e and 0x0Fu))
                setCarryFlag(a < e)
                cycles += 4
            }
            // SUB H
            0x94 -> {
                val a = getA()
                val h = getH()
                val result = a - h
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (h and 0x0Fu))
                setCarryFlag(a < h)
                cycles += 4
            }
            // SUB L
            0x95 -> {
                val a = getA()
                val l = getL()
                val result = a - l
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (l and 0x0Fu))
                setCarryFlag(a < l)
                cycles += 4
            }
            // SUB (HL)
            0x96 -> {
                val a = getA()
                val value = memory.readByte(hl)
                val result = a - value
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (value and 0x0Fu))
                setCarryFlag(a < value)
                cycles += 8
            }
            // SUB A
            0x97 -> {
                val a = getA()
                val result = a - a
                setA(result)
                setZeroFlag(true)
                setSubtractFlag(true)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // SBC A, B
            0x98 -> {
                val a = getA()
                val b = getB()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a - b - carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < ((b and 0x0Fu) + carry))
                setCarryFlag(a < (b + carry))
                cycles += 4
            }
            // SBC A, C
            0x99 -> {
                val a = getA()
                val c = getC()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a - c - carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < ((c and 0x0Fu) + carry))
                setCarryFlag(a < (c + carry))
                cycles += 4
            }
            // SBC A, D
            0x9A -> {
                val a = getA()
                val d = getD()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a - d - carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < ((d and 0x0Fu) + carry))
                setCarryFlag(a < (d + carry))
                cycles += 4
            }
            // SBC A, E
            0x9B -> {
                val a = getA()
                val e = getE()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a - e - carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < ((e and 0x0Fu) + carry))
                setCarryFlag(a < (e + carry))
                cycles += 4
            }
            // SBC A, H
            0x9C -> {
                val a = getA()
                val h = getH()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a - h - carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < ((h and 0x0Fu) + carry))
                setCarryFlag(a < (h + carry))
                cycles += 4
            }
            // SBC A, L
            0x9D -> {
                val a = getA()
                val l = getL()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a - l - carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < ((l and 0x0Fu) + carry))
                setCarryFlag(a < (l + carry))
                cycles += 4
            }
            // SBC A, (HL)
            0x9E -> {
                val a = getA()
                val value = memory.readByte(hl)
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a - value - carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < ((value and 0x0Fu) + carry))
                setCarryFlag(a < (value + carry))
                cycles += 8
            }
            // SBC A, A
            0x9F -> {
                val a = getA()
                val carry = if (getCarryFlag()) 1u else 0u
                val result = a - a - carry
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(true)
                setHalfCarryFlag(false)
                setCarryFlag(carry == 1u)
                cycles += 4
            }
            // AND B
            0xA0 -> {
                val a = getA()
                val b = getB()
                val result = a and b
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(true)
                setCarryFlag(false)
                cycles += 4
            }
            // AND C
            0xA1 -> {
                val a = getA()
                val c = getC()
                val result = a and c
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(true)
                setCarryFlag(false)
                cycles += 4
            }
            // AND D
            0xA2 -> {
                val a = getA()
                val d = getD()
                val result = a and d
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(true)
                setCarryFlag(false)
                cycles += 4
            }
            // AND E
            0xA3 -> {
                val a = getA()
                val e = getE()
                val result = a and e
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(true)
                setCarryFlag(false)
                cycles += 4
            }
            // AND H
            0xA4 -> {
                val a = getA()
                val h = getH()
                val result = a and h
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(true)
                setCarryFlag(false)
                cycles += 4
            }
            // AND L
            0xA5 -> {
                val a = getA()
                val l = getL()
                val result = a and l
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(true)
                setCarryFlag(false)
                cycles += 4
            }
            // AND (HL)
            0xA6 -> {
                val a = getA()
                val value = memory.readByte(hl)
                val result = a and value
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(true)
                setCarryFlag(false)
                cycles += 8
            }
            // AND A
            0xA7 -> {
                val a = getA()
                val result = a and a
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(true)
                setCarryFlag(false)
                cycles += 4
            }
            // XOR B
            0xA8 -> {
                val a = getA()
                val b = getB()
                val result = a xor b
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // XOR C
            0xA9 -> {
                val a = getA()
                val c = getC()
                val result = a xor c
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // XOR D
            0xAA -> {
                val a = getA()
                val d = getD()
                val result = a xor d
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // XOR E
            0xAB -> {
                val a = getA()
                val e = getE()
                val result = a xor e
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // XOR H
            0xAC -> {
                val a = getA()
                val h = getH()
                val result = a xor h
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // XOR L
            0xAD -> {
                val a = getA()
                val l = getL()
                val result = a xor l
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // XOR (HL)
            0xAE -> {
                val a = getA()
                val value = memory.readByte(hl)
                val result = a xor value
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 8
            }
            // XOR A
            0xAF -> {
                val a = getA()
                val result = a xor a
                setA(result)
                setZeroFlag(true)
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // OR B
            0xB0 -> {
                val a = getA()
                val b = getB()
                val result = a or b
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // OR C
            0xB1 -> {
                val a = getA()
                val c = getC()
                val result = a or c
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // OR D
            0xB2 -> {
                val a = getA()
                val d = getD()
                val result = a or d
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // OR E
            0xB3 -> {
                val a = getA()
                val e = getE()
                val result = a or e
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // OR H
            0xB4 -> {
                val a = getA()
                val h = getH()
                val result = a or h
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // OR L
            0xB5 -> {
                val a = getA()
                val l = getL()
                val result = a or l
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // OR (HL)
            0xB6 -> {
                val a = getA()
                val value = memory.readByte(hl)
                val result = a or value
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 8
            }
            // OR A
            0xB7 -> {
                val a = getA()
                val result = a or a
                setA(result)
                setZeroFlag(result == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // CP B
            0xB8 -> {
                val a = getA()
                val b = getB()
                val result = a.toInt() - b.toInt()
                setZeroFlag(result == 0)
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (b and 0x0Fu))
                setCarryFlag(result < 0)
                cycles += 4
            }
            // CP C
            0xB9 -> {
                val a = getA()
                val c = getC()
                val result = a.toInt() - c.toInt()
                setZeroFlag(result == 0)
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (c and 0x0Fu))
                setCarryFlag(result < 0)
                cycles += 4
            }
            // CP D
            0xBA -> {
                val a = getA()
                val d = getD()
                val result = a.toInt() - d.toInt()
                setZeroFlag(result == 0)
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (d and 0x0Fu))
                setCarryFlag(result < 0)
                cycles += 4
            }
            // CP E
            0xBB -> {
                val a = getA()
                val e = getE()
                val result = a.toInt() - e.toInt()
                setZeroFlag(result == 0)
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (e and 0x0Fu))
                setCarryFlag(result < 0)
                cycles += 4
            }
            // CP H
            0xBC -> {
                val a = getA()
                val h = getH()
                val result = a.toInt() - h.toInt()
                setZeroFlag(result == 0)
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (h and 0x0Fu))
                setCarryFlag(result < 0)
                cycles += 4
            }
            // CP L
            0xBD -> {
                val a = getA()
                val l = getL()
                val result = a.toInt() - l.toInt()
                setZeroFlag(result == 0)
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (l and 0x0Fu))
                setCarryFlag(result < 0)
                cycles += 4
            }
            // CP (HL)
            0xBE -> {
                val a = getA()
                val value = memory.readByte(hl)
                val result = a.toInt() - value.toInt()
                setZeroFlag(result == 0)
                setSubtractFlag(true)
                setHalfCarryFlag((a and 0x0Fu) < (value and 0x0Fu))
                setCarryFlag(result < 0)
                cycles += 8
            }
            // CP A
            0xBF -> {
                val a = getA()
                val result = a.toInt() - a.toInt()
                setZeroFlag(true)
                setSubtractFlag(true)
                setHalfCarryFlag(false)
                setCarryFlag(false)
                cycles += 4
            }
            // RET NZ
            0xC0 -> {
                if (!getZeroFlag()) {
                    val low = memory.readByte(sp).toInt()
                    sp = (sp + 1u).toUShort()
                    val high = memory.readByte(sp).toInt()
                    sp = (sp + 1u).toUShort()
                    pc = ((high shl 8) or low).toUShort()
                    cycles += 20
                } else {
                    cycles += 8
                }
            }
            // POP BC
            0xC1 -> {
                val low = memory.readByte(sp).toInt()
                sp = (sp + 1u).toUShort()
                val high = memory.readByte(sp).toInt()
                sp = (sp + 1u).toUShort()
                setBC((high shl 8) or low)
                cycles += 12
            }
            // JP NZ, nn
            0xC2 -> {
                val low = memory.readByte(pc).toInt()
                pc = (pc + 1u).toUShort()
                val high = memory.readByte(pc).toInt()
                pc = (pc + 1u).toUShort()
                if (!getZeroFlag()) {
                    pc = ((high shl 8) or low).toUShort()
                    cycles += 16
                } else {
                    cycles += 12
                }
            }
            // JP nn
            0xC3 -> {
                val low = memory.readByte(pc).toInt()
                pc = (pc + 1u).toUShort()
                val high = memory.readByte(pc).toInt()
                pc = ((high shl 8) or low).toUShort()
                cycles += 16
            }
            // CALL NZ, nn
            0xC4 -> {
                val low = memory.readByte(pc).toInt()
                pc = (pc + 1u).toUShort()
                val high = memory.readByte(pc).toInt()
                pc = (pc + 1u).toUShort()
                if (!getZeroFlag()) {
                    sp = (sp - 1u).toUShort()
                    memory.writeByte(sp, (pc shr 8).toUByte())
                    sp = (sp - 1u).toUShort()
                    memory.writeByte(sp, (pc and 0xFF).toUByte())
                    pc = ((high shl 8) or low).toUShort()
                    cycles += 24
                } else {
                    cycles += 12
                }
            }
            // PUSH BC
            0xC5 -> {
                sp = (sp - 1u).toUShort()
                memory.writeByte(sp, (bc shr 8).toUByte())
                sp = (sp - 1u).toUShort()
                memory.writeByte(sp, (bc and 0xFF).toUByte())
                cycles += 16
            }
            // ADD A, n
            0xC6 -> {
                val a = getA()
                val n = memory.readByte(pc)
                pc = (pc + 1u).toUShort()
                val result = a.toInt() + n.toInt()
                setA(result.toUByte())
                setZeroFlag(result.toUByte() == 0u.toUByte())
                setSubtractFlag(false)
                setHalfCarryFlag((a and 0x0Fu) + (n and 0x0Fu) > 0x0F)
                setCarryFlag(result > 0xFF)
                cycles += 8
            }
            // RST 00H
            0xC7 -> {
                sp = (sp - 1u).toUShort()
                memory.writeByte(sp, (pc shr 8).toUByte())
                sp = (sp - 1u).toUShort()
                memory.writeByte(sp, (pc and 0xFF).toUByte())
                pc = 0x0000u
                cycles += 16
            }
            // RET Z
            0xC8 -> {
                if (getZeroFlag()) {
                    val low = memory.readByte(sp).toInt()
                    sp = (sp + 1u).toUShort()
                    val high = memory.readByte(sp).toInt()
                    sp = (sp + 1u).toUShort()
                    pc = ((high shl 8) or low).toUShort()
                    cycles += 20
                } else {
                    cycles += 8
                }
            }
            // RET
            0xC9 -> {
                val low = memory.readByte(sp).toInt()
                sp = (sp + 1u).toUShort()
                val high = memory.readByte(sp).toInt()
                sp = (sp + 1u).toUShort()
                pc = ((high shl 8) or low).toUShort()
                cycles += 16
            }
            // JP Z, nn
            0xCA -> {
                val low = memory.readByte(pc).toInt()
                pc = (pc + 1u).toUShort()
                val high = memory.readByte(pc).toInt()
                pc = (pc + 1u).toUShort()
                if (getZeroFlag()) {
                    pc = ((high shl 8) or low).toUShort()
                    cycles += 16
                } else {
                    cycles += 12
                }
            }
            // PREFIX CB
            0xCB -> {
                val cbOpcode = memory.readByte(pc).toInt()
                pc = (pc + 1u).toUShort()
                cycles += cbHandler.execute(cbOpcode)
            }
            // ... Autres opcodes à implémenter ...
        }
    }
    
    // Méthode pour gérer les interruptions
    fun handleInterrupts() {
        if (!ime) return
        
        val if_reg = memory.readByte(0xFF0F.toUShort())
        val ie_reg = memory.readByte(0xFFFF.toUShort())
        
        if ((if_reg.toInt() and ie_reg.toInt() and 0x1F) != 0) {
            ime = false
            // Sauvegarder PC sur la pile
            sp = (sp - 2u).toUShort()
            memory.writeByte(sp, (pc shr 8).toUByte())
            memory.writeByte((sp + 1u).toUShort(), (pc and 0xFFu).toUByte())
            
            // Définir le vecteur d'interruption
            when {
                (if_reg.toInt() and 0x01) != 0 -> pc = 0x40u
                (if_reg.toInt() and 0x02) != 0 -> pc = 0x48u
                (if_reg.toInt() and 0x04) != 0 -> pc = 0x50u
                (if_reg.toInt() and 0x08) != 0 -> pc = 0x58u
                (if_reg.toInt() and 0x10) != 0 -> pc = 0x60u
            }
            
            cycles += 20
        }
    }
} 