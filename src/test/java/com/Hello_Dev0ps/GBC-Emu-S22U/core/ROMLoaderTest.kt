package com.Hello_Dev0ps.gbc_emu_s22u

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class ROMLoaderTest {
    private lateinit var tempDir: Path
    private lateinit var testRomPath: Path

    @BeforeEach
    fun setup() {
        tempDir = Files.createTempDirectory("rom_test")
        testRomPath = tempDir.resolve("test.gb")
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
        
        // Écriture de la ROM
        Files.write(testRomPath, romData)
        
        // Chargement de la ROM
        val loadedRom = ROMLoader.loadROM(testRomPath.toString())
        
        // Vérification du chargement
        assertNotNull(loadedRom)
        assertEquals(romData.size, loadedRom.size)
        assertArrayEquals(romData, loadedRom)
    }

    @Test
    fun `test ROM validation`() {
        // Création d'une ROM invalide
        val invalidRomData = ByteArray(32 * 1024) { 0x00u.toByte() }
        Files.write(testRomPath, invalidRomData)
        
        // Tentative de chargement
        assertThrows(IllegalArgumentException::class.java) {
            ROMLoader.loadROM(testRomPath.toString())
        }
    }

    @Test
    fun `test ROM header validation`() {
        // Création d'une ROM avec un en-tête valide
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
        
        // Ajout des informations de cartouche
        romData[0x147] = 0x00u.toByte() // ROM only
        romData[0x148] = 0x00u.toByte() // 32 Ko
        romData[0x149] = 0x00u.toByte() // No RAM
        
        // Écriture de la ROM
        Files.write(testRomPath, romData)
        
        // Chargement de la ROM
        val loadedRom = ROMLoader.loadROM(testRomPath.toString())
        
        // Vérification du chargement
        assertNotNull(loadedRom)
        assertEquals(romData.size, loadedRom.size)
        assertArrayEquals(romData, loadedRom)
    }

    @Test
    fun `test cartridge type detection`() {
        // Création d'une ROM avec différents types de cartouches
        val romData = ByteArray(32 * 1024) { 0x00u.toByte() }
        
        // Test MBC1
        romData[0x147] = 0x01u.toByte() // MBC1
        Files.write(testRomPath, romData)
        var loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x01u, loadedRom[0x147].toUByte())
        
        // Test MBC2
        romData[0x147] = 0x05u.toByte() // MBC2
        Files.write(testRomPath, romData)
        loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x05u, loadedRom[0x147].toUByte())
        
        // Test MBC3
        romData[0x147] = 0x0Fu.toByte() // MBC3
        Files.write(testRomPath, romData)
        loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x0Fu, loadedRom[0x147].toUByte())
        
        // Test MBC5
        romData[0x147] = 0x19u.toByte() // MBC5
        Files.write(testRomPath, romData)
        loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x19u, loadedRom[0x147].toUByte())
    }

    @Test
    fun `test ROM size detection`() {
        // Création d'une ROM avec différentes tailles
        val romData = ByteArray(32 * 1024) { 0x00u.toByte() }
        
        // Test 32 Ko
        romData[0x148] = 0x00u.toByte() // 32 Ko
        Files.write(testRomPath, romData)
        var loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x00u, loadedRom[0x148].toUByte())
        
        // Test 64 Ko
        romData[0x148] = 0x01u.toByte() // 64 Ko
        Files.write(testRomPath, romData)
        loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x01u, loadedRom[0x148].toUByte())
        
        // Test 128 Ko
        romData[0x148] = 0x02u.toByte() // 128 Ko
        Files.write(testRomPath, romData)
        loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x02u, loadedRom[0x148].toUByte())
        
        // Test 256 Ko
        romData[0x148] = 0x03u.toByte() // 256 Ko
        Files.write(testRomPath, romData)
        loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x03u, loadedRom[0x148].toUByte())
    }

    @Test
    fun `test RAM size detection`() {
        // Création d'une ROM avec différentes tailles de RAM
        val romData = ByteArray(32 * 1024) { 0x00u.toByte() }
        
        // Test pas de RAM
        romData[0x149] = 0x00u.toByte() // No RAM
        Files.write(testRomPath, romData)
        var loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x00u, loadedRom[0x149].toUByte())
        
        // Test 2 Ko
        romData[0x149] = 0x01u.toByte() // 2 Ko
        Files.write(testRomPath, romData)
        loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x01u, loadedRom[0x149].toUByte())
        
        // Test 8 Ko
        romData[0x149] = 0x02u.toByte() // 8 Ko
        Files.write(testRomPath, romData)
        loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x02u, loadedRom[0x149].toUByte())
        
        // Test 32 Ko
        romData[0x149] = 0x03u.toByte() // 32 Ko
        Files.write(testRomPath, romData)
        loadedRom = ROMLoader.loadROM(testRomPath.toString())
        assertEquals(0x03u, loadedRom[0x149].toUByte())
    }

    @Test
    fun `test file not found`() {
        assertThrows(IllegalArgumentException::class.java) {
            ROMLoader.loadROM("nonexistent.gb")
        }
    }

    @Test
    fun `test file too small`() {
        // Création d'une ROM trop petite
        val romData = ByteArray(1024) { 0x00u.toByte() }
        Files.write(testRomPath, romData)
        
        assertThrows(IllegalArgumentException::class.java) {
            ROMLoader.loadROM(testRomPath.toString())
        }
    }
} 