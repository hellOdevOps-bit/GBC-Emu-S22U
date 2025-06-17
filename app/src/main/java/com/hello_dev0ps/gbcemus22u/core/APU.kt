package com.hello_dev0ps.gbcemus22u.core

class APU(private val memory: Memory) {
    // Constantes pour les canaux sonores
    companion object {
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE = 4096
        const val CYCLES_PER_SAMPLE = 95 // ~4.19MHz / 44.1kHz
    }

    // État de l'APU
    private var enabled = false
    private var frameSequencer = 0
    private var frameSequencerClock = 0

    // Canaux sonores
    private val channel1 = SquareChannel(0xFF10) // Canal 1 (Square 1)
    private val channel2 = SquareChannel(0xFF15) // Canal 2 (Square 2)
    private val channel3 = WaveChannel(0xFF1A)   // Canal 3 (Wave)
    private val channel4 = NoiseChannel(0xFF20)  // Canal 4 (Noise)

    // Buffer audio
    private val audioBuffer = FloatArray(BUFFER_SIZE)
    private var bufferIndex = 0

    // Mise à jour de l'APU
    fun step(cycles: Int) {
        if (!enabled) return

        // Mise à jour du frame sequencer
        frameSequencerClock += cycles
        if (frameSequencerClock >= 8192) { // 512 Hz
            frameSequencerClock = 0
            frameSequencer = (frameSequencer + 1) % 8

            when (frameSequencer) {
                0, 4 -> { // 256 Hz
                    channel1.lengthCounter()
                    channel2.lengthCounter()
                    channel3.lengthCounter()
                    channel4.lengthCounter()
                }
                2, 6 -> { // 128 Hz
                    channel1.lengthCounter()
                    channel2.lengthCounter()
                    channel3.lengthCounter()
                    channel4.lengthCounter()
                    channel1.sweep()
                }
                7 -> { // 64 Hz
                    channel1.envelope()
                    channel2.envelope()
                    channel4.envelope()
                }
            }
        }

        // Mise à jour des canaux
        channel1.step(cycles)
        channel2.step(cycles)
        channel3.step(cycles)
        channel4.step(cycles)

        // Génération des échantillons audio
        for (i in 0 until cycles / CYCLES_PER_SAMPLE) {
            if (bufferIndex < BUFFER_SIZE) {
                val sample = (channel1.getSample() + channel2.getSample() + 
                            channel3.getSample() + channel4.getSample()) / 4.0f
                audioBuffer[bufferIndex++] = sample
            }
        }

        // Mise à jour du registre NR52 (Sound On/Off)
        updateNR52()
    }

