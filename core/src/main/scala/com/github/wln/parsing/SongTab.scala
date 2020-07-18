package com.github.wln.parsing

import scala.collection.mutable

case class SongTab(
                    title: Option[String] = None,
                    artist: Option[String] = None,
                    parts: mutable.Buffer[SongPart] = mutable.Buffer(),
                    chordsRegistry: ChordsRegistry = ChordsRegistry()
                  ) {
  def addSongPart(part: SongPart): SongTab = {
    parts.append(part)
    this
  }

}

case class SongPart(
                     partName: String,
                     lines: mutable.Buffer[ITabLine] = mutable.Buffer()
                   ) {
  def addLine(line: ITabLine): SongPart = {
    lines.append(line)
    this
  }
}

case class ChordsRegistry(
                           registry: mutable.Map[Chord, String] = mutable.Map()
                         ) {
  def addChord(chord: Chord, chordTab: String): ChordsRegistry = {
    registry.put(chord, chordTab)
    this
  }

}

sealed trait ITabLine

case class ChordsLine(line: String, chords: Seq[Chord] = Seq()) extends ITabLine

case class LyricsLine(line: String) extends ITabLine

case object EmptyLine extends ITabLine

case class Chord(root: Note.Value,
                 chordQuality: ChordQuality.Value = ChordQuality.MAJOR,
                 chordExtension: String = "",
                 altRoot: Option[Note.Value] = None
                )

object ChordQuality extends Enumeration {
  val MINOR, MAJOR, AUGMENTED, DIMINISHED = Value
}
object Note extends Enumeration {
  val C = Value("C")
  val `C#` = Value("C#")
  val D = Value("D")
  val `D#` = Value("D#")
  val E = Value("E")
  val F = Value("F")
  val `F#` = Value("F#")
  val G = Value("G")
  val `G#` = Value("G#")
  val A = Value("A")
  val `A#` = Value("A#")
  val B = Value("B")

  implicit class NoteValue(note: Note.Value) {
    val semitonesFromC: Int = noteToSemitonesFromC(note)

    def +(semitones: Int): Note.Value = semitonesFromCToNote((semitonesFromC+semitones)%Note.values.size)
    def -(semitones: Int): Note.Value = this + (-semitones)
    def sh : Note.Value = this + 1
    def fl : Note.Value = this - 1
  }

  private def noteToSemitonesFromC(note: Note.Value): Int = note match {
    case C => 0
    case `C#` => 1
    case D => 2
    case `D#` => 3
    case E => 4
    case F => 5
    case `F#` => 6
    case G => 7
    case `G#` => 8
    case A => 9
    case `A#` => 10
    case B => 11
  }

  private def semitonesFromCToNote(semitones: Int): Note.Value = semitones match {
    case 0 => C
    case 1 => `C#`
    case 2 => D
    case 3 => `D#`
    case 4 => E
    case 5 => F
    case 6 => `F#`
    case 7 => G
    case 8 => `G#`
    case 9 => A
    case 10 => `A#`
    case 11 => B
  }

}