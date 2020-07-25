package com.github.wln.screens

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.{Button, Label, TextButton}
import com.badlogic.gdx.scenes.scene2d.{InputEvent, InputListener, Stage}
import com.github.wln.TabsProvider
import com.github.wln.common.GameSkin
import com.github.wln.parsing._

class SongTabScreen(screenManager: IScreenManager, stage: Option[Stage], songDescription: SongBriefDescription) extends ScreenAdapter {
  val tab: SongTab = TabsProvider.getTabByDescription(songDescription)
  val font: BitmapFont = GameSkin.getFont(15)
  private val height: Int = screenManager.getHeight

  private val backButton = createBackButton

  private def createText(text: String, lineNumber: Int, lineWidth: Int = 20, horizontalOffset: Int = 10): Unit = {
    val label: Label = new Label(text, new Label.LabelStyle(font, Color.WHITE))
    label.setPosition(horizontalOffset, height - lineWidth * (lineNumber + 1))
    label.setWrap(false)
    stage.foreach(_.addActor(label))
  }

  private def createBackButton: Button = {
    val backButton = new TextButton("Back", GameSkin.getSkin)
    backButton.setPosition(20, 20)
    backButton.addListener(new InputListener() {
      override def touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Unit = {
      }

      override def touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean = {
        screenManager.switchScreen(new SongSelectionScreen(screenManager, stage))
        true
      }
    })
    backButton
  }


  override def show(): Unit = {
    println(tab)

    stage.foreach(_.addActor(backButton))
    var linesCount = 0
    createText(Seq(tab.artist, tab.title).flatten.mkString(" - "), linesCount)
    linesCount += 2

    tab.parts.zipWithIndex.foreach({
      case (part, idx) => {
        createText(part.partName, linesCount)
        linesCount += 2
        part.lines.foreach({
          case EmptyLine => linesCount += 1
          case LyricsLine(line) => createText(line, linesCount)
            linesCount += 1
          case ChordsLine(line, chords) => createText(line, linesCount)
            linesCount += 1
        })
      }
    })
  }
}
