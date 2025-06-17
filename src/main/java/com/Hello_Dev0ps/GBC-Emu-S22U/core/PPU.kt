package com.Hello_Dev0ps.gbc_emu_s22u

class PPU(private val memory: Memory) {
    // Constantes pour les modes PPU
    companion object {
        const val MODE_HBLANK = 0
        const val MODE_VBLANK = 1
        const val MODE_OAM = 2
        const val MODE_PIXEL_TRANSFER = 3

        const val SCREEN_WIDTH = 160
        const val SCREEN_HEIGHT = 144
        const val TILE_SIZE = 8
        const val TILES_PER_LINE = 32
        const val TILE_MAP_SIZE = 32 * 32

        // Interruptions
        const val INT_VBLANK = 0
        const val INT_LCD_STAT = 1
        const val INT_TIMER = 2
        const val INT_SERIAL = 3
        const val INT_JOYPAD = 4
    }

    // État du PPU
    private var mode = MODE_OAM
    private var modeClock = 0
    private var line = 0
    private var windowLine = 0
    private var frameBuffer = IntArray(SCREEN_WIDTH * SCREEN_HEIGHT)
    private var frameComplete = false
    private var isGBC = false

    // Palettes de couleurs
    private val bgPalette = IntArray(4)
    private val objPalette0 = IntArray(4)
    private val objPalette1 = IntArray(4)
    private val bgPalettes = Array(8) { IntArray(4) }
    private val objPalettes = Array(8) { IntArray(4) }

    // Sprites
    private data class Sprite(
        val y: Int,
        val x: Int,
        val tileNumber: Int,
        val attributes: Int
    )

    // Mise à jour du PPU
    fun step(cycles: Int) {
        modeClock += cycles

        when (mode) {
            MODE_OAM -> {
                if (modeClock >= 80) {
                    modeClock = 0
                    mode = MODE_PIXEL_TRANSFER
                    checkLYC()
                }
            }
            MODE_PIXEL_TRANSFER -> {
                if (modeClock >= 172) {
                    modeClock = 0
                    mode = MODE_HBLANK
                    renderScanline()
                    checkLYC()
                }
            }
            MODE_HBLANK -> {
                if (modeClock >= 204) {
                    modeClock = 0
                    line++
                    if (line == 144) {
                        mode = MODE_VBLANK
                        frameComplete = true
                        requestInterrupt(INT_VBLANK)
                    } else {
                        mode = MODE_OAM
                    }
                    checkLYC()
                }
            }
            MODE_VBLANK -> {
                if (modeClock >= 456) {
                    modeClock = 0
                    line++
                    if (line > 153) {
                        line = 0
                        windowLine = 0
                        mode = MODE_OAM
                    }
                    checkLYC()
                }
            }
        }

        // Mise à jour du registre STAT
        updateSTAT()
    }

    // Vérification de la coïncidence LYC=LY
    private fun checkLYC() {
        val lyc = memory.getLYC().toInt()
        val stat = memory.getSTAT().toInt()
        val coincidence = lyc == line

        // Mise à jour du bit de coïncidence
        var newStat = stat
        if (coincidence) {
            newStat = newStat or 0x04
        } else {
            newStat = newStat and 0xFB.inv()
        }
        memory.setSTAT(newStat.toUByte())

        // Déclenchement de l'interruption si activée
        if (coincidence && (stat and 0x40 != 0)) {
            requestInterrupt(INT_LCD_STAT)
        }
    }

    // Demande d'interruption
    private fun requestInterrupt(interrupt: Int) {
        val ifRegister = memory.readByte(0xFF0F.toUShort()).toInt()
        memory.writeByte(0xFF0F.toUShort(), (ifRegister or (1 shl interrupt)).toUByte())
    }

    // Rendu d'une ligne
    private fun renderScanline() {
        if (line >= SCREEN_HEIGHT) return

        val lcdc = memory.getLCDC()
        if (lcdc and 0x80u == 0u) return // LCD désactivé

        // Rendu du fond
        if (lcdc and 0x01u != 0u) {
            renderBackground()
        }

        // Rendu de la fenêtre
        if (lcdc and 0x20u != 0u) {
            renderWindow()
        }

        // Rendu des sprites
        if (lcdc and 0x02u != 0u) {
            renderSprites()
        }
    }

