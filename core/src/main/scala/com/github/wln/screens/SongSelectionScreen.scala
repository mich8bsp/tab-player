package com.github.wln.screens

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.{InputEvent, InputListener, Stage}
import com.github.wln.common.GameSkin

class SongSelectionScreen(screenManager: IScreenManager, stage: Option[Stage]) extends ScreenAdapter {

  val middle: Int = screenManager.getWidth / 2
  val quarterHeight: Int = screenManager.getHeight / 4

  val buttons: List[TextButton] = {
    List("take_on_me")
      .map(tabName => {
        val button = new TextButton(tabName, GameSkin.getSkin)
        button.getLabel.setFontScale(2)
        button.setSize(400, 100)
        button.setPosition(middle - 200, quarterHeight - 50)
        button.addListener(new InputListener() {
          override def touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Unit = {
          }

          override def touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean = {
            println("switching screens")
            screenManager.switchScreen(new SongTabScreen(screenManager, stage, tabName))
            true
          }
        })

        button
      })
  }

  override def show(): Unit = {
    stage.foreach(stageReal => {
      buttons.foreach(button => stageReal.addActor(button))
    })
  }
}
