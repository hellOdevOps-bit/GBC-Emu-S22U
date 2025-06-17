package com.Hello_Dev0ps.gbc_emu_s22u

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CBOpcodeHandlerTest {
    private lateinit var memory: Memory
    private lateinit var cpu: CPU
    private lateinit var handler: CBOpcodeHandler

    @BeforeEach
    fun setup() {
        memory = Memory()
        cpu = CPU(memory)
        handler = CBOpcodeHandler(cpu, memory)
    }

    @Test
    fun `test RLC B instruction`() {
        cpu.setB(0x80u)
        val cycles = handler.handleOpcode(0x00u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getB())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RLC C instruction`() {
        cpu.setC(0x80u)
        val cycles = handler.handleOpcode(0x01u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getC())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RLC D instruction`() {
        cpu.setD(0x80u)
        val cycles = handler.handleOpcode(0x02u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getD())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RLC E instruction`() {
        cpu.setE(0x80u)
        val cycles = handler.handleOpcode(0x03u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getE())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RLC H instruction`() {
        cpu.setH(0x80u)
        val cycles = handler.handleOpcode(0x04u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getH())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RLC L instruction`() {
        cpu.setL(0x80u)
        val cycles = handler.handleOpcode(0x05u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getL())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RLC (HL) instruction`() {
        cpu.setHL(0xC000u)
        memory.writeByte(0xC000u, 0x80u)
        val cycles = handler.handleOpcode(0x06u)
        assertEquals(16, cycles)
        assertEquals(0x01u, memory.readByte(0xC000u))
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RLC A instruction`() {
        cpu.setA(0x80u)
        val cycles = handler.handleOpcode(0x07u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getA())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RRC B instruction`() {
        cpu.setB(0x01u)
        val cycles = handler.handleOpcode(0x08u)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getB())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RRC C instruction`() {
        cpu.setC(0x01u)
        val cycles = handler.handleOpcode(0x09u)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getC())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RRC D instruction`() {
        cpu.setD(0x01u)
        val cycles = handler.handleOpcode(0x0Au)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getD())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RRC E instruction`() {
        cpu.setE(0x01u)
        val cycles = handler.handleOpcode(0x0Bu)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getE())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RRC H instruction`() {
        cpu.setH(0x01u)
        val cycles = handler.handleOpcode(0x0Cu)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getH())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RRC L instruction`() {
        cpu.setL(0x01u)
        val cycles = handler.handleOpcode(0x0Du)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getL())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RRC (HL) instruction`() {
        cpu.setHL(0xC000u)
        memory.writeByte(0xC000u, 0x01u)
        val cycles = handler.handleOpcode(0x0Eu)
        assertEquals(16, cycles)
        assertEquals(0x80u, memory.readByte(0xC000u))
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RRC A instruction`() {
        cpu.setA(0x01u)
        val cycles = handler.handleOpcode(0x0Fu)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getA())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RL B instruction`() {
        cpu.setB(0x80u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x10u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getB())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RL C instruction`() {
        cpu.setC(0x80u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x11u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getC())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RL D instruction`() {
        cpu.setD(0x80u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x12u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getD())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RL E instruction`() {
        cpu.setE(0x80u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x13u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getE())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RL H instruction`() {
        cpu.setH(0x80u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x14u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getH())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RL L instruction`() {
        cpu.setL(0x80u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x15u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getL())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RL (HL) instruction`() {
        cpu.setHL(0xC000u)
        memory.writeByte(0xC000u, 0x80u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x16u)
        assertEquals(16, cycles)
        assertEquals(0x01u, memory.readByte(0xC000u))
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RL A instruction`() {
        cpu.setA(0x80u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x17u)
        assertEquals(8, cycles)
        assertEquals(0x01u, cpu.getA())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RR B instruction`() {
        cpu.setB(0x01u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x18u)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getB())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RR C instruction`() {
        cpu.setC(0x01u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x19u)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getC())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RR D instruction`() {
        cpu.setD(0x01u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x1Au)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getD())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RR E instruction`() {
        cpu.setE(0x01u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x1Bu)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getE())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RR H instruction`() {
        cpu.setH(0x01u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x1Cu)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getH())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RR L instruction`() {
        cpu.setL(0x01u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x1Du)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getL())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RR (HL) instruction`() {
        cpu.setHL(0xC000u)
        memory.writeByte(0xC000u, 0x01u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x1Eu)
        assertEquals(16, cycles)
        assertEquals(0x80u, memory.readByte(0xC000u))
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }

    @Test
    fun `test RR A instruction`() {
        cpu.setA(0x01u)
        cpu.setFlag(CPU.FLAG_CARRY)
        val cycles = handler.handleOpcode(0x1Fu)
        assertEquals(8, cycles)
        assertEquals(0x80u, cpu.getA())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
    }
} 