    // Rendu du fond
    private fun renderBackground() {
        val scx = memory.getSCX().toInt()
        val scy = memory.getSCY().toInt()
        val lcdc = memory.getLCDC()
        val tileMapBase = if (lcdc and 0x08u != 0u) 0x9C00 else 0x9800
        val tileDataBase = if (lcdc and 0x10u != 0u) 0x8000 else 0x8800

        for (x in 0 until SCREEN_WIDTH) {
            val tileX = (x + scx) / TILE_SIZE
            val tileY = (line + scy) / TILE_SIZE
            val tileMapIndex = tileY * TILES_PER_LINE + tileX
            val tileNumber = memory.readByte((tileMapBase + tileMapIndex).toUShort()).toInt()
            val tileData = getTileData(tileNumber, tileDataBase)
            val pixelX = (x + scx) % TILE_SIZE
            val pixelY = (line + scy) % TILE_SIZE
            val colorIndex = getTilePixel(tileData, pixelX, pixelY)
            val color = if (isGBC) {
                val paletteIndex = memory.readByte((tileMapBase + tileMapIndex + 0x2000).toUShort()).toInt() and 0x07
                bgPalettes[paletteIndex][colorIndex]
            } else {
                bgPalette[colorIndex]
            }
            frameBuffer[line * SCREEN_WIDTH + x] = color
        }
    }

    // Rendu de la fenêtre
    private fun renderWindow() {
        val wx = memory.getWX().toInt() - 7
        val wy = memory.getWY().toInt()
        if (wx >= SCREEN_WIDTH || wy > line) return

        val lcdc = memory.getLCDC()
        val tileMapBase = if (lcdc and 0x40u != 0u) 0x9C00 else 0x9800
        val tileDataBase = if (lcdc and 0x10u != 0u) 0x8000 else 0x8800

        for (x in wx until SCREEN_WIDTH) {
            val tileX = (x - wx) / TILE_SIZE
            val tileY = windowLine / TILE_SIZE
            val tileMapIndex = tileY * TILES_PER_LINE + tileX
            val tileNumber = memory.readByte((tileMapBase + tileMapIndex).toUShort()).toInt()
            val tileData = getTileData(tileNumber, tileDataBase)
            val pixelX = (x - wx) % TILE_SIZE
            val pixelY = windowLine % TILE_SIZE
            val colorIndex = getTilePixel(tileData, pixelX, pixelY)
            val color = if (isGBC) {
                val paletteIndex = memory.readByte((tileMapBase + tileMapIndex + 0x2000).toUShort()).toInt() and 0x07
                bgPalettes[paletteIndex][colorIndex]
            } else {
                bgPalette[colorIndex]
            }
            frameBuffer[line * SCREEN_WIDTH + x] = color
        }
        windowLine++
    }

    // Rendu des sprites
    private fun renderSprites() {
        val sprites = getVisibleSprites()
        val lcdc = memory.getLCDC()
        val spriteHeight = if (lcdc and 0x04u != 0u) 16 else 8

        for (sprite in sprites) {
            val tileData = getTileData(sprite.tileNumber, 0x8000)
            val flipX = sprite.attributes and 0x20 != 0
            val flipY = sprite.attributes and 0x40 != 0
            val paletteIndex = if (isGBC) sprite.attributes and 0x07 else (sprite.attributes and 0x10) shr 4
            val palette = if (isGBC) objPalettes[paletteIndex] else if (paletteIndex == 0) objPalette0 else objPalette1
            val priority = sprite.attributes and 0x80 == 0

            for (y in 0 until spriteHeight) {
                val pixelY = if (flipY) spriteHeight - 1 - y else y
                for (x in 0 until TILE_SIZE) {
                    val pixelX = if (flipX) TILE_SIZE - 1 - x else x
                    val colorIndex = getTilePixel(tileData, pixelX, pixelY)
                    if (colorIndex != 0) { // 0 = transparent
                        val screenX = sprite.x + x
                        val screenY = sprite.y + y
                        if (screenX in 0 until SCREEN_WIDTH && screenY in 0 until SCREEN_HEIGHT) {
                            val index = screenY * SCREEN_WIDTH + screenX
                            if (priority || frameBuffer[index] == 0) {
                                frameBuffer[index] = palette[colorIndex]
                            }
                        }
                    }
                }
            }
        }
    }

