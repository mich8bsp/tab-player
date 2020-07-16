package com.github.wln.parsing

import java.net.URI

import scala.collection.mutable
import scala.io.Source
import scala.util.{Success, Try}

class TabFileParser {

  def parse(filePath: URI): SongTab = {
    val fileSource = Source.fromFile(filePath)
    val lines = fileSource.getLines()
    val tab = buildTabFromLines(lines)
    fileSource.close()
    tab
  }

  def buildTabFromLines(lines: Iterator[String]): SongTab = {
    var tab: SongTab = SongTab()
    val songPartsByName: mutable.Map[String, SongPart] = mutable.Map()
    var currentSongPart: Option[SongPart] = None
    var processingChords: Boolean = false
    lines.foreach {
      case l if l.contains("[Title]") => tab = tab.copy(title = Some(l.replaceFirst("\\[Title\\]", "").trim))
      case l if l.contains("[Artist]") => tab = tab.copy(artist = Some(l.replaceFirst("\\[Artist\\]", "").trim))
      case l if l.startsWith("[Chords]") => {
        currentSongPart.foreach(tab.addSongPart)
        currentSongPart = None
        processingChords = true
      }
      case l if l.startsWith("\\[") => {
        processingChords = false
        currentSongPart.foreach(tab.addSongPart)
        val partName = l.substring(1, l.indexOf("\\]"))
        val songPart = songPartsByName.getOrElseUpdate(partName, SongPart(partName = partName))
        currentSongPart = Some(SongPart(songPart.partName, mutable.Buffer(songPart.lines)))
      }
      case l if l.trim.isEmpty => currentSongPart.foreach(p => p.addLine(EmptyLine))
      case l if processingChords => {
        val Array(chordName, chordTab, _*) = l.split("-").map(_.trim)
        tab.chordsRegistry.addChord(parseChord(chordName), chordTab)
      }
      case l => {
        currentSongPart.foreach(p => p.addLine(parseLine(l)))
      }
    }
    currentSongPart.foreach(tab.addSongPart)
    tab
  }

  private def parseLine(line: String): ITabLine = {
    val lineParts = line.split("[ \t]")
      .map(_.trim)
      .filter(_.nonEmpty)
      .toSeq

    if(lineParts.isEmpty){
      EmptyLine
    }else{
      Try(lineParts.map(parseChord)) match {
        case Success(v) => ChordsLine(line, v)
        case _ => LyricsLine(line)
      }
    }
  }

  private def parseChord(str: String): Chord = {
    var chordStrToParse: String = str
    val root: Note.Value = parseNote(chordStrToParse)
    chordStrToParse = chordStrToParse.substring(root.toString.length, chordStrToParse.length)
    val chordQuality: ChordQuality.Value = parseChordQuality(chordStrToParse)
    chordStrToParse = removeChordQuality(chordStrToParse)

    val (chordExtension, altRoot): (String, Option[Note.Value]) = if(chordStrToParse.contains("/")){
      val Array(ext, altRootStr) = chordStrToParse.split("/")
      (ext, Some(parseNote(altRootStr)))
    }else{
      (chordStrToParse, None)
    }

    Chord(root, chordQuality, chordExtension, altRoot)
  }

  private def parseNote(str: String): Note.Value = {
    val note: Note.Value = Note.withName(str.substring(0, 1).toUpperCase)
    if(str.length > 1){
      str(1) match {
        case '#' => note sh
        case "b" => note fl
        case _ => note
      }
    }else{
      note
    }
  }

  private def parseChordQuality(str: String): ChordQuality.Value = str match {
    case x if x.startsWith("m") | x.startsWith("min") => ChordQuality.MINOR
    case x if x.toLowerCase().startsWith("aug") => ChordQuality.AUGMENTED
    case x if x.toLowerCase().startsWith("dim") => ChordQuality.DIMINISHED
    case _ => ChordQuality.MAJOR
  }

  private def removeChordQuality(str: String): String = {
    if(str.length >= 3 && Set("maj", "min", "aud", "dim").contains(str.substring(0, 3).toLowerCase())){
      str.substring(3, str.length)
    }else if(str.nonEmpty && str.head.toLower == 'm'){
      str.tail
    }else{
      str
    }
  }
}