    // Activation/désactivation de l'APU
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) {
            channel1.reset()
            channel2.reset()
            channel3.reset()
            channel4.reset()
            memory.setNR52(0u)
        } else {
            memory.setNR52(0xF0u) // Active tous les canaux
        }
    }

    // Obtention du buffer audio
    fun getAudioBuffer(): FloatArray {
        val buffer = audioBuffer.copyOf()
        bufferIndex = 0
        return buffer
    }

    // Mise à jour du registre NR52
    private fun updateNR52() {
        var value = 0u
        if (enabled) value = value or 0x80u
        if (channel1.isEnabled()) value = value or 0x01u
        if (channel2.isEnabled()) value = value or 0x02u
        if (channel3.isEnabled()) value = value or 0x04u
        if (channel4.isEnabled()) value = value or 0x08u
        memory.setNR52(0u.toUByte())
    }

    // Canal carré (Square Channel)
    private inner class SquareChannel(private val baseAddr: Int) {
        private var dutyCycle = 0
        private var dutyStep = 0
        private var frequency = 0
        private var frequencyTimer = 0
        private var lengthCounter = 0
        private var lengthEnabled = false
        private var volume = 0
        private var envelopeVolume = 0
        private var envelopeDirection = 0
        private var envelopePeriod = 0
        private var envelopeTimer = 0
        private var sweepEnabled = false
        private var sweepPeriod = 0
        private var sweepDirection = 0
        private var sweepShift = 0
        private var sweepTimer = 0
        private var sweepShadow = 0
        private var enabled = false

        init {
            reset()
        }

        fun reset() {
            dutyCycle = 0
            dutyStep = 0
            frequency = 0
            frequencyTimer = 0
            lengthCounter = 0
            lengthEnabled = false
            volume = 0
            envelopeVolume = 0
            envelopeDirection = 0
            envelopePeriod = 0
            envelopeTimer = 0
            sweepEnabled = false
            sweepPeriod = 0
            sweepDirection = 0
            sweepShift = 0
            sweepTimer = 0
            sweepShadow = 0
            enabled = false
        }

        fun isEnabled(): Boolean = enabled

        fun step(cycles: Int) {
            if (!enabled) return

            frequencyTimer -= cycles
            while (frequencyTimer <= 0) {
                frequencyTimer += (2048 - frequency) * 4
                dutyStep = (dutyStep + 1) % 8
            }
        }

        fun getSample(): Float {
            if (!enabled || lengthCounter == 0) return 0f

            val duty = when (dutyCycle) {
                0 -> 0x01 // 12.5%
                1 -> 0x81 // 25%
                2 -> 0x87 // 50%
                3 -> 0x7E // 75%
                else -> 0
            }

            val sample = if ((duty and (1 shl dutyStep)) != 0) {
                envelopeVolume / 15.0f
            } else {
                0f
            }

            return sample
        }

        fun lengthCounter() {
            if (lengthEnabled && lengthCounter > 0) {
                lengthCounter--
                if (lengthCounter == 0) {
                    enabled = false
                }
            }
        }

        fun envelope() {
            if (envelopePeriod > 0) {
                envelopeTimer--
                if (envelopeTimer <= 0) {
                    envelopeTimer = envelopePeriod
                    if (envelopeDirection == 1 && envelopeVolume < 15) {
                        envelopeVolume++
                    } else if (envelopeDirection == 0 && envelopeVolume > 0) {
                        envelopeVolume--
                    }
                }
            }
        }

        fun sweep() {
            if (sweepEnabled && sweepPeriod > 0) {
                sweepTimer--
                if (sweepTimer <= 0) {
                    sweepTimer = sweepPeriod
                    val newFreq = if (sweepDirection == 0) {
                        frequency + (frequency shr sweepShift)
                    } else {
                        frequency - (frequency shr sweepShift)
                    }
                    if (newFreq in 0..2047) {
                        frequency = newFreq
                        sweepShadow = newFreq
                    } else {
                        enabled = false
                    }
                }
            }
        }

        fun updateRegisters() {
            val nr11 = memory.getNR11()
            val nr12 = memory.getNR12()
            val nr13 = memory.getNR13()
            val nr14 = memory.getNR14()

            dutyCycle = (nr11.toInt() shr 6) and 0x03
            lengthCounter = 64 - (nr11.toInt() and 0x3F)
            envelopeVolume = (nr12.toInt() shr 4) and 0x0F
            envelopeDirection = (nr12.toInt() shr 3) and 0x01
            envelopePeriod = nr12.toInt() and 0x07
            frequency = ((nr14.toInt() and 0x07) shl 8) or nr13.toInt()
            lengthEnabled = (nr14.toInt() and 0x40) != 0
            enabled = (nr14.toInt() and 0x80) != 0

            if (baseAddr == 0xFF10) {
                val nr10 = memory.getNR10()
                sweepEnabled = (nr10.toInt() and 0x80) != 0
                sweepPeriod = (nr10.toInt() shr 4) and 0x07
                sweepDirection = (nr10.toInt() shr 3) and 0x01
                sweepShift = nr10.toInt() and 0x07
            }
        }
    }

    // Canal d'onde (Wave Channel)
    private inner class WaveChannel(baseAddr: Int) {
        private val waveRAM = ByteArray(32)
        private var enabled = false
        private var volume = 0
        private var frequency = 0
        private var frequencyTimer = 0
        private var wavePosition = 0
        private var lengthCounter = 0
        private var lengthEnabled = false

        init {
            reset()
        }

        fun reset() {
            enabled = false
            volume = 0
            frequency = 0
            frequencyTimer = 0
            wavePosition = 0
            lengthCounter = 0
            lengthEnabled = false
        }

        fun isEnabled(): Boolean = enabled

        fun step(cycles: Int) {
            if (!enabled) return

            frequencyTimer -= cycles
            while (frequencyTimer <= 0) {
                frequencyTimer += (2048 - frequency) * 2
                wavePosition = (wavePosition + 1) % 32
            }
        }

        fun getSample(): Float {
            if (!enabled || lengthCounter == 0) return 0f

            val sample = waveRAM[wavePosition / 2]
            val shift = if (wavePosition % 2 == 0) 4 else 0
            val value = ((sample.toInt() shr shift) and 0x0F)
            return value / 15.0f * (volume / 2.0f)
        }

        fun lengthCounter() {
            if (lengthEnabled && lengthCounter > 0) {
                lengthCounter--
                if (lengthCounter == 0) {
                    enabled = false
                }
            }
        }

        fun updateRegisters() {
            val nr30 = memory.getNR30()
            val nr31 = memory.getNR31()
            val nr32 = memory.getNR32()
            val nr33 = memory.getNR33()
            val nr34 = memory.getNR34()

            enabled = (nr30.toInt() and 0x80) != 0
            lengthCounter = 256 - nr31.toInt()
            volume = (nr32.toInt() shr 5) and 0x03
            frequency = ((nr34.toInt() and 0x07) shl 8) or nr33.toInt()
            lengthEnabled = (nr34.toInt() and 0x40) != 0
        }
    }

    // Canal de bruit (Noise Channel)
    private inner class NoiseChannel(baseAddr: Int) {
        private var lengthCounter = 0
        private var lengthEnabled = false
        private var volume = 0
        private var envelopeVolume = 0
        private var envelopeDirection = 0
        private var envelopePeriod = 0
        private var envelopeTimer = 0
        private var clockShift = 0
        private var widthMode = 0
        private var divisor = 0
        private var frequencyTimer = 0
        private var lfsr = 0x7FFF
        private var enabled = false

        init {
            reset()
        }

        fun reset() {
            lengthCounter = 0
            lengthEnabled = false
            volume = 0
            envelopeVolume = 0
            envelopeDirection = 0
            envelopePeriod = 0
            envelopeTimer = 0
            clockShift = 0
            widthMode = 0
            divisor = 0
            frequencyTimer = 0
            lfsr = 0x7FFF
            enabled = false
        }

        fun isEnabled(): Boolean = enabled

        fun step(cycles: Int) {
            if (!enabled) return

            frequencyTimer -= cycles
            while (frequencyTimer <= 0) {
                frequencyTimer += (divisor shl clockShift) * 4
                val xor = (lfsr and 1) xor ((lfsr shr 1) and 1)
                lfsr = lfsr shr 1
                lfsr = lfsr or (xor shl 14)
                if (widthMode == 1) {
                    lfsr = lfsr and 0xFFBF
                    lfsr = lfsr or (xor shl 6)
                }
            }
        }

        fun getSample(): Float {
            if (!enabled || lengthCounter == 0) return 0f

            val sample = if ((lfsr and 1) == 0) {
                envelopeVolume / 15.0f
            } else {
                0f
            }

            return sample
        }

        fun lengthCounter() {
            if (lengthEnabled && lengthCounter > 0) {
                lengthCounter--
                if (lengthCounter == 0) {
                    enabled = false
                }
            }
        }

        fun envelope() {
            if (envelopePeriod > 0) {
                envelopeTimer--
                if (envelopeTimer <= 0) {
                    envelopeTimer = envelopePeriod
                    if (envelopeDirection == 1 && envelopeVolume < 15) {
                        envelopeVolume++
                    } else if (envelopeDirection == 0 && envelopeVolume > 0) {
                        envelopeVolume--
                    }
                }
            }
        }

        fun updateRegisters() {
            val nr41 = memory.getNR41()
            val nr42 = memory.getNR42()
            val nr43 = memory.getNR43()
            val nr44 = memory.getNR44()

            lengthCounter = 64 - (nr41.toInt() and 0x3F)
            envelopeVolume = (nr42.toInt() shr 4) and 0x0F
            envelopeDirection = (nr42.toInt() shr 3) and 0x01
            envelopePeriod = nr42.toInt() and 0x07
            clockShift = (nr43.toInt() shr 4) and 0x0F
            widthMode = (nr43.toInt() shr 3) and 0x01
            divisor = nr43.toInt() and 0x07
            lengthEnabled = (nr44.toInt() and 0x40) != 0
            enabled = (nr44.toInt() and 0x80) != 0
        }
    }
} 