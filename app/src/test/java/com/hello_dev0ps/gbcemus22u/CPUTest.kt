package com.hello_dev0ps.gbcemus22u

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CPUTest {
    private lateinit var memory: Memory
    private lateinit var cpu: CPU

    @BeforeEach
    fun setup() {
        memory = Memory()
        cpu = CPU(memory)
    }

    @Test
    fun `test NOP instruction`() {
        memory.writeByte(0x0000u, 0x00u) // NOP
        val cycles = cpu.step()
        assertEquals(4, cycles)
        assertEquals(0x0001, cpu.getPC())
    }

    @Test
    fun `test LD BC, nn instruction`() {
        memory.writeByte(0x0000u, 0x01u) // LD BC, nn
        memory.writeByte(0x0001u, 0x34u) // nn low byte
        memory.writeByte(0x0002u, 0x12u) // nn high byte
        val cycles = cpu.step()
        assertEquals(12, cycles)
        assertEquals(0x1234, cpu.getBC())
        assertEquals(0x0003, cpu.getPC())
    }

    @Test
    fun `test LD (BC), A instruction`() {
        cpu.setA(0x42u)
        cpu.setBC(0xC000u)
        memory.writeByte(0x0000u, 0x02u) // LD (BC), A
        val cycles = cpu.step()
        assertEquals(8, cycles)
        assertEquals(0x42u, memory.readByte(0xC000u))
        assertEquals(0x0001, cpu.getPC())
    }

    @Test
    fun `test INC BC instruction`() {
        cpu.setBC(0xFFFFu)
        memory.writeByte(0x0000u, 0x03u) // INC BC
        val cycles = cpu.step()
        assertEquals(8, cycles)
        assertEquals(0x0000, cpu.getBC())
        assertEquals(0x0001, cpu.getPC())
    }

    @Test
    fun `test INC B instruction`() {
        cpu.setB(0xFFu)
        memory.writeByte(0x0000u, 0x04u) // INC B
        val cycles = cpu.step()
        assertEquals(4, cycles)
        assertEquals(0x00u, cpu.getB())
        assertTrue(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertTrue(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertEquals(0x0001, cpu.getPC())
    }

    @Test
    fun `test DEC B instruction`() {
        cpu.setB(0x01u)
        memory.writeByte(0x0000u, 0x05u) // DEC B
        val cycles = cpu.step()
        assertEquals(4, cycles)
        assertEquals(0x00u, cpu.getB())
        assertTrue(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertTrue(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertEquals(0x0001, cpu.getPC())
    }

    @Test
    fun `test LD B, n instruction`() {
        memory.writeByte(0x0000u, 0x06u) // LD B, n
        memory.writeByte(0x0001u, 0x42u) // n
        val cycles = cpu.step()
        assertEquals(8, cycles)
        assertEquals(0x42u, cpu.getB())
        assertEquals(0x0002, cpu.getPC())
    }

    @Test
    fun `test RLCA instruction`() {
        cpu.setA(0x80u)
        memory.writeByte(0x0000u, 0x07u) // RLCA
        val cycles = cpu.step()
        assertEquals(4, cycles)
        assertEquals(0x01u, cpu.getA())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
        assertEquals(0x0001, cpu.getPC())
    }

    @Test
    fun `test LD (nn), SP instruction`() {
        cpu.setSP(0x1234)
        memory.writeByte(0x0000u, 0x08u) // LD (nn), SP
        memory.writeByte(0x0001u, 0x00u) // nn low byte
        memory.writeByte(0x0002u, 0xC0u) // nn high byte
        val cycles = cpu.step()
        assertEquals(20, cycles)
        assertEquals(0x34u, memory.readByte(0xC000u))
        assertEquals(0x12u, memory.readByte(0xC001u))
        assertEquals(0x0003, cpu.getPC())
    }

    @Test
    fun `test ADD HL, BC instruction`() {
        cpu.setHL(0x0FFFu)
        cpu.setBC(0x0001u)
        memory.writeByte(0x0000u, 0x09u) // ADD HL, BC
        val cycles = cpu.step()
        assertEquals(8, cycles)
        assertEquals(0x1000, cpu.getHL())
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
        assertTrue(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertEquals(0x0001, cpu.getPC())
    }

    @Test
    fun `test LD A, (BC) instruction`() {
        memory.writeByte(0xC000u, 0x42u)
        cpu.setBC(0xC000u)
        memory.writeByte(0x0000u, 0x0Au) // LD A, (BC)
        val cycles = cpu.step()
        assertEquals(8, cycles)
        assertEquals(0x42u, cpu.getA())
        assertEquals(0x0001, cpu.getPC())
    }

    @Test
    fun `test DEC BC instruction`() {
        cpu.setBC(0x0001u)
        memory.writeByte(0x0000u, 0x0Bu) // DEC BC
        val cycles = cpu.step()
        assertEquals(8, cycles)
        assertEquals(0x0000, cpu.getBC())
        assertEquals(0x0001, cpu.getPC())
    }

    @Test
    fun `test INC C instruction`() {
        cpu.setC(0xFFu)
        memory.writeByte(0x0000u, 0x0Cu) // INC C
        val cycles = cpu.step()
        assertEquals(4, cycles)
        assertEquals(0x00u, cpu.getC())
        assertTrue(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertTrue(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertEquals(0x0001, cpu.getPC())
    }

    @Test
    fun `test DEC C instruction`() {
        cpu.setC(0x01u)
        memory.writeByte(0x0000u, 0x0Du) // DEC C
        val cycles = cpu.step()
        assertEquals(4, cycles)
        assertEquals(0x00u, cpu.getC())
        assertTrue(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertTrue(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertEquals(0x0001, cpu.getPC())
    }

    @Test
    fun `test LD C, n instruction`() {
        memory.writeByte(0x0000u, 0x0Eu) // LD C, n
        memory.writeByte(0x0001u, 0x42u) // n
        val cycles = cpu.step()
        assertEquals(8, cycles)
        assertEquals(0x42u, cpu.getC())
        assertEquals(0x0002, cpu.getPC())
    }

    @Test
    fun `test RRCA instruction`() {
        cpu.setA(0x01u)
        memory.writeByte(0x0000u, 0x0Fu) // RRCA
        val cycles = cpu.step()
        assertEquals(4, cycles)
        assertEquals(0x80u, cpu.getA())
        assertTrue(cpu.isFlagSet(CPU.FLAG_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_ZERO))
        assertFalse(cpu.isFlagSet(CPU.FLAG_HALF_CARRY))
        assertFalse(cpu.isFlagSet(CPU.FLAG_SUBTRACT))
        assertEquals(0x0001, cpu.getPC())
    }
} 