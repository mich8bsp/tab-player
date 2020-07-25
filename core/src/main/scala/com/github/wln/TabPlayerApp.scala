package com.github.wln

import com.badlogic.gdx.Screen
import com.github.wln.screens.{IScreenManager, SongSelectionScreen}

class TabPlayerApp(val width: Int, val height: Int) extends GameApp with IScreenManager {

  override def initialize(): Unit = {
    setScreen(new SongSelectionScreen(this, this.mainStage))
  }

  override def update(dt: Float): Unit = {

  }

  override def switchScreen(screen: Screen): Unit = {
    clear()
    setScreen(screen)
  }

  override def getHeight: Int = height

  override def getWidth: Int = width
}
