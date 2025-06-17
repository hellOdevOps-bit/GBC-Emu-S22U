package com.hello_dev0ps.gbcemus22u

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MemoryTest {
    private lateinit var memory: Memory

    @BeforeEach
    fun setup() {
        memory = Memory()
    }

    @Test
    fun `test ROM read write`() {
        // ROM est en lecture seule
        memory.writeByte(0x0000u, 0x42u)
        assertEquals(0x00u, memory.readByte(0x0000u))
    }

    @Test
    fun `test VRAM read write`() {
        memory.writeByte(0x8000u, 0x42u)
        assertEquals(0x42u, memory.readByte(0x8000u))
    }

    @Test
    fun `test WRAM read write`() {
        memory.writeByte(0xC000u, 0x42u)
        assertEquals(0x42u, memory.readByte(0xC000u))
    }

    @Test
    fun `test ECHO RAM mirror`() {
        memory.writeByte(0xC000u, 0x42u)
        assertEquals(0x42u, memory.readByte(0xE000u))
        memory.writeByte(0xE000u, 0x43u)
        assertEquals(0x43u, memory.readByte(0xC000u))
    }

    @Test
    fun `test OAM read write`() {
        memory.writeByte(0xFE00u, 0x42u)
        assertEquals(0x42u, memory.readByte(0xFE00u))
    }

    @Test
    fun `test IO Registers read write`() {
        memory.writeByte(0xFF00u, 0x42u)
        assertEquals(0x42u, memory.readByte(0xFF00u))
    }

    @Test
    fun `test HRAM read write`() {
        memory.writeByte(0xFF80u, 0x42u)
        assertEquals(0x42u, memory.readByte(0xFF80u))
    }

    @Test
    fun `test ROM banking`() {
        // Test avec une ROM de 64 Ko (4 banques)
        val romData = ByteArray(64 * 1024) { 0x42u.toByte() }
        memory.loadROM(romData)
        
        // Vérification de la banque 0
        assertEquals(0x42u, memory.readByte(0x0000u))
        
        // Vérification de la banque 1
        memory.writeByte(0x2000u, 0x01u) // Sélection de la banque 1
        assertEquals(0x42u, memory.readByte(0x4000u))
    }

    @Test
    fun `test VRAM banking`() {
        // Test avec 2 banques VRAM
        memory.writeByte(0xFF4Fu, 0x01u) // Sélection de la banque 1
        memory.writeByte(0x8000u, 0x42u)
        assertEquals(0x42u, memory.readByte(0x8000u))
        
        memory.writeByte(0xFF4Fu, 0x00u) // Retour à la banque 0
        assertEquals(0x00u, memory.readByte(0x8000u))
    }

    @Test
    fun `test WRAM banking`() {
        // Test avec 8 banques WRAM
        memory.writeByte(0xFF70u, 0x01u) // Sélection de la banque 1
        memory.writeByte(0xD000u, 0x42u)
        assertEquals(0x42u, memory.readByte(0xD000u))
        
        memory.writeByte(0xFF70u, 0x00u) // Retour à la banque 0
        assertEquals(0x00u, memory.readByte(0xD000u))
    }

    @Test
    fun `test DMA transfer`() {
        // Préparation des données source
        memory.writeByte(0xC000u, 0x42u)
        
        // Démarrage du transfert DMA
        memory.writeByte(0xFF46u, 0xC0u)
        
        // Vérification des données copiées
        assertEquals(0x42u, memory.readByte(0xFE00u))
    }

    @Test
    fun `test IO Register specific accessors`() {
        // Test des accesseurs spécifiques
        memory.setLCDC(0x42u)
        assertEquals(0x42u, memory.getLCDC())
        
        memory.setSTAT(0x43u)
        assertEquals(0x43u, memory.getSTAT())
        
        memory.setSCY(0x44u)
        assertEquals(0x44u, memory.getSCY())
        
        memory.setSCX(0x45u)
        assertEquals(0x45u, memory.getSCX())
        
        memory.setLY(0x46u)
        assertEquals(0x46u, memory.getLY())
        
        memory.setLYC(0x47u)
        assertEquals(0x47u, memory.getLYC())
        
        memory.setBGP(0x48u)
        assertEquals(0x48u, memory.getBGP())
        
        memory.setOBP0(0x49u)
        assertEquals(0x49u, memory.getOBP0())
        
        memory.setOBP1(0x4Au)
        assertEquals(0x4Au, memory.getOBP1())
        
        memory.setWY(0x4Bu)
        assertEquals(0x4Bu, memory.getWY())
        
        memory.setWX(0x4Cu)
        assertEquals(0x4Cu, memory.getWX())
    }

    @Test
    fun `test APU Register specific accessors`() {
        // Test des accesseurs APU
        memory.setNR10(0x42u)
        assertEquals(0x42u, memory.getNR10())
        
        memory.setNR11(0x43u)
        assertEquals(0x43u, memory.getNR11())
        
        memory.setNR12(0x44u)
        assertEquals(0x44u, memory.getNR12())
        
        memory.setNR13(0x45u)
        assertEquals(0x45u, memory.getNR13())
        
        memory.setNR14(0x46u)
        assertEquals(0x46u, memory.getNR14())
        
        memory.setNR21(0x47u)
        assertEquals(0x47u, memory.getNR21())
        
        memory.setNR22(0x48u)
        assertEquals(0x48u, memory.getNR22())
        
        memory.setNR23(0x49u)
        assertEquals(0x49u, memory.getNR23())
        
        memory.setNR24(0x4Au)
        assertEquals(0x4Au, memory.getNR24())
        
        memory.setNR30(0x4Bu)
        assertEquals(0x4Bu, memory.getNR30())
        
        memory.setNR31(0x4Cu)
        assertEquals(0x4Cu, memory.getNR31())
        
        memory.setNR32(0x4Du)
        assertEquals(0x4Du, memory.getNR32())
        
        memory.setNR33(0x4Eu)
        assertEquals(0x4Eu, memory.getNR33())
        
        memory.setNR34(0x4Fu)
        assertEquals(0x4Fu, memory.getNR34())
        
        memory.setNR41(0x50u)
        assertEquals(0x50u, memory.getNR41())
        
        memory.setNR42(0x51u)
        assertEquals(0x51u, memory.getNR42())
        
        memory.setNR43(0x52u)
        assertEquals(0x52u, memory.getNR43())
        
        memory.setNR44(0x53u)
        assertEquals(0x53u, memory.getNR44())
        
        memory.setNR50(0x54u)
        assertEquals(0x54u, memory.getNR50())
        
        memory.setNR51(0x55u)
        assertEquals(0x55u, memory.getNR51())
        
        memory.setNR52(0x56u)
        assertEquals(0x56u, memory.getNR52())
    }

    @Test
    fun `test RTC functionality`() {
        // Configuration du MBC3 avec RTC
        val romData = ByteArray(32 * 1024) { 0x00u.toByte() }
        romData[0x147] = 0x0Fu.toByte() // MBC3 + Timer + RAM
        memory.loadROM(romData)

        // Activation de la RAM/RTC
        memory.writeByte(0x0000u, 0x0Au)

        // Test de l'écriture des registres RTC
        memory.writeByte(0x4000u, 0x08u) // Sélection du registre des secondes
        memory.writeByte(0xA000u, 0x3Bu) // Écriture de 59 secondes
        assertEquals(0x3Bu, memory.readByte(0xA000u))

        memory.writeByte(0x4000u, 0x09u) // Sélection du registre des minutes
        memory.writeByte(0xA000u, 0x3Bu) // Écriture de 59 minutes
        assertEquals(0x3Bu, memory.readByte(0xA000u))

        memory.writeByte(0x4000u, 0x0Au) // Sélection du registre des heures
        memory.writeByte(0xA000u, 0x17u) // Écriture de 23 heures
        assertEquals(0x17u, memory.readByte(0xA000u))

        memory.writeByte(0x4000u, 0x0Bu) // Sélection du registre des jours (bas)
        memory.writeByte(0xA000u, 0xFFu) // Écriture des 8 bits bas des jours
        assertEquals(0xFFu, memory.readByte(0xA000u))

        memory.writeByte(0x4000u, 0x0Cu) // Sélection du registre des jours (haut) + contrôle
        memory.writeByte(0xA000u, 0x01u) // Écriture du bit haut des jours
        assertEquals(0x01u, memory.readByte(0xA000u))

        // Test du latch clock
        memory.writeByte(0x6000u, 0x01u)
        memory.writeByte(0x6000u, 0x00u)
        assertEquals(0x3Bu, memory.readByte(0xA000u)) // Vérification des secondes latched
    }

    @Test
    fun `test RTC overflow`() {
        // Configuration du MBC3 avec RTC
        val romData = ByteArray(32 * 1024) { 0x00u.toByte() }
        romData[0x147] = 0x0Fu.toByte() // MBC3 + Timer + RAM
        memory.loadROM(romData)

        // Activation de la RAM/RTC
        memory.writeByte(0x0000u, 0x0Au)

        // Configuration pour tester le débordement
        memory.writeByte(0x4000u, 0x08u) // Sélection du registre des secondes
        memory.writeByte(0xA000u, 0x3Bu) // 59 secondes
        memory.writeByte(0x4000u, 0x09u) // Sélection du registre des minutes
        memory.writeByte(0xA000u, 0x3Bu) // 59 minutes
        memory.writeByte(0x4000u, 0x0Au) // Sélection du registre des heures
        memory.writeByte(0xA000u, 0x17u) // 23 heures

        // Simulation du passage à minuit
        for (i in 0..32768) {
            memory.updateRTC(1)
        }

        // Vérification des valeurs après débordement
        memory.writeByte(0x4000u, 0x08u)
        assertEquals(0x00u, memory.readByte(0xA000u)) // Secondes remises à 0
        memory.writeByte(0x4000u, 0x09u)
        assertEquals(0x00u, memory.readByte(0xA000u)) // Minutes remises à 0
        memory.writeByte(0x4000u, 0x0Au)
        assertEquals(0x00u, memory.readByte(0xA000u)) // Heures remises à 0
    }

    @Test
    fun `test RTC halt`() {
        // Configuration du MBC3 avec RTC
        val romData = ByteArray(32 * 1024) { 0x00u.toByte() }
        romData[0x147] = 0x0Fu.toByte() // MBC3 + Timer + RAM
        memory.loadROM(romData)

        // Activation de la RAM/RTC
        memory.writeByte(0x0000u, 0x0Au)

        // Configuration initiale
        memory.writeByte(0x4000u, 0x08u) // Sélection du registre des secondes
        memory.writeByte(0xA000u, 0x3Bu) // 59 secondes

        // Activation du mode halt
        memory.writeByte(0x4000u, 0x0Cu) // Sélection du registre de contrôle
        memory.writeByte(0xA000u, 0x40u) // Activation du mode halt

        // Simulation du temps qui passe
        for (i in 0..32768) {
            memory.updateRTC(1)
        }

        // Vérification que les secondes n'ont pas changé
        memory.writeByte(0x4000u, 0x08u)
        assertEquals(0x3Bu, memory.readByte(0xA000u))
    }
} 