package com.github.wln

import com.github.wln.parsing.{SongBriefDescription, SongTab, TabFileParser}

object TabsProvider {

  def fetchListOfSongs: List[SongBriefDescription] = {
    List("take_on_me.tabs") //TODO: scan folder with tabs
        .map(fileName => {
          val tab = TabFileParser.parseFromResource(fileName, onlyMetadata = true)
          SongBriefDescription(tab.title,tab.artist,fileName)
        })
  }

  def getTabByDescription(description: SongBriefDescription): SongTab = {
   TabFileParser.parseFromResource(description.fileName)
  }
}


