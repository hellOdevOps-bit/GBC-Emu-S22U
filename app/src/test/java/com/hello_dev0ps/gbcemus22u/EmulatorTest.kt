package com.hello_dev0ps.gbcemus22u

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class EmulatorTest {
    private lateinit var emulator: Emulator

    @BeforeEach
    fun setup() {
        emulator = Emulator()
    }

    @Test
    fun `test emulator initialization`() {
        assertNotNull(emulator.getCPU())
        assertNotNull(emulator.getMemory())
        assertNotNull(emulator.getPPU())
        assertNotNull(emulator.getAPU())
    }

    @Test
    fun `test ROM loading`() {
        // Création d'une ROM de test
        val romData = ByteArray(32 * 1024) { 0x00u.toByte() }
        romData[0x100] = 0x00u.toByte() // NOP
        romData[0x101] = 0x00u.toByte() // NOP
        romData[0x102] = 0x00u.toByte() // NOP
        romData[0x103] = 0x00u.toByte() // NOP
        romData[0x104] = 0x00u.toByte() // NOP
        romData[0x105] = 0x00u.toByte() // NOP
        romData[0x106] = 0x00u.toByte() // NOP
        romData[0x107] = 0x00u.toByte() // NOP
        romData[0x108] = 0x00u.toByte() // NOP
        romData[0x109] = 0x00u.toByte() // NOP
        romData[0x10A] = 0x00u.toByte() // NOP
        romData[0x10B] = 0x00u.toByte() // NOP
        romData[0x10C] = 0x00u.toByte() // NOP
        romData[0x10D] = 0x00u.toByte() // NOP
        romData[0x10E] = 0x00u.toByte() // NOP
        romData[0x10F] = 0x00u.toByte() // NOP
        
        // Chargement de la ROM
        emulator.loadROM(romData)
        
        // Vérification du chargement
        assertEquals(0x0100, emulator.getCPU().getPC())
    }

    @Test
    fun `test CPU execution`() {
        // Création d'une ROM de test avec des instructions simples
        val romData = ByteArray(32 * 1024) { 0x00u.toByte() }
        romData[0x100] = 0x3Eu.toByte() // LD A, n
        romData[0x101] = 0x42u.toByte() // n = 0x42
        romData[0x102] = 0x06u.toByte() // LD B, n
        romData[0x103] = 0x43u.toByte() // n = 0x43
        
        // Chargement et exécution
        emulator.loadROM(romData)
        emulator.step()
        
        // Vérification des registres
        assertEquals(0x42u, emulator.getCPU().getA())
        assertEquals(0x43u, emulator.getCPU().getB())
    }

    @Test
    fun `test PPU rendering`() {
        // Configuration du PPU
        emulator.getMemory().setLCDC(0x01u) // Enable BG
        emulator.getMemory().setBGP(0xE4u) // Palette de couleurs
        
        // Exécution d'un cycle
        emulator.step()
        
        // Vérification du frame
        val frame = emulator.getPPU().getFrame()
        assertNotNull(frame)
    }

    @Test
    fun `test APU sound generation`() {
        // Configuration de l'APU
        emulator.getMemory().setNR52(0x80u) // Sound enabled
        emulator.getMemory().setNR10(0x00u) // Channel 1 enabled
        emulator.getMemory().setNR11(0x80u) // Duty = 50%
        emulator.getMemory().setNR12(0xF0u) // Volume = 15
        
        // Exécution d'un cycle
        emulator.step()
        
        // Vérification de la génération sonore
        val sample = emulator.getAPU().generateSample()
        assertNotNull(sample)
    }

    @Test
    fun `test timer operation`() {
        // Configuration du timer
        emulator.getMemory().writeByte(0xFF07u, 0x04u) // Timer enabled, clock = 4096 Hz
        
        // Exécution de plusieurs cycles
        repeat(4096) {
            emulator.step()
        }
        
        // Vérification du compteur
        val counter = emulator.getMemory().readByte(0xFF05u)
        assertTrue(counter > 0u)
    }

    @Test
    fun `test interrupt handling`() {
        // Configuration des interruptions
        emulator.getMemory().writeByte(0xFFFFu, 0x01u) // Enable VBlank interrupt
        emulator.getMemory().writeByte(0xFF0Fu, 0x01u) // Request VBlank interrupt
        
        // Exécution d'un cycle
        emulator.step()
        
        // Vérification de l'interruption
        assertTrue(emulator.getCPU().isInterruptEnabled(CPU.INT_VBLANK))
    }

    @Test
    fun `test save state`() {
        // Configuration de l'état
        emulator.getCPU().setA(0x42u)
        emulator.getCPU().setB(0x43u)
        emulator.getMemory().writeByte(0xC000u, 0x44u)
        
        // Sauvegarde de l'état
        val saveState = emulator.saveState()
        assertNotNull(saveState)
        
        // Modification de l'état
        emulator.getCPU().setA(0x00u)
        emulator.getCPU().setB(0x00u)
        emulator.getMemory().writeByte(0xC000u, 0x00u)
        
        // Chargement de l'état
        emulator.loadState(saveState!!)
        
        // Vérification de l'état restauré
        assertEquals(0x42u, emulator.getCPU().getA())
        assertEquals(0x43u, emulator.getCPU().getB())
        assertEquals(0x44u, emulator.getMemory().readByte(0xC000u))
    }

    @Test
    fun `test ROM validation`() {
        // Création d'une ROM invalide
        val invalidRomData = ByteArray(32 * 1024) { 0x00u.toByte() }
        
        // Tentative de chargement
        assertThrows(IllegalArgumentException::class.java) {
            emulator.loadROM(invalidRomData)
        }
    }

    @Test
    fun `test cartridge type detection`() {
        // Création d'une ROM avec un type de cartouche spécifique
        val romData = ByteArray(32 * 1024) { 0x00u.toByte() }
        romData[0x147] = 0x01u.toByte() // MBC1
        
        // Chargement de la ROM
        emulator.loadROM(romData)
        
        // Vérification du type de cartouche
        assertEquals(0x01u, emulator.getMemory().readByte(0x147u))
    }
} 