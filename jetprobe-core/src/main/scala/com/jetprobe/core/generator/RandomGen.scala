package com.jetprobe.core.generator
import java.io.File
import java.util.UUID

import com.github.tototoshi.csv.CSVReader
import com.jetprobe.core.structure.ScenarioContext
import com.jetprobe.core.util.FileReader

import scala.util.Random
import scala.util.matching.Regex

/**
  * @author Shad.
  */
object RandomGen {

  lazy val gen = """\$\{([a-zA-Z])\w+\.?([a-zA-Z])\w+\}""".r
  val startPattern = "${"
  val endPattern = "}"

  /**
    * Creates a basic random data generator
    * @param datasetPath
    * @param template
    * @param rows
    * @return
    */
  def apply(datasetPath : String, template : String, rows : Int) : DataGenerator= new DataGenerator {

    private[this] def getField(str : String) : String = str.substring(2).takeWhile(ch => ch != '}')
    private[this] def getRegex(str: String): Regex =   ("""\$\{""" + str + """\}""").r

    override def build(ctx: ScenarioContext): Generator = {
      val templateStr = FileReader.readFile(new File(template))
      val regexMatches = gen findAllIn templateStr toList
      val reader = CSVReader.open(new File(datasetPath))
      val dataset = reader.allWithHeaders().toArray
      val datasetSize = dataset.length

      def next : String = {
        var temp = templateStr
        val randomVal =  dataset(Random.nextInt(datasetSize))
        regexMatches.foreach {
          str: String =>
            val strPattern = getField(str)
            val fieldVal = {
              if(strPattern.equals("Random.UUID"))
                UUID.randomUUID().toString
              else
                randomVal.getOrElse(strPattern, str)
            }
            println(s"string pattern : ${strPattern}")
            temp = getRegex(strPattern).replaceAllIn(temp, fieldVal)
        }
        temp
      }
      Iterator.fill[String](rows)(next)
    }

  }

 /* private def getField(str : String) : String = str.substring(2).takeWhile(ch => ch != '}')
  private def getRegex(str: String): Regex =   ("""\$\{""" + str + """\}""").r


  def generate(template : String,datasetPath : String, rows : Int): Generator = {
    val templateStr = FileReader.readFile(new File(template))
    val regexMatches = gen findAllIn templateStr toList
    val reader = CSVReader.open(new File(datasetPath))
    val dataset = reader.allWithHeaders().toArray
    val datasetSize = dataset.length

    def next : String = {
      var temp = templateStr
      val randomVal =  dataset(Random.nextInt(datasetSize))
      regexMatches.foreach {
        str: String =>
          val strPattern = getField(str)
          val fieldVal = {
            if(strPattern.equals("Random.UUID"))
              UUID.randomUUID().toString
            else
              randomVal.getOrElse(strPattern, str)
          }
          temp = getRegex(strPattern).replaceAllIn(temp, fieldVal)
      }
      temp
    }
   val result =  Iterator.fill[String](rows)(next).toList
    result.toIterator
  }
*/

}
