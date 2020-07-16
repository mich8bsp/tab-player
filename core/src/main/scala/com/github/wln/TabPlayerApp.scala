package com.github.wln

import com.badlogic.gdx.ApplicationAdapter
import com.github.wln.parsing.TabFileParser

import scala.io.Source

class TabPlayerApp extends ApplicationAdapter{

  override def create(): Unit = {
    val parser = new TabFileParser()
    val tab = parser.parse(Source.fromResource("take_on_me.tabs"))
    println(tab)
  }

}