    // Obtention des sprites visibles
    private fun getVisibleSprites(): List<Sprite> {
        val sprites = mutableListOf<Sprite>()
        for (i in 0 until 40) {
            val baseAddr = 0xFE00 + i * 4
            val y = memory.readByte(baseAddr.toUShort()).toInt() - 16
            val x = memory.readByte((baseAddr + 1).toUShort()).toInt() - 8
            val tileNumber = memory.readByte((baseAddr + 2).toUShort()).toInt()
            val attributes = memory.readByte((baseAddr + 3).toUShort()).toInt()

            if (y in -16 until SCREEN_HEIGHT && x in -8 until SCREEN_WIDTH) {
                sprites.add(Sprite(y, x, tileNumber, attributes))
            }
        }
        return sprites.sortedBy { it.x }
    }

    // Obtention des données d'une tuile
    private fun getTileData(tileNumber: Int, baseAddr: Int): ByteArray {
        val tileData = ByteArray(16)
        val addr = if (baseAddr == 0x8800) {
            baseAddr + (tileNumber + 128) * 16
        } else {
            baseAddr + tileNumber * 16
        }
        for (i in 0 until 16) {
            tileData[i] = memory.readByte((addr + i).toUShort()).toByte()
        }
        return tileData
    }

    // Obtention d'un pixel d'une tuile
    private fun getTilePixel(tileData: ByteArray, x: Int, y: Int): Int {
        val byte1 = tileData[y * 2]
        val byte2 = tileData[y * 2 + 1]
        val bit1 = (byte1.toInt() shr (7 - x)) and 1
        val bit2 = (byte2.toInt() shr (7 - x)) and 1
        return (bit2 shl 1) or bit1
    }

    // Mise à jour du registre STAT
    private fun updateSTAT() {
        var stat = memory.getSTAT().toInt()
        stat = stat and 0xFC // Efface les bits de mode
        stat = stat or mode // Définit le nouveau mode
        memory.setSTAT(stat.toUByte())

        // Vérification des interruptions STAT
        if (stat and 0x08 != 0 && mode == MODE_HBLANK) {
            requestInterrupt(INT_LCD_STAT)
        }
        if (stat and 0x10 != 0 && mode == MODE_VBLANK) {
            requestInterrupt(INT_LCD_STAT)
        }
        if (stat and 0x20 != 0 && mode == MODE_OAM) {
            requestInterrupt(INT_LCD_STAT)
        }
    }

    // Mise à jour des palettes
    fun updatePalettes() {
        if (isGBC) {
            // Mise à jour des palettes GBC
            for (i in 0..7) {
                updateGBCPalette(i, true) // Palettes de fond
                updateGBCPalette(i, false) // Palettes d'objets
            }
        } else {
            // Mise à jour des palettes DMG
            val bgp = memory.getBGP()
            val obp0 = memory.getOBP0()
            val obp1 = memory.getOBP1()

            for (i in 0..3) {
                bgPalette[i] = getDMGColor(bgp, i)
                objPalette0[i] = getDMGColor(obp0, i)
                objPalette1[i] = getDMGColor(obp1, i)
            }
        }
    }

    // Mise à jour d'une palette GBC
    private fun updateGBCPalette(index: Int, isBackground: Boolean) {
        val baseAddr = if (isBackground) 0xFF69 else 0xFF6B
        val palette = if (isBackground) bgPalettes[index] else objPalettes[index]
        
        for (i in 0..3) {
            val color = (memory.readByte((baseAddr + i * 2).toUShort()).toInt() and 0xFF) or
                       ((memory.readByte((baseAddr + i * 2 + 1).toUShort()).toInt() and 0xFF) shl 8)
            palette[i] = convertGBColorToRGB(color)
        }
    }

    // Conversion d'une couleur GBC en RGB
    private fun convertGBColorToRGB(color: Int): Int {
        val r = (color and 0x1F) * 8
        val g = ((color shr 5) and 0x1F) * 8
        val b = ((color shr 10) and 0x1F) * 8
        return 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
    }

    // Conversion d'une couleur DMG en RGB
    private fun getDMGColor(palette: UByte, index: Int): Int {
        val color = (palette.toInt() shr (index * 2)) and 0x03
        return when (color) {
            0 -> 0xFFFFFFFF.toInt() // Blanc
            1 -> 0xFFAAAAAA.toInt() // Gris clair
            2 -> 0xFF555555.toInt() // Gris foncé
            3 -> 0xFF000000.toInt() // Noir
            else -> 0
        }
    }

    // Obtention du buffer d'image
    fun getFrameBuffer(): IntArray {
        frameComplete = false
        return frameBuffer
    }

    // Vérification si une frame est complète
    fun isFrameComplete(): Boolean = frameComplete

    // Initialisation du mode GBC
    fun setGBCMode(enabled: Boolean) {
        isGBC = enabled
    }
} 