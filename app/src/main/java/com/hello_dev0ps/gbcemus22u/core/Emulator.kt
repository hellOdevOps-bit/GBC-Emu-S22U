package com.hello_dev0ps.gbcemus22u

class Emulator(romData: ByteArray) {

    private val memory = Memory()
    private val cpu = CPU(memory)
    private val ppu = PPU(memory)
    private val apu = APU(memory)
    private var running = false

    // Timers
    private var dividerCounter = 0
    private var timerCounter = 0
    private var timerModulo = 0
    private var timerControl = 0
    private var timerEnabled = false
    private var timerClock = 0

    // Interruptions
    private val INT_VBLANK = 0
    private val INT_LCD_STAT = 1
    private val INT_TIMER = 2
    private val INT_SERIAL = 3
    private val INT_JOYPAD = 4

    init {
        memory.loadROM(romData)
    }

    fun loadROM(romPath: String) {
        val romData = ROMLoader.loadROM(romPath)
        memory.loadROM(romData)
    }

    fun start() {
        running = true
        apu.setEnabled(true)
        
        while (running) {
            val cycles = cpu.step()
            updateTimers(cycles)
            ppu.step(cycles)
            apu.step(cycles)
            checkInterrupts()
        }
    }

    fun stop() {
        running = false
        apu.setEnabled(false)
    }

    fun getAudioBuffer(): FloatArray {
        return apu.getAudioBuffer()
    }

    private fun updateTimers(cycles: Int) {
        // Mise à jour du Divider Register (FF04)
        dividerCounter += cycles
        if (dividerCounter >= 256) { // 16384 Hz
            dividerCounter = 0
            val currentDivider = memory.readByte(0xFF04u)
            memory.writeByte(0xFF04u, ((currentDivider.toInt() + 1) and 0xFF).toUByte())
        }

        // Mise à jour du Timer
        if (timerEnabled) {
            timerCounter += cycles
            val timerFrequency = when (timerControl and 0x03) {
                0 -> 4096  // 4096 Hz
                1 -> 262144 // 262144 Hz
                2 -> 65536  // 65536 Hz
                3 -> 16384  // 16384 Hz
                else -> 4096
            }

            if (timerCounter >= timerFrequency) {
                timerCounter = 0
                var currentTimer = memory.readByte(0xFF05u)
                if (currentTimer.toInt() == 0xFF) {
                    // Débordement du timer
                    memory.writeByte(0xFF05u, timerModulo.toUByte())
                    requestInterrupt(INT_TIMER)
                } else {
                    memory.writeByte(0xFF05u, (currentTimer.toInt() + 1).toUByte())
                }
            }
        }
    }

    private fun checkInterrupts() {
        val interruptFlag = memory.readByte(0xFF0Fu)
        val interruptEnable = memory.readByte(0xFFFFu)

        // Vérification de chaque interruption
        if (interruptFlag.toInt() and 0x01 != 0 && interruptEnable.toInt() and 0x01 != 0) {
            // VBlank
            handleInterrupt(INT_VBLANK)
        } else if (interruptFlag.toInt() and 0x02 != 0 && interruptEnable.toInt() and 0x02 != 0) {
            // LCD STAT
            handleInterrupt(INT_LCD_STAT)
        } else if (interruptFlag.toInt() and 0x04 != 0 && interruptEnable.toInt() and 0x04 != 0) {
            // Timer
            handleInterrupt(INT_TIMER)
        } else if (interruptFlag.toInt() and 0x08 != 0 && interruptEnable.toInt() and 0x08 != 0) {
            // Serial
            handleInterrupt(INT_SERIAL)
        } else if (interruptFlag.toInt() and 0x10 != 0 && interruptEnable.toInt() and 0x10 != 0) {
            // Joypad
            handleInterrupt(INT_JOYPAD)
        }
    }

    private fun handleInterrupt(interruptType: Int) {
        // Désactiver l'interruption
        val interruptFlag = memory.readByte(0xFF0Fu)
        memory.writeByte(0xFF0Fu, (interruptFlag.toInt() and (0xFF xor (1 shl interruptType))).toUByte())

        // Mode HALT
        if (cpu.isHalted()) {
            cpu.resume()
        }

        // Mode STOP
        if (cpu.isStopped()) {
            cpu.resume()
        }

        // Désactiver les interruptions
        cpu.disableInterrupts()

        // Pousser PC sur la pile
        val pc = cpu.getPC()
        val sp = cpu.getSP()
        memory.writeByte((sp - 1).toUShort(), ((pc shr 8) and 0xFF).toUByte())
        memory.writeByte((sp - 2).toUShort(), (pc and 0xFF).toUByte())
        cpu.setSP(sp - 2)

        // Sauter à l'adresse de l'interruption
        val interruptAddress = when (interruptType) {
            INT_VBLANK -> 0x40
            INT_LCD_STAT -> 0x48
            INT_TIMER -> 0x50
            INT_SERIAL -> 0x58
            INT_JOYPAD -> 0x60
            else -> 0x40
        }
        cpu.setPC(interruptAddress)
    }

    fun requestInterrupt(interruptType: Int) {
        val interruptFlag = memory.readByte(0xFF0Fu)
        memory.writeByte(0xFF0Fu, (interruptFlag.toInt() or (1 shl interruptType)).toUByte())
    }

    // Getters et setters pour les timers
    fun getTimerControl(): UByte = timerControl.toUByte()
    fun setTimerControl(value: UByte) {
        timerControl = value.toInt()
        timerEnabled = (value.toInt() and 0x04) != 0
    }

    fun getTimerModulo(): UByte = timerModulo.toUByte()
    fun setTimerModulo(value: UByte) {
        timerModulo = value.toInt()
    }
}