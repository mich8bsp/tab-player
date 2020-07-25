package com.github.wln.common

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.Skin

object GameSkin {

  private val uiSkin = new Skin(Gdx.files.internal("neon-ui.json"))

  def getFont(size: Int): BitmapFont = {
    val generator = new FreeTypeFontGenerator(Gdx.files.internal("OpenSans-Regular.ttf"))
    val parameter = new FreeTypeFontGenerator.FreeTypeFontParameter
    parameter.size = size
    val font = generator.generateFont(parameter)
    generator.dispose()
    font
  }

  def getSkin: Skin = uiSkin
}
