package com.Hello_Dev0ps.gbc_emu_s22u

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class ROMTest {
    @Test
    fun `test CPU instructions ROM`() {
        // Vérification que la ROM existe
        val romPath = "src/main/assets/cpu_instrs.gb"
        val romFile = File(romPath)
        assertTrue(romFile.exists(), "La ROM de test doit exister")
        
        // Vérification de la taille de la ROM
        assertTrue(romFile.length() > 0, "La ROM ne doit pas être vide")
        assertTrue(romFile.length() <= 64 * 1024, "La ROM ne doit pas dépasser 64 Ko")
        
        // Lecture des premiers octets pour vérifier le header
        val romData = romFile.readBytes()
        assertTrue(romData.size >= 0x150, "La ROM doit avoir un header valide")
        
        // Vérification du type de cartouche
        val cartridgeType = romData[0x147].toUByte()
        assertTrue(cartridgeType in 0x00u..0x1Eu, "Type de cartouche invalide")
        
        // Vérification de la taille de la ROM
        val romSize = romData[0x148].toUByte()
        assertTrue(romSize in 0x00u..0x08u, "Taille de ROM invalide")
        
        // Vérification de la taille de la RAM
        val ramSize = romData[0x149].toUByte()
        assertTrue(ramSize in 0x00u..0x05u, "Taille de RAM invalide")
    }
} 