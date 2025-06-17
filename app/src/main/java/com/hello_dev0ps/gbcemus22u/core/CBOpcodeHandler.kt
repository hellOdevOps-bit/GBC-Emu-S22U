package com.hello_dev0ps.gbcemus22u

class CBOpcodeHandler(private val cpu: CPU, private val memory: Memory) {
    private var cycles: Int = 0

    fun execute(opcode: Int): Int {
        cycles = 0
        when (opcode) {
            in 0x80..0xBF -> handleRES(opcode)
            in 0xC0..0xFF -> handleSET(opcode)
            else -> throw IllegalArgumentException("Opcode CB ${opcode.toString(16)} non reconnu")
        }
        return cycles
    }

    private fun handleRES(opcode: Int) {
        val bit = (opcode - 0x80) / 8
        val registerIndex = opcode % 8
        val value = getRegisterOrHL(registerIndex)
        val result = value and (0xFFu.toUByte() xor (1u shl bit).toUByte())
        setRegisterOrHL(registerIndex, result)
        cycles += if (registerIndex == 6) 16 else 8 // 16 cycles pour (HL), 8 pour les registres
    }

    private fun handleSET(opcode: Int) {
        val bit = (opcode - 0xC0) / 8
        val registerIndex = opcode % 8
        val value = getRegisterOrHL(registerIndex)
        val result = value or (1u shl bit).toUByte()
        setRegisterOrHL(registerIndex, result)
        cycles += if (registerIndex == 6) 16 else 8 // 16 cycles pour (HL), 8 pour les registres
    }

    private fun getRegisterOrHL(index: Int): UByte {
        return when (index) {
            0 -> cpu.getB()
            1 -> cpu.getC()
            2 -> cpu.getD()
            3 -> cpu.getE()
            4 -> cpu.getH()
            5 -> cpu.getL()
            6 -> memory.readByte(cpu.hl) // (HL)
            7 -> cpu.getA()
            else -> throw IllegalArgumentException("Index registre $index invalide")
        }
    }

    private fun setRegisterOrHL(index: Int, value: UByte) {
        when (index) {
            0 -> cpu.setB(value)
            1 -> cpu.setC(value)
            2 -> cpu.setD(value)
            3 -> cpu.setE(value)
            4 -> cpu.setH(value)
            5 -> cpu.setL(value)
            6 -> memory.writeByte(cpu.hl, value)
            7 -> cpu.setA(value)
            else -> throw IllegalArgumentException("Index registre $index invalide")
        }
    }
} 