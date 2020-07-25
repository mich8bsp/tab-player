package com.github.wln.parsing

import java.net.URI

import scala.collection.mutable
import scala.io.Source
import scala.util.{Success, Try}

object TabFileParser {

  def parse(fileSource: Source, onlyMetadata: Boolean = false): SongTab = {
    val lines = fileSource.getLines()
    val builder = new TabBuilder(onlyMetadata)
    lines.foreach(line => builder.addLine(line))
    val tab = builder.build
    fileSource.close()
    tab
  }

  def parseFromResource(filePath: String, onlyMetadata: Boolean = false): SongTab = {
    parse(Source.fromResource(filePath), onlyMetadata)
  }

  def parseFromFile(filePath: String, onlyMetadata: Boolean = false): SongTab = {
    parse(Source.fromFile(URI.create(filePath)), onlyMetadata)
  }

}

class TabBuilder(parseOnlyMetadata: Boolean) {

  import TabParserUtils._

  var chordsRegistry = ChordsRegistry()
  var tab: SongTab = SongTab(chordsRegistry = chordsRegistry)
  val songPartsByName: mutable.Map[String, SongPart] = mutable.Map()
  var currentSongPart: Option[SongPart] = None
  var processingChords: Boolean = false

  def addLine(line: String): Unit = line match {
    case l if l.contains("[Title]") => tab = tab.copy(title = Some(l.replaceFirst("\\[Title\\]", "").trim))
    case l if l.contains("[Artist]") => tab = tab.copy(artist = Some(l.replaceFirst("\\[Artist\\]", "").trim))
    case l if l.startsWith("[Chords]") && !parseOnlyMetadata => {
      finalizeCurrentPart()
      processingChords = true
    }
    case l if l.startsWith("[") && !parseOnlyMetadata => {
      finalizeCurrentPart()
      val partName = l.substring(1, l.indexOf("]"))
      currentSongPart = Some(SongPart(partName = partName))
    }
    case l if l.trim.isEmpty => currentSongPart.foreach(p => p.addLine(EmptyLine))
    case l if processingChords => {
      val Array(chordName, chordTab, _*) = l.split("-").map(_.trim)
      chordsRegistry.addChord(parseChord(chordName), chordTab)
    }
    case l => {
      currentSongPart.foreach(p => p.addLine(parseLine(l)))
    }
  }

  def finalizeCurrentPart(): Unit = {
    processingChords = false
    val repeatingPart: Option[SongPart] = currentSongPart.map(_.partName).flatMap(songPartsByName.get)

    val partToAdd: Option[SongPart] = (for {
      previousPartOfSameName <- repeatingPart
      currPart <- currentSongPart
    }yield {
      mergeParts(previousPartOfSameName, currPart)
    }).orElse(currentSongPart)

    partToAdd.foreach(tab.addSongPart)

    if(repeatingPart.isEmpty && currentSongPart.nonEmpty){
      songPartsByName.put(currentSongPart.get.partName, currentSongPart.get)
    }
    currentSongPart = None
  }

  def build: SongTab = {
    finalizeCurrentPart()
    tab
  }

}

object TabParserUtils {
  import scala.language.postfixOps

  def parseLine(line: String): ITabLine = {
    val lineParts = line.split("[ \t]")
      .map(_.trim)
      .filter(_.nonEmpty)
      .toSeq

    if (lineParts.isEmpty) {
      EmptyLine
    } else {
      Try(lineParts.map(parseChord)) match {
        case Success(v) => ChordsLine(line, v)
        case _ => LyricsLine(line)
      }
    }
  }

  def parseChord(str: String): Chord = {
    var chordStrToParse: String = str
    val root: Note.Value = parseNote(chordStrToParse)
    chordStrToParse = chordStrToParse.substring(root.toString.length, chordStrToParse.length)
    val chordQuality: ChordQuality.Value = parseChordQuality(chordStrToParse)
    chordStrToParse = removeChordQuality(chordStrToParse)

    val (chordExtension, altRoot): (String, Option[Note.Value]) = if (chordStrToParse.contains("/")) {
      val Array(ext, altRootStr) = chordStrToParse.split("/")
      (ext, Some(parseNote(altRootStr)))
    } else {
      (chordStrToParse, None)
    }

    Chord(root, chordQuality, chordExtension, altRoot)
  }

  private def parseNote(str: String): Note.Value = {
    val note: Note.Value = Note.withName(str.substring(0, 1).toUpperCase)
    if (str.length > 1) {
      str(1) match {
        case '#' => note sh
        case 'b' => note fl
        case _ => note
      }
    } else {
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
    if (str.length >= 3 && Set("maj", "min", "aud", "dim").contains(str.substring(0, 3).toLowerCase())) {
      str.substring(3, str.length)
    } else if (str.nonEmpty && str.head.toLower == 'm') {
      str.tail
    } else {
      str
    }
  }

  def mergeParts(previousPart: SongPart, currentPart: SongPart): SongPart = {
    if(currentPart.lines.isEmpty){
      previousPart
    }else{
      var prevLinesIdx: Int = 0
      var currLinesIdx: Int = 0
      val prevPartLines: mutable.Buffer[ITabLine] = previousPart.lines
      val currPartLines: mutable.Buffer[ITabLine] = currentPart.lines
      val mergeBuffer: mutable.Buffer[ITabLine] = mutable.Buffer()

      while(prevLinesIdx < prevPartLines.size && currLinesIdx < currPartLines.size){
        val prevLine: ITabLine = prevPartLines(prevLinesIdx)
        val currLine: ITabLine = currPartLines(currLinesIdx)

        val currIsReplacing: Boolean = (prevLine, currLine) match {
          case (EmptyLine, EmptyLine) => true
          case (ChordsLine(_, _), ChordsLine(_, _)) => true
          case (LyricsLine(_), LyricsLine(_)) => true
          case _ => false
        }

        if(currIsReplacing){
          mergeBuffer.addOne(currLine)
          prevLinesIdx+=1
          currLinesIdx+=1
        }else{
          mergeBuffer.addOne(prevLine)
          prevLinesIdx+=1
        }
      }

      if(prevLinesIdx < prevPartLines.size){
        mergeBuffer.addAll(prevPartLines.toSeq.drop(prevLinesIdx))
      }
      if(currLinesIdx < currPartLines.size){
        mergeBuffer.addAll(currPartLines.toSeq.drop(currLinesIdx))
      }

      SongPart(currentPart.partName, mergeBuffer)
    }
  }
}
