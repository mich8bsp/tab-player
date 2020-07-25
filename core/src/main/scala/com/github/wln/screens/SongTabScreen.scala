package com.github.wln.screens

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.github.wln.common.GameSkin
import com.github.wln.parsing.{ChordsLine, EmptyLine, LyricsLine, TabFileParser}

import scala.io.Source

class SongTabScreen(sizeProvider: IScreenManager, stage: Option[Stage], tabName: String) extends ScreenAdapter {
  val parser = new TabFileParser()
  val tab = parser.parse(Source.fromResource(s"$tabName.tabs"))
  val batch = new SpriteBatch()
  val font = GameSkin.getFont(15)
  private val width: Int = sizeProvider.getWidth
  private val height: Int = sizeProvider.getHeight

  private def createText(text: String, lineNumber: Int, lineWidth: Int = 20, horizontalOffset: Int = 10): Unit = {
    val label: Label = new Label(text, new Label.LabelStyle(font, Color.WHITE))
    label.setPosition(horizontalOffset, height - lineWidth * (lineNumber + 1))
    label.setWrap(false)
    stage.foreach(_.addActor(label))
  }


  override def show(): Unit = {
    println(tab)

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
