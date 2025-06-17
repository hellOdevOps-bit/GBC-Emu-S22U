package com.hello_dev0ps.gbcemus22u

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class APUTest {
    private lateinit var memory: Memory
    private lateinit var apu: APU

    @BeforeEach
    fun setup() {
        memory = Memory()
        apu = APU(memory)
    }

    @Test
    fun `test APU initialization`() {
        assertFalse(apu.isEnabled())
        assertEquals(0, apu.getSampleRate())
        assertEquals(0, apu.getBufferSize())
    }

    @Test
    fun `test channel 1 initialization`() {
        val channel1 = apu.getChannel1()
        assertNotNull(channel1)
        assertFalse(channel1!!.isEnabled())
        assertEquals(0, channel1.getFrequency())
        assertEquals(0, channel1.getVolume())
    }

    @Test
    fun `test channel 2 initialization`() {
        val channel2 = apu.getChannel2()
        assertNotNull(channel2)
        assertFalse(channel2!!.isEnabled())
        assertEquals(0, channel2.getFrequency())
        assertEquals(0, channel2.getVolume())
    }

    @Test
    fun `test channel 3 initialization`() {
        val channel3 = apu.getChannel3()
        assertNotNull(channel3)
        assertFalse(channel3!!.isEnabled())
        assertEquals(0, channel3.getFrequency())
        assertEquals(0, channel3.getVolume())
    }

    @Test
    fun `test channel 4 initialization`() {
        val channel4 = apu.getChannel4()
        assertNotNull(channel4)
        assertFalse(channel4!!.isEnabled())
        assertEquals(0, channel4.getFrequency())
        assertEquals(0, channel4.getVolume())
    }

    @Test
    fun `test channel 1 sweep`() {
        val channel1 = apu.getChannel1()!!
        
        // Configuration du sweep
        memory.setNR10(0x70u) // Sweep time = 7, direction = decrease, shift = 0
        channel1.updateSweep()
        
        // Vérification de la fréquence
        assertEquals(0, channel1.getFrequency())
    }

    @Test
    fun `test channel 1 length counter`() {
        val channel1 = apu.getChannel1()!!
        
        // Configuration du compteur de longueur
        memory.setNR11(0x3Fu) // Duty = 50%, Length = 63
        channel1.updateLength()
        
        // Vérification de la longueur
        assertEquals(63, channel1.getLength())
    }

    @Test
    fun `test channel 1 volume envelope`() {
        val channel1 = apu.getChannel1()!!
        
        // Configuration de l'enveloppe
        memory.setNR12(0xF3u) // Initial volume = 15, direction = increase, sweep = 3
        channel1.updateEnvelope()
        
        // Vérification du volume
        assertEquals(15, channel1.getVolume())
    }

    @Test
    fun `test channel 2 length counter`() {
        val channel2 = apu.getChannel2()!!
        
        // Configuration du compteur de longueur
        memory.setNR21(0x3Fu) // Duty = 50%, Length = 63
        channel2.updateLength()
        
        // Vérification de la longueur
        assertEquals(63, channel2.getLength())
    }

    @Test
    fun `test channel 2 volume envelope`() {
        val channel2 = apu.getChannel2()!!
        
        // Configuration de l'enveloppe
        memory.setNR22(0xF3u) // Initial volume = 15, direction = increase, sweep = 3
        channel2.updateEnvelope()
        
        // Vérification du volume
        assertEquals(15, channel2.getVolume())
    }

    @Test
    fun `test channel 3 wave pattern`() {
        val channel3 = apu.getChannel3()!!
        
        // Configuration du pattern d'onde
        memory.setNR30(0x80u) // Channel 3 enabled
        memory.writeByte(0xFF30u, 0x00u) // Wave pattern
        channel3.updateWavePattern()
        
        // Vérification du pattern
        assertNotNull(channel3.getWavePattern())
    }

    @Test
    fun `test channel 3 length counter`() {
        val channel3 = apu.getChannel3()!!
        
        // Configuration du compteur de longueur
        memory.setNR31(0xFFu) // Length = 255
        channel3.updateLength()
        
        // Vérification de la longueur
        assertEquals(255, channel3.getLength())
    }

    @Test
    fun `test channel 4 length counter`() {
        val channel4 = apu.getChannel4()!!
        
        // Configuration du compteur de longueur
        memory.setNR41(0x3Fu) // Length = 63
        channel4.updateLength()
        
        // Vérification de la longueur
        assertEquals(63, channel4.getLength())
    }

    @Test
    fun `test channel 4 volume envelope`() {
        val channel4 = apu.getChannel4()!!
        
        // Configuration de l'enveloppe
        memory.setNR42(0xF3u) // Initial volume = 15, direction = increase, sweep = 3
        channel4.updateEnvelope()
        
        // Vérification du volume
        assertEquals(15, channel4.getVolume())
    }

    @Test
    fun `test channel 4 polynomial counter`() {
        val channel4 = apu.getChannel4()!!
        
        // Configuration du compteur polynomial
        memory.setNR43(0x00u) // Clock shift = 0, width = 15 bits, divisor = 8
        channel4.updatePolynomialCounter()
        
        // Vérification du compteur
        assertNotNull(channel4.getPolynomialCounter())
    }

    @Test
    fun `test master volume control`() {
        // Configuration du volume maître
        memory.setNR50(0x77u) // Left volume = 7, Right volume = 7
        apu.updateMasterVolume()
        
        // Vérification du volume
        assertEquals(7, apu.getLeftVolume())
        assertEquals(7, apu.getRightVolume())
    }

    @Test
    fun `test sound output control`() {
        // Configuration de la sortie sonore
        memory.setNR51(0xF3u) // Channel 1-4 enabled on both speakers
        apu.updateSoundOutput()
        
        // Vérification de la configuration
        assertTrue(apu.isChannelEnabled(0))
        assertTrue(apu.isChannelEnabled(1))
        assertTrue(apu.isChannelEnabled(2))
        assertTrue(apu.isChannelEnabled(3))
    }

    @Test
    fun `test sound on/off control`() {
        // Configuration du son
        memory.setNR52(0x80u) // Sound enabled
        apu.updateSoundControl()
        
        // Vérification de l'état
        assertTrue(apu.isEnabled())
    }

    @Test
    fun `test sample generation`() {
        // Configuration des canaux
        memory.setNR52(0x80u) // Sound enabled
        memory.setNR10(0x00u) // Channel 1 enabled
        memory.setNR11(0x80u) // Duty = 50%
        memory.setNR12(0xF0u) // Volume = 15
        memory.setNR13(0x00u) // Frequency = 0
        memory.setNR14(0x80u) // Channel 1 enabled
        
        // Génération d'un échantillon
        val sample = apu.generateSample()
        assertNotNull(sample)
        assertTrue(sample!!.any { it != 0 })
    }
} 