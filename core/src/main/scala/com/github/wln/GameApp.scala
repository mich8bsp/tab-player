package com.github.wln

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.{Game, Gdx}

import scala.jdk.CollectionConverters._

trait GameApp extends Game{

  var mainStage: Option[Stage] = None

  override def create(): Unit = {
    mainStage = Some(new Stage())
    mainStage.foreach(Gdx.input.setInputProcessor)
    initialize()
  }

  def initialize()

  override def render(): Unit = {
    super.render()
    val dt: Float = Gdx.graphics.getDeltaTime

    if(mainStage.isEmpty){
      throw new Exception("Game App was not initialized via create")
    }

    mainStage.get.act(dt)

    update(dt)

    Gdx.gl.glClearColor(0,0,0,1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    mainStage.get.draw()
  }

  def update(dt: Float)

  def clear(): Unit = {
    mainStage.toSeq.flatMap(_.getActors.asScala.toSeq)
      .foreach(actor => actor.addAction(Actions.removeActor()))
  }
}
