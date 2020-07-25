package com.github.wln.screens

import com.badlogic.gdx.Screen

trait IScreenManager {

  def getHeight: Int
  def getWidth: Int
  def switchScreen(screen: Screen): Unit
}
