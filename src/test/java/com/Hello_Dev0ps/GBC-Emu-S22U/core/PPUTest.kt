package com.Hello_Dev0ps.gbc_emu_s22u

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class PPUTest {
    private lateinit var memory: Memory
    private lateinit var ppu: PPU

    @BeforeEach
    fun setup() {
        memory = Memory()
        ppu = PPU(memory)
    }

    @Test
    fun `test PPU initialization`() {
        assertEquals(0, ppu.getMode())
        assertEquals(0, ppu.getLine())
        assertFalse(ppu.isLycCoincidence())
    }

    @Test
    fun `test PPU mode transitions`() {
        // Mode 2 (OAM)
        ppu.step(80)
        assertEquals(2, ppu.getMode())
        
        // Mode 3 (Pixel Transfer)
        ppu.step(172)
        assertEquals(3, ppu.getMode())
        
        // Mode 0 (HBlank)
        ppu.step(204)
        assertEquals(0, ppu.getMode())
        
        // Mode 1 (VBlank)
        ppu.step(456)
        assertEquals(1, ppu.getMode())
    }

    @Test
    fun `test LYC coincidence`() {
        memory.setLY(0x42u)
        memory.setLYC(0x42u)
        ppu.step(1)
        assertTrue(ppu.isLycCoincidence())
        
        memory.setLY(0x43u)
        ppu.step(1)
        assertFalse(ppu.isLycCoincidence())
    }

    @Test
    fun `test background rendering`() {
        // Configuration du fond
        memory.setLCDC(0x01u) // Enable BG
        memory.setBGP(0xE4u) // Palette de couleurs
        memory.setSCX(0x00u)
        memory.setSCY(0x00u)
        
        // Données de tuile
        memory.writeByte(0x8000u, 0x3Cu) // Données de tuile
        memory.writeByte(0x9800u, 0x00u) // Carte de tuiles
        
        ppu.step(456) // Un cycle complet
        val frame = ppu.getFrame()
        assertNotNull(frame)
        assertTrue(frame!!.any { it != 0 }) // Vérifie que le frame n'est pas vide
    }

    @Test
    fun `test window rendering`() {
        // Configuration de la fenêtre
        memory.setLCDC(0x20u) // Enable Window
        memory.setWX(0x07u)
        memory.setWY(0x00u)
        
        // Données de tuile
        memory.writeByte(0x8000u, 0x3Cu)
        memory.writeByte(0x9C00u, 0x00u) // Carte de tuiles de la fenêtre
        
        ppu.step(456) // Un cycle complet
        val frame = ppu.getFrame()
        assertNotNull(frame)
        assertTrue(frame!!.any { it != 0 })
    }

    @Test
    fun `test sprite rendering`() {
        // Configuration des sprites
        memory.setLCDC(0x02u) // Enable Sprites
        memory.setOBP0(0xE4u) // Palette de couleurs des sprites
        
        // Données de sprite
        memory.writeByte(0xFE00u, 0x00u) // Y position
        memory.writeByte(0xFE01u, 0x00u) // X position
        memory.writeByte(0xFE02u, 0x00u) // Tile number
        memory.writeByte(0xFE03u, 0x00u) // Attributes
        
        ppu.step(456) // Un cycle complet
        val frame = ppu.getFrame()
        assertNotNull(frame)
        assertTrue(frame!!.any { it != 0 })
    }

    @Test
    fun `test GBC color palettes`() {
        // Configuration des palettes GBC
        memory.writeByte(0xFF68u, 0x80u) // Index auto-incrémenté
        memory.writeByte(0xFF69u, 0x1Fu) // Couleur 0
        memory.writeByte(0xFF69u, 0x7Fu) // Couleur 1
        memory.writeByte(0xFF69u, 0xE0u) // Couleur 2
        memory.writeByte(0xFF69u, 0xFFu) // Couleur 3
        
        ppu.updatePalettes()
        val palette = ppu.getBackgroundPalette(0)
        assertNotNull(palette)
        assertEquals(4, palette!!.size)
    }

    @Test
    fun `test sprite priority`() {
        // Configuration des sprites
        memory.setLCDC(0x02u) // Enable Sprites
        
        // Sprite 1 (priorité basse)
        memory.writeByte(0xFE00u, 0x50u) // Y position
        memory.writeByte(0xFE01u, 0x50u) // X position
        memory.writeByte(0xFE02u, 0x00u) // Tile number
        memory.writeByte(0xFE03u, 0x00u) // Attributes
        
        // Sprite 2 (priorité haute)
        memory.writeByte(0xFE04u, 0x50u) // Y position
        memory.writeByte(0xFE05u, 0x50u) // X position
        memory.writeByte(0xFE06u, 0x00u) // Tile number
        memory.writeByte(0xFE07u, 0x80u) // Attributes (priorité haute)
        
        ppu.step(456) // Un cycle complet
        val frame = ppu.getFrame()
        assertNotNull(frame)
        assertTrue(frame!!.any { it != 0 })
    }

    @Test
    fun `test sprite flipping`() {
        // Configuration du sprite
        memory.setLCDC(0x02u) // Enable Sprites
        
        // Sprite avec flip horizontal
        memory.writeByte(0xFE00u, 0x50u) // Y position
        memory.writeByte(0xFE01u, 0x50u) // X position
        memory.writeByte(0xFE02u, 0x00u) // Tile number
        memory.writeByte(0xFE03u, 0x20u) // Attributes (flip horizontal)
        
        ppu.step(456) // Un cycle complet
        val frame = ppu.getFrame()
        assertNotNull(frame)
        assertTrue(frame!!.any { it != 0 })
    }

    @Test
    fun `test window layer priority`() {
        // Configuration du fond et de la fenêtre
        memory.setLCDC(0x21u) // Enable BG et Window
        
        // Données de tuile
        memory.writeByte(0x8000u, 0xFFu) // Tuile pleine
        memory.writeByte(0x9800u, 0x00u) // Carte de tuiles du fond
        memory.writeByte(0x9C00u, 0x00u) // Carte de tuiles de la fenêtre
        
        ppu.step(456) // Un cycle complet
        val frame = ppu.getFrame()
        assertNotNull(frame)
        assertTrue(frame!!.any { it != 0 })
    }
} 